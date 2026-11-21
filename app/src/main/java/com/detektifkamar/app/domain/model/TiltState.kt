package com.detektifkamar.app.domain.model

/**
 * State orientasi kemiringan ponsel dari akselerometer.
 *
 * @property pitch Sudut angguk (rotasi sumbu X dalam derajat)
 * @property roll Sudut guling (rotasi sumbu Y dalam derajat)
 * @property tiltAngle Sudut deviasi total ponsel dari posisi mendatar (derajat, 0° = mendatar sempurna)
 * @property isTilted Apakah ponsel miring melebihi batas toleransi pengukuran datar (> 45°)
 */
data class TiltState(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val tiltAngle: Float = 0f,
    val isTilted: Boolean = false
) {
    companion object {
        const val MAX_FLAT_ANGLE_DEGREES = 45.0f
    }
}
