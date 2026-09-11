package com.cosmoswatch.feature.apod.presentation.common

import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.compose.material3.Player
import androidx.media3.ui.compose.material3.PlayerDefaults
import androidx.media3.common.Player as CorePlayer
import com.cosmoswatch.core.ui.component.CosmosWatchAsyncImage
import com.cosmoswatch.feature.apod.R
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import com.cosmoswatch.feature.apod.domain.isPlayableVideo
import com.cosmoswatch.feature.apod.presentation.playback.ApodPlaybackService
import java.time.format.DateTimeFormatter

@Composable
internal fun ApodContent(
    apod: ApodDomain,
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showTodayBadge: Boolean = false
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            apod.mediaType == ApodMediaType.IMAGE -> {
                val fullImageUrl = apod.hdImageUrl ?: apod.imageUrl
                Box {
                    CosmosWatchAsyncImage(
                        model = fullImageUrl,
                        contentDescription = apod.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(MaterialTheme.shapes.large)
                            .clickable { onImageClick(fullImageUrl) },
                    )
                    if(showTodayBadge) {
                        TodayBadge(Modifier.align(Alignment.TopStart).padding(12.dp))
                    }
                }
            }
            apod.isPlayableVideo() -> {
                ApodVideoPlayer(
                    videoUrl = apod.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.large),
                )
            }
            apod.thumbnailUrl != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(MaterialTheme.shapes.large)
                        .clickable { uriHandler.openUri(apod.imageUrl) },
                ) {
                    CosmosWatchAsyncImage(
                        model = apod.thumbnailUrl,
                        contentDescription = apod.title,
                        modifier = Modifier.fillMaxSize(),
                    )
                    VideoPlayOverlay(
                        contentDescription = stringResource(R.string.apod_watch_video),
                        modifier = Modifier.align(Alignment.Center),
                        iconSize = 48.dp,
                        iconPadding = 8.dp,
                    )
                }
            }
            else -> {
                Text(
                    text = stringResource(R.string.apod_watch_video),
                    style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable { uriHandler.openUri(apod.imageUrl) },
                )
            }
        }
        Text(
            text = stringResource(R.string.apod_kicker_daily),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = apod.title,
//            style = MaterialTheme.typography.headlineSmall
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = apod.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Box(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .size(width = 32.dp, height = 3.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
        )
        Text(
            text = apod.explanation,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify
        )
        apod.copyright?.let {
            Text(
                text = stringResource(R.string.apod_copyright, it.replace("\n", ", ")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(markerClass = [UnstableApi::class, ExperimentalApi::class])
@Composable
private fun ApodVideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var mediaController by remember { mutableStateOf<MediaController?>(null) }
    var playbackFailed by remember { mutableStateOf(false) }

    DisposableEffect(videoUrl) {
        val sessionToken = SessionToken(context, ComponentName(context, ApodPlaybackService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get().apply {
                    addListener(object : CorePlayer.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            playbackFailed = true
                        }
                    })
                    setMediaItem(MediaItem.fromUri(videoUrl))
                    prepare()
                }
            },
            ContextCompat.getMainExecutor(context),
        )

        onDispose {
            mediaController?.stop()
            MediaController.releaseFuture(controllerFuture)
        }
    }

    if (playbackFailed) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.apod_watch_video),
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable { uriHandler.openUri(videoUrl) },
            )
        }
    } else {
        Player(
            player = mediaController,
            modifier = modifier,
            showControls = true,
            centerControls = { player, showControls ->
                PlayerDefaults.CenterControls(
                    player = player,
                    visible = showControls,
                    back = {},
                    backSecondary = {},
                    forward = {},
                    forwardSecondary = {},
                )
            },
        )
    }
}

@Composable
private fun TodayBadge(modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.apod_today_badge),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)

        )
    }
}

@Composable
internal fun VideoPlayOverlay(
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 28.dp,
    iconPadding: Dp = 4.dp,
) {
    Icon(
        imageVector = Icons.Filled.PlayArrow,
        contentDescription = contentDescription,
        tint = Color.White,
        modifier = modifier
            .size(iconSize)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .padding(iconPadding),
    )
}
