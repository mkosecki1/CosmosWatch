package com.cosmoswatch.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun CosmosWatchAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderColor: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val context = LocalPlatformContext.current
    val request = remember(model) {
        ImageRequest.Builder(context)
            .data(model)
            .crossfade(true)
            .build()
    }
    var isLoading by remember(model) { mutableStateOf(true) }

    Box(modifier = modifier.background(color = placeholderColor)) {
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            onState = { state ->
                isLoading = state !is AsyncImagePainter.State.Success && state !is AsyncImagePainter.State.Error
            },
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).size(24.dp))
        }
    }
}
