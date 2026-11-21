package com.detektifkamar.app.domain.model

/**
 * Status kualitas hasil kalibrasi magnetometer.
 */
enum class CalibrationQuality(val label: String, val description: String) {
    BAIK("Baik", "Sensor terkalibrasi optimal dengan cakupan rotasi merata."),
    CUKUP("Cukup", "Kalibrasi dapat digunakan, namun variasi rotasi masih terbatas."),
    ULANG("Ulangi", "Cakupan rotasi kurang luas. Gerakkan ponsel membentuk angka 8 penuh.");
}

/**
 * Data hasil kalibrasi magnetometer ponsel.
 *
 * @property biasX Offset hard-iron sumbu X (µT)
 * @property biasY Offset hard-iron sumbu Y (µT)
 * @property biasZ Offset hard-iron sumbu Z (µT)
 * @property quality Kualitas hasil perhitungan kalibrasi
 * @property sampleCount Jumlah sampel data selama proses kalibrasi 15 detik
 * @property calibratedAt Waktu kalibrasi disimpan (epoch milliseconds)
 */
data class CalibrationData(
    val biasX: Float = 0f,
    val biasY: Float = 0f,
    val biasZ: Float = 0f,
    val quality: CalibrationQuality = CalibrationQuality.CUKUP,
    val sampleCount: Int = 0,
    val calibratedAt: Long = 0L
) {
    val isCalibrated: Boolean
        get() = calibratedAt > 0L
}
