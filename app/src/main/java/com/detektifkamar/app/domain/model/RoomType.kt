package com.detektifkamar.app.domain.model

/**
 * Klasifikasi tipe ruangan yang dipetakan.
 */
enum class RoomType(val displayName: String) {
    KAMAR_TIDUR("Kamar Tidur"),
    RUANG_KERJA("Ruang Kerja"),
    DAPUR("Dapur"),
    RUANG_TAMU("Ruang Tamu"),
    LAINNYA("Lainnya");

    companion object {
        fun fromDisplayName(name: String): RoomType {
            return entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: LAINNYA
        }
    }
}
