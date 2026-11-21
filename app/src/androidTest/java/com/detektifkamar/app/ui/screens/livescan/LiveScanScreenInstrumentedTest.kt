package com.detektifkamar.app.ui.screens.livescan

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.domain.logic.MagneticClassifier
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.RoomType
import com.detektifkamar.app.domain.model.TiltState
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.ui.theme.DetektifKamarTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class LiveScanScreenInstrumentedTest {

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
    fun rapidUpdatesThroughTinggiAndRecovery_keepDigitalReadoutResponsive() {
        var uiState by mutableStateOf(buildUiState(45f))

        composeRule.setContent {
            DetektifKamarTheme(themeMode = 2) {
                LiveScanContent(uiState = uiState)
            }
        }

        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(32)
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("45.0")

        for (ut in listOf(110f, 175f, 225f, 265f, 310f)) {
            Snapshot.withMutableSnapshot {
                uiState = buildUiState(ut)
            }
            composeRule.mainClock.advanceTimeBy(8)
        }
        composeRule.mainClock.advanceTimeBy(24)

        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("310.0")
        composeRule.onNodeWithText("TINGGI").assertIsDisplayed()

        for (ut in listOf(220f, 145f, 95f, 62f, 45f)) {
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
    fun gaugeScaleLabels_stayBoundedAndUnitToggleUpdatesCleanly() {
        var uiState by mutableStateOf(buildUiState(1500f, sensorMaxRangeUt = 4912f))

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

        fun assertBounded() {
            val node = composeRule.onNodeWithTag("arc_gauge").fetchSemanticsNode()
            val desc = node.config[SemanticsProperties.ContentDescription].joinToString(" ")
            val match = Regex("""label=\[([^\]]+)]""").find(desc)
            assertTrue(match != null)
            val labels = match!!.groupValues[1].split(",")
            assertTrue(labels.size in 6..11)
        }

        assertBounded()
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("1500.0")

        composeRule.onNodeWithTag("btn_toggle_unit").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("1.500")
        assertBounded()

        composeRule.onNodeWithTag("btn_toggle_unit").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("arc_gauge_reading").assertTextEquals("15.00")
        assertBounded()
    }

    @Test
    fun yellowStatusAndTilt_zeroLayoutShiftAndButtonsAlwaysClickable() {
        var uiState by mutableStateOf(buildUiState(45f, isTilted = false))
        var saveClicked = 0

        composeRule.setContent {
            DetektifKamarTheme(themeMode = 1) {
                LiveScanContent(
                    uiState = uiState,
                    onOpenSaveModal = {
                        saveClicked++
                        uiState = uiState.copy(isSaveModalVisible = true)
                    },
                    onDismissSaveModal = {
                        uiState = uiState.copy(isSaveModalVisible = false)
                    }
                )
            }
        }

        composeRule.waitForIdle()
        val baselineGauge = composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot()
        val baselineSave = composeRule.onNodeWithTag("btn_save_point").getBoundsInRoot()

        uiState = buildUiState(280f, isTilted = true)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tilt_warning_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("btn_save_point").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithTag("btn_toggle_unit").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithTag("btn_toggle_freeze").assertIsDisplayed().assertIsEnabled()

        val tiltedGauge = composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot()
        val tiltedSave = composeRule.onNodeWithTag("btn_save_point").getBoundsInRoot()

        assertTrue(abs(tiltedGauge.top.value - baselineGauge.top.value) < 0.5f)
        assertTrue(abs(tiltedSave.top.value - baselineSave.top.value) < 0.5f)

        composeRule.onNodeWithTag("btn_save_point").performClick()
        composeRule.waitForIdle()
        assertEquals(1, saveClicked)
        composeRule.onNodeWithText("SIMPAN TITIK UKUR").assertIsDisplayed()
    }

    @Test
    fun largeFontScale15_tiltBannerFitsSlotAndStatusBadgeDoesNotShift() {
        var uiState by mutableStateOf(buildUiState(45f, isTilted = false))

        composeRule.setContent {
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density = baseDensity.density, fontScale = 1.5f)
            ) {
                DetektifKamarTheme(themeMode = 1) {
                    LiveScanContent(uiState = uiState)
                }
            }
        }

        composeRule.waitForIdle()
        val baselineSlot = composeRule.onNodeWithTag("tilt_warning_slot").getBoundsInRoot()
        val baselineGauge = composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot()
        val baselineBadge = composeRule.onNodeWithTag("live_scan_status_badge").getBoundsInRoot()

        uiState = buildUiState(650f, isTilted = true, tiltDeg = 60f)
        composeRule.waitForIdle()

        val bannerBounds = composeRule.onNodeWithTag("tilt_warning_banner")
            .assertIsDisplayed()
            .getBoundsInRoot()
        assertTrue(bannerBounds.bottom.value <= baselineSlot.bottom.value + 0.5f)
        assertTrue(abs(composeRule.onNodeWithTag("arc_gauge").getBoundsInRoot().top.value - baselineGauge.top.value) < 0.5f)
        assertTrue(abs(composeRule.onNodeWithTag("live_scan_status_badge").getBoundsInRoot().left.value - baselineBadge.left.value) < 0.5f)
        composeRule.onNodeWithTag("btn_save_point").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithTag("btn_toggle_unit").assertIsDisplayed().assertIsEnabled()
        composeRule.onNodeWithTag("btn_toggle_freeze").assertIsDisplayed().assertIsEnabled()
    }
}
