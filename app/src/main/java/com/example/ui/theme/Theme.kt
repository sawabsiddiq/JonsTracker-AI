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

private val DarkColorScheme = LightColorScheme // Force the premium warm paper LightColorScheme across both states

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to false (Light Mode) for the literary Claude aesthetic
    dynamicColor: Boolean = false, // Handcrafted palette prevents generic style washouts
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
