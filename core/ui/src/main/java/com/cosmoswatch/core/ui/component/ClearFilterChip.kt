package com.cosmoswatch.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosmoswatch.core.ui.theme.OnAccentPrimary

@Composable
fun ClearFilterChip(contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(32.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.onSurface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.Clear,
                contentDescription = contentDescription,
                tint = OnAccentPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}