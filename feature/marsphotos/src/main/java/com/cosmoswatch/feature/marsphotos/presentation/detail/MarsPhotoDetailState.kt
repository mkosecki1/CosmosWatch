package com.cosmoswatch.feature.marsphotos.presentation.detail

import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain

sealed interface MarsPhotoDetailState {
    data object Loading : MarsPhotoDetailState
    data object NotAvailable : MarsPhotoDetailState
    data class Content(val photo: MarsPhotoDomain) : MarsPhotoDetailState
}
