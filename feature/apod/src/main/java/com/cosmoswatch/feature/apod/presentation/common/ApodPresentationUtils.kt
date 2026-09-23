package com.cosmoswatch.feature.apod.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.apod.R

@Composable
internal fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.apod_error_network)
    is AppError.Server -> stringResource(R.string.apod_error_server)
    is AppError.Unknown -> stringResource(R.string.apod_error_unknown)
    is AppError.Validation -> stringResource(R.string.apod_error_unknown)
}
