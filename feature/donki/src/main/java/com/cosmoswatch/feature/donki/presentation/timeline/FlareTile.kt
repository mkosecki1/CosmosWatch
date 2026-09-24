package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain

@Composable
fun FlareTile(flare: DonkiEventDomain.Flare, modifier: Modifier = Modifier) {
    DonkiEventCard(
        icon = Icons.Outlined.WbSunny,
        title = stringResource(R.string.donki_flare_title),
        eventTime = flare.eventTime,
        severity = flare.severity,
        badge = flare.classType,
        modifier = modifier
    )
}