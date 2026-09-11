package com.cosmoswatch.feature.apod.presentation.archivedetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@Composable
fun ApodArchiveDetailScreen(
    onBackClick: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApodArchiveDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ApodArchiveDetailScreenContent(
        state = state,
        onBackClick = onBackClick,
        onImageClick = onImageClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApodArchiveDetailScreenContent(
    state: ApodArchiveDetailState,
    onBackClick: () -> Unit,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.apod_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (state) {
            ApodArchiveDetailState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding).fillMaxSize())
            ApodArchiveDetailState.NotAvailable -> ErrorState(
                message = stringResource(R.string.apod_archive_detail_not_available),
                retryLabel = stringResource(R.string.apod_back),
                onRetry = onBackClick,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
            )
            is ApodArchiveDetailState.Content -> ApodContent(
                apod = state.apod,
                onImageClick = onImageClick,
                modifier = Modifier.padding(innerPadding),
                showTodayBadge = false
            )
        }
    }
}
