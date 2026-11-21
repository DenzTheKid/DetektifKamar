package com.detektifkamar.app.domain.model

/**
 * Entitas domain untuk titik data pengukuran medan magnet di ruangan.
 *
 * @property id ID unik pengukuran
 * @property roomId ID ruangan tempat titik ini berada
 * @property timestamp Waktu pengukuran (epoch milliseconds)
 * @property x Komponen medan magnet sumbu X (µT)
 * @property y Komponen medan magnet sumbu Y (µT)
 * @property z Komponen medan magnet sumbu Z (µT)
 * @property magnitude Magnitudo total medan magnet: sqrt(x² + y² + z²) dalam µT
 * @property sensorAccuracy Nilai akurasi SensorManager saat pengukuran (0=Unreliable, 3=High)
 * @property biasX Offset bias sumbu X yang diterapkan saat kalibrasi
 * @property biasY Offset bias sumbu Y yang diterapkan saat kalibrasi
 * @property biasZ Offset bias sumbu Z yang diterapkan saat kalibrasi
 * @property lux Intensitas cahaya lingkungan saat pengukuran (opsional, jika sensor tersedia)
 * @property tiltAngle Sudut kemiringan ponsel dari posisi datar (derajat)
 * @property isTilted Apakah ponsel miring melebihi batas aman (mis. > 45°)
 * @property status Kategori intensitas medan magnet
 * @property notes Catatan pengguna atau identifikasi objek dekat titik ukur
 * @property pointX Koordinat X pada denah ruangan dalam meter (opsional)
 * @property pointY Koordinat Y pada denah ruangan dalam meter (opsional)
 */
data class Measurement(
    val id: Long = 0,
    val roomId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val sensorAccuracy: Int = 3,
    val biasX: Float = 0f,
    val biasY: Float = 0f,
    val biasZ: Float = 0f,
    val lux: Float? = null,
    val tiltAngle: Float = 0f,
    val isTilted: Boolean = false,
    val status: MagneticStatus = MagneticStatus.NORMAL,
    val notes: String = "",
    val pointX: Float? = null,
    val pointY: Float? = null
)
