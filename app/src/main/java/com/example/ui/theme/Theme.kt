package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberColorScheme = darkColorScheme(
    primary = CyberRed,
    onPrimary = Color.Black,
    primaryContainer = CyberRedDark,
    onPrimaryContainer = CyberRedGlow,
    secondary = CyberRedBright,
    onSecondary = Color.Black,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = CyberTextPrimary,
    tertiary = CyberWarning,
    onTertiary = Color.Black,
    background = CyberBackground,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderSubtle
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
