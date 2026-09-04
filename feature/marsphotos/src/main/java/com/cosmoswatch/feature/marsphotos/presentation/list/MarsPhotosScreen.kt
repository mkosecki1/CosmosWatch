package com.cosmoswatch.feature.marsphotos.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.cosmoswatch.core.common.result.AppError
import com.cosmoswatch.core.ui.component.CosmosWatchAsyncImage
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.feature.marsphotos.R
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.domain.MarsRover
import com.cosmoswatch.feature.marsphotos.domain.MarsRoverStatus
import com.cosmoswatch.feature.marsphotos.presentation.displayName
import com.cosmoswatch.feature.marsphotos.presentation.labelRes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun MarsPhotosScreen(
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MarsPhotosViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val photos = viewModel.photos.collectAsLazyPagingItems()

    MarsPhotosScreenContent(
        state = state,
        photos = photos,
        onIntent = viewModel::onIntent,
        onPhotoClick = onPhotoClick,
        modifier = modifier,
    )
}

@Composable
private fun MarsPhotosScreenContent(
    state: MarsPhotosState,
    photos: LazyPagingItems<MarsPhotoDomain>,
    onIntent: (MarsPhotosIntent) -> Unit,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        RoverSwitcher(
            selected = state.rover,
            onRoverSelected = { onIntent(MarsPhotosIntent.RoverSelected(it)) },
        )

        when (state) {
            is MarsPhotosState.ResolvingDefaultDate -> LoadingState(modifier = Modifier.fillMaxSize())
            is MarsPhotosState.DefaultDateError -> ErrorState(
                message = state.error.toMessage(),
                retryLabel = stringResource(R.string.marsphotos_retry),
                onRetry = { onIntent(MarsPhotosIntent.RetryResolvingDefaultDate) },
                modifier = Modifier.fillMaxSize(),
            )
            is MarsPhotosState.Ready -> ReadyContent(
                state = state,
                photos = photos,
                onIntent = onIntent,
                onPhotoClick = onPhotoClick,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadyContent(
    state: MarsPhotosState.Ready,
    photos: LazyPagingItems<MarsPhotoDomain>,
    onIntent: (MarsPhotosIntent) -> Unit,
    onPhotoClick: (Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        MissionInfoStrip(rover = state.rover, earthDate = state.earthDate, status = state.status, photos = photos)
        DateControlRow(
            rover = state.rover,
            earthDate = state.earthDate,
            latestAvailableDate = state.latestAvailableDate,
            onIntent = onIntent,
        )
        CameraFilterChips(
            rover = state.rover,
            selectedCamera = state.camera,
            onCameraSelected = { onIntent(MarsPhotosIntent.CameraSelected(it)) },
        )

        if (photos.itemCount == 0 && photos.loadState.refresh is LoadState.Loading) {
            LoadingState(modifier = Modifier.fillMaxSize())
        } else if (photos.itemCount == 0 && photos.loadState.refresh is LoadState.Error) {
            ErrorState(
                message = stringResource(R.string.marsphotos_error_unknown),
                retryLabel = stringResource(R.string.marsphotos_retry),
                onRetry = { photos.retry() },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            PhotoGrid(
                photos = photos,
                groupByCamera = state.camera == null,
                onPhotoClick = onPhotoClick,
            )
        }
    }
}

@Composable
private fun RoverSwitcher(selected: MarsRover, onRoverSelected: (MarsRover) -> Unit, modifier: Modifier = Modifier) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth().padding(16.dp)) {
        MarsRover.entries.forEachIndexed { index, rover ->
            SegmentedButton(
                selected = rover == selected,
                onClick = { onRoverSelected(rover) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = MarsRover.entries.size),
            ) {
                Text(text = rover.displayName())
            }
        }
    }
}

@Composable
private fun MissionInfoStrip(
    rover: MarsRover,
    earthDate: LocalDate,
    status: MarsRoverStatus,
    photos: LazyPagingItems<MarsPhotoDomain>,
    modifier: Modifier = Modifier,
) {
    val sol = if (photos.itemCount > 0) photos[0]?.sol else null
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = if (status == MarsRoverStatus.ACTIVE) Color(0xFF3E8E5A) else Color.Gray,
                        shape = CircleShape,
                    ),
            )
            Text(
                text = stringResource(
                    R.string.marsphotos_status_and_landed,
                    stringResource(status.labelRes()),
                    rover.landingDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        if (sol != null) {
            Text(
                text = stringResource(R.string.marsphotos_sol_and_date, sol, earthDate.format(DateTimeFormatter.ISO_LOCAL_DATE)),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateControlRow(
    rover: MarsRover,
    earthDate: LocalDate,
    latestAvailableDate: LocalDate,
    onIntent: (MarsPhotosIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(
            onClick = { onIntent(MarsPhotosIntent.PreviousDay) },
            enabled = earthDate.isAfter(rover.landingDate),
        ) {
            Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = stringResource(R.string.marsphotos_previous_day))
        }
        TextButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.marsphotos_pick_date))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = earthDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }
        IconButton(
            onClick = { onIntent(MarsPhotosIntent.NextDay) },
            enabled = earthDate.isBefore(latestAvailableDate),
        ) {
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = stringResource(R.string.marsphotos_next_day))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = earthDate.toEpochMillisUtc(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    val date = utcTimeMillis.toLocalDateUtc()
                    return !date.isBefore(rover.landingDate) && !date.isAfter(latestAvailableDate)
                }
            },
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onIntent(MarsPhotosIntent.DateSelected(it.toLocalDateUtc()))
                    }
                    showDatePicker = false
                }) {
                    Text(text = stringResource(R.string.marsphotos_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(text = stringResource(R.string.marsphotos_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun CameraFilterChips(
    rover: MarsRover,
    selectedCamera: String?,
    onCameraSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selectedCamera == null,
                onClick = { onCameraSelected(null) },
                label = { Text(text = stringResource(R.string.marsphotos_camera_all)) },
            )
        }
        items(items = rover.cameras, key = { it }) { camera ->
            FilterChip(
                selected = selectedCamera == camera,
                onClick = { onCameraSelected(camera) },
                label = { Text(text = camera) },
            )
        }
    }
}

@Composable
private fun PhotoGrid(
    photos: LazyPagingItems<MarsPhotoDomain>,
    groupByCamera: Boolean,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        var previousCameraFullName: String? = null
        for (index in 0 until photos.itemCount) {
            val photo = photos[index]
            if (groupByCamera && photo != null && photo.cameraFullName != previousCameraFullName) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header-${photo.cameraFullName}") {
                    CameraSectionHeader(cameraFullName = photo.cameraFullName)
                }
                previousCameraFullName = photo.cameraFullName
            }
            item(span = { GridItemSpan(1) }, key = photo?.id ?: "placeholder-$index") {
                if (photo != null) {
                    PhotoTile(photo = photo, onClick = { onPhotoClick(photo.id) })
                }
            }
        }

        if (photos.loadState.append is LoadState.Loading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun CameraSectionHeader(cameraFullName: String, modifier: Modifier = Modifier) {
    Text(
        text = cameraFullName,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun PhotoTile(photo: MarsPhotoDomain, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
    ) {
        CosmosWatchAsyncImage(
            model = photo.imageUrl,
            contentDescription = photo.cameraFullName,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
            Text(
                text = "${photo.cameraName} · Sol ${photo.sol}",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun AppError.toMessage(): String = when (this) {
    AppError.Network -> stringResource(R.string.marsphotos_error_network)
    is AppError.Server -> stringResource(R.string.marsphotos_error_server)
    is AppError.Unknown -> stringResource(R.string.marsphotos_error_unknown)
    is AppError.Validation -> stringResource(R.string.marsphotos_error_unknown)
}

private fun LocalDate.toEpochMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDateUtc(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
