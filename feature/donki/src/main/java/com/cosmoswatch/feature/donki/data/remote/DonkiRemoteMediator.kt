package com.cosmoswatch.feature.donki.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.cosmoswatch.feature.donki.data.local.DonkiDatabase
import com.cosmoswatch.feature.donki.data.local.DonkiRemoteKeyEntity
import com.cosmoswatch.feature.donki.data.local.DonkiTimelineEntity
import com.cosmoswatch.feature.donki.data.mapper.toDomain
import com.cosmoswatch.feature.donki.data.mapper.toEntity
import com.cosmoswatch.feature.donki.domain.DONKI_ARCHIVE_START_DATE
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.LocalDate

private const val WINDOW_DAYS = 30L
private const val MAX_CACHED_PAGES = 20

@OptIn(ExperimentalPagingApi::class)
class DonkiRemoteMediator(
    private val api: DonkiApi,
    private val database: DonkiDatabase,
    private val clock: Clock,
    private val filter: DonkiTimelineFilter,
) : RemoteMediator<Int, DonkiTimelineEntity>() {

    private val timelineDao = database.donkiTimelineDao()
    private val remoteKeyDao = database.donkiRemoteKeyDao()
    private val boundaryStartDate = filter.startDate ?: DONKI_ARCHIVE_START_DATE

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DonkiTimelineEntity>,
    ): MediatorResult {
        val currentKey = remoteKeyDao.getRemoteKey()
        val range = rangeToLoad(loadType, currentKey)
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val events = fetchRange(range)
            val page = pageToLoad(loadType, currentKey)
            val endOfPaginationReached = range.start <= boundaryStartDate
            persistPage(loadType, page, events, range.start, endOfPaginationReached, currentKey)
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun fetchRange(range: DateRange): List<DonkiEventDomain> {
        val startDate = range.start.toString()
        val endDate = range.end.toString()
        val flares = api.getFlares(startDate, endDate).map { it.toDomain() }
        val cmes = api.getCmes(startDate, endDate).map { it.toDomain() }
        val storms = api.getStorms(startDate, endDate).map { it.toDomain() }
        return flares + cmes + storms
    }

    private fun pageToLoad(loadType: LoadType, currentKey: DonkiRemoteKeyEntity?): Int =
        if (loadType == LoadType.REFRESH) 0 else (currentKey?.newestCachedPage ?: 0) + 1

    private fun rangeToLoad(loadType: LoadType, currentKey: DonkiRemoteKeyEntity?): DateRange? = when (loadType) {
        LoadType.PREPEND -> null
        LoadType.REFRESH -> {
            val end = filter.endDate ?: LocalDate.now(clock)
            val start = maxOf(end.minusDays(WINDOW_DAYS - 1), boundaryStartDate)
            DateRange(start = start, end = end)
        }
        LoadType.APPEND -> {
            val nextEndDate = currentKey?.nextEndDate?.let(LocalDate::parse) ?: return null
            val start = maxOf(nextEndDate.minusDays(WINDOW_DAYS - 1), boundaryStartDate)
            DateRange(start = start, end = nextEndDate)
        }
    }

    private suspend fun persistPage(
        loadType: LoadType,
        page: Int,
        events: List<DonkiEventDomain>,
        rangeStart: LocalDate,
        endOfPaginationReached: Boolean,
        currentKey: DonkiRemoteKeyEntity?,
    ) = database.withTransaction {
        if (loadType == LoadType.REFRESH) {
            timelineDao.clearAll()
        }
        timelineDao.insertAll(events.map { it.toEntity(page = page) })

        val oldestCachedPageBeforeEviction = if (loadType == LoadType.REFRESH) {
            page
        } else {
            currentKey?.oldestCachedPage ?: page
        }
        val oldestCachedPage = evictOldestPageIfOverCapacity(
            oldestCachedPage = oldestCachedPageBeforeEviction,
            newestCachedPage = page,
        )

        remoteKeyDao.insertOrReplace(
            DonkiRemoteKeyEntity(
                nextEndDate = if (endOfPaginationReached) null else rangeStart.minusDays(1).toString(),
                oldestCachedPage = oldestCachedPage,
                newestCachedPage = page,
            ),
        )
    }

    private suspend fun evictOldestPageIfOverCapacity(oldestCachedPage: Int, newestCachedPage: Int): Int {
        if (newestCachedPage - oldestCachedPage + 1 <= MAX_CACHED_PAGES) return oldestCachedPage
        timelineDao.deletePage(oldestCachedPage)
        return oldestCachedPage + 1
    }

    private data class DateRange(val start: LocalDate, val end: LocalDate)
}
