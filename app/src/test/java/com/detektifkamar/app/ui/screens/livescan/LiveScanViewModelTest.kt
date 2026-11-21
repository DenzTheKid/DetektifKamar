package com.detektifkamar.app.ui.screens.livescan

import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.sensor.CalibrationEngine
import com.detektifkamar.app.data.sensor.HapticController
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.SimulationSensorSource
import com.detektifkamar.app.domain.logic.MagneticClassifier
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.RoomType
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeLiveSensorRepository : SensorRepository {
    val sensorFlow = MutableSharedFlow<LiveSensorState>(extraBufferCapacity = 64)
    override fun getLiveSensorState(): Flow<LiveSensorState> = sensorFlow
    override fun createCalibrationEngine(): CalibrationEngine = CalibrationEngine(SimulationSensorSource())
    override val isMagnetometerAvailable: Boolean = true
    override val sensorMaxRangeUt: Float = 2000f
}

private class FakeRoomRepository : RoomRepository {
    private val rooms = MutableStateFlow(
        listOf(Room(id = 1L, name = "Kamar Tidur", roomType = RoomType.KAMAR_TIDUR))
    )
    override fun getAllRoomsFlow(): Flow<List<Room>> = rooms
    override suspend fun getRoomById(id: Long): Room? = rooms.value.find { it.id == id }
    override suspend fun insertRoom(room: Room): Long = 1L
    override suspend fun updateRoom(room: Room) {}
    override suspend fun deleteRoom(id: Long) {}
}

private class FakeMeasurementRepository : MeasurementRepository {
    val inserted = mutableListOf<Measurement>()
    private val empty = MutableStateFlow<List<Measurement>>(emptyList())
    override fun getMeasurementsForRoomFlow(roomId: Long): Flow<List<Measurement>> = empty
    override fun getAllMeasurementsFlow(): Flow<List<Measurement>> = empty
    override fun getFilteredMeasurementsFlow(
        roomId: Long?,
        status: MagneticStatus?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Measurement>> = empty
    override suspend fun getMeasurementById(id: Long): Measurement? = null
    override suspend fun insertMeasurement(measurement: Measurement): Long {
        inserted.add(measurement)
        return inserted.size.toLong()
    }
    override suspend fun deleteMeasurement(id: Long) {}
    override suspend fun restoreMeasurement(measurement: Measurement) {}
}

private class FakeVmPreferencesRepository : PreferencesRepository {
    override val defaultBaselineUt: StateFlow<Float> = MutableStateFlow(45f)
    override val isVibrationEnabled: StateFlow<Boolean> = MutableStateFlow(true)
    override val vibrationEnabledFlow: Flow<Boolean> = isVibrationEnabled
    override val themeMode: StateFlow<Int> = MutableStateFlow(0)
    override val themeModeFlow: Flow<String> = MutableStateFlow("SYSTEM")
    override val isSimulationMode: StateFlow<Boolean> = MutableStateFlow(false)
    override val hasShownGuide: StateFlow<Boolean> = MutableStateFlow(false)
    private val _unit = MutableStateFlow(UnitType.MICROTESLA)
    override val selectedUnitState: StateFlow<UnitType> = _unit
    override val selectedUnitFlow: Flow<UnitType> = _unit

    override suspend fun setDefaultBaselineUt(value: Float) {}
    override suspend fun setVibrationEnabled(enabled: Boolean) {}
    override suspend fun setThemeMode(mode: Int) {}
    override suspend fun setThemeMode(mode: String) {}
    override suspend fun setSelectedUnit(unit: UnitType) {
        _unit.value = unit
    }
    override suspend fun setSimulationMode(enabled: Boolean) {}
    override suspend fun setHasShownGuide(shown: Boolean) {}
    override suspend fun resetAllData() {}
}

private class FakeHapticController : HapticController {
    var pulseCount = 0
    override fun vibrateShortPulse(durationMs: Long) {
        pulseCount++
    }
    override fun tickPulse() {
        pulseCount++
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LiveScanViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sensorState(ut: Float): LiveSensorState {
        return LiveSensorState(
            magneticUt = ut,
            bz = ut,
            status = MagneticClassifier.classify(ut, 2000f)
        )
    }

    @Test
    fun simulatedStreamThroughTinggiAndRecovery_updatesWithoutSampleInflation() = runTest {
        val fakeSensorRepo = FakeLiveSensorRepository()
        val fakeHaptic = FakeHapticController()
        var nowMs = 10_000L
        val viewModel = LiveScanViewModel(
            sensorRepository = fakeSensorRepo,
            roomRepository = FakeRoomRepository(),
            measurementRepository = FakeMeasurementRepository(),
            preferencesRepository = FakeVmPreferencesRepository(),
            vibratorHelper = fakeHaptic,
            timeProvider = { nowMs }
        )

        val job = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        val stream = listOf(
            45f, 46f, 44f, 45f, 47f,
            180f, 220f, 260f, 310f, 350f,
            200f, 140f, 90f, 60f, 45f
        )

        for (ut in stream) {
            nowMs += 50L
            fakeSensorRepo.sensorFlow.emit(sensorState(ut))
        }

        val state = viewModel.uiState.value
        assertEquals(45f, state.sensorState.magneticUt, 0.01f)
        assertEquals(MagneticStatus.NORMAL, state.sensorState.status)
        assertEquals("Sample count must match actual emissions (no combine feedback loop)", stream.size, state.sampleCountSinceStart)
        assertEquals(stream, state.history10s)
        assertEquals(44f, state.minUtSinceStart, 0.01f)
        assertEquals(350f, state.maxUtSinceStart, 0.01f)
        assertEquals(1, fakeHaptic.pulseCount)

        job.cancel()
    }

    @Test
    fun freezeAndUnfreeze_holdsAndResumesLiveValuesCleanly() = runTest {
        val fakeSensorRepo = FakeLiveSensorRepository()
        val viewModel = LiveScanViewModel(
            sensorRepository = FakeSensorRepoWrapper(fakeSensorRepo),
            roomRepository = FakeRoomRepository(),
            measurementRepository = FakeMeasurementRepository(),
            preferencesRepository = FakeVmPreferencesRepository(),
            vibratorHelper = FakeHapticController()
        )

        val job = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        fakeSensorRepo.sensorFlow.emit(sensorState(45f))
        fakeSensorRepo.sensorFlow.emit(sensorState(260f))
        assertEquals(260f, viewModel.uiState.value.sensorState.magneticUt, 0.01f)
        assertEquals(MagneticStatus.TINGGI, viewModel.uiState.value.sensorState.status)

        viewModel.toggleFreeze()
        assertTrue(viewModel.uiState.value.isFrozen)

        // Emit baseline while frozen: UI holds 260 µT and sampleCount does not grow
        fakeSensorRepo.sensorFlow.emit(sensorState(45f))
        assertEquals(260f, viewModel.uiState.value.sensorState.magneticUt, 0.01f)
        assertEquals(2, viewModel.uiState.value.sampleCountSinceStart)

        // Unfreeze: UI immediately displays current live value (45 µT)
        viewModel.toggleFreeze()
        assertFalse(viewModel.uiState.value.isFrozen)
        assertEquals(45f, viewModel.uiState.value.sensorState.magneticUt, 0.01f)
        assertEquals(MagneticStatus.NORMAL, viewModel.uiState.value.sensorState.status)

        job.cancel()
    }

    @Test
    fun toggleUnitAndResetStatistics_workDeterministically() = runTest {
        val fakeSensorRepo = FakeLiveSensorRepository()
        val viewModel = LiveScanViewModel(
            sensorRepository = fakeSensorRepo,
            roomRepository = FakeRoomRepository(),
            measurementRepository = FakeMeasurementRepository(),
            preferencesRepository = FakeVmPreferencesRepository(),
            vibratorHelper = FakeHapticController()
        )

        val job = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        fakeSensorRepo.sensorFlow.emit(sensorState(120f))
        assertEquals(UnitType.MICROTESLA, viewModel.uiState.value.displayUnit)

        viewModel.toggleUnit()
        assertEquals(UnitType.MILLITESLA, viewModel.uiState.value.displayUnit)

        viewModel.toggleUnit()
        assertEquals(UnitType.GAUSS, viewModel.uiState.value.displayUnit)

        viewModel.toggleUnit()
        assertEquals(UnitType.MICROTESLA, viewModel.uiState.value.displayUnit)

        viewModel.resetStatistics()
        assertEquals(0, viewModel.uiState.value.sampleCountSinceStart)
        assertTrue(viewModel.uiState.value.history10s.isEmpty())

        job.cancel()
    }

    @Test
    fun nonFiniteSensorEmission_doesNotForwardNaNToUiState_andSnackbarResets() = runTest {
        val fakeSensorRepo = FakeLiveSensorRepository()
        val viewModel = LiveScanViewModel(
            sensorRepository = fakeSensorRepo,
            roomRepository = FakeRoomRepository(),
            measurementRepository = FakeMeasurementRepository(),
            preferencesRepository = FakeVmPreferencesRepository(),
            vibratorHelper = FakeHapticController()
        )

        val job = launch(testDispatcher) {
            viewModel.uiState.collect {}
        }

        fakeSensorRepo.sensorFlow.emit(sensorState(45f))
        fakeSensorRepo.sensorFlow.emit(
            LiveSensorState(
                magneticUt = Float.NaN,
                bx = Float.NaN,
                by = Float.POSITIVE_INFINITY,
                bz = Float.NaN,
                status = MagneticStatus.SANGAT_TINGGI
            )
        )

        val stateAfterNan = viewModel.uiState.value
        assertTrue(stateAfterNan.sensorState.magneticUt.isFinite())
        assertEquals(45f, stateAfterNan.sensorState.magneticUt, 0.01f)
        assertEquals(MagneticStatus.NORMAL, stateAfterNan.sensorState.status)
        assertEquals(1, stateAfterNan.sampleCountSinceStart)

        // Verify saved snackbar flag clears when onSavedSnackbarShown is invoked
        viewModel.openSaveModal()
        viewModel.savePointToRoom()
        assertTrue(viewModel.uiState.value.isPointSavedSuccess)
        viewModel.onSavedSnackbarShown()
        assertFalse(viewModel.uiState.value.isPointSavedSuccess)

        job.cancel()
    }
}

private class FakeSensorRepoWrapper(private val delegate: FakeLiveSensorRepository) : SensorRepository by delegate
