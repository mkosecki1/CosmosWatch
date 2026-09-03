package com.cosmoswatch.feature.apod.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.domain.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ApodViewModel @Inject constructor(
    private val repository: ApodRepository,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val state: StateFlow<ApodState> = retryTrigger
        .flatMapLatest {
            repository.getApod()
                .map { result ->
                    when (result) {
                        is AppResult.Success -> ApodState.Success(result.data)
                        is AppResult.Failure -> ApodState.Error(result.error)
                    }
                }
                .onStart { emit(ApodState.Loading) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = ApodState.Loading,
        )

    fun onIntent(intent: ApodIntent) {
        when (intent) {
            ApodIntent.Retry -> retryTrigger.update { it + 1 }
        }
    }
}
