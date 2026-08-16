package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = VivoGold,
    onPrimary = Color.Black,
    primaryContainer = VivoGoldDark,
    onPrimaryContainer = Color.White,
    secondary = AccentCyan,
    onSecondary = Color.Black,
    secondaryContainer = CameraSurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = VivoGoldLight,
    onTertiary = Color.Black,
    background = CameraBackground,
    onBackground = TextPrimary,
    surface = CameraSurface,
    onSurface = TextPrimary,
    surfaceVariant = CameraSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = CameraBorder,
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Camera app is always in professional dark mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
