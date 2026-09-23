package com.cosmoswatch.feature.donki.presentation.timeline

import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter

sealed interface DonkiTimelineIntent {
    data class FilterChanged(val filter: DonkiTimelineFilter) : DonkiTimelineIntent
    data object ClearFilter : DonkiTimelineIntent
}
