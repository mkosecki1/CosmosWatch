package com.cosmoswatch.feature.neows.presentation.upcoming

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.ui.Alignment
import com.cosmoswatch.core.ui.component.InfoDialogIcon
import com.cosmoswatch.core.ui.theme.SentryBlue
import com.cosmoswatch.feature.neows.R
import com.cosmoswatch.feature.neows.domain.NeoDomain
import com.cosmoswatch.feature.neows.presentation.common.approachLabel
import com.cosmoswatch.feature.neows.presentation.common.daysUntilCloseApproach
import com.cosmoswatch.feature.neows.presentation.common.toMessage
import java.time.LocalDate

@Composable
fun NeoTile(
    neoDomain: NeoDomain,
    today: LocalDate,
    modifier: Modifier = Modifier
) {
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
            NeoTileMainIcon(isPotentiallyHazardous = neoDomain.isPotentiallyHazardous)
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
                Row(
                    modifier = Modifier.padding(top = 6.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (neoDomain.isPotentiallyHazardous) {
                        NeoWsTag(
                            title = stringResource(R.string.neo_badge_hazardous),
                            color = MaterialTheme.colorScheme.errorContainer
                        )
                        InfoDialogIcon(
                            title = stringResource(R.string.neo_info_hazardous_title),
                            body = stringResource(R.string.neo_info_hazardous_body),
                            closeLabel = stringResource(R.string.neo_info_dialog_close)
                        )
                    }
                    if (neoDomain.isSentryObject) {
                        NeoWsTag(
                            title = stringResource(R.string.neo_badge_sentry),
                            color = SentryBlue
                        )
                        InfoDialogIcon(
                            title = stringResource(R.string.neo_info_sentry_title),
                            body = stringResource(R.string.neo_info_sentry_body),
                            closeLabel = stringResource(R.string.neo_info_dialog_close)
                        )
                    }
                }
                NeoStatRow(
                    label = stringResource(R.string.neo_stat_approach_label),
                    value = approachLabel(daysUntilCloseApproach(neoDomain.closeApproachDate, today))
                )
                NeoStatRow(
                    label = stringResource(R.string.neo_stat_distance_label),
                    value = "%.2f LD".format(neoDomain.missDistanceLunar),
                    infoTitle = stringResource(R.string.neo_info_distance_title),
                    infoBody = stringResource(R.string.neo_info_distance_body)
                )
                NeoStatRow(
                    label = stringResource(R.string.neo_stat_velocity_label),
                    value = "%,.0f km/h".format(neoDomain.relativeVelocityKmh)
                )
                NeoStatRow(
                    label = stringResource(R.string.neo_stat_diameter_label),
                    value = "%.0f-%.0f m".format(
                        neoDomain.estimatedDiameterMinMeters,
                        neoDomain.estimatedDiameterMaxMeters
                    )
                )
                NeoSizeReferenceRow(
                    diameterMinMeters = neoDomain.estimatedDiameterMinMeters,
                    diameterMaxMeters = neoDomain.estimatedDiameterMaxMeters
                )
            }
        }

    }
}

@Composable
private fun NeoTileMainIcon(isPotentiallyHazardous: Boolean) {
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
        Icon(
            painter = painterResource(R.drawable.ic_badge_radar),
            contentDescription = null,
            tint = if (isPotentiallyHazardous) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
private fun NeoWsTag(title: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
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
private fun NeoStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    infoTitle: String? = null,
    infoBody: String? = null
) {
    Row(
        modifier = modifier.padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 7.dp, height = 16.dp)
                    .clip(RoundedCornerShape(3.5.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
        if (infoTitle != null && infoBody != null) {
            InfoDialogIcon(
                title = infoTitle,
                body = infoBody,
                closeLabel = stringResource(R.string.neo_info_dialog_close)
            )
        }
    }
}

@Composable
private fun NeoSizeReferenceRow(
    diameterMinMeters: Double,
    diameterMaxMeters: Double,
    modifier: Modifier = Modifier,
) {
    val comparison = closestSizeComparison(diameterMinMeters, diameterMaxMeters)

    Column(modifier = modifier.padding(top = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SizeReferenceChip(icon = Icons.Outlined.DirectionsCar, highlighted = comparison.referenceIndex == 0)
            SizeReferenceChip(icon = Icons.Outlined.Flight, highlighted = comparison.referenceIndex == 1)
            SizeReferenceChip(iconRes = R.drawable.ic_football_pitch, highlighted = comparison.referenceIndex == 2)
            SizeReferenceChip(icon = Icons.Outlined.Apartment, highlighted = comparison.referenceIndex == 3)
        }
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = comparison.toMessage(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SizeReferenceChip(
    highlighted: Boolean,
    icon: ImageVector? = null,
    iconRes: Int? = null
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (highlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .then(
                if (highlighted) Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        val tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
        } else if (iconRes != null) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
        }
    }
}