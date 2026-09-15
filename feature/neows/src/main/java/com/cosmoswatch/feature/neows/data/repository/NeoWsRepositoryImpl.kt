package com.cosmoswatch.feature.neows.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.data.local.NeoUpcomingDao
import com.cosmoswatch.feature.neows.data.local.NeoUpcomingEntity
import com.cosmoswatch.feature.neows.data.local.NeoWsDatabase
import com.cosmoswatch.feature.neows.data.mapper.toDomain
import com.cosmoswatch.feature.neows.data.mapper.toUpcomingEntity
import com.cosmoswatch.feature.neows.data.remote.NeoArchiveRemoteMediator
import com.cosmoswatch.feature.neows.data.remote.NeoWsApi
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_FETCH_ATTEMPTS = 3
private const val INITIAL_RETRY_DELAY_MILLIS = 1_000L
private val CACHE_TTL: Duration = Duration.ofHours(12)
private const val UPCOMING_WINDOW_DAYS = 14L
private const val API_MAX_WINDOW_DAYS = 7L
private const val ARCHIVE_PAGE_SIZE = 20

class NeoWsRepositoryImpl @Inject constructor(
    private val api: NeoWsApi,
    private val dao: NeoUpcomingDao,
    private val database: NeoWsDatabase,
    private val clock: Clock,
) : NeoWsRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getArchive(): Flow<PagingData<NeoDomain>> = Pager(
        config = PagingConfig(pageSize = ARCHIVE_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = NeoArchiveRemoteMediator(api = api, database = database, clock = clock),
        pagingSourceFactory = { database.neoArchiveDao().pagingSource() },
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun getUpcoming(): Flow<AppResult<List<NeoDomain>>> = flow {
        val cached = dao.observe().first()

        if (isStale(cached)) {
            val refreshError = refreshFromNetwork()
            if (refreshError != null && cached.isEmpty()) {
                emit(AppResult.Failure(refreshError))
                return@flow
            }
        }

        emitAll(observeAsResult())
    }

    private fun isStale(cached: List<NeoUpcomingEntity>): Boolean {
        val oldest = cached.firstOrNull() ?: return true
        val age = Duration.between(Instant.ofEpochMilli(oldest.fetchedAtEpochMillis), clock.instant())
        return age >= CACHE_TTL
    }

    private suspend fun refreshFromNetwork(): AppError? = try {
        val today = LocalDate.now(clock)
        val entries = fetchWindow(today, today.plusDays(UPCOMING_WINDOW_DAYS - 1))
        val fetchedAt = clock.millis()
        database.withTransaction {
            dao.clearAll()
            dao.insertAll(entries.map { it.toUpcomingEntity(fetchedAtEpochMillis = fetchedAt) })
        }
        null
    } catch (e: IOException) {
        AppError.Network
    } catch (e: HttpException) {
        AppError.Server(e.code())
    } catch (e: Exception) {
        AppError.Unknown(e)
    }

    private suspend fun fetchWindow(start: LocalDate, end: LocalDate): List<NeoDomain> {
        val results = mutableListOf<NeoDomain>()
        var chunkStart = start
        while (!chunkStart.isAfter(end)) {
            val chunkEnd = minOf(chunkStart.plusDays(API_MAX_WINDOW_DAYS - 1), end)
            results += fetchRangeWithRetry(chunkStart, chunkEnd)
            chunkStart = chunkEnd.plusDays(1)
        }
        return results
    }

    private suspend fun fetchRangeWithRetry(start: LocalDate, end: LocalDate): List<NeoDomain> {
        var attempt = 0
        while (true) {
            try {
                return api.getFeed(startDate = start.toString(), endDate = end.toString()).toDomain()
            } catch (e: IOException) {
                attempt++
                if (attempt >= MAX_FETCH_ATTEMPTS) throw e
                delay((INITIAL_RETRY_DELAY_MILLIS * (1L shl (attempt - 1))).milliseconds)
            }
        }
    }

    private fun observeAsResult(): Flow<AppResult<List<NeoDomain>>> = dao.observe().map { entities ->
        AppResult.Success(entities.map { it.toDomain() })
    }
}
