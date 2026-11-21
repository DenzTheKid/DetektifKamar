package com.detektifkamar.app.data.sensor

import com.detektifkamar.app.domain.logic.CalibrationCalculator
import com.detektifkamar.app.domain.model.CalibrationData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class CalibrationSessionState(
    val progress: Float = 0f,
    val remainingSeconds: Int = 15,
    val sampleCount: Int = 0,
    val currentX: Float = 0f,
    val currentY: Float = 0f,
    val currentZ: Float = 0f,
    val isCompleted: Boolean = false,
    val result: CalibrationData? = null
)

/**
 * Pengelola sesi kalibrasi magnetometer ponsel selama 15 detik.
 */
class CalibrationEngine(
    private val sensorSource: SensorSource
) {
    companion object {
        const val SESSION_DURATION_MS = 15_000L
        const val UPDATE_INTERVAL_MS = 100L
    }

    /**
     * Memulai sesi kalibrasi berdurasi 15 detik dan mengalirkan status progress secara bertahap.
     */
    fun startSession(): Flow<CalibrationSessionState> = flow {
        val samples = mutableListOf<CalibrationCalculator.Sample3D>()
        var lastX = 0f
        var lastY = 0f
        var lastZ = 0f

        val startTime = System.currentTimeMillis()
        val endTime = startTime + SESSION_DURATION_MS

        val magneticFlow = sensorSource.getMagneticFieldFlow()

        coroutineScope {
            val magneticJob = launch {
                magneticFlow.collect { mag ->
                    lastX = mag.x
                    lastY = mag.y
                    lastZ = mag.z
                    samples.add(CalibrationCalculator.Sample3D(mag.x, mag.y, mag.z))
                }
            }

            try {
                while (currentCoroutineContext().isActive) {
                    val now = System.currentTimeMillis()
                    val elapsed = now - startTime
                    val progress = (elapsed.toFloat() / SESSION_DURATION_MS.toFloat()).coerceIn(0f, 1f)
                    val remainingMs = (endTime - now).coerceAtLeast(0L)
                    val remainingSec = (remainingMs / 1000L).toInt() + (if (remainingMs % 1000L > 0) 1 else 0)

                    if (now >= endTime) {
                        val calibrationResult = CalibrationCalculator.calculate(samples)
                        emit(
                            CalibrationSessionState(
                                progress = 1f,
                                remainingSeconds = 0,
                                sampleCount = samples.size,
                                currentX = lastX,
                                currentY = lastY,
                                currentZ = lastZ,
                                isCompleted = true,
                                result = calibrationResult
                            )
                        )
                        break
                    } else {
                        emit(
                            CalibrationSessionState(
                                progress = progress,
                                remainingSeconds = remainingSec,
                                sampleCount = samples.size,
                                currentX = lastX,
                                currentY = lastY,
                                currentZ = lastZ,
                                isCompleted = false,
                                result = null
                            )
                        )
                    }
                    delay(UPDATE_INTERVAL_MS)
                }
            } finally {
                magneticJob.cancel()
            }
        }
    }
}
