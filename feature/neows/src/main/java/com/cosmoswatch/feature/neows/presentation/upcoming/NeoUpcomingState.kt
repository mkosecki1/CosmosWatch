package com.cosmoswatch.feature.neows.presentation.upcoming

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.NeoUpcomingFilter
import java.time.LocalDate

sealed interface NeoUpcomingState {
    data object Loading : NeoUpcomingState
    data class Success(
        val neos: List<NeoDomain>,
        val today: LocalDate,
        val filter: NeoUpcomingFilter,
    ) : NeoUpcomingState
    data class Error(val error: AppError) : NeoUpcomingState
}
