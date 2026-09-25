package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cosmoswatch.core.ui.component.InfoDialogIcon
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain
import java.time.Instant
import kotlin.math.roundToInt

@Composable
fun CmeTile(cme: DonkiEventDomain.CmeEjection, modifier: Modifier = Modifier) {
    DonkiEventCard(
        icon = Icons.Outlined.NorthEast,
        title = stringResource(R.string.donki_cme_title),
        eventTime = cme.eventTime,
        severity = cme.severity,
        modifier = modifier,
        content = {
            Spacer(Modifier.height(12.dp))
            DonkiStatRow(
                label = stringResource(R.string.donki_stat_detected_label),
                value = cme.eventTime.toUtcTimeText()
            )
            cme.speedKmS?.let {
                DonkiStatRow(
                    label = stringResource(R.string.donki_stat_speed_label),
                    value = stringResource(R.string.donki_speed_value, it.roundToInt())
                )
            }
            when {
                !cme.isEarthDirected -> CmeNotHeadingToEarth()
                cme.estimatedArrivalTime != null -> CmeEarthForecast(
                    arrivalTime = cme.estimatedArrivalTime,
                    forecastKp = cme.forecastKp,
                    accent = cme.severity.accentColor()
                )
            }
        }
    )
}

@Composable
private fun CmeEarthForecast(
    arrivalTime: Instant,
    forecastKp: Double?,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.12f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = stringResource(R.string.donki_cme_forecast_header).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                ForecastLabel(text = stringResource(R.string.donki_cme_forecast_arrival_label))
                Text(
                    text = arrivalTime.toArrivalTimeText(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            if (forecastKp != null) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ForecastLabel(text = stringResource(R.string.donki_cme_forecast_storm_label))
                        InfoDialogIcon(
                            title = stringResource(R.string.donki_info_storm_scale_title),
                            body = stringResource(R.string.donki_info_storm_scale_body),
                            closeLabel = stringResource(R.string.donki_info_dialog_close)
                        )
                    }
                    ExpectedStorm(forecastKp = forecastKp, accent = accent)
                }
            }
        }
    }
}

@Composable
private fun ExpectedStorm(forecastKp: Double, accent: Color) {
    if (!isStormLevelKp(forecastKp)) {
        Text(
            text = stringResource(R.string.donki_cme_forecast_no_storm),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        return
    }
    val gScale = geomagneticStormScale(forecastKp)
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = accent.copy(alpha = 0.15f),
            contentColor = accent
        ) {
            Text(
                text = stringResource(R.string.donki_badge_storm_scale, gScale),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Text(
            text = stringResource(stormLevelNameRes(gScale)),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
private fun ForecastLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun CmeNotHeadingToEarth(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Block,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.donki_cme_not_heading_to_earth),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
