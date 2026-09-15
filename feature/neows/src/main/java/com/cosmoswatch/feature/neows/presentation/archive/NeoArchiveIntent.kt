package com.cosmoswatch.feature.neows.presentation.archive

import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter

sealed interface NeoArchiveIntent {
    data class FilterChanged(val filter: NeoArchiveFilter) : NeoArchiveIntent
    data object ClearFilter : NeoArchiveIntent
}
