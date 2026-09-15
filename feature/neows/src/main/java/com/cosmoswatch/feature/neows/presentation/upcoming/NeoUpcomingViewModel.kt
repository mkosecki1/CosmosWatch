package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
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
class NeoUpcomingViewModel @Inject constructor(
    private val repository: NeoWsRepository,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val state: StateFlow<NeoUpcomingState> = retryTrigger
        .flatMapLatest {
            repository.getUpcoming()
                .map { result ->
                    when (result) {
                        is AppResult.Success -> NeoUpcomingState.Success(result.data)
                        is AppResult.Failure -> NeoUpcomingState.Error(result.error)
                    }
                }
                .onStart { emit(NeoUpcomingState.Loading) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = NeoUpcomingState.Loading,
        )

    fun onIntent(intent: NeoUpcomingIntent) {
        when (intent) {
            NeoUpcomingIntent.Retry -> retryTrigger.update { it + 1 }
        }
    }
}
