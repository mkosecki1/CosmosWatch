package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain

@Composable
fun CmeTile(cme: DonkiEventDomain.CmeEjection, modifier: Modifier = Modifier) {
    DonkiEventCard(
        icon = Icons.Outlined.NorthEast,
        title = stringResource(R.string.donki_cme_title),
        eventTime = cme.eventTime,
        severity = cme.severity,
        modifier = modifier
    )
}
