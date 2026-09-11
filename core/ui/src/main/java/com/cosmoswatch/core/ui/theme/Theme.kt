package com.cosmoswatch.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CosmosWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) VoidColorScheme else DeepSpaceColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CosmosWatchTypography,
        shapes = CosmosWatchShapes,
        content = content,
    )
}
