package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.presentation.common.toMessage

@Composable
fun NeoUpcomingScreen(
    onArchiveClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NeoUpcomingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NeoUpcomingScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onArchiveClick = onArchiveClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeoUpcomingScreenContent(
    state: NeoUpcomingState,
    onIntent: (NeoUpcomingIntent) -> Unit,
    onArchiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var showFilterInfo by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.neo_upcoming_title)) },
                actions = {
                    IconButton(onClick = { showFilterInfo = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = stringResource(R.string.neo_filters_info_description),
                        )
                    }
                    IconButton(onClick = onArchiveClick) {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = stringResource(R.string.neo_browse_archive),
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->

        when (state) {
            is NeoUpcomingState.Loading -> LoadingState(modifier = Modifier.padding(innerPadding).fillMaxSize())
            is NeoUpcomingState.Error -> ErrorState(
                message = state.error.toMessage(),
                modifier = Modifier.padding(innerPadding).fillMaxWidth(),
                retryLabel = stringResource(R.string.neo_retry),
                onRetry = { onIntent(NeoUpcomingIntent.Retry) }
            )
            is NeoUpcomingState.Success -> NeoUpcomingScreenList(
                innerPadding = innerPadding,
                state = state,
                onIntent =  onIntent
            )
        }
    }
    if (showFilterInfo) {
        FilterInfoDialog(onDismiss = { showFilterInfo = false })
    }
}

@Composable
private fun FilterInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.neo_info_dialog_close))
            }
        },
        title = { Text(
            text = stringResource(R.string.neo_filters_info_title),
            fontWeight = FontWeight.Bold
        )},
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterExplanation(stringResource(R.string.neo_info_ld_title), stringResource(R.string.neo_info_ld_body))
                FilterExplanation(stringResource(R.string.neo_filter_hazardous), stringResource(R.string.neo_info_hazardous_body))
                FilterExplanation(stringResource(R.string.neo_filter_close), stringResource(R.string.neo_info_close_body))
                FilterExplanation(stringResource(R.string.neo_filter_large), stringResource(R.string.neo_info_large_body))
                FilterExplanation(stringResource(R.string.neo_badge_sentry), stringResource(R.string.neo_info_sentry_body))
            }
        }
    )
}

@Composable
private fun FilterExplanation(title: String, body: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}
