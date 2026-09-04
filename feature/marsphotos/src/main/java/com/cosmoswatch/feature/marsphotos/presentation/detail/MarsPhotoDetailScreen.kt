package com.cosmoswatch.feature.marsphotos.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cosmoswatch.core.ui.component.CosmosWatchAsyncImage
import com.cosmoswatch.core.ui.component.ErrorState
import com.cosmoswatch.core.ui.component.LoadingState
import com.cosmoswatch.feature.marsphotos.R
import com.cosmoswatch.feature.marsphotos.domain.MarsPhotoDomain
import com.cosmoswatch.feature.marsphotos.presentation.displayName
import java.time.format.DateTimeFormatter

@Composable
fun MarsPhotoDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MarsPhotoDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MarsPhotoDetailScreenContent(
        state = state,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun MarsPhotoDetailScreenContent(
    state: MarsPhotoDetailState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        MarsPhotoDetailState.Loading -> LoadingState(modifier = modifier.fillMaxSize())
        MarsPhotoDetailState.NotAvailable -> ErrorState(
            message = stringResource(R.string.marsphotos_detail_not_available),
            retryLabel = stringResource(R.string.marsphotos_back),
            onRetry = onBackClick,
            modifier = modifier.fillMaxSize(),
        )
        is MarsPhotoDetailState.Content -> PhotoDetailContent(
            photo = state.photo,
            onBackClick = onBackClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PhotoDetailContent(photo: MarsPhotoDomain, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        CosmosWatchAsyncImage(
            model = photo.imageUrl,
            contentDescription = photo.cameraFullName,
            modifier = Modifier.fillMaxSize(),
        )

        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(color = Color.Black.copy(alpha = 0.45f), shape = CircleShape),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.marsphotos_back),
                tint = Color.White,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = photo.cameraFullName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(
                        R.string.marsphotos_sol_date_rover,
                        photo.sol,
                        photo.earthDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        photo.rover.displayName(),
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
