package com.detektifkamar.app.data.repository

import android.hardware.SensorManager
import com.detektifkamar.app.data.sensor.HotColdTrend
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.RealSensorSource
import com.detektifkamar.app.data.sensor.SensorSource
import com.detektifkamar.app.data.sensor.SimulationSensorSource
import com.detektifkamar.app.domain.logic.AzimuthUtils
import com.detektifkamar.app.domain.logic.StabilityDetector
import com.detektifkamar.app.domain.logic.StatisticsUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import java.util.LinkedList
import kotlin.math.abs

interface SensorRepository {
    fun getLiveSensorState(): Flow<LiveSensorState>
}

class SensorRepositoryImpl(
    private val realSensorSource: RealSensorSource,
    private val simulationSensorSource: SimulationSensorSource,
    private val preferencesRepository: PreferencesRepository
) : SensorRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLiveSensorState(): Flow<LiveSensorState> {
        return preferencesRepository.isSimulationMode.flatMapLatest { isSimulation ->
            val activeSource: SensorSource = if (isSimulation) simulationSensorSource else realSensorSource
            createProcessedSensorFlow(activeSource, isSimulation)
        }.flowOn(Dispatchers.Default)
    }

    private fun createProcessedSensorFlow(source: SensorSource, isSimulation: Boolean): Flow<LiveSensorState> {
        // State internal untuk low-pass filter (alpha = 0.15)
        val alpha = 0.15f
        val filteredMag = FloatArray(3)
        val filteredAccel = FloatArray(3)
        var hasMagInit = false
        var hasAccelInit = false

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        // Sliding window kestabilan akselerometer (0.5 detik = ~10 sampel pada 20Hz)
        val accelWindow = LinkedList<Float>()
        val maxAccelWindowSize = 10

        // Sliding window hangat-dingin (1.5 detik terakhir = 30 sampel, 1.5 detik sebelumnya = 30 sampel)
        val recentMagWindow = LinkedList<Float>()
        val previousMagWindow = LinkedList<Float>()
        val maxHalfWindowSize = 30
        var currentTrend = HotColdTrend.STABIL

        return combine(
            source.getMagneticFieldFlow(),
            source.getAccelerometerFlow(),
            source.getLightFlow()
        ) { mag, accel, lux ->
            // 1. Low-Pass Filter untuk Vektor Magnetik & Akselerometer
            if (!hasMagInit) {
                filteredMag[0] = mag.x
                filteredMag[1] = mag.y
                filteredMag[2] = mag.z
                hasMagInit = true
            } else {
                filteredMag[0] += alpha * (mag.x - filteredMag[0])
                filteredMag[1] += alpha * (mag.y - filteredMag[1])
                filteredMag[2] += alpha * (mag.z - filteredMag[2])
            }

            if (!hasAccelInit) {
                filteredAccel[0] = accel.x
                filteredAccel[1] = accel.y
                filteredAccel[2] = accel.z
                hasAccelInit = true
            } else {
                filteredAccel[0] += alpha * (accel.x - filteredAccel[0])
                filteredAccel[1] += alpha * (accel.y - filteredAccel[1])
                filteredAccel[2] += alpha * (accel.z - filteredAccel[2])
            }

            // 2. Azimuth Kompas (hanya valid jika tilt < 45 derajat)
            var azimuthDeg: Float? = null
            if (AzimuthUtils.isAzimuthValid(accel.tiltDeg)) {
                val success = SensorManager.getRotationMatrix(rotationMatrix, null, filteredAccel, filteredMag)
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (az < 0f) az += 360f
                    azimuthDeg = az % 360f
                }
            }

            // 3. Gerbang Kestabilan Akselerometer
            accelWindow.add(accel.magnitude)
            if (accelWindow.size > maxAccelWindowSize) {
                accelWindow.removeFirst()
            }
            val isStable = StabilityDetector.isStable(accelWindow)

            // 4. Deteksi Hangat-Dingin dengan Histeresis
            recentMagWindow.add(mag.magnitude)
            if (recentMagWindow.size > maxHalfWindowSize) {
                val popped = recentMagWindow.removeFirst()
                previousMagWindow.add(popped)
                if (previousMagWindow.size > maxHalfWindowSize) {
                    previousMagWindow.removeFirst()
                }
            }

            if (recentMagWindow.size >= 15 && previousMagWindow.size >= 15) {
                val recentAvg = StatisticsUtils.calculateMean(recentMagWindow)
                val prevAvg = StatisticsUtils.calculateMean(previousMagWindow)
                val diff = recentAvg - prevAvg

                // Ambang 0.8 uT dengan histeresis 0.2 uT agar tidak berkedip
                currentTrend = when (currentTrend) {
                    HotColdTrend.MAKIN_PANAS -> {
                        if (diff > 0.6f) HotColdTrend.MAKIN_PANAS
                        else if (diff < -0.8f) HotColdTrend.MAKIN_DINGIN
                        else HotColdTrend.STABIL
                    }
                    HotColdTrend.MAKIN_DINGIN -> {
                        if (diff < -0.6f) HotColdTrend.MAKIN_DINGIN
                        else if (diff > 0.8f) HotColdTrend.MAKIN_PANAS
                        else HotColdTrend.STABIL
                    }
                    HotColdTrend.STABIL -> {
                        if (diff >= 0.8f) HotColdTrend.MAKIN_PANAS
                        else if (diff <= -0.8f) HotColdTrend.MAKIN_DINGIN
                        else HotColdTrend.STABIL
                    }
                }
            }

            LiveSensorState(
                magneticUt = mag.magnitude,
                bx = mag.x,
                by = mag.y,
                bz = mag.z,
                accuracy = mag.accuracy,
                isCalibrated = mag.accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM,
                isStable = isStable,
                unstableRatio = 0f,
                tiltDeg = accel.tiltDeg,
                azimuthDeg = azimuthDeg,
                lux = lux,
                hotColdTrend = currentTrend,
                isLightSensorAvailable = source.isLightSensorAvailable,
                isSimulation = isSimulation
            )
        }
    }
}
