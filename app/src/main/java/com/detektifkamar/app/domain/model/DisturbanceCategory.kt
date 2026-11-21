package com.detektifkamar.app.domain.model

/**
 * Empat kategori tingkat fluktuasi intensitas medan magnet terhadap nilai acuan lingkungan (baseline).
 */
enum class DisturbanceCategory(
    val label: String,
    val minDeltaUt: Float,
    val maxDeltaUt: Float,
    val microcopy: String,
    val codeSymbol: String
) {
    TENANG(
        label = "Normal / Aman",
        minDeltaUt = 0.0f,
        maxDeltaUt = 5.0f,
        microcopy = "Medan magnet normal dan stabil. Tidak ada anomali terdeteksi.",
        codeSymbol = "●"
    ),
    GANGGUAN_RINGAN(
        label = "Medan Rendah",
        minDeltaUt = 5.0f,
        maxDeltaUt = 15.0f,
        microcopy = "Terdeteksi sedikit fluktuasi medan magnet di sekitar titik ini.",
        codeSymbol = "▲"
    ),
    GANGGUAN_KUAT(
        label = "Medan Tinggi",
        minDeltaUt = 15.0f,
        maxDeltaUt = 50.0f,
        microcopy = "Fluktuasi medan cukup signifikan. Terdapat kabel atau alat elektronik aktif di dekat sini.",
        codeSymbol = "◆"
    ),
    SUMBER_DEKAT(
        label = "Medan Sangat Kuat",
        minDeltaUt = 50.0f,
        maxDeltaUt = Float.MAX_VALUE,
        microcopy = "Intensitas medan sangat tinggi. Berada tepat di samping sumber elektromagnetik aktif.",
        codeSymbol = "✦"
    );

    companion object {
        fun fromDelta(deltaUt: Float): DisturbanceCategory {
            val nonNegativeDelta = kotlin.math.abs(deltaUt)
            return when {
                nonNegativeDelta < 5.0f -> TENANG
                nonNegativeDelta < 15.0f -> GANGGUAN_RINGAN
                nonNegativeDelta < 50.0f -> GANGGUAN_KUAT
                else -> SUMBER_DEKAT
            }
        }
    }
}
