package com.cosmoswatch.feature.apod.presentation.archivedetail

import com.cosmoswatch.feature.apod.domain.ApodDomain

sealed interface ApodArchiveDetailState {
    data object Loading : ApodArchiveDetailState
    data object NotAvailable : ApodArchiveDetailState
    data class Content(val apod: ApodDomain) : ApodArchiveDetailState
}
