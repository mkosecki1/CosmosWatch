package com.cosmoswatch.feature.donki.presentation.timeline

import kotlin.math.floor

private const val KP_TO_G_SCALE_OFFSET = 4
private const val MIN_G_SCALE = 1
private const val MAX_G_SCALE = 5

internal fun geomagneticStormScale(kp: Double): Int =
    (floor(kp).toInt() - KP_TO_G_SCALE_OFFSET).coerceIn(MIN_G_SCALE, MAX_G_SCALE)
