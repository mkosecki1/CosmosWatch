package com.cosmoswatch.feature.apod.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.usecase.GetApodArchiveUseCase
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
class ApodArchiveViewModel @Inject constructor(
    private val getApodArchiveUseCase: GetApodArchiveUseCase,
) : ViewModel() {

    private val filter = MutableStateFlow(ApodArchiveFilter())

    val state: StateFlow<ApodArchiveState> = filter
        .map { currentFilter ->
            val result = getApodArchiveUseCase(currentFilter)
            ApodArchiveState(
                filter = currentFilter,
                filterError = (result as? AppResult.Failure)?.error,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
            initialValue = ApodArchiveState(),
        )

    val archive: Flow<PagingData<ApodDomain>> = filter
        .flatMapLatest { currentFilter ->
            when (val result = getApodArchiveUseCase(currentFilter)) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> flowOf(PagingData.empty())
            }
        }
        .cachedIn(viewModelScope)

    fun onIntent(intent: ApodArchiveIntent) {
        when (intent) {
            is ApodArchiveIntent.FilterChanged -> filter.value = intent.filter
            ApodArchiveIntent.ClearFilter -> filter.value = ApodArchiveFilter()
        }
    }
}
