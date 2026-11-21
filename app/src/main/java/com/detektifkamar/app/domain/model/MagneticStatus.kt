package com.detektifkamar.app.domain.model

/**
 * Status intensitas medan magnet terpusat.
 * Berfungsi sebagai klasifikasi batas ambang tunggal untuk seluruh aplikasi.
 *
 * Rentang mengacu pada pedoman keselamatan EMF dan fluks geomagnetik bumi:
 * - Medan magnet bumi normal di permukaan berkisar 25–65 µT.
 * - [NORMAL]: Di bawah 65 µT (Latar belakang bumi normal tanpa gangguan buatan signifikan).
 * - [SEDANG]: 65–150 µT (Peningkatan ringan dekat kabel atau perangkat daya rendah).
 * - [TINGGI]: 150–500 µT (Dekat transformator, adaptor daya, blower AC, atau motor).
 * - [SANGAT_TINGGI]: > 500 µT hingga di bawah batas saturasi (Dekat speaker besar, kompor induksi).
 * - [SATURASI]: Mencapai atau melebihi batas jangkauan maksimum sensor ponsel (sensor.maximumRange).
 */
enum class MagneticStatus(
    val label: String,
    val description: String,
    val minUt: Float,
    val maxUt: Float
) {
    NORMAL(
        label = "NORMAL",
        description = "Latar belakang geomagnetik bumi alami (aman)",
        minUt = 0f,
        maxUt = 65f
    ),
    SEDANG(
        label = "SEDANG",
        description = "Fluks magnet ringan terdeteksi di sekitar perangkat",
        minUt = 65f,
        maxUt = 150f
    ),
    TINGGI(
        label = "TINGGI",
        description = "Medan magnet cukup kuat dekat adaptor/motor/kabel",
        minUt = 150f,
        maxUt = 500f
    ),
    SANGAT_TINGGI(
        label = "SANGAT TINGGI",
        description = "Medan sangat kuat dekat speaker magnetik atau induksi",
        minUt = 500f,
        maxUt = Float.MAX_VALUE
    ),
    SATURASI(
        label = "SATURASI",
        description = "Di luar jangkauan pengukuran sensor ponsel",
        minUt = Float.MAX_VALUE,
        maxUt = Float.MAX_VALUE
    );

    companion object {
        /** Batas bawah medan magnet bumi alami */
        const val EARTH_FIELD_MIN_UT = 25f

        /** Batas atas medan magnet bumi alami */
        const val EARTH_FIELD_MAX_UT = 65f
    }
}
