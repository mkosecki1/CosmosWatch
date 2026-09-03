package com.cosmoswatch.feature.apod.presentation

sealed interface ApodIntent {
    data object Retry : ApodIntent
}
