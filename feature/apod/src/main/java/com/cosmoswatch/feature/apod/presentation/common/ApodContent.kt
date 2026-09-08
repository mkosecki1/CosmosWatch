package com.cosmoswatch.feature.apod.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cosmoswatch.core.ui.component.CosmosWatchAsyncImage
import com.cosmoswatch.feature.apod.R
import com.cosmoswatch.feature.apod.domain.ApodDomain
import com.cosmoswatch.feature.apod.domain.ApodMediaType
import java.time.format.DateTimeFormatter

@Composable
internal fun ApodContent(apod: ApodDomain, onImageClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (apod.mediaType == ApodMediaType.IMAGE) {
            val fullImageUrl = apod.hdImageUrl ?: apod.imageUrl
            CosmosWatchAsyncImage(
                model = fullImageUrl,
                contentDescription = apod.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onImageClick(fullImageUrl) },
            )
        } else {
            Text(
                text = stringResource(R.string.apod_watch_video),
                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable { uriHandler.openUri(apod.imageUrl) },
            )
        }
        Text(text = apod.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = apod.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(text = apod.explanation, style = MaterialTheme.typography.bodyMedium)
        apod.copyright?.let {
            Text(
                text = stringResource(R.string.apod_copyright, it),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
