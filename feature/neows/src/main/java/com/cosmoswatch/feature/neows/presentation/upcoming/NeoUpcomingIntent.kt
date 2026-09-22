package com.cosmoswatch.feature.neows.presentation.upcoming

import com.cosmoswatch.feature.neows.domain.NeoUpcomingFilter

sealed interface NeoUpcomingIntent {
    data object Retry : NeoUpcomingIntent
    data class FilterChanged(val filter: NeoUpcomingFilter) : NeoUpcomingIntent
    data object ClearFilter : NeoUpcomingIntent
}
