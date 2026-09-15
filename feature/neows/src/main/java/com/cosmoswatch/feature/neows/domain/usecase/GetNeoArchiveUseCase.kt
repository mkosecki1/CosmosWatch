package com.cosmoswatch.feature.neows.domain.usecase

import androidx.paging.PagingData
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

class GetNeoArchiveUseCase @Inject constructor(
    private val repository: NeoWsRepository,
    private val clock: Clock,
) {
    operator fun invoke(filter: NeoArchiveFilter = NeoArchiveFilter()): AppResult<Flow<PagingData<NeoDomain>>> {
        val validationError = validate(filter)
        return if (validationError != null) {
            AppResult.Failure(validationError)
        } else {
            AppResult.Success(repository.getArchive(filter))
        }
    }

    private fun validate(filter: NeoArchiveFilter): AppError? {
        val today = LocalDate.now(clock)
        val start = filter.startDate
        val end = filter.endDate
        return when {
            end != null && end.isAfter(today) -> AppError.Validation("end_date_in_future")
            start != null && end != null && start.isAfter(end) -> AppError.Validation("start_date_after_end_date")
            else -> null
        }
    }
}
