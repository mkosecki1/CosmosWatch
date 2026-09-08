package com.cosmoswatch.feature.apod.presentation.daily

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.feature.apod.R
import com.cosmoswatch.feature.apod.presentation.common.ApodContent
import com.cosmoswatch.feature.apod.presentation.common.toMessage

@Composable
fun ApodScreen(
    onArchiveClick: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApodViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ApodScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onArchiveClick = onArchiveClick,
        onImageClick = onImageClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApodScreenContent(
    state: ApodState,
    onIntent: (ApodIntent) -> Unit,
    onArchiveClick: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.apod_title)) },
                actions = {
                    IconButton(onClick = onArchiveClick) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.apod_browse_archive))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            is ApodState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is ApodState.Error -> ErrorState(
                message = state.error.toMessage(),
                retryLabel = stringResource(R.string.apod_retry),
                onRetry = { onIntent(ApodIntent.Retry) },
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            is ApodState.Success -> ApodContent(
                apod = state.apod,
                onImageClick = onImageClick,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
