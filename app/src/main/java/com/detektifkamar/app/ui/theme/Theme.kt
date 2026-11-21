package com.detektifkamar.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalDetectiveColors = staticCompositionLocalOf { LightDetectiveColors }

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

    val customColors = if (isDark) DarkDetectiveColors else LightDetectiveColors

    val materialColors = if (isDark) {
        darkColorScheme(
            primary = customColors.stampRed,
            onPrimary = customColors.paperBackground,
            secondary = customColors.cyanotypeBlue,
            onSecondary = customColors.paperBackground,
            background = customColors.paperBackground,
            onBackground = customColors.inkText,
            surface = customColors.surface,
            onSurface = customColors.inkText,
            outline = customColors.borderOutline
        )
    } else {
        lightColorScheme(
            primary = customColors.stampRed,
            onPrimary = customColors.paperBackground,
            secondary = customColors.cyanotypeBlue,
            onSecondary = customColors.paperBackground,
            background = customColors.paperBackground,
            onBackground = customColors.inkText,
            surface = customColors.surface,
            onSurface = customColors.inkText,
            outline = customColors.borderOutline
        )
    }

    CompositionLocalProvider(
        LocalDetectiveColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = DetectiveTypography,
            shapes = DetectiveShapes,
            content = content
        )
    }
}
