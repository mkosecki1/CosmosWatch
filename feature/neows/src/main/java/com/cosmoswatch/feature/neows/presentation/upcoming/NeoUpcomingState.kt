package com.cosmoswatch.feature.neows.presentation.upcoming

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.neows.domain.NeoDomain

sealed interface NeoUpcomingState {
    data object Loading : NeoUpcomingState
    data class Success(val neos: List<NeoDomain>) : NeoUpcomingState
    data class Error(val error: AppError) : NeoUpcomingState
}
