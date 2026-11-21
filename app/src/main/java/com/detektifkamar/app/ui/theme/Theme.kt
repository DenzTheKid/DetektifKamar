package com.detektifkamar.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalDetectiveColors = staticCompositionLocalOf { DarkDetectiveColors }

private val MaterialDarkColors = darkColorScheme(
    primary = DarkDetectiveColors.accentCyan,
    surface = DarkDetectiveColors.surface,
    background = DarkDetectiveColors.paperBackground,
    onPrimary = DarkDetectiveColors.paperBackground,
    onSurface = DarkDetectiveColors.inkText,
    onBackground = DarkDetectiveColors.inkText,
    outline = DarkDetectiveColors.borderOutline
)

private val MaterialLightColors = lightColorScheme(
    primary = LightDetectiveColors.accentCyan,
    surface = LightDetectiveColors.surface,
    background = LightDetectiveColors.paperBackground,
    onPrimary = LightDetectiveColors.paperBackground,
    onSurface = LightDetectiveColors.inkText,
    onBackground = LightDetectiveColors.inkText,
    outline = LightDetectiveColors.borderOutline
)

object DetectiveTheme {
    val colors: DetectiveColors
        @Composable
        @ReadOnlyComposable
        get() = LocalDetectiveColors.current

    val typography = DetectiveTypography
    val shapes = DetectiveShapes
}

@Composable
fun DetektifKamarTheme(
    themeMode: Int = 0, // 0: Sistem, 1: Terang, 2: Gelap
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    val colors = if (isDark) DarkDetectiveColors else LightDetectiveColors
    val materialColors = if (isDark) MaterialDarkColors else MaterialLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colors.paperBackground.toArgb()
                window.navigationBarColor = colors.paperBackground.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalDetectiveColors provides colors) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = DetectiveTypography,
            shapes = DetectiveShapes,
            content = content
        )
    }
}
