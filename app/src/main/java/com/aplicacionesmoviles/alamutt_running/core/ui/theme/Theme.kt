package com.aplicacionesmoviles.alamutt_running.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider

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
        content = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                content()
            }
        }
    )
}