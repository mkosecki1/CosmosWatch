package com.cosmoswatch.feature.apod.domain.usecase

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.domain.APOD_ARCHIVE_START_DATE
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetApodArchiveUseCase @Inject constructor(
    private val repository: ApodRepository,
    private val clock: Clock,
) {
    operator fun invoke(filter: ApodArchiveFilter = ApodArchiveFilter()): AppResult<Flow<PagingData<ApodDomain>>> {
        val validationError = validate(filter)
        return if (validationError != null) {
            AppResult.Failure(validationError)
        } else {
            AppResult.Success(repository.getArchive(filter))
        }
    }

    private fun validate(filter: ApodArchiveFilter): AppError? {
        val today = LocalDate.now(clock)
        val start = filter.startDate
        val end = filter.endDate
        return when {
            start != null && start.isBefore(APOD_ARCHIVE_START_DATE) -> AppError.Validation("start_date_before_archive")
            end != null && end.isAfter(today) -> AppError.Validation("end_date_in_future")
            start != null && end != null && start.isAfter(end) -> AppError.Validation("start_date_after_end_date")
            else -> null
        }
    }
}
