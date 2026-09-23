package com.cosmoswatch.feature.neows.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.presentation.upcoming.SizeComparison
import com.cosmoswatch.feature.neows.presentation.upcoming.SizeComparisonMagnitude
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
internal fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.neo_error_network)
    is AppError.Server -> stringResource(R.string.neo_error_server)
    is AppError.Unknown -> stringResource(R.string.neo_error_unknown)
    is AppError.Validation -> stringResource(R.string.neo_error_unknown)
}

private val sizeReferenceLabelRes = listOf(
    R.string.neo_size_ref_car,
    R.string.neo_size_ref_airplane,
    R.string.neo_size_ref_pitch,
    R.string.neo_size_ref_skyscraper
)

@Composable
internal fun SizeComparison.toMessage(): String {
    val referenceLabel = stringResource(sizeReferenceLabelRes[referenceIndex])
    val template = when (magnitude) {
        SizeComparisonMagnitude.MUCH_SMALLER -> R.string.neo_size_comparison_much_smaller
        SizeComparisonMagnitude.SMALLER -> R.string.neo_size_comparison_smaller
        SizeComparisonMagnitude.SIMILAR -> R.string.neo_size_comparison_similar
        SizeComparisonMagnitude.BIGGER -> R.string.neo_size_comparison_bigger
        SizeComparisonMagnitude.MUCH_BIGGER -> R.string.neo_size_comparison_much_bigger
    }
    return stringResource(template, referenceLabel)
}

internal fun daysUntilCloseApproach(closeApproachDate: LocalDate, today: LocalDate): Long =
    ChronoUnit.DAYS.between(today, closeApproachDate)

@Composable
internal fun approachLabel(daysUntil: Long): String = when (daysUntil) {
    0L -> stringResource(R.string.neo_approach_today)
    1L -> stringResource(R.string.neo_approach_tomorrow)
    -1L -> stringResource(R.string.neo_approach_yesterday)
    in Long.MIN_VALUE..-2L -> stringResource(R.string.neo_approach_days_ago, -daysUntil.toInt())
    else -> stringResource(R.string.neo_approach_in_days, daysUntil.toInt())
}
