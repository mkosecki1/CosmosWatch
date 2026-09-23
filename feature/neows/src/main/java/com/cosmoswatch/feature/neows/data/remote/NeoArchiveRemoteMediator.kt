package com.cosmoswatch.feature.neows.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.cosmoswatch.feature.neows.data.local.NeoArchiveEntity
import com.cosmoswatch.feature.neows.data.local.NeoArchiveRemoteKeyEntity
import com.cosmoswatch.feature.neows.data.local.NeoWsDatabase
import com.cosmoswatch.feature.neows.data.mapper.toArchiveEntity
import com.cosmoswatch.feature.neows.data.mapper.toDomain
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import com.cosmoswatch.feature.neows.domain.NeoDomain
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.LocalDate

private const val WINDOW_DAYS = 7L
private const val MAX_CACHED_PAGES = 20

@OptIn(ExperimentalPagingApi::class)
class NeoArchiveRemoteMediator(
    private val api: NeoWsApi,
    private val database: NeoWsDatabase,
    private val clock: Clock,
    private val filter: NeoArchiveFilter,
) : RemoteMediator<Int, NeoArchiveEntity>() {

    private val archiveDao = database.neoArchiveDao()
    private val remoteKeyDao = database.neoArchiveRemoteKeyDao()
    private val boundaryStartDate = filter.startDate

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NeoArchiveEntity>,
    ): MediatorResult {
        val currentKey = remoteKeyDao.getRemoteKey()
        val range = rangeToLoad(loadType, currentKey)
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val entries = api.getFeed(startDate = range.start.toString(), endDate = range.end.toString()).toDomain()
            val page = pageToLoad(loadType, currentKey)
            val endOfPaginationReached = boundaryStartDate != null && range.start <= boundaryStartDate
            persistPage(loadType, page, entries, range.start, endOfPaginationReached, currentKey)
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private fun pageToLoad(loadType: LoadType, currentKey: NeoArchiveRemoteKeyEntity?): Int =
        if (loadType == LoadType.REFRESH) 0 else (currentKey?.newestCachedPage ?: 0) + 1

    private fun rangeToLoad(loadType: LoadType, currentKey: NeoArchiveRemoteKeyEntity?): DateRange? = when (loadType) {
        LoadType.PREPEND -> null
        LoadType.REFRESH -> {
            val end = filter.endDate ?: LocalDate.now(clock).minusDays(1)
            val start = end.minusDays(WINDOW_DAYS - 1)
            DateRange(start = if (boundaryStartDate != null) maxOf(start, boundaryStartDate) else start, end = end)
        }
        LoadType.APPEND -> {
            val nextEndDate = currentKey?.nextEndDate?.let(LocalDate::parse) ?: return null
            val start = nextEndDate.minusDays(WINDOW_DAYS - 1)
            DateRange(
                start = if (boundaryStartDate != null) maxOf(start, boundaryStartDate) else start,
                end = nextEndDate,
            )
        }
    }

    private suspend fun persistPage(
        loadType: LoadType,
        page: Int,
        entries: List<NeoDomain>,
        rangeStart: LocalDate,
        endOfPaginationReached: Boolean,
        currentKey: NeoArchiveRemoteKeyEntity?,
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
            NeoArchiveRemoteKeyEntity(
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
