package com.detektifkamar.app.data.repository

import android.hardware.SensorManager
import com.detektifkamar.app.data.sensor.CalibrationEngine
import com.detektifkamar.app.data.sensor.HotColdTrend
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.SensorSource
import com.detektifkamar.app.domain.logic.MagneticClassifier
import com.detektifkamar.app.domain.logic.TiltDetector
import com.detektifkamar.app.domain.model.TiltState
import com.detektifkamar.app.domain.repository.CalibrationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.LinkedList
import kotlin.math.sqrt

interface SensorRepository {
    fun getLiveSensorState(): Flow<LiveSensorState>
    fun createCalibrationEngine(): CalibrationEngine
    val isMagnetometerAvailable: Boolean
    val sensorMaxRangeUt: Float
}

private data class FilteredMagSample(
    val x: Float,
    val y: Float,
    val z: Float,
    val accuracy: Int,
    val seq: Long
)

private data class FilteredAccelSample(
    val x: Float,
    val y: Float,
    val z: Float
)

class SensorRepositoryImpl(
    private val realSensorSource: SensorSource,
    private val simulationSensorSource: SensorSource,
    private val preferencesRepository: PreferencesRepository,
    private val calibrationRepository: CalibrationRepository,
    private val processingDispatcher: CoroutineDispatcher = Dispatchers.Default
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
        }.flowOn(processingDispatcher)
    }

    private fun createProcessedSensorFlow(source: SensorSource, isSimulation: Boolean): Flow<LiveSensorState> {
        val alpha = 0.15f

        // 1a. Low-Pass Filter Magnetik independen (hanya diperbarui saat event magnetometer masuk)
        val filteredMagFlow: Flow<FilteredMagSample> = flow {
            var hasMagInit = false
            var fx = 0f
            var fy = 0f
            var fz = 0f
            var seq = 0L
            source.getMagneticFieldFlow().collect { mag ->
                if (!mag.x.isFinite() || !mag.y.isFinite() || !mag.z.isFinite()) {
                    return@collect
                }
                if (!hasMagInit) {
                    fx = mag.x
                    fy = mag.y
                    fz = mag.z
                    hasMagInit = true
                } else {
                    val nextFx = fx + alpha * (mag.x - fx)
                    val nextFy = fy + alpha * (mag.y - fy)
                    val nextFz = fz + alpha * (mag.z - fz)
                    if (!nextFx.isFinite() || !nextFy.isFinite() || !nextFz.isFinite()) {
                        return@collect
                    }
                    fx = nextFx
                    fy = nextFy
                    fz = nextFz
                }
                seq++
                emit(FilteredMagSample(fx, fy, fz, mag.accuracy, seq))
            }
        }

        // 1b. Low-Pass Filter Akselerometer independen (tidak pernah mencemari LPF magnetik)
        val filteredAccelFlow: Flow<FilteredAccelSample> = flow {
            var hasAccelInit = false
            var ax = 0f
            var ay = 0f
            var az = 0f
            source.getAccelerometerFlow().collect { accel ->
                if (!accel.x.isFinite() || !accel.y.isFinite() || !accel.z.isFinite()) {
                    return@collect
                }
                if (!hasAccelInit) {
                    ax = accel.x
                    ay = accel.y
                    az = accel.z
                    hasAccelInit = true
                } else {
                    val nextAx = ax + alpha * (accel.x - ax)
                    val nextAy = ay + alpha * (accel.y - ay)
                    val nextAz = az + alpha * (accel.z - az)
                    if (!nextAx.isFinite() || !nextAy.isFinite() || !nextAz.isFinite()) {
                        return@collect
                    }
                    ax = nextAx
                    ay = nextAy
                    az = nextAz
                }
                emit(FilteredAccelSample(ax, ay, az))
            }
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        val accelVec = FloatArray(3)
        val magVec = FloatArray(3)

        // Sliding window hangat-dingin (1.5 detik terakhir = 30 sampel, 1.5 detik sebelumnya = 30 sampel)
        val recentMagWindow = LinkedList<Float>()
        val previousMagWindow = LinkedList<Float>()
        val maxHalfWindowSize = 30
        var currentTrend = HotColdTrend.STABIL
        var lastTiltedState = false
        var lastMagSeq = -1L

        return combine(
            filteredMagFlow,
            filteredAccelFlow,
            source.getLightFlow(),
            calibrationRepository.getCalibrationFlow()
        ) { magSample, accelSample, lux, calibration ->
            // 2. Terapkan Bias Kalibrasi Hard-Iron (dengan sanitasi non-finite)
            val biasX = if (calibration.biasX.isFinite()) calibration.biasX else 0f
            val biasY = if (calibration.biasY.isFinite()) calibration.biasY else 0f
            val biasZ = if (calibration.biasZ.isFinite()) calibration.biasZ else 0f

            val calX = (magSample.x - biasX).let { if (it.isFinite()) it else 0f }
            val calY = (magSample.y - biasY).let { if (it.isFinite()) it else 0f }
            val calZ = (magSample.z - biasZ).let { if (it.isFinite()) it else 0f }
            val rawMagnitude = sqrt(calX * calX + calY * calY + calZ * calZ)
            val calMagnitude = if (rawMagnitude.isFinite()) rawMagnitude else 0f

            // 3. Deteksi Kemiringan (TiltDetector) dari Vektor Akselerometer dengan Histeresis Anti-Jitter
            val rawTiltState = TiltDetector.detectTilt(accelSample.x, accelSample.y, accelSample.z, lastTiltedState)
            val tiltState = if (rawTiltState.tiltAngle.isFinite() && rawTiltState.pitch.isFinite() && rawTiltState.roll.isFinite()) {
                rawTiltState
            } else {
                TiltState()
            }
            lastTiltedState = tiltState.isTilted

            // 4. Azimuth Kompas (hanya valid jika ponsel mendatar / tilt < 45°)
            var azimuthDeg: Float? = null
            if (!tiltState.isTilted) {
                accelVec[0] = accelSample.x
                accelVec[1] = accelSample.y
                accelVec[2] = accelSample.z
                magVec[0] = magSample.x
                magVec[1] = magSample.y
                magVec[2] = magSample.z
                azimuthDeg = try {
                    val success = SensorManager.getRotationMatrix(rotationMatrix, null, accelVec, magVec)
                    if (success) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        var az = Math.toDegrees(orientation[0].toDouble()).toFloat()
                        if (az.isFinite()) {
                            if (az < 0f) az += 360f
                            az % 360f
                        } else null
                    } else null
                } catch (_: RuntimeException) {
                    // Fallback aman jika berjalan di JVM unit test tanpa stub Android SensorManager
                    null
                }
            }

            // 5. Tren Hangat-Dingin (hanya diperbarui saat ada sampel magnet baru)
            if (magSample.seq != lastMagSeq) {
                lastMagSeq = magSample.seq
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
            }

            // 6. Klasifikasi Status Intensitas Terpusat
            val magneticStatus = MagneticClassifier.classify(
                magnitudeUt = calMagnitude,
                sensorMaxRange = source.sensorMaxRangeUt
            )

            val safeLux = lux?.takeIf { it.isFinite() }

            LiveSensorState(
                isMagnetometerAvailable = source.isMagnetometerAvailable,
                sensorMaxRangeUt = source.sensorMaxRangeUt,
                magneticUt = calMagnitude,
                bx = calX,
                by = calY,
                bz = calZ,
                rawBx = magSample.x,
                rawBy = magSample.y,
                rawBz = magSample.z,
                accuracy = magSample.accuracy,
                isCalibrated = calibration.isCalibrated,
                appliedBias = calibration,
                isStable = !tiltState.isTilted,
                tiltState = tiltState,
                tiltDeg = tiltState.tiltAngle,
                azimuthDeg = azimuthDeg,
                lux = safeLux,
                status = magneticStatus,
                hotColdTrend = currentTrend,
                isLightSensorAvailable = source.isLightSensorAvailable,
                isSimulation = isSimulation
            )
        }
    }
}
