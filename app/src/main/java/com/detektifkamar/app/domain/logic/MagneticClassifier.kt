package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.MagneticStatus

/**
 * Logika tunggal terpusat untuk mengklasifikasikan intensitas medan magnet.
 */
object MagneticClassifier {

    /**
     * Mengklasifikasikan magnitudo medan magnet ke status enum.
     *
     * @param magnitudeUt Magnitudo medan magnet dalam mikrotesla (µT)
     * @param sensorMaxRange Batas maksimum pembacaan sensor ponsel dari sensor.maximumRange
     */
    fun classify(
        magnitudeUt: Float,
        sensorMaxRange: Float = 2000f
    ): MagneticStatus {
        // Jika pembacaan mendekati atau melampaui batas saturasi hardware (>= 98% maxRange)
        if (sensorMaxRange > 0f && magnitudeUt >= (sensorMaxRange * 0.98f)) {
            return MagneticStatus.SATURASI
        }

        return when {
            magnitudeUt < MagneticStatus.NORMAL.maxUt -> MagneticStatus.NORMAL
            magnitudeUt < MagneticStatus.SEDANG.maxUt -> MagneticStatus.SEDANG
            magnitudeUt < MagneticStatus.TINGGI.maxUt -> MagneticStatus.TINGGI
            else -> MagneticStatus.SANGAT_TINGGI
        }
    }
}
