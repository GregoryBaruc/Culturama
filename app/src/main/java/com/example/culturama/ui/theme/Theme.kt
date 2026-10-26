package com.example.culturama.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CulturamaColorScheme = darkColorScheme(
    primary          = CulturamaOrange,
    onPrimary        = Color.White,
    primaryContainer = CulturamaOrangeDeep,
    secondary        = TextSecondary,
    onSecondary      = Color.White,
    background       = CulturamaBackground,
    onBackground     = TextPrimary,
    surface          = CulturamaSurface,
    onSurface        = TextPrimary,
    surfaceVariant   = CulturamaCard,
    onSurfaceVariant = TextSecondary,
    error            = Color(0xFFCF6679),
    onError          = Color.White
)

@Composable
fun CulturamaTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CulturamaBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = CulturamaColorScheme,
        typography  = CulturamaTypography,
        content     = content
    )
}
