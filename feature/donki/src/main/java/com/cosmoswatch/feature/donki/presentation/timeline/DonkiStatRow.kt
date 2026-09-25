package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cosmoswatch.feature.donki.R
import com.cosmoswatch.core.ui.component.InfoDialogIcon

@Composable
fun DonkiStatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    info: DonkiInfo? = null
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
        if (info != null) {
            InfoDialogIcon(
                title = info.title,
                body = info.body,
                closeLabel = stringResource(R.string.donki_info_dialog_close)
            )
        }
    }
}