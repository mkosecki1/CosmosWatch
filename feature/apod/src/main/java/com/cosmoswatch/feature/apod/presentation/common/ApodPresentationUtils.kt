package com.cosmoswatch.feature.apod.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.apod.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
internal fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.apod_error_network)
    is AppError.Server -> stringResource(R.string.apod_error_server)
    is AppError.Unknown -> stringResource(R.string.apod_error_unknown)
    is AppError.Validation -> stringResource(R.string.apod_error_unknown)
}

internal fun LocalDate.toEpochMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

internal fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
