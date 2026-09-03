package com.cosmoswatch.feature.apod.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.ui.component.CosmosWatchAsyncImage
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.feature.apod.R
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import java.time.format.DateTimeFormatter

@Composable
fun ApodScreen(
    modifier: Modifier = Modifier,
    viewModel: ApodViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ApodScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
private fun ApodScreenContent(
    state: ApodState,
    onIntent: (ApodIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is ApodState.Loading -> LoadingState(modifier = modifier)
        is ApodState.Error -> ErrorState(
            message = state.error.toMessage(),
            retryLabel = stringResource(R.string.apod_retry),
            onRetry = { onIntent(ApodIntent.Retry) },
            modifier = modifier,
        )
        is ApodState.Success -> ApodContent(apod = state.apod, modifier = modifier)
    }
}

@Composable
private fun ApodContent(apod: ApodDomain, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (apod.mediaType == ApodMediaType.IMAGE) {
            CosmosWatchAsyncImage(
                model = apod.hdImageUrl ?: apod.imageUrl,
                contentDescription = apod.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
        } else {
            Text(
                text = stringResource(R.string.apod_watch_video),
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable { uriHandler.openUri(apod.imageUrl) },
            )
        }
        Text(text = apod.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = apod.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(text = apod.explanation, style = MaterialTheme.typography.bodyMedium)
        apod.copyright?.let {
            Text(
                text = stringResource(R.string.apod_copyright, it),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.apod_error_network)
    is AppError.Server -> stringResource(R.string.apod_error_server)
    is AppError.Unknown -> stringResource(R.string.apod_error_unknown)
    is AppError.Validation -> stringResource(R.string.apod_error_unknown)
}
