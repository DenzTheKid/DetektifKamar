package com.detektifkamar.app.data.repository

import android.hardware.SensorManager
import com.detektifkamar.app.data.sensor.CalibrationEngine
import com.detektifkamar.app.data.sensor.HotColdTrend
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.RealSensorSource
import com.detektifkamar.app.data.sensor.SensorSource
import com.detektifkamar.app.data.sensor.SimulationSensorSource
import com.detektifkamar.app.domain.logic.AzimuthUtils
import com.detektifkamar.app.domain.logic.MagneticClassifier
import com.detektifkamar.app.domain.logic.TiltDetector
import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.repository.CalibrationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import java.util.LinkedList
import kotlin.math.sqrt

interface SensorRepository {
    fun getLiveSensorState(): Flow<LiveSensorState>
    fun createCalibrationEngine(): CalibrationEngine
    val isMagnetometerAvailable: Boolean
    val sensorMaxRangeUt: Float
}

class SensorRepositoryImpl(
    private val realSensorSource: RealSensorSource,
    private val simulationSensorSource: SimulationSensorSource,
    private val preferencesRepository: PreferencesRepository,
    private val calibrationRepository: CalibrationRepository
) : SensorRepository {

    override val isMagnetometerAvailable: Boolean
        get() = realSensorSource.isMagnetometerAvailable

    override val sensorMaxRangeUt: Float
        get() = realSensorSource.sensorMaxRangeUt

    override fun createCalibrationEngine(): CalibrationEngine {
        return CalibrationEngine(realSensorSource)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getLiveSensorState(): Flow<LiveSensorState> {
        return preferencesRepository.isSimulationMode.flatMapLatest { isSimulation ->
            val activeSource: SensorSource = if (isSimulation) simulationSensorSource else realSensorSource
            createProcessedSensorFlow(activeSource, isSimulation)
        }.flowOn(Dispatchers.Default)
    }

    private fun createProcessedSensorFlow(source: SensorSource, isSimulation: Boolean): Flow<LiveSensorState> {
        val alpha = 0.15f
        val filteredMag = FloatArray(3)
        val filteredAccel = FloatArray(3)
        var hasMagInit = false
        var hasAccelInit = false

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        // Sliding window hangat-dingin (1.5 detik terakhir = 30 sampel, 1.5 detik sebelumnya = 30 sampel)
        val recentMagWindow = LinkedList<Float>()
        val previousMagWindow = LinkedList<Float>()
        val maxHalfWindowSize = 30
        var currentTrend = HotColdTrend.STABIL

        return combine(
            source.getMagneticFieldFlow(),
            source.getAccelerometerFlow(),
            source.getLightFlow(),
            calibrationRepository.getCalibrationFlow()
        ) { mag, accel, lux, calibration ->
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

            // 2. Terapkan Bias Kalibrasi Hard-Iron
            val biasX = calibration.biasX
            val biasY = calibration.biasY
            val biasZ = calibration.biasZ

            val calX = filteredMag[0] - biasX
            val calY = filteredMag[1] - biasY
            val calZ = filteredMag[2] - biasZ
            val calMagnitude = sqrt(calX * calX + calY * calY + calZ * calZ)

            // 3. Deteksi Kemiringan (TiltDetector) dari Vektor Akselerometer
            val tiltState = TiltDetector.detectTilt(filteredAccel[0], filteredAccel[1], filteredAccel[2])

            // 4. Azimuth Kompas (hanya valid jika ponsel mendatar / tilt < 45°)
            var azimuthDeg: Float? = null
            if (!tiltState.isTilted) {
                val success = SensorManager.getRotationMatrix(rotationMatrix, null, filteredAccel, filteredMag)
                if (success) {
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (az < 0f) az += 360f
                    azimuthDeg = az % 360f
                }
            }

            // 5. Tren Hangat-Dingin
            recentMagWindow.addLast(calMagnitude)
            if (recentMagWindow.size > maxHalfWindowSize) {
                val moved = recentMagWindow.removeFirst()
                previousMagWindow.addLast(moved)
                if (previousMagWindow.size > maxHalfWindowSize) {
                    previousMagWindow.removeFirst()
                }
            }

            if (recentMagWindow.size >= 10 && previousMagWindow.size >= 10) {
                val recentAvg = recentMagWindow.average().toFloat()
                val prevAvg = previousMagWindow.average().toFloat()
                val delta = recentAvg - prevAvg
                val threshold = 0.8f
                currentTrend = when {
                    delta > threshold -> HotColdTrend.MAKIN_PANAS
                    delta < -threshold -> HotColdTrend.MAKIN_DINGIN
                    else -> HotColdTrend.STABIL
                }
            }

            // 6. Klasifikasi Status Intensitas Terpusat
            val magneticStatus = MagneticClassifier.classify(
                magnitudeUt = calMagnitude,
                sensorMaxRange = source.sensorMaxRangeUt
            )

            LiveSensorState(
                isMagnetometerAvailable = source.isMagnetometerAvailable,
                sensorMaxRangeUt = source.sensorMaxRangeUt,
                magneticUt = calMagnitude,
                bx = calX,
                by = calY,
                bz = calZ,
                rawBx = filteredMag[0],
                rawBy = filteredMag[1],
                rawBz = filteredMag[2],
                accuracy = mag.accuracy,
                isCalibrated = calibration.isCalibrated,
                appliedBias = calibration,
                isStable = !tiltState.isTilted,
                tiltState = tiltState,
                tiltDeg = tiltState.tiltAngle,
                azimuthDeg = azimuthDeg,
                lux = lux,
                status = magneticStatus,
                hotColdTrend = currentTrend,
                isLightSensorAvailable = source.isLightSensorAvailable,
                isSimulation = isSimulation
            )
        }
    }
}
