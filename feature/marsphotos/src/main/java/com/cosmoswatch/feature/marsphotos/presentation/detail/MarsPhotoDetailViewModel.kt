package com.cosmoswatch.feature.marsphotos.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class MarsPhotoDetailViewModel @Inject constructor(
    repository: MarsPhotosRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val state: StateFlow<MarsPhotoDetailState> = repository
        .observePhoto(savedStateHandle.toRoute<MarsPhotoDetailRoute>().photoId)
        .map { photo ->
            if (photo != null) {
                MarsPhotoDetailState.Content(photo)
            } else {
                MarsPhotoDetailState.NotAvailable
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = MarsPhotoDetailState.Loading,
        )
}
