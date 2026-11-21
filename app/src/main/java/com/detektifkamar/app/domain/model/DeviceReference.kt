package com.detektifkamar.app.domain.model

/**
 * Kategori perangkat atau objek sumber medan magnet di rumah.
 */
enum class DeviceCategory(val displayName: String) {
    ELEKTRONIK("Elektronik"),
    AUDIO("Audio"),
    DAPUR("Dapur"),
    KABEL_DAN_LISTRIK("Kabel dan Listrik"),
    STRUKTUR_BANGUNAN("Struktur Bangunan"),
    LAINNYA("Lainnya");

    companion object {
        fun fromDisplayName(name: String): DeviceCategory {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: LAINNYA
        }
    }
}

/**
 * Entitas katalog referensi perangkat sumber medan magnet.
 *
 * @property id ID unik perangkat
 * @property name Nama perangkat atau objek
 * @property category Kategori perangkat
 * @property minUt Nilai fluks magnetik minimum tipikal (µT)
 * @property maxUt Nilai fluks magnetik maksimum tipikal (µT)
 * @property measurementDistance Jarak standar saat pengukuran dilakukan
 * @property physicalExplanation Penjelasan fisika timbulnya medan magnet
 * @property safetyNote Catatan keamanan dan pengaruhnya terhadap perangkat
 * @property verificationSource Sumber rujukan literatur ilmiah yang dapat diverifikasi
 * @property isSaturatingSensor Apakah objek ini berpotensi memicu saturasi sensor ponsel
 */
data class DeviceReference(
    val id: Long,
    val name: String,
    val category: DeviceCategory,
    val minUt: Float,
    val maxUt: Float,
    val measurementDistance: String,
    val physicalExplanation: String,
    val safetyNote: String,
    val verificationSource: String,
    val isSaturatingSensor: Boolean = false
) {
    /** Format rentang teks (mis. "80 – 350 µT") */
    val rangeLabel: String
        get() = if (isSaturatingSensor) {
            String.format(java.util.Locale.US, "> %.0f µT (Saturasi)", minUt)
        } else {
            String.format(java.util.Locale.US, "%.0f – %.0f µT", minUt, maxUt)
        }
}
