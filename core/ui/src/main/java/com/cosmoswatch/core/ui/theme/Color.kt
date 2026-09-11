package com.cosmoswatch.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

val AccentPrimary = Color(0xFFE3A458)
val OnAccentPrimary = Color(0xFF14110A)

private val ErrorColor = Color(0xFFFFB4AB)
private val OnErrorColor = Color(0xFF690005)
private val ErrorContainerColor = Color(0xFF93000A)
private val OnErrorContainerColor = Color(0xFFFFDAD6)

private fun editorialDarkColorScheme(
    background: Color,
    surface: Color,
    surfaceVariant: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
): ColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = OnAccentPrimary,
    primaryContainer = lerp(background, AccentPrimary, 0.24f),
    onPrimaryContainer = lerp(AccentPrimary, Color.White, 0.25f),
    secondary = onSurfaceVariant,
    onSecondary = background,
    secondaryContainer = lerp(surface, surfaceVariant, 0.6f),
    onSecondaryContainer = onSurface,
    tertiary = lerp(AccentPrimary, onSurfaceVariant, 0.5f),
    onTertiary = background,
    tertiaryContainer = lerp(surfaceVariant, AccentPrimary, 0.15f),
    onTertiaryContainer = onSurface,
    error = ErrorColor,
    onError = OnErrorColor,
    errorContainer = ErrorContainerColor,
    onErrorContainer = OnErrorContainerColor,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceVariant = onSurfaceVariant,
    outline = lerp(surfaceVariant, onSurfaceVariant, 0.5f),
    outlineVariant = lerp(surface, surfaceVariant, 0.5f),
    scrim = Color.Black,
    inverseSurface = onSurface,
    inverseOnSurface = background,
    inversePrimary = lerp(AccentPrimary, Color.Black, 0.35f),
    surfaceTint = AccentPrimary,
    surfaceDim = background,
    surfaceBright = lerp(surface, surfaceVariant, 0.7f),
    surfaceContainerLowest = lerp(background, Color.Black, 0.2f),
    surfaceContainerLow = lerp(background, surface, 0.5f),
    surfaceContainer = surface,
    surfaceContainerHigh = lerp(surface, surfaceVariant, 0.5f),
    surfaceContainerHighest = surfaceVariant,
)

val DeepSpaceColorScheme = editorialDarkColorScheme(
    background = Color(0xFF12142A),
    surface = Color(0xFF191C38),
    surfaceVariant = Color(0xFF232852),
    onSurface = Color(0xFFF3EEE2),
    onSurfaceVariant = Color(0xFFA5A8C2),
)

val VoidColorScheme = editorialDarkColorScheme(
    background = Color(0xFF05060A),
    surface = Color(0xFF0C0E17),
    surfaceVariant = Color(0xFF131521),
    onSurface = Color(0xFFEDE9E0),
    onSurfaceVariant = Color(0xFF9697A2),
)
