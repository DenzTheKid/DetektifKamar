package com.detektifkamar.app.domain.logic

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utilitas trigonometri dan rata-rata sirkular untuk arah azimuth kompas.
 */
object AzimuthUtils {

    /**
     * Rata-rata sirkular menggunakan vektor trigonometri (atan2 dari mean sin dan cos).
     * Menghindari kekeliruan rata-rata aritmetika biasa (mis. 359° dan 1° menjadi 180°).
     * Output selalu dalam rentang [0, 360) derajat.
     */
    fun circularMean(anglesDeg: List<Float>): Float {
        if (anglesDeg.isEmpty()) return 0f
        var sumSin = 0.0
        var sumCos = 0.0
        for (deg in anglesDeg) {
            val rad = Math.toRadians(deg.toDouble())
            sumSin += sin(rad)
            sumCos += cos(rad)
        }
        val meanRad = atan2(sumSin / anglesDeg.size, sumCos / anglesDeg.size)
        var meanDeg = Math.toDegrees(meanRad).toFloat()
        if (meanDeg < 0f) {
            meanDeg += 360f
        }
        return meanDeg % 360f
    }

    /**
     * Menghitung selisih sudut terpendek dari sudut 'fromDeg' ke 'toDeg'
     * dalam rentang [-180, 180] derajat agar jarum tidak berputar balik 360 derajat.
     */
    fun shortestAngleDiff(fromDeg: Float, toDeg: Float): Float {
        var diff = (toDeg - fromDeg) % 360f
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f
        return diff
    }

    /**
     * Azimuth hanya valid secara fisis jika kemiringan (tilt) HP terhadap bidang horizontal < 45 derajat.
     */
    fun isAzimuthValid(tiltDeg: Float): Boolean {
        return tiltDeg < 45f
    }
}
