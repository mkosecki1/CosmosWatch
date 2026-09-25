package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.annotation.StringRes
import com.cosmoswatch.feature.donki.R
import kotlin.math.floor

private const val KP_TO_G_SCALE_OFFSET = 4
private const val MIN_G_SCALE = 1
private const val MAX_G_SCALE = 5
private const val MIN_STORM_KP = 5

internal fun geomagneticStormScale(kp: Double): Int =
    (floor(kp).toInt() - KP_TO_G_SCALE_OFFSET).coerceIn(MIN_G_SCALE, MAX_G_SCALE)

internal fun isStormLevelKp(kp: Double): Boolean = floor(kp).toInt() >= MIN_STORM_KP

@StringRes
internal fun stormLevelNameRes(gScale: Int): Int = when (gScale) {
    1 -> R.string.donki_storm_level_minor
    2 -> R.string.donki_storm_level_moderate
    3 -> R.string.donki_storm_level_strong
    4 -> R.string.donki_storm_level_severe
    else -> R.string.donki_storm_level_extreme
}
