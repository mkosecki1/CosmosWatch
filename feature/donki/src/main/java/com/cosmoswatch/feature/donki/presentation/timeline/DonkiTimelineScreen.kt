package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
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
import androidx.paging.compose.itemKey
import com.cosmoswatch.core.ui.component.EmptyState
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.core.ui.component.LocalBottomBarPadding
import com.cosmoswatch.core.ui.component.ToggleChip
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import com.cosmoswatch.feature.donki.domain.DonkiTimelineFilter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

private const val NEW_RESULTS_TIMEOUT_MILLIS = 1000L

@Composable
fun DonkiTimelineScreen(
    modifier: Modifier = Modifier,
    viewModel: DonkiTimelineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeline = viewModel.timeline.collectAsLazyPagingItems()

    DonkiTimelineScreenContent(
        state = state,
        timeline = timeline,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DonkiTimelineScreenContent(
    state: DonkiTimelineState,
    timeline: LazyPagingItems<DonkiEventDomain>,
    onIntent: (DonkiTimelineIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBarPadding = LocalBottomBarPadding.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val listState = rememberLazyListState()
    val currentFilter by rememberUpdatedState(state.filter)

    LaunchedEffect(listState) {
        snapshotFlow { currentFilter }
            .drop(1)
            .collectLatest {
                listState.scrollToItem(0)
                val previousItem = timeline.itemSnapshotList
                withTimeoutOrNull(NEW_RESULTS_TIMEOUT_MILLIS.milliseconds) {
                    snapshotFlow { timeline.itemSnapshotList }.first { it != previousItem }
                    listState.scrollToItem(0)
                }
            }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.donki_timeline_title)) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.fillMaxSize()

        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            FilterChipsRow(
                filter = state.filter,
                onIntent = onIntent
            )
            when {
                timeline.itemCount == 0 && timeline.loadState.refresh is LoadState.Loading ->
                    LoadingState(modifier = contentModifier)
                timeline.itemCount == 0 && timeline.loadState.refresh is LoadState.Error -> ErrorState(
                    message = stringResource(R.string.donki_error_unknown),
                    retryLabel = stringResource(R.string.donki_retry),
                    onRetry = { timeline.retry() },
                    modifier = contentModifier,
                )
                timeline.itemCount == 0 -> EmptyState(
                    message = stringResource(R.string.donki_timeline_empty),
                    modifier = contentModifier,
                )
                else -> LazyColumn(
                    modifier = contentModifier,
                    contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = bottomBarPadding),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState
                ) {
                    items(count = timeline.itemCount, key = timeline.itemKey { it.id }) { index ->
                        timeline[index]?.let { event ->
                            DonkiEventTile(event = event, modifier = Modifier.animateItem())
                        }
                    }

                    if (timeline.loadState.append is LoadState.Loading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun FilterChipsRow(
    filter: DonkiTimelineFilter,
    onIntent: (DonkiTimelineIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val noTypeSelected = !filter.flaresSelected && !filter.cmesSelected && !filter.stormsSelected

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ToggleChip(
            label = stringResource(R.string.donki_filter_flares),
            selected = filter.flaresSelected,
            onClick = { onIntent(DonkiTimelineIntent.FilterChanged(filter.copy(flaresSelected = !filter.flaresSelected))) }
        )
        ToggleChip(
            label = stringResource(R.string.donki_filter_cmes),
            selected = filter.cmesSelected,
            onClick = { onIntent(DonkiTimelineIntent.FilterChanged(filter.copy(cmesSelected = !filter.cmesSelected))) }
        )
        ToggleChip(
            label = stringResource(R.string.donki_filter_storms),
            selected = filter.stormsSelected,
            onClick = { onIntent(DonkiTimelineIntent.FilterChanged(filter.copy(stormsSelected = !filter.stormsSelected))) }
        )
        ToggleChip(
            label = stringResource(R.string.donki_filter_significant_flares),
            selected = filter.significantOnly,
            onClick = { onIntent(DonkiTimelineIntent.FilterChanged(filter.copy(significantOnly = !filter.significantOnly))) },
            enabled = filter.flaresSelected || noTypeSelected
        )
        ToggleChip(
            label = stringResource(R.string.donki_filter_earth_directed),
            selected = filter.earthDirectedOnly,
            onClick = { onIntent(DonkiTimelineIntent.FilterChanged(filter.copy(earthDirectedOnly = !filter.earthDirectedOnly))) },
            enabled = filter.cmesSelected || noTypeSelected
        )
    }
}