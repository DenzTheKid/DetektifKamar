package com.detektifkamar.app.domain.model

/**
 * State orientasi kemiringan ponsel dari akselerometer.
 *
 * @property pitch Sudut angguk (rotasi sumbu X dalam derajat)
 * @property roll Sudut guling (rotasi sumbu Y dalam derajat)
 * @property tiltAngle Sudut deviasi total ponsel dari posisi mendatar (derajat, 0° = mendatar sempurna)
 * @property isTilted Apakah ponsel miring melebihi batas toleransi pengukuran datar
 */
data class TiltState(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val tiltAngle: Float = 0f,
    val isTilted: Boolean = false
) {
    companion object {
        /** Ambang batas masuk status miring (Hysteresis upper bound) */
        const val TILT_ENTER_DEGREES = 48.0f

        /** Ambang batas kembali ke status datar (Hysteresis lower bound) */
        const val TILT_EXIT_DEGREES = 38.0f

        const val MAX_FLAT_ANGLE_DEGREES = 45.0f
    }
}
