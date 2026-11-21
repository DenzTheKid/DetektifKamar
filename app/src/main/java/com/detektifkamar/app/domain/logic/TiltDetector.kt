package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.TiltState
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Logika murni untuk mendeteksi sudut kemiringan ponsel dari vektor gravitasi akselerometer.
 */
object TiltDetector {

    /**
     * Menghitung sudut kemiringan ponsel (pitch, roll, dan sudut deviasi total dari posisi datar).
     *
     * @param ax Percepatan sumbu X (m/s²)
     * @param ay Percepatan sumbu Y (m/s²)
     * @param az Percepatan sumbu Z (m/s²)
     */
    fun detectTilt(ax: Float, ay: Float, az: Float): TiltState {
        val norm = sqrt(ax * ax + ay * ay + az * az)
        if (norm < 0.1f) {
            return TiltState()
        }

        // Posisi mendatar sempurna adalah saat gravitasi terkonsentrasi di sumbu Z (|az| = norm)
        val zRatio = (abs(az) / norm).coerceIn(0.0f, 1.0f)
        val tiltAngleRad = acos(zRatio)
        val tiltAngleDeg = Math.toDegrees(tiltAngleRad.toDouble()).toFloat()

        // Pitch & Roll dalam derajat
        val pitch = Math.toDegrees(atan2(-ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))).toFloat()
        val roll = Math.toDegrees(atan2(ay.toDouble(), az.toDouble())).toFloat()

        val isTilted = tiltAngleDeg > TiltState.MAX_FLAT_ANGLE_DEGREES

        return TiltState(
            pitch = pitch,
            roll = roll,
            tiltAngle = tiltAngleDeg,
            isTilted = isTilted
        )
    }
}
