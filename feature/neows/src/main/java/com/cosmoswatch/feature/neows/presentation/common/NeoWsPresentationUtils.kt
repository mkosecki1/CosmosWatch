package com.cosmoswatch.feature.neows.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.feature.neows.R

@Composable
internal fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.neo_error_network)
    is AppError.Server -> stringResource(R.string.neo_error_server)
    is AppError.Unknown -> stringResource(R.string.neo_error_unknown)
    is AppError.Validation -> stringResource(R.string.neo_error_unknown)
}
