package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain

@Composable
fun DonkiEventTile(event: DonkiEventDomain, modifier: Modifier = Modifier) {
    when (event) {
        is DonkiEventDomain.Flare -> FlareTile(event, modifier)
        is DonkiEventDomain.CmeEjection -> CmeTile(event, modifier)
        is DonkiEventDomain.GeomagneticStorm -> StormTile(event, modifier)
    }
}
