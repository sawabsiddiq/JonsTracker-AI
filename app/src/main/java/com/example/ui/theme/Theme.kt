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

import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimary,
    secondary = SleekSecondary,
    tertiary = SleekTertiary,
    background = SleekBg,
    surface = SleekSurface,
    onBackground = SleekOnBg,
    onSurface = SleekOnBg,
    outline = SleekSubtext,
    outlineVariant = SleekBorder,
    surfaceVariant = SleekNavBarBg
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFD97745),          // Accent
    secondary = Color(0xFFB8AAA0),        // Text Secondary
    tertiary = Color(0xFF2B2521),         // Elevated Surface
    background = Color(0xFF171412),       // Background
    surface = Color(0xFF221E1B),          // Surface
    onBackground = Color(0xFFF6EFE8),     // Text Primary
    onSurface = Color(0xFFF6EFE8),        // Text Primary
    outline = Color(0xFFB8AAA0),          // Text Secondary
    outlineVariant = Color(0xFF3A332E),   // Border
    surfaceVariant = Color(0xFF2B2521)    // Elevated Surface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
