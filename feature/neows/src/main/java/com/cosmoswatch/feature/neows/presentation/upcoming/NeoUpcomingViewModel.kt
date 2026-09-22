package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.NeoUpcomingFilter
import com.cosmoswatch.feature.neows.domain.NeoWsRepository
import com.cosmoswatch.feature.neows.domain.matches
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NeoUpcomingViewModel @Inject constructor(
    private val repository: NeoWsRepository,
    private val clock: Clock,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private val filter = MutableStateFlow(NeoUpcomingFilter())

    private val fetchResult: Flow<AppResult<List<NeoDomain>>?> = retryTrigger
        .flatMapLatest {
            repository.getUpcoming()
                .map<AppResult<List<NeoDomain>>, AppResult<List<NeoDomain>>?> { it }
                .onStart { emit(null) }
        }

    val state: StateFlow<NeoUpcomingState> = combine(fetchResult, filter) { result, currentFilter ->
        when (result) {
            null -> NeoUpcomingState.Loading
            is AppResult.Success -> NeoUpcomingState.Success(
                neos = result.data.filter { it.matches(currentFilter) },
                today = LocalDate.now(clock),
                filter = currentFilter,
            )
            is AppResult.Failure -> NeoUpcomingState.Error(result.error)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
        initialValue = NeoUpcomingState.Loading,
    )

    fun onIntent(intent: NeoUpcomingIntent) {
        when (intent) {
            NeoUpcomingIntent.Retry -> retryTrigger.update { it + 1 }
            is NeoUpcomingIntent.FilterChanged -> filter.value = intent.filter
            NeoUpcomingIntent.ClearFilter -> filter.value = NeoUpcomingFilter()
        }
    }
}
