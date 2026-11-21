package com.detektifkamar.app.domain.logic

import kotlin.math.sqrt

/**
 * Utilitas kalkulasi statistik matematika murni.
 */
object StatisticsUtils {
    fun calculateMean(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        return values.sum() / values.size
    }

    fun calculateMin(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        return values.minOrNull() ?: 0f
    }

    fun calculateMax(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        return values.maxOrNull() ?: 0f
    }

    fun calculateStdDev(values: List<Float>, mean: Float = calculateMean(values)): Float {
        if (values.size < 2) return 0f
        var sumSquares = 0.0
        for (v in values) {
            val diff = v - mean
            sumSquares += diff * diff
        }
        return sqrt(sumSquares / (values.size - 1)).toFloat()
    }
}
