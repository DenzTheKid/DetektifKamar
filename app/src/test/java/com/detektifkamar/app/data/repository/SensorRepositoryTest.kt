package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.sensor.RawAccelerometerData
import com.detektifkamar.app.data.sensor.RawMagneticData
import com.detektifkamar.app.data.sensor.SensorSource
import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.domain.repository.CalibrationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSensorSource(
    override val isMagnetometerAvailable: Boolean = true,
    override val sensorMaxRangeUt: Float = 2000f,
    override val isLightSensorAvailable: Boolean = true
) : SensorSource {
    val magFlow = MutableSharedFlow<RawMagneticData>(extraBufferCapacity = 64)
    val accelFlow = MutableSharedFlow<RawAccelerometerData>(extraBufferCapacity = 128)
    val lightFlow = MutableStateFlow<Float?>(300f)

    override fun getMagneticFieldFlow(): Flow<RawMagneticData> = magFlow
    override fun getAccelerometerFlow(): Flow<RawAccelerometerData> = accelFlow
    override fun getLightFlow(): Flow<Float?> = lightFlow
}

private class FakePreferencesRepository : PreferencesRepository {
    override val defaultBaselineUt: StateFlow<Float> = MutableStateFlow(45f)
    override val isVibrationEnabled: StateFlow<Boolean> = MutableStateFlow(true)
    override val vibrationEnabledFlow: Flow<Boolean> = isVibrationEnabled
    override val themeMode: StateFlow<Int> = MutableStateFlow(0)
    override val themeModeFlow: Flow<String> = MutableStateFlow("SYSTEM")
    override val isSimulationMode: StateFlow<Boolean> = MutableStateFlow(false)
    override val hasShownGuide: StateFlow<Boolean> = MutableStateFlow(false)
    private val _selectedUnit = MutableStateFlow(UnitType.MICROTESLA)
    override val selectedUnitState: StateFlow<UnitType> = _selectedUnit
    override val selectedUnitFlow: Flow<UnitType> = _selectedUnit

    override suspend fun setDefaultBaselineUt(value: Float) {}
    override suspend fun setVibrationEnabled(enabled: Boolean) {}
    override suspend fun setThemeMode(mode: Int) {}
    override suspend fun setThemeMode(mode: String) {}
    override suspend fun setSelectedUnit(unit: UnitType) {
        _selectedUnit.value = unit
    }
    override suspend fun setSimulationMode(enabled: Boolean) {}
    override suspend fun setHasShownGuide(shown: Boolean) {}
    override suspend fun resetAllData() {}
}

private class FakeCalibrationRepository : CalibrationRepository {
    private val flow = MutableStateFlow(CalibrationData())
    override fun getCalibrationFlow(): Flow<CalibrationData> = flow
    override suspend fun saveCalibration(data: CalibrationData) {
        flow.value = data
    }
    override suspend fun resetCalibration() {
        flow.value = CalibrationData()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SensorRepositoryTest {

    @Test
    fun highRateAccelEmissions_doNotCorruptMagneticLowPassFilter() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeSource = FakeSensorSource()
        val repo = SensorRepositoryImpl(
            realSensorSource = fakeSource,
            simulationSensorSource = fakeSource,
            preferencesRepository = FakePreferencesRepository(),
            calibrationRepository = FakeCalibrationRepository(),
            processingDispatcher = dispatcher
        )

        val collected = mutableListOf<Float>()
        val job = launch(dispatcher) {
            repo.getLiveSensorState().collect { collected.add(it.magneticUt) }
        }

        // Initial baseline emission
        fakeSource.accelFlow.emit(RawAccelerometerData(0f, 0f, 9.81f, 9.81f, 0f, 1L))
        fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 100f, 100f, 3, 1L))
        assertEquals(100f, collected.last(), 0.01f)

        // Burst of 50 accelerometer events while magnetometer has NOT emitted
        repeat(50) { idx ->
            fakeSource.accelFlow.emit(RawAccelerometerData(0.01f, -0.01f, 9.81f, 9.81f, 0.1f, 10L + idx))
        }
        assertEquals(100f, collected.last(), 0.01f)

        // Single magnetometer step to 200 µT: must step once by alpha=0.15f -> 115.0 µT
        fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 200f, 200f, 3, 100L))
        assertEquals(115f, collected.last(), 0.05f)

        job.cancel()
    }

    @Test
    fun elevatedFieldTransitionAndRemoval_reachesTinggiAndRecoversToNormal() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeSource = FakeSensorSource()
        val repo = SensorRepositoryImpl(
            realSensorSource = fakeSource,
            simulationSensorSource = fakeSource,
            preferencesRepository = FakePreferencesRepository(),
            calibrationRepository = FakeCalibrationRepository(),
            processingDispatcher = dispatcher
        )

        val states = mutableListOf<com.detektifkamar.app.data.sensor.LiveSensorState>()
        val job = launch(dispatcher) {
            repo.getLiveSensorState().collect { states.add(it) }
        }

        fakeSource.accelFlow.emit(RawAccelerometerData(0f, 0f, 9.81f, 9.81f, 0f, 1L))
        fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 1L))
        assertEquals(MagneticStatus.NORMAL, states.last().status)

        // Approach strong magnetic source (300 µT -> TINGGI)
        repeat(25) { i ->
            fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 300f, 300f, 3, 10L + i))
        }
        assertEquals(MagneticStatus.TINGGI, states.last().status)
        assertTrue(states.last().magneticUt > 250f)

        // Remove magnetic source back to 45 µT ambient
        repeat(35) { i ->
            fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 100L + i))
        }
        assertEquals(MagneticStatus.NORMAL, states.last().status)
        assertTrue(
            "Expected recovery < 65 µT, got ${states.last().magneticUt}",
            states.last().magneticUt < 65f
        )

        job.cancel()
    }

    @Test
    fun singleDefaultAccelFallback_allowsMagneticStreamToEmit() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeSource = FakeSensorSource()
        val repo = SensorRepositoryImpl(
            realSensorSource = fakeSource,
            simulationSensorSource = fakeSource,
            preferencesRepository = FakePreferencesRepository(),
            calibrationRepository = FakeCalibrationRepository(),
            processingDispatcher = dispatcher
        )

        val states = mutableListOf<com.detektifkamar.app.data.sensor.LiveSensorState>()
        val job = launch(dispatcher) {
            repo.getLiveSensorState().collect { states.add(it) }
        }

        // Simulate missing accelerometer fallback (one flat sample emitted, stream stays open)
        fakeSource.accelFlow.emit(RawAccelerometerData(0f, 0f, 9.81f, 9.81f, 0f, 0L))
        fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 1L))
        repeat(20) { i ->
            fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 250f, 250f, 3, 2L + i))
        }

        assertEquals(MagneticStatus.TINGGI, states.last().status)
        assertTrue(states.last().isStable)
        assertEquals(0f, states.last().tiltDeg, 0.01f)

        job.cancel()
    }

    @Test
    fun nonFiniteMagAndAccelSamples_doNotPoisonLowPassFiltersOrTilt() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val fakeSource = FakeSensorSource()
        val calRepo = FakeCalibrationRepository()
        val repo = SensorRepositoryImpl(
            realSensorSource = fakeSource,
            simulationSensorSource = fakeSource,
            preferencesRepository = FakePreferencesRepository(),
            calibrationRepository = calRepo,
            processingDispatcher = dispatcher
        )

        val states = mutableListOf<com.detektifkamar.app.data.sensor.LiveSensorState>()
        val job = launch(dispatcher) {
            repo.getLiveSensorState().collect { states.add(it) }
        }

        // Baseline valid samples
        fakeSource.accelFlow.emit(RawAccelerometerData(0f, 0f, 9.81f, 9.81f, 0f, 1L))
        fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 1L))
        assertEquals(45f, states.last().magneticUt, 0.01f)
        assertEquals(MagneticStatus.NORMAL, states.last().status)

        // Inject NaN and +Infinity samples into both mag and accel flows, plus NaN light
        fakeSource.magFlow.emit(RawMagneticData(Float.NaN, 0f, 45f, Float.NaN, 3, 2L))
        fakeSource.magFlow.emit(RawMagneticData(Float.POSITIVE_INFINITY, 0f, 45f, Float.POSITIVE_INFINITY, 3, 3L))
        fakeSource.accelFlow.emit(RawAccelerometerData(Float.NaN, 0f, 9.81f, Float.NaN, Float.NaN, 4L))
        fakeSource.accelFlow.emit(RawAccelerometerData(0f, Float.NEGATIVE_INFINITY, 9.81f, Float.POSITIVE_INFINITY, 0f, 5L))
        fakeSource.lightFlow.value = Float.NaN

        // Follow with valid 45f mag and flat accel samples
        repeat(10) { idx ->
            fakeSource.accelFlow.emit(RawAccelerometerData(0f, 0f, 9.81f, 9.81f, 0f, 10L + idx))
            fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 10L + idx))
        }

        val finalState = states.last()
        assertTrue("Expected finite magneticUt, got ${finalState.magneticUt}", finalState.magneticUt.isFinite())
        assertEquals(45f, finalState.magneticUt, 0.1f)
        assertEquals(MagneticStatus.NORMAL, finalState.status)
        assertTrue("Expected finite tiltDeg, got ${finalState.tiltDeg}", finalState.tiltDeg.isFinite())
        assertTrue("Expected finite tiltAngle, got ${finalState.tiltState.tiltAngle}", finalState.tiltState.tiltAngle.isFinite())

        // Also ensure non-finite calibration bias cannot produce NaN
        calRepo.saveCalibration(CalibrationData(biasX = Float.NaN, biasY = Float.POSITIVE_INFINITY, biasZ = 0f, calibratedAt = 1L))
        fakeSource.magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 100L))
        assertTrue("Expected finite magneticUt under NaN bias, got ${states.last().magneticUt}", states.last().magneticUt.isFinite())

        job.cancel()
    }
}
