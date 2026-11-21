package com.detektifkamar.app.data.sensor

import kotlinx.coroutines.flow.Flow

data class RawMagneticData(
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val accuracy: Int,
    val timestamp: Long
)

data class RawAccelerometerData(
    val x: Float,
    val y: Float,
    val z: Float,
    val magnitude: Float,
    val tiltDeg: Float,
    val timestamp: Long
)

enum class HotColdTrend(val label: String) {
    MAKIN_PANAS("Intensitas Meningkat"),
    MAKIN_DINGIN("Intensitas Menurun"),
    STABIL("Medan Stabil")
}

data class LiveSensorState(
    val magneticUt: Float = 45.0f,
    val bx: Float = 0f,
    val by: Float = 0f,
    val bz: Float = 45.0f,
    val accuracy: Int = 3,
    val isCalibrated: Boolean = true,
    val isStable: Boolean = true,
    val unstableRatio: Float = 0f,
    val tiltDeg: Float = 0f,
    val azimuthDeg: Float? = 0f,
    val lux: Float? = null,
    val hotColdTrend: HotColdTrend = HotColdTrend.STABIL,
    val isLightSensorAvailable: Boolean = true,
    val isSimulation: Boolean = false
)

interface SensorSource {
    val isLightSensorAvailable: Boolean
    fun getMagneticFieldFlow(): Flow<RawMagneticData>
    fun getAccelerometerFlow(): Flow<RawAccelerometerData>
    fun getLightFlow(): Flow<Float?>
}
