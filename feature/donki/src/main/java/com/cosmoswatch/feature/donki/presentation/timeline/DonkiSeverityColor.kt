package com.cosmoswatch.feature.donki.presentation.timeline

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.cosmoswatch.core.ui.theme.WarningOrange
import com.cosmoswatch.feature.donki.domain.DonkiSeverity

@Composable
@ReadOnlyComposable
internal fun DonkiSeverity.accentColor(): Color = when (this) {
    DonkiSeverity.SEVERE -> MaterialTheme.colorScheme.error
    DonkiSeverity.MODERATE -> WarningOrange
    DonkiSeverity.NONE -> MaterialTheme.colorScheme.onSurface
}
