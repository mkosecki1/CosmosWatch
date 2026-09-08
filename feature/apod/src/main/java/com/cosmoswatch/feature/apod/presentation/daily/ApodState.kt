package com.cosmoswatch.feature.apod.presentation.daily

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.apod.domain.ApodDomain

sealed interface ApodState {
    data object Loading : ApodState
    data class Success(val apod: ApodDomain) : ApodState
    data class Error(val error: AppError) : ApodState
}
