package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CamsDarkColorScheme = darkColorScheme(
    primary = AccentActiveState,
    onPrimary = NeutralBlack,
    secondary = NeutralTextSecondary,
    onSecondary = NeutralBlack,
    background = NeutralBlack,
    onBackground = NeutralTextPrimary,
    surface = NeutralDarkSurface,
    onSurface = NeutralTextPrimary,
    surfaceVariant = NeutralSurfaceVariant,
    onSurfaceVariant = NeutralTextSecondary,
    outline = NeutralBorder
)

@Composable
fun CamsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CamsDarkColorScheme,
        typography = Typography,
        content = content
    )
}
