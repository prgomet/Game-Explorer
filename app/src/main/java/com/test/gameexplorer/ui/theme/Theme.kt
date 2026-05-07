package com.test.gameexplorer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    tertiary = NeonGreen,
    background = DarkBackground,
    surface = SurfaceDark,
    onPrimary = TextPrimary,
    onSecondary = DarkBackground,
    onTertiary = DarkBackground,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = TextPrimary
)

/**
 * Custom Material3 Theme for Game Explorer.
 * 
 * This theme implements a "Gamer" aesthetic using a dark-mode-only color scheme with
 * high-contrast neon accents (Purple, Blue, Green).
 */
@Composable
fun GameExplorerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
