package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter
import com.cosmoswatch.feature.donki.domain.usecase.GetDonkiTimelineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DonkiTimelineViewModel @Inject constructor(
    private val getDonkiTimelineUseCase: GetDonkiTimelineUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(DonkiTimelineFilter())

    val state: StateFlow<DonkiTimelineState> = filter
        .map { currentFilter ->
            val result = getDonkiTimelineUseCase(currentFilter)
            DonkiTimelineState(
                filter = currentFilter,
                filterError = (result as? AppResult.Failure)?.error,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = DonkiTimelineState(filter = DonkiTimelineFilter()),
        )

    val timeline: Flow<PagingData<DonkiEventDomain>> = filter
        .flatMapLatest { currentFilter ->
            when (val result = getDonkiTimelineUseCase(currentFilter)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    fun onIntent(intent: DonkiTimelineIntent) {
        when (intent) {
            is DonkiTimelineIntent.FilterChanged -> filter.value = intent.filter
            DonkiTimelineIntent.ClearFilter -> filter.value = DonkiTimelineFilter()
        }
    }
}
