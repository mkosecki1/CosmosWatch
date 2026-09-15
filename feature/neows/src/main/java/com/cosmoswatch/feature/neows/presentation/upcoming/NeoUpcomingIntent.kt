package com.cosmoswatch.feature.neows.presentation.upcoming

sealed interface NeoUpcomingIntent {
    data object Retry : NeoUpcomingIntent
}
