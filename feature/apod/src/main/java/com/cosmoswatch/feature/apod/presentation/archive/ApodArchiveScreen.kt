package com.cosmoswatch.feature.apod.presentation.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.cosmoswatch.core.ui.component.CosmosWatchAsyncImage
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.feature.apod.R
import com.cosmoswatch.feature.apod.domain.APOD_ARCHIVE_START_DATE
import com.cosmoswatch.feature.apod.domain.ApodArchiveFilter
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.presentation.common.VideoPlayOverlay
import com.cosmoswatch.feature.apod.presentation.common.toEpochMillisUtc
import com.cosmoswatch.feature.apod.presentation.common.toLocalDateUtc
import com.cosmoswatch.feature.apod.presentation.common.toMessage
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ApodArchiveScreen(
    onBackClick: () -> Unit,
    onEntryClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApodArchiveViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val archive = viewModel.archive.collectAsLazyPagingItems()

    ApodArchiveScreenContent(
        state = state,
        archive = archive,
        onIntent = viewModel::onIntent,
        onBackClick = onBackClick,
        onEntryClick = onEntryClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApodArchiveScreenContent(
    state: ApodArchiveState,
    archive: LazyPagingItems<ApodDomain>,
    onIntent: (ApodArchiveIntent) -> Unit,
    onBackClick: () -> Unit,
    onEntryClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showFilterRow by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.apod_archive_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.apod_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterRow = !showFilterRow }) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.apod_archive_filter))
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (showFilterRow) {
                FilterRow(filter = state.filter, onIntent = onIntent)
            }

            when {
                state.filterError != null -> ErrorState(
                    message = state.filterError.toMessage(),
                    modifier = Modifier.fillMaxSize(),
                )
                archive.itemCount == 0 && archive.loadState.refresh is LoadState.Loading ->
                    LoadingState(modifier = Modifier.fillMaxSize())
                archive.itemCount == 0 && archive.loadState.refresh is LoadState.Error -> ErrorState(
                    message = stringResource(R.string.apod_error_unknown),
                    retryLabel = stringResource(R.string.apod_retry),
                    onRetry = { archive.retry() },
                    modifier = Modifier.fillMaxSize(),
                )
                archive.itemCount == 0 -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.apod_archive_empty), style = MaterialTheme.typography.bodyLarge)
                }
                else -> ArchiveList(archive = archive, onEntryClick = onEntryClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(filter: ApodArchiveFilter, onIntent: (ApodArchiveIntent) -> Unit, modifier: Modifier = Modifier) {
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = { pickingStart = true }, modifier = Modifier.weight(1f)) {
            Text(
                text = filter.startDate?.let { stringResource(R.string.apod_archive_filter_from, it.iso()) }
                    ?: stringResource(R.string.apod_archive_filter_from_unset),
            )
        }
        TextButton(onClick = { pickingEnd = true }, modifier = Modifier.weight(1f)) {
            Text(
                text = filter.endDate?.let { stringResource(R.string.apod_archive_filter_to, it.iso()) }
                    ?: stringResource(R.string.apod_archive_filter_to_unset),
            )
        }
        if (filter.startDate != null || filter.endDate != null) {
            IconButton(onClick = { onIntent(ApodArchiveIntent.ClearFilter) }) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.apod_archive_clear_filter))
            }
        }
    }

    if (pickingStart) {
        DateBoundPickerDialog(
            initialDate = filter.startDate ?: LocalDate.now(),
            earliest = APOD_ARCHIVE_START_DATE,
            latest = filter.endDate ?: LocalDate.now(),
            onDismiss = { pickingStart = false },
            onConfirm = { picked ->
                onIntent(ApodArchiveIntent.FilterChanged(filter.copy(startDate = picked)))
                pickingStart = false
            },
        )
    }
    if (pickingEnd) {
        DateBoundPickerDialog(
            initialDate = filter.endDate ?: LocalDate.now(),
            earliest = filter.startDate ?: APOD_ARCHIVE_START_DATE,
            latest = LocalDate.now(),
            onDismiss = { pickingEnd = false },
            onConfirm = { picked ->
                onIntent(ApodArchiveIntent.FilterChanged(filter.copy(endDate = picked)))
                pickingEnd = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateBoundPickerDialog(
    initialDate: LocalDate,
    earliest: LocalDate,
    latest: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochMillisUtc(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val date = utcTimeMillis.toLocalDateUtc()
                return !date.isBefore(earliest) && !date.isAfter(latest)
            }
        },
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { onConfirm(it.toLocalDateUtc()) } ?: onDismiss()
            }) {
                Text(text = stringResource(R.string.apod_archive_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.apod_archive_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
private fun ArchiveList(archive: LazyPagingItems<ApodDomain>, onEntryClick: (LocalDate) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(
            count = archive.itemCount,
            key = { index -> archive.peek(index)?.date?.toString() ?: "placeholder-$index" },
        ) { index ->
            val entry = archive[index]
            if (entry != null) {
                ArchiveRow(entry = entry, onClick = { onEntryClick(entry.date) })
            }
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

@Composable
private fun ArchiveRow(entry: ApodDomain, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ArchiveThumbnail(entry = entry, modifier = Modifier.size(88.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ArchiveThumbnail(entry: ApodDomain, modifier: Modifier = Modifier) {
    val thumbnailModel = when (entry.mediaType) {
        ApodMediaType.IMAGE -> entry.imageUrl
        ApodMediaType.VIDEO -> entry.thumbnailUrl
    }

    Box(modifier = modifier.clip(RoundedCornerShape(14.dp))) {
        if (thumbnailModel != null) {
            CosmosWatchAsyncImage(
                model = thumbnailModel,
                contentDescription = entry.title,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.primaryContainer,
                            ),
                        ),
                    ),
            )
        }
        if (entry.mediaType == ApodMediaType.VIDEO) {
            VideoPlayOverlay(
                contentDescription = stringResource(R.string.apod_archive_video_icon),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

private fun LocalDate.iso(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)
