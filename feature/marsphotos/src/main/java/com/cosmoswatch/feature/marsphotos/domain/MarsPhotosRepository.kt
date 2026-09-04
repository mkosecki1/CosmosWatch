package com.cosmoswatch.feature.marsphotos.domain

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MarsPhotosRepository {
    fun getPhotos(filter: MarsPhotoFilter): Flow<PagingData<MarsPhotoDomain>>
    fun getLatestAvailablePhotoDate(rover: MarsRover): Flow<AppResult<LocalDate>>
}
