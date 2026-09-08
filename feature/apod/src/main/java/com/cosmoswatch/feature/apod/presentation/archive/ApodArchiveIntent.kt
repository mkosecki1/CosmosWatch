package com.cosmoswatch.feature.apod.presentation.archive

import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter

sealed interface ApodArchiveIntent {
    data class FilterChanged(val filter: ApodArchiveFilter) : ApodArchiveIntent
    data object ClearFilter : ApodArchiveIntent
}
