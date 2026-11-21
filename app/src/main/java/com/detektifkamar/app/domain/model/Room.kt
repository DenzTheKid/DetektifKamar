package com.detektifkamar.app.domain.model

/**
 * Entitas domain untuk ruangan tempat investigasi medan magnet dilakukan.
 *
 * @property id ID unik ruangan (primary key)
 * @property name Nama ruangan (wajib, maks 40 karakter)
 * @property roomType Kategori ruangan
 * @property widthMeters Lebar ruangan dalam meter (opsional)
 * @property lengthMeters Panjang ruangan dalam meter (opsional)
 * @property notes Catatan tambahan tentang ruangan
 * @property createdAt Waktu pembuatan ruangan (epoch milliseconds)
 * @property pointCount Jumlah titik pengukuran yang tersimpan di ruangan ini
 * @property latestAverageUt Rata-rata kekuatan medan magnet (µT) dari seluruh titik pengukuran
 * @property dominantStatus Status medan magnet dominan/tertinggi di ruangan ini
 */
data class Room(
    val id: Long = 0,
    val name: String,
    val roomType: RoomType,
    val widthMeters: Float? = null,
    val lengthMeters: Float? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val pointCount: Int = 0,
    val latestAverageUt: Float? = null,
    val dominantStatus: MagneticStatus = MagneticStatus.NORMAL
) {
    /** Menghitung luas ruangan jika lebar dan panjang tersedia */
    val areaSquareMeters: Float?
        get() = if (widthMeters != null && lengthMeters != null) widthMeters * lengthMeters else null

    /** Format dimensi ruangan yang ringkas (mis. "3.5 x 4.0 m") */
    val dimensionsLabel: String
        get() = if (widthMeters != null && lengthMeters != null) {
            String.format(java.util.Locale.US, "%.1f x %.1f m", widthMeters, lengthMeters)
        } else {
            "Dimensi bebas"
        }
}
