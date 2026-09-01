package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SolemColorScheme = darkColorScheme(
    primary = SolemPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = SolemPrimaryBlueDark,
    onPrimaryContainer = SolemPrimaryBlueLight,
    secondary = SolemAccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF0C4A6E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = SolemAccentEmerald,
    onTertiary = Color.Black,
    background = SolemBackground,
    onBackground = SolemTextPrimary,
    surface = SolemSurface,
    onSurface = SolemTextPrimary,
    surfaceVariant = SolemSurfaceVariant,
    onSurfaceVariant = SolemTextSecondary,
    outline = SolemBorder
)

@Composable
fun SolemTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SolemColorScheme,
        typography = SolemTypography,
        content = content
    )
}
