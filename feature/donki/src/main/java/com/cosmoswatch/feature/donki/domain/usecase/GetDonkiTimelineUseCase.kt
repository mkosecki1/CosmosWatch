package com.cosmoswatch.feature.donki.domain.usecase

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.donki.domain.DONKI_ARCHIVE_START_DATE
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiRepository
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetDonkiTimelineUseCase @Inject constructor(
    private val repository: DonkiRepository,
    private val clock: Clock,
) {
    operator fun invoke(filter: DonkiTimelineFilter = DonkiTimelineFilter()): AppResult<Flow<PagingData<DonkiEventDomain>>> {
        val validationError = validate(filter)
        return if (validationError != null) {
            AppResult.Failure(validationError)
        } else {
            AppResult.Success(repository.getTimeline(filter))
        }
    }

    private fun validate(filter: DonkiTimelineFilter): AppError? {
        val today = LocalDate.now(clock)
        val start = filter.startDate
        val end = filter.endDate
        return when {
            end != null && end.isAfter(today) -> AppError.Validation("end_date_in_future")
            start != null && start.isBefore(DONKI_ARCHIVE_START_DATE) -> AppError.Validation("start_date_before_archive")
            start != null && end != null && start.isAfter(end) -> AppError.Validation("start_date_after_end_date")
            else -> null
        }
    }
}
