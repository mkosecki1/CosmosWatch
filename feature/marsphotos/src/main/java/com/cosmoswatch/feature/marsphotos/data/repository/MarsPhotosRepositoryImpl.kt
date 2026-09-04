package com.cosmoswatch.feature.marsphotos.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotoManifestEntity
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotosDatabase
import com.cosmoswatch.feature.marsphotos.data.mapper.toDomain
import com.cosmoswatch.feature.marsphotos.data.mapper.toEntity
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotoManifestDto
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotosApi
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotosRemoteMediator
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoFilter
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotosRepository
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
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

private const val MARS_PHOTOS_PAGE_SIZE = 25
private const val MAX_FETCH_ATTEMPTS = 3
private const val INITIAL_RETRY_DELAY_MILLIS = 1_000L
private val MANIFEST_CACHE_TTL: Duration = Duration.ofHours(12)

class MarsPhotosRepositoryImpl @Inject constructor(
    private val api: MarsPhotosApi,
    private val database: MarsPhotosDatabase,
    private val clock: Clock,
) : MarsPhotosRepository {

    private val manifestDao = database.marsPhotoManifestDao()

    @OptIn(ExperimentalPagingApi::class)
    override fun getPhotos(filter: MarsPhotoFilter): Flow<PagingData<MarsPhotoDomain>> = Pager(
        config = PagingConfig(pageSize = MARS_PHOTOS_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = MarsPhotosRemoteMediator(filter = filter, api = api, database = database),
        pagingSourceFactory = { database.marsPhotoDao().pagingSource() },
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override fun getLatestAvailablePhotoDate(rover: MarsRover): Flow<AppResult<LocalDate>> = flow {
        val cached = manifestDao.observeManifest(rover.apiName).first()

        if (isStale(cached)) {
            val refreshError = refreshManifestFromNetwork(rover)
            if (refreshError != null && cached == null) {
                emit(AppResult.Failure(refreshError))
                return@flow
            }
        }

        emitAll(observeManifestAsResult(rover))
    }

    private fun isStale(cached: MarsPhotoManifestEntity?): Boolean {
        if (cached == null) return true
        val age = Duration.between(Instant.ofEpochMilli(cached.fetchedAtEpochMillis), clock.instant())
        return age >= MANIFEST_CACHE_TTL
    }

    private suspend fun refreshManifestFromNetwork(rover: MarsRover): AppError? = try {
        val dto = fetchManifestWithRetry(rover)
        manifestDao.insertOrReplace(
            dto.toEntity(roverApiName = rover.apiName, fetchedAtEpochMillis = clock.millis()),
        )
        null
    } catch (e: IOException) {
        AppError.Network
    } catch (e: HttpException) {
        AppError.Server(e.code())
    } catch (e: Exception) {
        AppError.Unknown(e)
    }

    private fun observeManifestAsResult(rover: MarsRover): Flow<AppResult<LocalDate>> =
        manifestDao.observeManifest(rover.apiName).map { entity ->
            if (entity != null) {
                AppResult.Success(LocalDate.parse(entity.maxEarthDate))
            } else {
                AppResult.Failure(AppError.Unknown())
            }
        }

    private suspend fun fetchManifestWithRetry(rover: MarsRover): MarsPhotoManifestDto {
        var attempt = 0
        while (true) {
            try {
                return api.getManifest(rover.apiName).photoManifest
            } catch (e: IOException) {
                attempt++
                if (attempt >= MAX_FETCH_ATTEMPTS) throw e
                delay((INITIAL_RETRY_DELAY_MILLIS * (1L shl (attempt - 1))).milliseconds)
            }
        }
    }
}
