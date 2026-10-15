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
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

private const val APOD_REMOTE_PAGE_SIZE = 25
private const val FIRST_REMOTE_PAGE = 1
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
        val remotePage = when (loadType) {
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.REFRESH -> firstRemotePageForFilter()
            LoadType.APPEND -> currentKey?.nextRemotePage
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }

        return try {
            val batch = fetchFirstBatchInRange(remotePage)
            val page = pageToLoad(loadType, currentKey)
            persistPage(loadType, page, batch, currentKey)
            MediatorResult.Success(endOfPaginationReached = batch.nextRemotePage == null)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        } catch (e: SerializationException) {
            MediatorResult.Error(e)
        } catch (e: DateTimeParseException) {
            MediatorResult.Error(e)
        }
    }

    private fun firstRemotePageForFilter(): Int {
        val endDate = filter.endDate ?: return FIRST_REMOTE_PAGE
        val newerEntries = ChronoUnit.DAYS.between(endDate, LocalDate.now(clock)).coerceAtLeast(0)
        val estimatedPage = (newerEntries / APOD_REMOTE_PAGE_SIZE).toInt() + FIRST_REMOTE_PAGE
        return (estimatedPage - 1).coerceAtLeast(FIRST_REMOTE_PAGE)
    }

    private suspend fun fetchFirstBatchInRange(firstRemotePage: Int): Batch {
        var remotePage = firstRemotePage
        while (true) {
            val entries = api.getPage(page = remotePage, perPage = APOD_REMOTE_PAGE_SIZE)
            val dates = entries.map { LocalDate.parse(it.date) }
            val reachedBoundary = entries.size < APOD_REMOTE_PAGE_SIZE || dates.any { it <= boundaryStartDate }
            val nextRemotePage = if (reachedBoundary) null else remotePage + 1
            val entriesInRange = entries.filterIndexed { index, _ -> dates[index].isWithinFilter() }

            if (entriesInRange.isNotEmpty() || nextRemotePage == null) {
                return Batch(entriesInRange, nextRemotePage)
            }
            remotePage = nextRemotePage
        }
    }

    private fun LocalDate.isWithinFilter(): Boolean =
        this >= boundaryStartDate && (filter.endDate == null || this <= filter.endDate)

    private fun pageToLoad(loadType: LoadType, currentKey: ApodArchiveRemoteKeyEntity?): Int =
        if (loadType == LoadType.REFRESH) 0 else (currentKey?.newestCachedPage ?: 0) + 1

    private suspend fun persistPage(
        loadType: LoadType,
        page: Int,
        batch: Batch,
        currentKey: ApodArchiveRemoteKeyEntity?,
    ) = database.withTransaction {
        if (loadType == LoadType.REFRESH) {
            archiveDao.clearAll()
        }
        archiveDao.insertAll(batch.entries.map { it.toArchiveEntity(page = page) })

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
                nextRemotePage = batch.nextRemotePage,
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

    private class Batch(val entries: List<ApodDto>, val nextRemotePage: Int?)
}
