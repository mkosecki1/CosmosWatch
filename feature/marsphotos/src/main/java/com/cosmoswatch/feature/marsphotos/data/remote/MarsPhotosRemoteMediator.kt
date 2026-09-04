package com.cosmoswatch.feature.marsphotos.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotoEntity
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotoRemoteKeyEntity
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotosDatabase
import com.cosmoswatch.feature.marsphotos.data.mapper.toEntity
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoFilter
import retrofit2.HttpException
import java.io.IOException

private const val MAX_CACHED_PAGES = 20

@OptIn(ExperimentalPagingApi::class)
class MarsPhotosRemoteMediator(
    private val filter: MarsPhotoFilter,
    private val api: MarsPhotosApi,
    private val database: MarsPhotosDatabase,
) : RemoteMediator<Int, MarsPhotoEntity>() {

    private val photoDao = database.marsPhotoDao()
    private val remoteKeyDao = database.marsPhotoRemoteKeyDao()

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MarsPhotoEntity>,
    ): MediatorResult {
        val currentKey = remoteKeyDao.getRemoteKey()
        val page = pageToLoad(loadType, currentKey)
            ?: return MediatorResult.Success(endOfPaginationReached = true)

        return try {
            val photos = fetchPhotos(page)
            val endOfPaginationReached = photos.isEmpty()
            persistPage(loadType, page, photos, endOfPaginationReached, currentKey)
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private fun pageToLoad(loadType: LoadType, currentKey: MarsPhotoRemoteKeyEntity?): Int? = when (loadType) {
        LoadType.REFRESH -> 1
        LoadType.PREPEND -> null
        LoadType.APPEND -> currentKey?.nextPage
    }

    private suspend fun fetchPhotos(page: Int): List<MarsPhotoDto> = api.getPhotos(
        rover = filter.rover.apiName,
        earthDate = filter.earthDate.toString(),
        camera = filter.camera,
        page = page,
    ).photos

    private suspend fun persistPage(
        loadType: LoadType,
        page: Int,
        photos: List<MarsPhotoDto>,
        endOfPaginationReached: Boolean,
        currentKey: MarsPhotoRemoteKeyEntity?,
    ) = database.withTransaction {
        if (loadType == LoadType.REFRESH) {
            photoDao.clearAll()
        }
        photoDao.insertAll(photos.map { it.toEntity(page = page, roverApiName = filter.rover.apiName) })

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
            MarsPhotoRemoteKeyEntity(
                nextPage = if (endOfPaginationReached) null else page + 1,
                oldestCachedPage = oldestCachedPage,
                newestCachedPage = page,
            ),
        )
    }

    private suspend fun evictOldestPageIfOverCapacity(oldestCachedPage: Int, newestCachedPage: Int): Int {
        if (newestCachedPage - oldestCachedPage + 1 <= MAX_CACHED_PAGES) return oldestCachedPage
        photoDao.deletePage(oldestCachedPage)
        return oldestCachedPage + 1
    }
}
