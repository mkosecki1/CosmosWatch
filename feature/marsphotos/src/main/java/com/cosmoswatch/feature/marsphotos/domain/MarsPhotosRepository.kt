package com.cosmoswatch.feature.marsphotos.domain

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow

interface MarsPhotosRepository {
    fun getPhotos(filter: MarsPhotoFilter): Flow<PagingData<MarsPhotoDomain>>
    fun getRoverManifest(rover: MarsRover): Flow<AppResult<MarsRoverManifest>>
    fun observePhoto(photoId: Int): Flow<MarsPhotoDomain?>
}
