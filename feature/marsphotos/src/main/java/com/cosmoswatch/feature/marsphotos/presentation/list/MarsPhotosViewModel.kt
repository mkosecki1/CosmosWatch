package com.cosmoswatch.feature.marsphotos.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.cosmoswatch.core.common.result.AppResult
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoFilter
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotosRepository
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import com.cosmoswatch.feature.marsphotos.domain.MarsRoverManifest
import com.cosmoswatch.feature.marsphotos.domain.usecase.GetMarsPhotosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

private const val STATE_SHARING_TIMEOUT_MILLIS = 5_000L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MarsPhotosViewModel @Inject constructor(
    private val repository: MarsPhotosRepository,
    private val getMarsPhotosUseCase: GetMarsPhotosUseCase,
) : ViewModel() {

    private val selectedRover = MutableStateFlow(MarsRover.entries.first())
    private val explicitDate = MutableStateFlow<LocalDate?>(null)
    private val selectedCamera = MutableStateFlow<String?>(null)
    private val retryTrigger = MutableStateFlow(0)

    private val defaultDateOutcome: Flow<DefaultDateOutcome> = combine(selectedRover, retryTrigger) { rover, _ -> rover }
        .flatMapLatest { rover ->
            repository.getRoverManifest(rover)
                .map<AppResult<MarsRoverManifest>, DefaultDateOutcome> { DefaultDateOutcome.Loaded(rover, it) }
                .onStart { emit(DefaultDateOutcome.Loading(rover)) }
        }

    val state: StateFlow<MarsPhotosState> = combine(
        defaultDateOutcome,
        explicitDate,
        selectedCamera,
    ) { outcome, explicit, camera ->
        when (outcome) {
            is DefaultDateOutcome.Loading -> MarsPhotosState.ResolvingDefaultDate(outcome.rover)
            is DefaultDateOutcome.Loaded -> when (val result = outcome.result) {
                is AppResult.Failure -> MarsPhotosState.DefaultDateError(outcome.rover, result.error)
                is AppResult.Success -> MarsPhotosState.Ready(
                    rover = outcome.rover,
                    earthDate = explicit ?: result.data.latestAvailableDate,
                    latestAvailableDate = result.data.latestAvailableDate,
                    status = result.data.status,
                    camera = camera,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MILLIS),
        initialValue = MarsPhotosState.ResolvingDefaultDate(selectedRover.value),
    )

    val photos: Flow<PagingData<MarsPhotoDomain>> = state
        .flatMapLatest { current ->
            val ready = current as? MarsPhotosState.Ready
            if (ready == null) {
                flowOf(PagingData.empty())
            } else {
                val filter = MarsPhotoFilter(rover = ready.rover, earthDate = ready.earthDate, camera = ready.camera)
                when (val result = getMarsPhotosUseCase(filter)) {
                    is AppResult.Success -> result.data
                    is AppResult.Failure -> flowOf(PagingData.empty())
                }
            }
        }
        .cachedIn(viewModelScope)

    fun onIntent(intent: MarsPhotosIntent) {
        when (intent) {
            is MarsPhotosIntent.RoverSelected -> {
                selectedRover.value = intent.rover
                explicitDate.value = null
                selectedCamera.value = null
            }
            is MarsPhotosIntent.CameraSelected -> selectedCamera.value = intent.camera
            is MarsPhotosIntent.DateSelected -> explicitDate.value = intent.earthDate
            MarsPhotosIntent.PreviousDay -> shiftSelectedDate(days = -1)
            MarsPhotosIntent.NextDay -> shiftSelectedDate(days = 1)
            MarsPhotosIntent.RetryResolvingDefaultDate -> retryTrigger.update { it + 1 }
        }
    }

    private fun shiftSelectedDate(days: Long) {
        val current = (state.value as? MarsPhotosState.Ready)?.earthDate ?: return
        explicitDate.value = current.plusDays(days)
    }

    private sealed interface DefaultDateOutcome {
        data class Loading(val rover: MarsRover) : DefaultDateOutcome
        data class Loaded(val rover: MarsRover, val result: AppResult<MarsRoverManifest>) : DefaultDateOutcome
    }
}
