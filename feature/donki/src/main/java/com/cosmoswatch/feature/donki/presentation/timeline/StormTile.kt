package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain

@Composable
fun StormTile(storm: DonkiEventDomain.GeomagneticStorm, modifier: Modifier = Modifier) {
    val badge = DonkiBadge(
        label = stringResource(R.string.donki_badge_storm_scale, geomagneticStormScale(storm.peakKp)),
        info = DonkiInfo(
            title = stringResource(R.string.donki_info_storm_scale_title),
            body = stringResource(R.string.donki_info_storm_scale_body)
        )
    )
    val durationStart = storm.kpIndexSeries.firstOrNull()?.time ?: storm.eventTime
    val durationEnd = storm.kpIndexSeries.lastOrNull()?.time ?: storm.eventTime
    val relatedCmes = storm.linkedEventIds.count { "CME" in it }

    DonkiEventCard(
        icon = Icons.Outlined.Thunderstorm,
        title = stringResource(R.string.donki_storm_title),
        eventTime = storm.eventTime,
        severity = storm.severity,
        badge = badge,
        modifier = modifier,
        content = {
            Spacer(modifier = Modifier.height(8.dp))
            DonkiStatRow(
                label = stringResource(R.string.donki_stat_duration_label),
                value = dateRangeText(durationStart, durationEnd)
            )
            if (relatedCmes > 0) {
                DonkiStatRow(
                    label = stringResource(R.string.donki_stat_related_cmes_label),
                    value = relatedCmes.toString()
                )
            }
        }
    )
}
