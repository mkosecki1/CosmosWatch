package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Thunderstorm
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain

@Composable
fun StormTile(storm: DonkiEventDomain.GeomagneticStorm, modifier: Modifier = Modifier) {
    DonkiEventCard(
        icon = Icons.Outlined.Thunderstorm,
        title = stringResource(R.string.donki_storm_title),
        eventTime = storm.eventTime,
        severity = storm.severity,
        badge = stringResource(R.string.donki_badge_storm_scale, geomagneticStormScale(storm.peakKp)),
        modifier = modifier
    )
}
