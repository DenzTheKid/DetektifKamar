package com.detektifkamar.app.ui.screens.livescan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.sensor.HapticController
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.TiltState
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.math.max
import kotlin.math.min

data class LiveScanUiState(
    val sensorState: LiveSensorState = LiveSensorState(),
    val displayUnit: UnitType = UnitType.MICROTESLA,
    val isFrozen: Boolean = false,
    val frozenValueUt: Float = 0f,
    val history10s: List<Float> = emptyList(),
    val minUtSinceStart: Float = Float.MAX_VALUE,
    val maxUtSinceStart: Float = 0f,
    val averageUtSinceStart: Float = 0f,
    val sampleCountSinceStart: Int = 0,
    val roomsList: List<Room> = emptyList(),
    val isSaveModalVisible: Boolean = false,
    val selectedRoomId: Long? = null,
    val pointNotesInput: String = "",
    val isPointSavedSuccess: Boolean = false
)

class LiveScanViewModel(
    private val sensorRepository: SensorRepository,
    private val roomRepository: RoomRepository,
    private val measurementRepository: MeasurementRepository,
    private val preferencesRepository: PreferencesRepository,
    private val vibratorHelper: HapticController,
    private val timeProvider: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private data class SensorMetricsSnapshot(
        val rawSensor: LiveSensorState,
        val history10s: List<Float>,
        val minUt: Float,
        val maxUt: Float,
        val avgUt: Float,
        val sampleCount: Int,
        val resetEpoch: Int
    )

    private data class ControlState(
        val unit: UnitType,
        val isFrozen: Boolean,
        val resetEpoch: Int
    )

    private data class ModalFormState(
        val isVisible: Boolean = false,
        val selectedRoomId: Long? = null,
        val notesInput: String = "",
        val isSavedSuccess: Boolean = false
    )

    private val _isFrozen = MutableStateFlow(false)
    private val _frozenState = MutableStateFlow<LiveSensorState?>(null)
    private val _resetEpoch = MutableStateFlow(0)

    private val metricsLock = Any()
    private val _historyWindow = LinkedList<Float>()
    private var runningSum = 0.0
    private var runningCount = 0
    private var sessionMin = Float.MAX_VALUE
    private var sessionMax = 0f
    private var currentResetEpoch = 0
    private var lastVibrateTime = 0L
    private var lastFiniteSensor: LiveSensorState? = null

    private val _isSaveModalVisible = MutableStateFlow(false)
    private val _selectedRoomId = MutableStateFlow<Long?>(null)
    private val _pointNotesInput = MutableStateFlow("")
    private val _isPointSavedSuccess = MutableStateFlow(false)

    // Akumulasi statistik & riwayat 1x per emisi sensor di hulu (bebas loop umpan balik combine)
    private val sensorMetricsFlow: Flow<SensorMetricsSnapshot> =
        sensorRepository.getLiveSensorState().map { rawSensor ->
            var shouldVibrate = false
            val snapshot = synchronized(metricsLock) {
                val sanitizedSensor = if (rawSensor.magneticUt.isFinite()) {
                    val fallback = lastFiniteSensor
                    val safeBx = if (rawSensor.bx.isFinite()) rawSensor.bx else (fallback?.bx ?: 0f)
                    val safeBy = if (rawSensor.by.isFinite()) rawSensor.by else (fallback?.by ?: 0f)
                    val safeBz = if (rawSensor.bz.isFinite()) rawSensor.bz else (fallback?.bz ?: 0f)
                    val safeTiltAngle = if (rawSensor.tiltState.tiltAngle.isFinite()) {
                        rawSensor.tiltState.tiltAngle
                    } else {
                        fallback?.tiltState?.tiltAngle ?: 0f
                    }
                    val safeTiltState = if (rawSensor.tiltState.tiltAngle.isFinite()) {
                        rawSensor.tiltState
                    } else {
                        TiltState(tiltAngle = safeTiltAngle, isTilted = rawSensor.tiltState.isTilted)
                    }
                    val safeTiltDeg = if (rawSensor.tiltDeg.isFinite()) rawSensor.tiltDeg else safeTiltAngle
                    rawSensor.copy(
                        bx = safeBx,
                        by = safeBy,
                        bz = safeBz,
                        tiltState = safeTiltState,
                        tiltDeg = safeTiltDeg,
                        lux = rawSensor.lux?.takeIf { it.isFinite() }
                    ).also { lastFiniteSensor = it }
                } else {
                    val fallback = lastFiniteSensor
                    val safeTiltAngle = if (rawSensor.tiltState.tiltAngle.isFinite()) {
                        rawSensor.tiltState.tiltAngle
                    } else {
                        fallback?.tiltState?.tiltAngle ?: 0f
                    }
                    val safeTiltState = if (rawSensor.tiltState.tiltAngle.isFinite()) {
                        rawSensor.tiltState
                    } else {
                        fallback?.tiltState ?: TiltState(tiltAngle = safeTiltAngle)
                    }
                    rawSensor.copy(
                        magneticUt = fallback?.magneticUt ?: 0f,
                        bx = fallback?.bx ?: 0f,
                        by = fallback?.by ?: 0f,
                        bz = fallback?.bz ?: 0f,
                        status = fallback?.status ?: MagneticStatus.NORMAL,
                        tiltState = safeTiltState,
                        tiltDeg = if (rawSensor.tiltDeg.isFinite()) rawSensor.tiltDeg else (fallback?.tiltDeg ?: 0f),
                        lux = rawSensor.lux?.takeIf { it.isFinite() } ?: fallback?.lux
                    )
                }

                if (!_isFrozen.value && rawSensor.magneticUt.isFinite()) {
                    val mag = sanitizedSensor.magneticUt
                    runningSum += mag
                    runningCount++
                    sessionMin = min(sessionMin, mag)
                    sessionMax = max(sessionMax, mag)

                    _historyWindow.addLast(mag)
                    if (_historyWindow.size > 100) {
                        _historyWindow.removeFirst()
                    }

                    // Pulsa getar haptik terkendali (cooldown minimal 3 detik)
                    val now = timeProvider()
                    val isHighOrExtreme = (
                        sanitizedSensor.status == MagneticStatus.TINGGI ||
                            sanitizedSensor.status == MagneticStatus.SANGAT_TINGGI
                        )
                    if (isHighOrExtreme && preferencesRepository.isVibrationEnabled.value) {
                        if (now - lastVibrateTime >= 3000L) {
                            shouldVibrate = true
                            lastVibrateTime = now
                        }
                    }
                }
                val avg = if (runningCount > 0) (runningSum / runningCount).toFloat() else 0f
                SensorMetricsSnapshot(
                    rawSensor = sanitizedSensor,
                    history10s = _historyWindow.toList(),
                    minUt = sessionMin,
                    maxUt = sessionMax,
                    avgUt = avg,
                    sampleCount = runningCount,
                    resetEpoch = currentResetEpoch
                )
            }
            if (shouldVibrate) {
                vibratorHelper.tickPulse()
            }
            snapshot
        }

    private val controlFlow: Flow<ControlState> = combine(
        preferencesRepository.selectedUnitFlow,
        _isFrozen,
        _resetEpoch
    ) { unit, frozen, resetEpoch ->
        ControlState(unit = unit, isFrozen = frozen, resetEpoch = resetEpoch)
    }

    private val modalFormFlow: Flow<ModalFormState> = combine(
        _isSaveModalVisible,
        _selectedRoomId,
        _pointNotesInput,
        _isPointSavedSuccess
    ) { visible, selectedRoom, notes, saved ->
        ModalFormState(
            isVisible = visible,
            selectedRoomId = selectedRoom,
            notesInput = notes,
            isSavedSuccess = saved
        )
    }

    val uiState: StateFlow<LiveScanUiState> = combine(
        sensorMetricsFlow,
        controlFlow,
        roomRepository.getAllRoomsFlow(),
        modalFormFlow
    ) { metrics, control, rooms, modal ->
        val rawSensor = metrics.rawSensor
        val frozenSnapshot = _frozenState.value
        val effectiveSensor = if (control.isFrozen && frozenSnapshot != null) frozenSnapshot else rawSensor

        val statsReset = (control.resetEpoch != metrics.resetEpoch)
        val history = if (statsReset) emptyList() else metrics.history10s
        val sampleCount = if (statsReset) 0 else metrics.sampleCount
        val minUt = if (statsReset || metrics.minUt == Float.MAX_VALUE) {
            effectiveSensor.magneticUt
        } else {
            metrics.minUt
        }
        val maxUt = if (statsReset) 0f else metrics.maxUt
        val avgUt = if (statsReset) 0f else metrics.avgUt

        LiveScanUiState(
            sensorState = effectiveSensor,
            displayUnit = control.unit,
            isFrozen = control.isFrozen,
            frozenValueUt = effectiveSensor.magneticUt,
            history10s = history,
            minUtSinceStart = minUt,
            maxUtSinceStart = maxUt,
            averageUtSinceStart = avgUt,
            sampleCountSinceStart = sampleCount,
            roomsList = rooms,
            isSaveModalVisible = modal.isVisible,
            selectedRoomId = modal.selectedRoomId ?: rooms.firstOrNull()?.id,
            pointNotesInput = modal.notesInput,
            isPointSavedSuccess = modal.isSavedSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LiveScanUiState()
    )

    fun toggleUnit() {
        val current = preferencesRepository.selectedUnitState.value
        val next = when (current) {
            UnitType.MICROTESLA -> UnitType.MILLITESLA
            UnitType.MILLITESLA -> UnitType.GAUSS
            UnitType.GAUSS -> UnitType.MICROTESLA
        }
        viewModelScope.launch {
            preferencesRepository.setSelectedUnit(next)
        }
    }

    fun toggleFreeze() {
        val current = _isFrozen.value
        if (!current) {
            _frozenState.value = uiState.value.sensorState
            _isFrozen.value = true
        } else {
            _frozenState.value = null
            _isFrozen.value = false
        }
    }

    fun resetStatistics() {
        synchronized(metricsLock) {
            runningSum = 0.0
            runningCount = 0
            sessionMin = Float.MAX_VALUE
            sessionMax = 0f
            _historyWindow.clear()
            currentResetEpoch++
            _resetEpoch.value = currentResetEpoch
        }
    }

    fun openSaveModal() {
        val rooms = uiState.value.roomsList
        _selectedRoomId.value = rooms.firstOrNull()?.id
        _pointNotesInput.value = ""
        _isPointSavedSuccess.value = false
        _isSaveModalVisible.value = true
    }

    fun closeSaveModal() {
        _isSaveModalVisible.value = false
    }

    fun onRoomSelected(roomId: Long) {
        _selectedRoomId.value = roomId
    }

    fun onNotesInputChanged(notes: String) {
        _pointNotesInput.value = notes
    }

    fun onSavedSnackbarShown() {
        _isPointSavedSuccess.value = false
    }

    fun savePointToRoom() {
        val state = uiState.value
        val targetRoomId = state.selectedRoomId ?: return
        val currentSensor = state.sensorState

        viewModelScope.launch {
            val measurement = Measurement(
                roomId = targetRoomId,
                timestamp = timeProvider(),
                x = currentSensor.bx,
                y = currentSensor.by,
                z = currentSensor.bz,
                magnitude = currentSensor.magneticUt,
                sensorAccuracy = currentSensor.accuracy,
                biasX = currentSensor.appliedBias.biasX,
                biasY = currentSensor.appliedBias.biasY,
                biasZ = currentSensor.appliedBias.biasZ,
                lux = currentSensor.lux,
                tiltAngle = currentSensor.tiltDeg,
                isTilted = currentSensor.tiltState.isTilted,
                status = currentSensor.status,
                notes = state.pointNotesInput.trim()
            )

            measurementRepository.insertMeasurement(measurement)
            _isPointSavedSuccess.value = true
            _isSaveModalVisible.value = false
        }
    }
}
