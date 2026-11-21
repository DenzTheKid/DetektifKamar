package com.detektifkamar.app.data.sensor

import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.TiltState
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

/**
 * Data state terintegrasi dari seluruh sensor fisik yang diproses real-time.
 */
data class LiveSensorState(
    val isMagnetometerAvailable: Boolean = true,
    val sensorMaxRangeUt: Float = 2000f,
    val magneticUt: Float = 45.0f,
    val bx: Float = 0f,
    val by: Float = 0f,
    val bz: Float = 45.0f,
    val rawBx: Float = 0f,
    val rawBy: Float = 0f,
    val rawBz: Float = 45.0f,
    val accuracy: Int = 3,
    val isCalibrated: Boolean = false,
    val appliedBias: CalibrationData = CalibrationData(),
    val isStable: Boolean = true,
    val tiltState: TiltState = TiltState(),
    val tiltDeg: Float = 0f,
    val azimuthDeg: Float? = 0f,
    val lux: Float? = null,
    val status: MagneticStatus = MagneticStatus.NORMAL,
    val hotColdTrend: HotColdTrend = HotColdTrend.STABIL,
    val isLightSensorAvailable: Boolean = true,
    val isSimulation: Boolean = false
)

interface SensorSource {
    val isMagnetometerAvailable: Boolean
    val sensorMaxRangeUt: Float
    val isLightSensorAvailable: Boolean
    fun getMagneticFieldFlow(): Flow<RawMagneticData>
    fun getAccelerometerFlow(): Flow<RawAccelerometerData>
    fun getLightFlow(): Flow<Float?>
}
