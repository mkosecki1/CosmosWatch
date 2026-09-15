package com.cosmoswatch.feature.neows.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.domain.usecase.GetNeoArchiveUseCase
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
class NeoArchiveViewModel @Inject constructor(
    private val getNeoArchiveUseCase: GetNeoArchiveUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(NeoArchiveFilter())

    val state: StateFlow<NeoArchiveState> = filter
        .map { currentFilter ->
            val result = getNeoArchiveUseCase(currentFilter)
            NeoArchiveState(
                filter = currentFilter,
                filterError = (result as? AppResult.Failure)?.error,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = NeoArchiveState(),
        )

    val archive: Flow<PagingData<NeoDomain>> = filter
        .flatMapLatest { currentFilter ->
            when (val result = getNeoArchiveUseCase(currentFilter)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    fun onIntent(intent: NeoArchiveIntent) {
        when (intent) {
            is NeoArchiveIntent.FilterChanged -> filter.value = intent.filter
            NeoArchiveIntent.ClearFilter -> filter.value = NeoArchiveFilter()
        }
    }
}
