package com.cosmoswatch.feature.marsphotos.presentation

import androidx.annotation.StringRes
import com.cosmoswatch.feature.marsphotos.R
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import com.cosmoswatch.feature.marsphotos.domain.MarsRoverStatus

internal fun MarsRover.displayName(): String = when (this) {
    MarsRover.CURIOSITY -> "Curiosity"
    MarsRover.PERSEVERANCE -> "Perseverance"
}

@StringRes
internal fun MarsRoverStatus.labelRes(): Int = when (this) {
    MarsRoverStatus.ACTIVE -> R.string.marsphotos_status_active
    MarsRoverStatus.COMPLETE -> R.string.marsphotos_status_complete
}
