package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.TiltState
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Logika murni untuk mendeteksi sudut kemiringan ponsel dari vektor gravitasi akselerometer.
 * Dilengkapi dengan filter histeresis (Schmitt Trigger) agar indikator tidak berkedip (jitter)
 * saat ponsel berada di sekitar ambang batas kemiringan.
 */
object TiltDetector {

    /**
     * Menghitung sudut kemiringan ponsel dengan histeresis anti-jitter.
     *
     * @param ax Percepatan sumbu X (m/s²)
     * @param ay Percepatan sumbu Y (m/s²)
     * @param az Percepatan sumbu Z (m/s²)
     * @param wasTilted Status kemiringan pada siklus sebelumnya (untuk histeresis)
     */
    fun detectTilt(
        ax: Float,
        ay: Float,
        az: Float,
        wasTilted: Boolean = false
    ): TiltState {
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

        // Histeresis: Masuk miring jika > 48°, kembali datar hanya jika < 38°
        val isTilted = if (wasTilted) {
            tiltAngleDeg > TiltState.TILT_EXIT_DEGREES
        } else {
            tiltAngleDeg > TiltState.TILT_ENTER_DEGREES
        }

        return TiltState(
            pitch = pitch,
            roll = roll,
            tiltAngle = tiltAngleDeg,
            isTilted = isTilted
        )
    }
}
