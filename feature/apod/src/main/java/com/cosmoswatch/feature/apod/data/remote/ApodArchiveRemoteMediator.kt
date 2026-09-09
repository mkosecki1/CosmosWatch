package com.cosmoswatch.feature.apod.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.cosmoswatch.feature.apod.data.local.ApodArchiveEntity
import com.cosmoswatch.feature.apod.data.local.ApodArchiveRemoteKeyEntity
import com.cosmoswatch.feature.apod.data.local.ApodDatabase
import com.cosmoswatch.feature.apod.data.mapper.toArchiveEntity
import com.cosmoswatch.feature.apod.domain.APOD_ARCHIVE_START_DATE
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.LocalDate

private const val WINDOW_DAYS = 30L
private const val MAX_CACHED_PAGES = 20

@OptIn(ExperimentalPagingApi::class)
class ApodArchiveRemoteMediator(
    private val api: ApodApi,
    private val database: ApodDatabase,
    private val clock: Clock,
    private val filter: ApodArchiveFilter,
) : RemoteMediator<Int, ApodArchiveEntity>() {

    private val archiveDao = database.apodArchiveDao()
    private val remoteKeyDao = database.apodArchiveRemoteKeyDao()
    private val boundaryStartDate = filter.startDate ?: APOD_ARCHIVE_START_DATE

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ApodArchiveEntity>,
    ): MediatorResult {
        val currentKey = remoteKeyDao.getRemoteKey()
        val range = rangeToLoad(loadType, currentKey)
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val entries = api.getApodRange(startDate = range.start.toString(), endDate = range.end.toString())
            val page = pageToLoad(loadType, currentKey)
            val endOfPaginationReached = range.start <= boundaryStartDate
            persistPage(loadType, page, entries, range.start, endOfPaginationReached, currentKey)
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private fun pageToLoad(loadType: LoadType, currentKey: ApodArchiveRemoteKeyEntity?): Int =
        if (loadType == LoadType.REFRESH) 0 else (currentKey?.newestCachedPage ?: 0) + 1

    private fun rangeToLoad(loadType: LoadType, currentKey: ApodArchiveRemoteKeyEntity?): DateRange? = when (loadType) {
        LoadType.PREPEND -> null
        LoadType.REFRESH -> {
            val end = filter.endDate ?: LocalDate.now(clock)
            DateRange(start = maxOf(end.minusDays(WINDOW_DAYS - 1), boundaryStartDate), end = end)
        }
        LoadType.APPEND -> {
            val nextEndDate = currentKey?.nextEndDate?.let(LocalDate::parse) ?: return null
            DateRange(start = maxOf(nextEndDate.minusDays(WINDOW_DAYS - 1), boundaryStartDate), end = nextEndDate)
        }
    }

    private suspend fun persistPage(
        loadType: LoadType,
        page: Int,
        entries: List<ApodDto>,
        rangeStart: LocalDate,
        endOfPaginationReached: Boolean,
        currentKey: ApodArchiveRemoteKeyEntity?,
    ) = database.withTransaction {
        if (loadType == LoadType.REFRESH) {
            archiveDao.clearAll()
        }
        archiveDao.insertAll(entries.map { it.toArchiveEntity(page = page) })

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
            ApodArchiveRemoteKeyEntity(
                nextEndDate = if (endOfPaginationReached) null else rangeStart.minusDays(1).toString(),
                oldestCachedPage = oldestCachedPage,
                newestCachedPage = page,
            ),
        )
    }

    private suspend fun evictOldestPageIfOverCapacity(oldestCachedPage: Int, newestCachedPage: Int): Int {
        if (newestCachedPage - oldestCachedPage + 1 <= MAX_CACHED_PAGES) return oldestCachedPage
        archiveDao.deletePage(oldestCachedPage)
        return oldestCachedPage + 1
    }

    private data class DateRange(val start: LocalDate, val end: LocalDate)
}
