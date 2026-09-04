package com.cosmoswatch.feature.marsphotos.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.cosmoswatch.feature.marsphotos.data.local.MarsPhotosDatabase
import com.cosmoswatch.feature.marsphotos.data.mapper.toDomain
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotosApi
import com.cosmoswatch.feature.marsphotos.data.remote.MarsPhotosRemoteMediator
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoFilter
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotosRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val MARS_PHOTOS_PAGE_SIZE = 25

class MarsPhotosRepositoryImpl @Inject constructor(
    private val api: MarsPhotosApi,
    private val database: MarsPhotosDatabase,
) : MarsPhotosRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPhotos(filter: MarsPhotoFilter): Flow<PagingData<MarsPhotoDomain>> = Pager(
        config = PagingConfig(pageSize = MARS_PHOTOS_PAGE_SIZE, enablePlaceholders = false),
        remoteMediator = MarsPhotosRemoteMediator(filter = filter, api = api, database = database),
        pagingSourceFactory = { database.marsPhotoDao().pagingSource() },
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
}
