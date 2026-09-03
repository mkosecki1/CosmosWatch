package com.cosmoswatch.feature.marsphotos.domain

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MarsPhotosRepository {
    fun getPhotos(filter: MarsPhotoFilter): Flow<PagingData<MarsPhotoDomain>>
}
