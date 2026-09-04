package com.cosmoswatch.feature.marsphotos.presentation.list

import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import java.time.LocalDate

sealed interface MarsPhotosIntent {
    data class RoverSelected(val rover: MarsRover) : MarsPhotosIntent
    data class CameraSelected(val camera: String?) : MarsPhotosIntent
    data class DateSelected(val earthDate: LocalDate) : MarsPhotosIntent
    data object PreviousDay : MarsPhotosIntent
    data object NextDay : MarsPhotosIntent
    data object RetryResolvingDefaultDate : MarsPhotosIntent
}
