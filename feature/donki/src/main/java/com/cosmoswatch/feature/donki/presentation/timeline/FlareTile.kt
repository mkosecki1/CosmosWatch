package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.feature.donki.domain.DonkiEventDomain

@Composable
fun FlareTile(flare: DonkiEventDomain.Flare, modifier: Modifier = Modifier) {
    val badge = DonkiBadge(
        label = flare.classType,
        info = DonkiInfo(
            title = stringResource(R.string.donki_info_flare_class_title),
            body = stringResource(R.string.donki_info_flare_class_body)
        )
    )

    DonkiEventCard(
        icon = Icons.Outlined.WbSunny,
        title = stringResource(R.string.donki_flare_title),
        eventTime = flare.eventTime,
        severity = flare.severity,
        badge = badge,
        modifier = modifier,
        content = {
            Spacer(Modifier.height(12.dp))
            DonkiStatRow(
                label = stringResource(R.string.donki_stat_peak_time_label),
                value = flare.eventTime.toUtcTimeText()
            )
            flare.activeRegionNum?.let {
                DonkiStatRow(
                    label = stringResource(R.string.donki_stat_active_region_label),
                    value = stringResource(R.string.donki_active_region_value, it),
                    info = DonkiInfo(
                        title = stringResource(R.string.donki_info_active_region_title),
                        body = stringResource(R.string.donki_info_active_region_body)
                    )
                )
            }
        }
    )
}