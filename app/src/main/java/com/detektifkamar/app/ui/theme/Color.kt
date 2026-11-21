package com.detektifkamar.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class DetectiveColors(
    val paperBackground: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val inkText: Color,
    val textMuted: Color,
    val cyanotypeBlue: Color,
    val stampRed: Color,
    val highlighterYellow: Color,
    val labGreen: Color,
    val gridLine: Color,
    val borderOutline: Color,
    val isDark: Boolean
)

// Palet Modern Terang (Precision Studio Slate)
val LightDetectiveColors = DetectiveColors(
    paperBackground = Color(0xFFF8FAFC), // Slate 50
    surface = Color(0xFFFFFFFF),         // Crisp White
    surfaceSecondary = Color(0xFFF1F5F9),// Slate 100
    inkText = Color(0xFF0F172A),         // Slate 900
    textMuted = Color(0xFF64748B),       // Slate 500
    cyanotypeBlue = Color(0xFF2563EB),   // Precision Blue 600
    stampRed = Color(0xFFEF4444),        // Modern Coral Red
    highlighterYellow = Color(0xFFF59E0B), // Warm Amber
    labGreen = Color(0xFF10B981),        // Emerald Green
    gridLine = Color(0xFFE2E8F0),        // Subtle Hairline Grid
    borderOutline = Color(0xFFCBD5E1),   // Slate 300
    isDark = false
)

// Palet Modern Gelap (Obsidian Studio & Dark Instrument)
val DarkDetectiveColors = DetectiveColors(
    paperBackground = Color(0xFF0B0F17), // Deep Obsidian
    surface = Color(0xFF151C28),         // Slate Surface
    surfaceSecondary = Color(0xFF1E283A),// Accent Surface
    inkText = Color(0xFFF8FAFC),         // Titanium White
    textMuted = Color(0xFF94A3B8),       // Slate 400
    cyanotypeBlue = Color(0xFF38BDF8),   // Electric Sky Cyan
    stampRed = Color(0xFFF87171),        // Vivid Red
    highlighterYellow = Color(0xFFFBBF24), // Vibrant Amber
    labGreen = Color(0xFF34D399),        // Mint Emerald
    gridLine = Color(0xFF1E293B),        // Dark Grid
    borderOutline = Color(0xFF334155),   // Slate 700
    isDark = true
)

// Skala Warna Peta Panas Berbasis Ambang Kategori Modern
object HeatmapColors {
    val Tenang = Color(0x3310B981)        // Emerald transparan
    val GangguanRingan = Color(0x66F59E0B) // Amber transparan
    val GangguanKuat = Color(0x99F97316)   // Orange bata
    val SumberDekat = Color(0xCCEF4444)    // Vivid Coral Red

    val LightGradientLow = Color(0x220284C7)
    val LightGradientHigh = Color(0xBBF59E0B)
}
