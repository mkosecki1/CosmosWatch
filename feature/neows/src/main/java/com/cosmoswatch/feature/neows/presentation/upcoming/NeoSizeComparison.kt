package com.cosmoswatch.feature.neows.presentation.upcoming

import kotlin.math.abs
import kotlin.math.ln

internal val sizeReferenceMeters = listOf(4.0, 40.0, 105.0, 300.0)

internal enum class SizeComparisonMagnitude {
    MUCH_SMALLER,
    SMALLER,
    SIMILAR,
    BIGGER,
    MUCH_BIGGER
}

internal data class SizeComparison(val referenceIndex: Int, val magnitude: SizeComparisonMagnitude)

internal fun closestSizeComparison(diameterMinMeters: Double, diameterMaxMeters: Double): SizeComparison {
    val averageMeters = (diameterMinMeters + diameterMaxMeters) / 2
    val closestIndex = sizeReferenceMeters.indices.minBy { index ->
        abs(ln(averageMeters) - ln(sizeReferenceMeters[index]))
    }
    val ratio = averageMeters / sizeReferenceMeters[closestIndex]
    val magnitude = when {
        ratio < 0.33 -> SizeComparisonMagnitude.MUCH_SMALLER
        ratio < 0.75 -> SizeComparisonMagnitude.SMALLER
        ratio <= 1.33 -> SizeComparisonMagnitude.SIMILAR
        ratio <= 3.0 -> SizeComparisonMagnitude.BIGGER
        else -> SizeComparisonMagnitude.MUCH_BIGGER
    }
    return SizeComparison(closestIndex, magnitude)
}
