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
    primary = SlcDarkPrimary,
    onPrimary = SlcDarkOnPrimary,
    primaryContainer = SlcDarkPrimaryContainer,
    onPrimaryContainer = SlcDarkOnPrimaryContainer,
    secondary = SlcDarkSecondary,
    onSecondary = SlcDarkOnSecondary,
    secondaryContainer = SlcDarkSecondaryContainer,
    onSecondaryContainer = SlcDarkOnSecondaryContainer,
    background = SlcDarkBackground,
    surface = SlcDarkSurface,
    surfaceVariant = SlcDarkSurfaceVariant,
    onBackground = SlcDarkOnSurface,
    onSurface = SlcDarkOnSurface,
    onSurfaceVariant = SlcDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = SlcPrimary,
    onPrimary = SlcOnPrimary,
    primaryContainer = SlcPrimaryContainer,
    onPrimaryContainer = SlcOnPrimaryContainer,
    secondary = SlcSecondary,
    onSecondary = SlcOnSecondary,
    secondaryContainer = SlcSecondaryContainer,
    onSecondaryContainer = SlcOnSecondaryContainer,
    tertiary = SlcTertiary,
    onTertiary = SlcOnTertiary,
    tertiaryContainer = SlcTertiaryContainer,
    onTertiaryContainer = SlcOnTertiaryContainer,
    background = SlcBackground,
    surface = SlcSurface,
    surfaceVariant = SlcSurfaceVariant,
    onBackground = SlcOnSurface,
    onSurface = SlcOnSurface,
    onSurfaceVariant = SlcOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our branded collegiate palette by default
    content: @Composable () -> Unit
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
