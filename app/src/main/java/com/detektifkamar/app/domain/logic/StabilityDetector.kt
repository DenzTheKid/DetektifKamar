package com.detektifkamar.app.domain.logic

/**
 * Detektor kestabilan HP berdasarkan simpangan baku magnitudo percepatan akselerometer.
 */
object StabilityDetector {

    const val STABILITY_THRESHOLD_STD_DEV = 0.25f // m/s^2
    const val UNSTABLE_RATIO_REJECT_THRESHOLD = 0.30f // 30%

    /**
     * Memeriksa apakah sampel percepatan pada jendela waktu stabil (stdDev < 0.25 m/s²).
     */
    fun isStable(accelerationMagnitudes: List<Float>): Boolean {
        if (accelerationMagnitudes.size < 3) return false
        val stdDev = StatisticsUtils.calculateStdDev(accelerationMagnitudes)
        return stdDev < STABILITY_THRESHOLD_STD_DEV
    }

    /**
     * Menghitung rasio sampel tidak stabil selama sesi pengukuran.
     */
    fun calculateUnstableRatio(windowResults: List<Boolean>): Float {
        if (windowResults.isEmpty()) return 1.0f
        val unstableCount = windowResults.count { !it }
        return unstableCount.toFloat() / windowResults.size
    }
}
