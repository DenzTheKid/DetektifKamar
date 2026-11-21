package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.DisturbanceCategory

/**
 * Pengklasifikasi tingkat gangguan medan magnet murni tanpa dependensi Android.
 * Ambang batas:
 * - Tenang: delta < 5.0 µT
 * - Gangguan Ringan: 5.0 <= delta < 15.0 µT
 * - Gangguan Kuat: 15.0 <= delta < 50.0 µT
 * - Sumber Dekat: delta >= 50.0 µT
 */
object DisturbanceClassifier {
    fun classify(deltaUt: Float): DisturbanceCategory {
        return DisturbanceCategory.fromDelta(deltaUt)
    }

    fun calculateDelta(meanUt: Float, baselineUt: Float): Float {
        return kotlin.math.abs(meanUt - baselineUt)
    }
}
