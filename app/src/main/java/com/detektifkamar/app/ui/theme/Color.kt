package com.detektifkamar.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.detektifkamar.app.domain.model.MagneticStatus

@Immutable
data class DetectiveColors(
    val paperBackground: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val borderOutline: Color,
    val inkText: Color,
    val textMuted: Color,
    val accentCyan: Color,
    val warningAmber: Color,
    val dangerRed: Color,
    val normalGreen: Color,
    val gridLine: Color,
    val isDark: Boolean
) {
    /** Mendapatkan warna status untuk kategori medan magnet */
    fun colorForStatus(status: MagneticStatus): Color {
        return when (status) {
            MagneticStatus.NORMAL -> accentCyan
            MagneticStatus.SEDANG -> normalGreen
            MagneticStatus.TINGGI -> warningAmber
            MagneticStatus.SANGAT_TINGGI -> dangerRed
            MagneticStatus.SATURASI -> Color(0xFFFF2E4C)
        }
    }
}

// Palet Gelap "Instrumen Lapangan Detektif"
val DarkDetectiveColors = DetectiveColors(
    paperBackground = Color(0xFF0E1116), // Gelap arang kebiruan
    surface = Color(0xFF161B22),         // Permukaan kartu instrumen
    surfaceSecondary = Color(0xFF1C222B),// Kartu sekunder
    borderOutline = Color(0xFF242C36),   // Garis tipis 1dp
    inkText = Color(0xFFE6EDF3),         // Teks utama kontras tinggi
    textMuted = Color(0xFF8B98A5),       // Teks sekunder
    accentCyan = Color(0xFF3BC9F2),      // Aksen dingin / medan normal (Cyan)
    warningAmber = Color(0xFFF5A524),    // Peringatan tinggi (Amber)
    dangerRed = Color(0xFFFF5A5F),       // Bahaya sangat tinggi / saturasi (Crimson)
    normalGreen = Color(0xFF2DD4BF),     // Aksen hijau toska
    gridLine = Color(0xFF1F252E),        // Kisi instrumen halus
    isDark = true
)

// Palet Terang (Abu hangat & presisi instrumen)
val LightDetectiveColors = DetectiveColors(
    paperBackground = Color(0xFFF4F6F8), // Abu hangat instrumen
    surface = Color(0xFFFFFFFF),         // Putih bersih
    surfaceSecondary = Color(0xFFEAEFF4),// Abu terang
    borderOutline = Color(0xFFD3DBE4),   // Garis batas tipis 1dp
    inkText = Color(0xFF0E1116),         // Teks utama arang pekat
    textMuted = Color(0xFF57606A),       // Teks sekunder
    accentCyan = Color(0xFF0284C7),      // Cyan lebih gelap untuk kontras terang
    warningAmber = Color(0xFFD97706),    // Amber hangat
    dangerRed = Color(0xFFDC2626),       // Merah tegas
    normalGreen = Color(0xFF0D9488),     // Toska gelap
    gridLine = Color(0xFFE2E8F0),        // Garis kisi millimeter
    isDark = false
)
