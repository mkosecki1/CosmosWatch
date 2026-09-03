package com.cosmoswatch.feature.apod.data.repository

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.data.local.ApodDao
import com.cosmoswatch.feature.apod.data.local.ApodEntity
import com.cosmoswatch.feature.apod.data.mapper.toDomain
import com.cosmoswatch.feature.apod.data.mapper.toEntity
import com.cosmoswatch.feature.apod.data.remote.ApodApi
import com.cosmoswatch.feature.apod.data.remote.ApodDto
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodRepository
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
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val MAX_FETCH_ATTEMPTS = 3
private const val INITIAL_RETRY_DELAY_MILLIS = 1_000L
private val CACHE_TTL: Duration = Duration.ofHours(12)

class ApodRepositoryImpl @Inject constructor(
    private val api: ApodApi,
    private val dao: ApodDao,
    private val clock: Clock,
) : ApodRepository {

    override fun getApod(): Flow<AppResult<ApodDomain>> = flow {
        val cached = dao.observe().first()

        if (isStale(cached)) {
            val refreshError = refreshFromNetwork()
            if (refreshError != null && cached == null) {
                emit(AppResult.Failure(refreshError))
                return@flow
            }
        }

        emitAll(observeAsResult())
    }

    private fun isStale(cached: ApodEntity?): Boolean {
        if (cached == null) return true
        val age = Duration.between(Instant.ofEpochMilli(cached.fetchedAtEpochMillis), clock.instant())
        return age >= CACHE_TTL
    }

    private suspend fun refreshFromNetwork(): AppError? = try {
        val dto = fetchWithRetry()
        dao.insertOrReplace(dto.toEntity(fetchedAtEpochMillis = clock.millis()))
        null
    } catch (e: IOException) {
        AppError.Network
    } catch (e: HttpException) {
        AppError.Server(e.code())
    } catch (e: Exception) {
        AppError.Unknown(e)
    }

    private fun observeAsResult(): Flow<AppResult<ApodDomain>> = dao.observe().map { entity ->
        if (entity != null) {
            AppResult.Success(entity.toDomain())
        } else {
            AppResult.Failure(AppError.Unknown())
        }
    }

    private suspend fun fetchWithRetry(): ApodDto {
        var attempt = 0
        while (true) {
            try {
                return api.getApod()
            } catch (e: IOException) {
                attempt++
                if (attempt >= MAX_FETCH_ATTEMPTS) throw e
                delay((INITIAL_RETRY_DELAY_MILLIS * (1L shl (attempt - 1))).milliseconds)
            }
        }
    }
}
