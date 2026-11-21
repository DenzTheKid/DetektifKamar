package com.detektifkamar.app.data.sensor

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.math.sin
import kotlin.random.Random

/**
 * Sumber sensor simulasi untuk emulator Android atau pengujian tanpa sensor fisik.
 * Menghasilkan data magnet sekitar 42-48 µT dengan derau acak realistis,
 * percepatan stabil 9.81 m/s², dan cahaya ~320 lux.
 */
class SimulationSensorSource : SensorSource {

    override val isLightSensorAvailable: Boolean = true

    override fun getMagneticFieldFlow(): Flow<RawMagneticData> = flow {
        var tick = 0L
        while (currentCoroutineContext().isActive) {
            tick++
            // Variasi gelombang halus + derau gaussian kecil
            val base = 44.5f + (sin(tick * 0.05) * 3.0).toFloat()
            val noiseX = (Random.nextFloat() - 0.5f) * 0.8f
            val noiseY = (Random.nextFloat() - 0.5f) * 0.8f
            val noiseZ = (Random.nextFloat() - 0.5f) * 1.2f

            val bx = 12.0f + noiseX
            val by = -5.0f + noiseY
            val bz = base + noiseZ
            val magnitude = kotlin.math.sqrt(bx * bx + by * by + bz * bz)

            emit(
                RawMagneticData(
                    x = bx,
                    y = by,
                    z = bz,
                    magnitude = magnitude,
                    accuracy = 3,
                    timestamp = System.currentTimeMillis()
                )
            )
            delay(50) // ~20 Hz
        }
    }

    override fun getAccelerometerFlow(): Flow<RawAccelerometerData> = flow {
        while (currentCoroutineContext().isActive) {
            val noiseX = (Random.nextFloat() - 0.5f) * 0.05f
            val noiseY = (Random.nextFloat() - 0.5f) * 0.05f
            val az = 9.81f + (Random.nextFloat() - 0.5f) * 0.08f
            val magnitude = kotlin.math.sqrt(noiseX * noiseX + noiseY * noiseY + az * az)

            emit(
                RawAccelerometerData(
                    x = noiseX,
                    y = noiseY,
                    z = az,
                    magnitude = magnitude,
                    tiltDeg = 4.0f,
                    timestamp = System.currentTimeMillis()
                )
            )
            delay(50) // ~20 Hz
        }
    }

    override fun getLightFlow(): Flow<Float?> = flow {
        while (currentCoroutineContext().isActive) {
            val lux = 320.0f + (Random.nextFloat() - 0.5f) * 15.0f
            emit(lux)
            delay(500)
        }
    }
}
