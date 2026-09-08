package com.cosmoswatch.feature.apod.presentation.daily

sealed interface ApodIntent {
    data object Retry : ApodIntent
}
