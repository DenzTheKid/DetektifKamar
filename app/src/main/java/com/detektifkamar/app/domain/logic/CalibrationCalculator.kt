package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.model.CalibrationQuality
import kotlin.math.sqrt

/**
 * Logika murni untuk menghitung offset kalibrasi hard-iron magnetometer
 * dari kumpulan sampel gerakan angka 8 (3D figure-8 motion).
 */
object CalibrationCalculator {

    data class Sample3D(val x: Float, val y: Float, val z: Float)

    /**
     * Menghitung offset bias hard-iron dan mengevaluasi kualitas kalibrasi.
     *
     * @param samples Daftar sampel 3D yang terkumpul selama sesi kalibrasi
     * @return [CalibrationData] berisi offset bias dan skor kualitas
     */
    fun calculate(samples: List<Sample3D>): CalibrationData {
        if (samples.size < 20) {
            return CalibrationData(
                biasX = 0f,
                biasY = 0f,
                biasZ = 0f,
                quality = CalibrationQuality.ULANG,
                sampleCount = samples.size,
                calibratedAt = System.currentTimeMillis()
            )
        }

        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = -Float.MAX_VALUE

        for (s in samples) {
            if (s.x < minX) minX = s.x
            if (s.x > maxX) maxX = s.x
            if (s.y < minY) minY = s.y
            if (s.y > maxY) maxY = s.y
            if (s.z < minZ) minZ = s.z
            if (s.z > maxZ) maxZ = s.z
        }

        val biasX = (minX + maxX) / 2.0f
        val biasY = (minY + maxY) / 2.0f
        val biasZ = (minZ + maxZ) / 2.0f

        val spanX = maxX - minX
        val spanY = maxY - minY
        val spanZ = maxZ - minZ

        // Evaluasi apakah rotasi cukup mencakup seluruh sumbu
        val minAcceptableSpan = 18.0f // Rentang minimal perubahan fluks saat orientasi dibolak-balik
        val optimalSpan = 30.0f

        val quality = when {
            spanX < minAcceptableSpan || spanY < minAcceptableSpan || spanZ < minAcceptableSpan -> {
                CalibrationQuality.ULANG
            }
            spanX >= optimalSpan && spanY >= optimalSpan && spanZ >= optimalSpan -> {
                // Hitung variasi radius bola untuk memastikan konsistensi
                val radii = samples.map { s ->
                    val dx = s.x - biasX
                    val dy = s.y - biasY
                    val dz = s.z - biasZ
                    sqrt(dx * dx + dy * dy + dz * dz)
                }
                val meanR = radii.average()
                if (meanR > 5.0) {
                    val variance = radii.map { (it - meanR) * (it - meanR) }.average()
                    val stdDev = sqrt(variance)
                    val coefficientOfVariation = stdDev / meanR
                    if (coefficientOfVariation < 0.28) {
                        CalibrationQuality.BAIK
                    } else {
                        CalibrationQuality.CUKUP
                    }
                } else {
                    CalibrationQuality.CUKUP
                }
            }
            else -> CalibrationQuality.CUKUP
        }

        return CalibrationData(
            biasX = biasX,
            biasY = biasY,
            biasZ = biasZ,
            quality = quality,
            sampleCount = samples.size,
            calibratedAt = System.currentTimeMillis()
        )
    }
}
