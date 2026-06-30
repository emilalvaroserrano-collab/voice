package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AuraAccentViolet,
    secondary = AuraAccentPink,
    tertiary = AuraAccentCyan,
    background = AuraBackground,
    surface = AuraCardBackground,
    onPrimary = AuraTextPrimary,
    onSecondary = AuraTextPrimary,
    onTertiary = AuraBackground,
    onBackground = AuraTextPrimary,
    onSurface = AuraTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme to match the image concept
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve visual branding
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
