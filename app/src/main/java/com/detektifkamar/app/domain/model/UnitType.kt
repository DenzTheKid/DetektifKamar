package com.detektifkamar.app.domain.model

/**
 * Pilihan satuan medan magnet:
 * - [MICROTESLA]: µT (Satuan standar SI turunan untuk sensor magnetometer ponsel)
 * - [MILLITESLA]: mT (1 mT = 1.000 µT)
 * - [GAUSS]: G (1 G = 100 µT, 1 mT = 10 G)
 */
enum class UnitType(
    val symbol: String,
    val displayName: String,
    val decimalPlaces: Int
) {
    MICROTESLA(
        symbol = "µT",
        displayName = "Mikrotesla (µT)",
        decimalPlaces = 1
    ),
    MILLITESLA(
        symbol = "mT",
        displayName = "Militesla (mT)",
        decimalPlaces = 3
    ),
    GAUSS(
        symbol = "G",
        displayName = "Gauss (G)",
        decimalPlaces = 2
    );

    /**
     * Mengonversi nilai dari satuan basis mikrotesla (µT) ke satuan target ini.
     */
    fun convertFromMicrotesla(valueUt: Float): Float {
        return when (this) {
            MICROTESLA -> valueUt
            MILLITESLA -> valueUt / 1000f
            GAUSS -> valueUt / 100f
        }
    }

    /**
     * Mengonversi nilai dari satuan ini kembali ke mikrotesla (µT).
     */
    fun convertToMicrotesla(value: Float): Float {
        return when (this) {
            MICROTESLA -> value
            MILLITESLA -> value * 1000f
            GAUSS -> value * 100f
        }
    }
}
