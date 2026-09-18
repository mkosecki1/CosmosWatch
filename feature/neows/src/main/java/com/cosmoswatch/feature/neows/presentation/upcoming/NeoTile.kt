package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.domain.NeoDomain

@Composable
fun NeoTile(neoDomain: NeoDomain, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            val iconResource =
                if (neoDomain.isPotentiallyHazardous) R.drawable.ic_asteroid_hazardous else R.drawable.ic_asteroid

            NeoTailMainIcon(
                isPotentiallyHazardous = neoDomain.isPotentiallyHazardous,
                iconResource = iconResource
            )
            Column {
                Text(
                    text = stringResource(R.string.neo_tile_designation_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = neoDomain.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (neoDomain.isPotentiallyHazardous) {
                        NeoWsTag(
                            title = stringResource(R.string.neo_badge_hazardous),
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                    if (neoDomain.isSentryObject) {
                        NeoWsTag(
                            title = stringResource(R.string.neo_badge_sentry),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                NeoStatRow(
                    iconRes = R.drawable.ic_stat_distance,
                    label = stringResource(R.string.neo_stat_distance_label),
                    value = "%.2f LD".format(neoDomain.missDistanceLunar)
                )
                NeoStatRow(
                    iconRes = R.drawable.ic_stat_velocity,
                    label = stringResource(R.string.neo_stat_velocity_label),
                    value = "%,.0f km/h".format(neoDomain.relativeVelocityKmh)
                )
                NeoStatRow(
                    iconRes = R.drawable.ic_stat_diameter,
                    label = stringResource(R.string.neo_stat_diameter_label),
                    value = "%.0f-%.0f m".format(
                        neoDomain.estimatedDiameterMinMeters,
                        neoDomain.estimatedDiameterMaxMeters
                    )
                )
            }
        }

    }
}

@Composable
private fun NeoTailMainIcon(isPotentiallyHazardous: Boolean, iconResource: Int) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPotentiallyHazardous) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconResource),
            contentDescription = null,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun NeoWsTag(title: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.padding(top = 6.dp, bottom = 4.dp),
        shape = RoundedCornerShape(50),
        color = color
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun NeoStatRow(iconRes: Int, label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}