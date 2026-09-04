package com.cosmoswatch.feature.marsphotos.presentation.list

import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import com.cosmoswatch.feature.marsphotos.domain.MarsRoverStatus
import java.time.LocalDate

sealed interface MarsPhotosState {
    val rover: MarsRover

    data class ResolvingDefaultDate(override val rover: MarsRover) : MarsPhotosState

    data class DefaultDateError(override val rover: MarsRover, val error: AppError) : MarsPhotosState

    data class Ready(
        override val rover: MarsRover,
        val earthDate: LocalDate,
        val latestAvailableDate: LocalDate,
        val status: MarsRoverStatus,
        val camera: String?,
    ) : MarsPhotosState
}
