package com.cosmoswatch.feature.marsphotos.domain.usecase

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoFilter
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotosRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetMarsPhotosUseCase @Inject constructor(
    private val repository: MarsPhotosRepository,
    private val clock: Clock,
) {
    operator fun invoke(filter: MarsPhotoFilter): AppResult<Flow<PagingData<MarsPhotoDomain>>> {
        val validationError = validate(filter)
        return if (validationError != null) {
            AppResult.Failure(validationError)
        } else {
            AppResult.Success(repository.getPhotos(filter))
        }
    }

    private fun validate(filter: MarsPhotoFilter): AppError? = when {
        filter.earthDate.isBefore(filter.rover.landingDate) -> AppError.Validation("date_before_landing")
        filter.earthDate.isAfter(LocalDate.now(clock)) -> AppError.Validation("date_in_future")
        else -> null
    }
}
