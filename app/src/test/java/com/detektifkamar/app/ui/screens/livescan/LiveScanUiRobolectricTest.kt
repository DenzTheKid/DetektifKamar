package com.detektifkamar.app.ui.screens.livescan

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SensorRepositoryImpl
import com.detektifkamar.app.data.sensor.HapticController
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.RawAccelerometerData
import com.detektifkamar.app.data.sensor.RawMagneticData
import com.detektifkamar.app.data.sensor.SensorSource
import com.detektifkamar.app.domain.logic.MagneticClassifier
import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.RoomType
import com.detektifkamar.app.domain.model.TiltState
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.domain.repository.CalibrationRepository
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository
import com.detektifkamar.app.ui.theme.DetektifKamarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.abs

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LiveScanUiRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun buildUiState(
        ut: Float,
        unit: UnitType = UnitType.MICROTESLA,
        sensorMaxRangeUt: Float = 4912f,
        isTilted: Boolean = false,
        tiltDeg: Float = if (isTilted) 55f else 3f,
        isFrozen: Boolean = false,
        isSaveModalVisible: Boolean = false
    ): LiveScanUiState {
        val status = MagneticClassifier.classify(ut, sensorMaxRangeUt)
        return LiveScanUiState(
            sensorState = LiveSensorState(
                isMagnetometerAvailable = true,
                sensorMaxRangeUt = sensorMaxRangeUt,
                magneticUt = ut,
                bx = 10f,
                by = -5f,
                bz = ut,
                isStable = !isTilted,
                tiltState = TiltState(tiltAngle = tiltDeg, isTilted = isTilted),
                tiltDeg = tiltDeg,
                status = status
            ),
            displayUnit = unit,
            isFrozen = isFrozen,
            history10s = listOf(45f, ut),
            minUtSinceStart = 45f,
            maxUtSinceStart = ut,
            averageUtSinceStart = (45f + ut) / 2f,
            sampleCountSinceStart = 2,
            roomsList = listOf(Room(id = 1L, name = "Kamar Tidur", roomType = RoomType.KAMAR_TIDUR)),
            selectedRoomId = 1L,
            isSaveModalVisible = isSaveModalVisible
        )
    }

    @Test
    fun rapidSubFrameUpdates_doNotFreezeReadingInTinggiOrStickHighAfterRemoval() {
        var uiState by mutableStateOf(buildUiState(45f))

        composeRule.setContent {
            DetektifKamarTheme(themeMode = 2) {
                LiveScanContent(uiState = uiState)
            }
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(32)
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("45.0")

        // Emit rapid (<16.6ms) updates climbing into TINGGI
        val rising = listOf(110f, 175f, 225f, 265f, 310f)
        for (ut in rising) {
            Snapshot.withMutableSnapshot {
                uiState = buildUiState(ut)
            }
            composeRule.mainClock.advanceTimeBy(8)
        }
        composeRule.mainClock.advanceTimeBy(24)

        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("310.0")
        composeRule.onNodeWithTag("live_scan_status_badge").assertIsDisplayed()
        composeRule.onNodeWithText("TINGGI").assertIsDisplayed()

        // Emit rapid (<16.6ms) removal updates returning to ambient 45 µT
        val falling = listOf(220f, 145f, 95f, 62f, 45f)
        for (ut in falling) {
            Snapshot.withMutableSnapshot {
                uiState = buildUiState(ut)
            }
            composeRule.mainClock.advanceTimeBy(8)
        }
        composeRule.mainClock.advanceTimeBy(300)

        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("45.0")
        composeRule.onNodeWithText("NORMAL").assertIsDisplayed()
    }

    @Test
    fun gaugeScaleLabels_stayBoundedAcross4912UtRangeAndUnitToggles() {
        var uiState by mutableStateOf(buildUiState(45f, sensorMaxRangeUt = 4912f))

        composeRule.setContent {
            DetektifKamarTheme(themeMode = 1) {
                LiveScanContent(
                    uiState = uiState,
                    onToggleUnit = {
                        val next = when (uiState.displayUnit) {
                            UnitType.MICROTESLA -> UnitType.MILLITESLA
                            UnitType.MILLITESLA -> UnitType.GAUSS
                            UnitType.GAUSS -> UnitType.MICROTESLA
                        }
                        uiState = uiState.copy(displayUnit = next)
                    }
                )
            }
        }

        fun assertScaleLabelsBounded(expectedMaxLabelsRange: IntRange = 6..11) {
            val semanticsNode = composeRule.onNodeWithTag("arc_gauge").fetchSemanticsNode()
            val desc = semanticsNode.config[androidx.compose.ui.semantics.SemanticsProperties.ContentDescription]
                .joinToString(" ")
            val match = Regex("""label=\[([^\]]+)]""").find(desc)
            assertTrue("Missing scale label semantics in: $desc", match != null)
            val labels = match!!.groupValues[1].split(",")
            assertTrue(
                "Label count ${labels.size} overflowed in: $desc",
                labels.size in expectedMaxLabelsRange
            )
        }

        assertScaleLabelsBounded()

        // Step into 1500 µT (triggers sensorMaxRangeUt = 4912f scale tier)
        uiState = buildUiState(1500f, sensorMaxRangeUt = 4912f)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("1500.0")
        composeRule.onNodeWithTag("arc_gauge_unit").assertTextEquals("µT")
        assertScaleLabelsBounded()

        // Toggle to mT
        composeRule.onNodeWithTag("btn_toggle_unit").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("1.500")
        composeRule.onNodeWithTag("arc_gauge_unit").assertTextEquals("mT")
        assertScaleLabelsBounded()

        // Toggle to G
        composeRule.onNodeWithTag("btn_toggle_unit").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("15.00")
        composeRule.onNodeWithTag("arc_gauge_unit").assertTextEquals("G")
        assertScaleLabelsBounded()
    }

    @Test
    fun yellowStatusAndTiltTransitions_haveZeroLayoutJitterAndKeepButtonsUsable() {
        var uiState by mutableStateOf(buildUiState(45f, isTilted = false))
        var saveClickedCount = 0

        composeRule.setContent {
            DetektifKamarTheme(themeMode = 1) {
                LiveScanContent(
                    uiState = uiState,
                    onToggleUnit = {
                        uiState = uiState.copy(displayUnit = UnitType.GAUSS)
                    },
                    onToggleFreeze = {
                        uiState = uiState.copy(isFrozen = !uiState.isFrozen)
                    },
                    onOpenSaveModal = {
                        saveClickedCount++
                        uiState = uiState.copy(isSaveModalVisible = true)
                    },
                    onDismissSaveModal = {
                        uiState = uiState.copy(isSaveModalVisible = false)
                    }
                )
            }
        }

        composeRule.waitForIdle()
        val baselineGaugeBounds = composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot()
        val baselineSaveBounds = composeRule.onNodeWithTag("btn_save_point").getBoundsInRoot()

        val testCases = listOf(
            45f to false,    // NORMAL flat
            100f to false,   // SEDANG flat
            280f to true,    // TINGGI (yellow) + tilted (yellow banner)
            650f to true,    // SANGAT_TINGGI + tilted
            1980f to false,  // SATURASI flat
            45f to false     // Recovery back to NORMAL
        )

        for ((ut, tilted) in testCases) {
            uiState = buildUiState(ut = ut, isTilted = tilted)
            composeRule.waitForIdle()

            if (tilted) {
                composeRule.onNodeWithTag("tilt_warning_banner").assertIsDisplayed()
            }

            composeRule.onNodeWithTag("btn_save_point")
                .assertIsDisplayed()
                .assertIsEnabled()
            composeRule.onNodeWithTag("btn_toggle_unit")
                .assertIsDisplayed()
                .assertIsEnabled()
            composeRule.onNodeWithTag("btn_toggle_freeze")
                .assertIsDisplayed()
                .assertIsEnabled()

            val currentGaugeBounds = composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot()
            val currentSaveBounds = composeRule.onNodeWithTag("btn_save_point").getBoundsInRoot()

            assertTrue(
                "Gauge top shifted at ut=$ut tilted=$tilted: ${currentGaugeBounds.top} vs ${baselineGaugeBounds.top}",
                abs(currentGaugeBounds.top.value - baselineGaugeBounds.top.value) < 0.5f
            )
            assertTrue(
                "Save button top shifted at ut=$ut tilted=$tilted: ${currentSaveBounds.top} vs ${baselineSaveBounds.top}",
                abs(currentSaveBounds.top.value - baselineSaveBounds.top.value) < 0.5f
            )
        }

        // Verify interactive clicks work while yellow status + tilt warning are active
        uiState = buildUiState(ut = 280f, isTilted = true)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("btn_toggle_freeze").performClick()
        composeRule.waitForIdle()
        assertEquals(true, uiState.isFrozen)
        composeRule.onNodeWithText("TERTAHAN").assertIsDisplayed()

        composeRule.onNodeWithTag("btn_save_point").performClick()
        composeRule.waitForIdle()
        assertEquals(1, saveClickedCount)
        composeRule.onNodeWithText("SIMPAN TITIK UKUR").assertIsDisplayed()
    }

    @Test
    fun endToEndLiveScanScreen_withSensorRepositoryAndViewModel_risesToTinggiAndRecovers() {
        val magFlow = MutableSharedFlow<RawMagneticData>(extraBufferCapacity = 128)
        val accelFlow = MutableSharedFlow<RawAccelerometerData>(extraBufferCapacity = 128)
        val sensorSource = object : SensorSource {
            override val isMagnetometerAvailable: Boolean = true
            override val sensorMaxRangeUt: Float = 2000f
            override val isLightSensorAvailable: Boolean = true
            override fun getMagneticFieldFlow(): Flow<RawMagneticData> = magFlow
            override fun getAccelerometerFlow(): Flow<RawAccelerometerData> = accelFlow
            override fun getLightFlow(): Flow<Float?> = MutableStateFlow(300f)
        }
        val prefs = object : PreferencesRepository {
            override val defaultBaselineUt: StateFlow<Float> = MutableStateFlow(45f)
            override val isVibrationEnabled: StateFlow<Boolean> = MutableStateFlow(false)
            override val vibrationEnabledFlow: Flow<Boolean> = isVibrationEnabled
            override val themeMode: StateFlow<Int> = MutableStateFlow(2)
            override val themeModeFlow: Flow<String> = MutableStateFlow("DARK")
            override val isSimulationMode: StateFlow<Boolean> = MutableStateFlow(false)
            override val hasShownGuide: StateFlow<Boolean> = MutableStateFlow(false)
            private val unitFlow = MutableStateFlow(UnitType.MICROTESLA)
            override val selectedUnitState: StateFlow<UnitType> = unitFlow
            override val selectedUnitFlow: Flow<UnitType> = unitFlow
            override suspend fun setDefaultBaselineUt(value: Float) {}
            override suspend fun setVibrationEnabled(enabled: Boolean) {}
            override suspend fun setThemeMode(mode: Int) {}
            override suspend fun setThemeMode(mode: String) {}
            override suspend fun setSelectedUnit(unit: UnitType) { unitFlow.value = unit }
            override suspend fun setSimulationMode(enabled: Boolean) {}
            override suspend fun setHasShownGuide(shown: Boolean) {}
            override suspend fun resetAllData() {}
        }
        val calRepo = object : CalibrationRepository {
            override fun getCalibrationFlow(): Flow<CalibrationData> = MutableStateFlow(CalibrationData())
            override suspend fun saveCalibration(data: CalibrationData) {}
            override suspend fun resetCalibration() {}
        }
        val roomRepo = object : RoomRepository {
            override fun getAllRoomsFlow(): Flow<List<Room>> = MutableStateFlow(
                listOf(Room(id = 1L, name = "Kamar", roomType = RoomType.KAMAR_TIDUR))
            )
            override suspend fun getRoomById(id: Long): Room? = null
            override suspend fun insertRoom(room: Room): Long = 1L
            override suspend fun updateRoom(room: Room) {}
            override suspend fun deleteRoom(id: Long) {}
        }
        val measurementRepo = object : MeasurementRepository {
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
            override suspend fun insertMeasurement(measurement: Measurement): Long = 1L
            override suspend fun deleteMeasurement(id: Long) {}
            override suspend fun restoreMeasurement(measurement: Measurement) {}
        }
        val haptic = object : HapticController {
            override fun vibrateShortPulse(durationMs: Long) {}
            override fun tickPulse() {}
        }

        val sensorRepo = SensorRepositoryImpl(
            realSensorSource = sensorSource,
            simulationSensorSource = sensorSource,
            preferencesRepository = prefs,
            calibrationRepository = calRepo,
            processingDispatcher = Dispatchers.Main.immediate
        )
        val viewModel = LiveScanViewModel(
            sensorRepository = sensorRepo,
            roomRepository = roomRepo,
            measurementRepository = measurementRepo,
            preferencesRepository = prefs,
            vibratorHelper = haptic
        )

        composeRule.setContent {
            DetektifKamarTheme(themeMode = 2) {
                LiveScanScreen(viewModel = viewModel, onNavigateToCreateRoom = {})
            }
        }
        composeRule.waitForIdle()

        runBlocking {
            accelFlow.emit(RawAccelerometerData(0f, 0f, 9.81f, 9.81f, 0f, 1L))
            magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 1L))
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("NORMAL").assertIsDisplayed()

        // Approach strong field -> TINGGI
        runBlocking {
            repeat(25) { idx ->
                magFlow.emit(RawMagneticData(0f, 0f, 300f, 300f, 3, 10L + idx))
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("TINGGI").assertIsDisplayed()
        val highReadingNode = composeRule.onNodeWithTag("arc_gauge_reading").fetchSemanticsNode()
        val highReading = highReadingNode.config[androidx.compose.ui.semantics.SemanticsProperties.Text]
            .joinToString("").toFloat()
        assertTrue("Expected reading > 150 µT in TINGGI, got $highReading", highReading > 150f)

        // Remove field -> recovers to NORMAL (< 65 µT)
        runBlocking {
            repeat(40) { idx ->
                magFlow.emit(RawMagneticData(0f, 0f, 45f, 45f, 3, 100L + idx))
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("NORMAL").assertIsDisplayed()
        val recoveredNode = composeRule.onNodeWithTag("arc_gauge_reading").fetchSemanticsNode()
        val recoveredReading = recoveredNode.config[androidx.compose.ui.semantics.SemanticsProperties.Text]
            .joinToString("").toFloat()
        assertTrue("Expected recovered reading < 65 µT, got $recoveredReading", recoveredReading < 65f)
    }

    @Config(sdk = [34], qualifiers = "w320dp-h600dp-xxhdpi")
    @Test
    fun compact320dpWithFontScale15_tiltBannerFitsSlotAndButtonsStayInteractive() {
        var uiState by mutableStateOf(buildUiState(45f, isTilted = false))
        var unitClicks = 0
        var freezeClicks = 0
        var saveClicks = 0

        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = baseDensity.density, fontScale = 1.5f)
            ) {
                DetektifKamarTheme(themeMode = 1) {
                    LiveScanContent(
                        uiState = uiState,
                        onToggleUnit = {
                            unitClicks++
                            uiState = uiState.copy(displayUnit = UnitType.MILLITESLA)
                        },
                        onToggleFreeze = {
                            freezeClicks++
                            uiState = uiState.copy(isFrozen = !uiState.isFrozen)
                        },
                        onOpenSaveModal = {
                            saveClicks++
                        }
                    )
                }
            }
        }

        composeRule.waitForIdle()
        val baselineSlot = composeRule.onNodeWithTag("tilt_warning_slot").getBoundsInRoot()
        val baselineGauge = composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot()
        val baselineSave = composeRule.onNodeWithTag("btn_save_point").getBoundsInRoot()
        val baselineBadge = composeRule.onNodeWithTag("live_scan_status_badge").getBoundsInRoot()

        // Transition to TINGGI + tilted (with large tilt angle)
        uiState = buildUiState(ut = 280f, isTilted = true, tiltDeg = 62f)
        composeRule.waitForIdle()

        val bannerBoundsTinggi = composeRule.onNodeWithTag("tilt_warning_banner")
            .assertIsDisplayed()
            .getBoundsInRoot()
        assertTrue(
            "Tilted banner bottom ${bannerBoundsTinggi.bottom} exceeded slot bottom ${baselineSlot.bottom}",
            bannerBoundsTinggi.bottom.value <= baselineSlot.bottom.value + 0.5f
        )
        assertTrue(
            "Gauge shifted vertically on tilt at fontScale=1.5f",
            abs(composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot().top.value - baselineGauge.top.value) < 0.5f
        )
        assertTrue(
            "Save button shifted vertically on tilt at fontScale=1.5f",
            abs(composeRule.onNodeWithTag("btn_save_point").getBoundsInRoot().top.value - baselineSave.top.value) < 0.5f
        )

        // Transition to SANGAT_TINGGI + tilted: badge must not shift horizontally
        uiState = buildUiState(ut = 650f, isTilted = true, tiltDeg = 65f)
        composeRule.waitForIdle()
        val sangatBadge = composeRule.onNodeWithTag("live_scan_status_badge").getBoundsInRoot()
        assertTrue(
            "Status badge shifted horizontally (${sangatBadge.left} vs ${baselineBadge.left})",
            abs(sangatBadge.left.value - baselineBadge.left.value) < 0.5f
        )

        // Also guard non-finite tiltDeg in TiltWarningBanner without crashing
        uiState = buildUiState(ut = 280f, isTilted = true, tiltDeg = Float.NaN)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tilt_warning_banner").assertIsDisplayed()

        // Verify all action buttons stay visible, enabled, and clickable
        composeRule.onNodeWithTag("btn_toggle_unit").assertIsDisplayed().assertIsEnabled().performClick()
        composeRule.onNodeWithTag("btn_toggle_freeze").assertIsDisplayed().assertIsEnabled().performClick()
        composeRule.onNodeWithTag("btn_save_point").assertIsDisplayed().assertIsEnabled().performClick()
        composeRule.waitForIdle()

        assertEquals(1, unitClicks)
        assertEquals(1, freezeClicks)
        assertEquals(1, saveClicks)
    }
}
