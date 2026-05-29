package com.aplicacionesmoviles.alamutt_running.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

private val AlamuttColorScheme = darkColorScheme(
    primary = AccentRed,
    background = DarkBackground,
    surface = DarkerHeader,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun AlamuttRunningTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlamuttColorScheme,
        typography = Typography,
        content = content
    )
}