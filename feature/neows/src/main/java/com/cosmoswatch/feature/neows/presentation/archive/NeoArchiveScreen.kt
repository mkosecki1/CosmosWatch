package com.cosmoswatch.feature.neows.presentation.archive

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.cosmoswatch.core.ui.component.ClearFilterChip
import com.cosmoswatch.core.ui.component.DateBoundPickerDialog
import com.cosmoswatch.core.ui.component.DateFilterChip
import com.cosmoswatch.core.ui.component.EmptyState
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.core.ui.component.ToggleChip
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.domain.NeoArchiveFilter
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.presentation.common.toMessage
import com.cosmoswatch.feature.neows.presentation.upcoming.NeoTile
import java.time.LocalDate

@Composable
fun NeoArchiveScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NeoArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val archive = viewModel.archive.collectAsLazyPagingItems()

    NeoArchiveScreenContent(
        state = state,
        archive = archive,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NeoArchiveScreenContent(
    state: NeoArchiveState,
    archive: LazyPagingItems<NeoDomain>,
    onIntent: (NeoArchiveIntent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilterRow by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.neo_archive_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.neo_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterRow = !showFilterRow }) {
                        Icon(Icons.Filled.FilterList, contentDescription = stringResource(R.string.neo_archive_filter))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            AnimatedVisibility(visible = showFilterRow) {
                Column {
                    DateFilterRow(filter = state.filter, today = state.today, onIntent = onIntent)
                    FilterChipsRow(filter = state.filter, onIntent = onIntent)
                }
            }

            when {
                state.filterError != null -> ErrorState(
                    message = state.filterError.toMessage(),
                    modifier = Modifier.fillMaxSize(),
                )
                archive.itemCount == 0 && archive.loadState.refresh is LoadState.Loading ->
                    LoadingState(modifier = Modifier.fillMaxSize())
                archive.itemCount == 0 && archive.loadState.refresh is LoadState.Error -> ErrorState(
                    message = stringResource(R.string.neo_error_unknown),
                    retryLabel = stringResource(R.string.neo_retry),
                    onRetry = { archive.retry() },
                    modifier = Modifier.fillMaxSize(),
                )
                archive.itemCount == 0 -> EmptyState(message = stringResource(R.string.neo_archive_empty))
                else -> ArchiveList(archive = archive, today = state.today)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterRow(
    filter: NeoArchiveFilter,
    today: LocalDate,
    onIntent: (NeoArchiveIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    val latestEndDate = today.minusDays(1)

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ClearFilterChip(
            contentDescription = stringResource(R.string.neo_clear_filter),
            onClick = { onIntent(NeoArchiveIntent.ClearFilter) }
        )
        DateFilterChip(
            label = filter.startDate?.toString() ?: stringResource(R.string.neo_archive_filter_from_unset),
            onClick = { pickingStart = true },
            modifier = Modifier.weight(1f)
        )
        DateFilterChip(
            label = filter.endDate?.toString() ?: stringResource(R.string.neo_archive_filter_to_unset),
            onClick = { pickingEnd = true },
            modifier = Modifier.weight(1f)
        )
    }

    if (pickingStart) {
        DateBoundPickerDialog(
            initialDate = filter.startDate ?: latestEndDate,
            latest = filter.endDate ?: latestEndDate,
            confirmLabel = stringResource(R.string.neo_archive_confirm),
            cancelLabel = stringResource(R.string.neo_archive_cancel),
            onDismiss = { pickingStart = false },
            onConfirm = { picked ->
                onIntent(NeoArchiveIntent.FilterChanged(filter.copy(startDate = picked)))
                pickingStart = false
            },
        )
    }
    if (pickingEnd) {
        DateBoundPickerDialog(
            initialDate = filter.endDate ?: latestEndDate,
            earliest = filter.startDate,
            latest = latestEndDate,
            confirmLabel = stringResource(R.string.neo_archive_confirm),
            cancelLabel = stringResource(R.string.neo_archive_cancel),
            onDismiss = { pickingEnd = false },
            onConfirm = { picked ->
                onIntent(NeoArchiveIntent.FilterChanged(filter.copy(endDate = picked)))
                pickingEnd = false
            },
        )
    }
}

@Composable
private fun FilterChipsRow(filter: NeoArchiveFilter, onIntent: (NeoArchiveIntent) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleChip(
            selected = filter.hazardousOnly,
            onClick = { onIntent(NeoArchiveIntent.FilterChanged(filter.copy(hazardousOnly = !filter.hazardousOnly))) },
            label = stringResource(R.string.neo_filter_hazardous)
        )
        ToggleChip(
            selected = filter.closeOnly,
            onClick = { onIntent(NeoArchiveIntent.FilterChanged(filter.copy(closeOnly = !filter.closeOnly))) },
            label = stringResource(R.string.neo_filter_close)
        )
        ToggleChip(
            selected = filter.largeOnly,
            onClick = { onIntent(NeoArchiveIntent.FilterChanged(filter.copy(largeOnly = !filter.largeOnly))) },
            label = stringResource(R.string.neo_filter_large)
        )
        ToggleChip(
            selected = filter.sentryOnly,
            onClick = { onIntent(NeoArchiveIntent.FilterChanged(filter.copy(sentryOnly = !filter.sentryOnly))) },
            label = stringResource(R.string.neo_filter_sentry)
        )
    }
}

@Composable
private fun ArchiveList(
    archive: LazyPagingItems<NeoDomain>,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val bottomContentPadding = LocalBottomBarPadding.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = bottomContentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            count = archive.itemCount,
            key = { index -> archive.peek(index)?.id ?: "placeholder-$index" },
        ) { index ->
            archive[index]?.let { neoItem -> NeoTile(neoDomain = neoItem, today = today) }
        }

        if (archive.loadState.append is LoadState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
