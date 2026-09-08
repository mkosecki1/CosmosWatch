package com.cosmoswatch.feature.apod.presentation.archivedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cosmoswatch.feature.apod.domain.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

@HiltViewModel
class ApodArchiveDetailViewModel @Inject constructor(
    repository: ApodRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val state: StateFlow<ApodArchiveDetailState> = repository
        .observeArchiveEntry(LocalDate.parse(savedStateHandle.toRoute<ApodArchiveDetailRoute>().date))
        .map { entry ->
            if (entry != null) {
                ApodArchiveDetailState.Content(entry)
            } else {
                ApodArchiveDetailState.NotAvailable
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = ApodArchiveDetailState.Loading,
        )
}
