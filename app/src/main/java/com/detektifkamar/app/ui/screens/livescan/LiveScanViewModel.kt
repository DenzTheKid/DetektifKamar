package com.detektifkamar.app.ui.screens.livescan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.VibratorHelper
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    private val vibratorHelper: VibratorHelper
) : ViewModel() {

    private val _isFrozen = MutableStateFlow(false)
    private val _frozenState = MutableStateFlow<LiveSensorState?>(null)
    private val _historyWindow = LinkedList<Float>()
    private val _historyList = MutableStateFlow<List<Float>>(emptyList())

    private var runningSum = 0.0
    private var runningCount = 0
    private var sessionMin = Float.MAX_VALUE
    private var sessionMax = 0f
    private var lastVibrateTime = 0L
    private var lastVibratedStatus: MagneticStatus? = null

    private val _statsFlow = MutableStateFlow(Triple(0f, 0f, 0f)) // min, max, avg

    private val _isSaveModalVisible = MutableStateFlow(false)
    private val _selectedRoomId = MutableStateFlow<Long?>(null)
    private val _pointNotesInput = MutableStateFlow("")
    private val _isPointSavedSuccess = MutableStateFlow(false)

    val uiState: StateFlow<LiveScanUiState> = combine(
        sensorRepository.getLiveSensorState(),
        preferencesRepository.selectedUnitFlow,
        _isFrozen,
        _historyList,
        roomRepository.getAllRoomsFlow(),
        _isSaveModalVisible,
        _selectedRoomId,
        _pointNotesInput
    ) { args ->
        val rawSensor = args[0] as LiveSensorState
        val unit = args[1] as UnitType
        val frozen = args[2] as Boolean
        @Suppress("UNCHECKED_CAST")
        val history = args[3] as List<Float>
        @Suppress("UNCHECKED_CAST")
        val rooms = args[4] as List<Room>
        val modalVisible = args[5] as Boolean
        val selectedRoom = args[6] as Long?
        val notes = args[7] as String

        val effectiveSensor = if (frozen && _frozenState.value != null) _frozenState.value!! else rawSensor

        // Perbarui statistik jika tidak dibekukan
        if (!frozen) {
            val mag = rawSensor.magneticUt
            runningSum += mag
            runningCount++
            sessionMin = min(sessionMin, mag)
            sessionMax = max(sessionMax, mag)
            _statsFlow.value = Triple(sessionMin, sessionMax, (runningSum / runningCount).toFloat())

            // Tambahkan ke history 10 detik (maksimal 100 sampel)
            _historyWindow.addLast(mag)
            if (_historyWindow.size > 100) {
                _historyWindow.removeFirst()
            }
            _historyList.value = _historyWindow.toList()

            // Pulsa getar haptik terkendali (hanya bergetar saat masuk batas TINGGI/SANGAT_TINGGI, dibatasi jeda cooldown minimal 3 detik agar tidak getar berlebihan)
            val now = System.currentTimeMillis()
            val isHighOrExtreme = (rawSensor.status == MagneticStatus.TINGGI || rawSensor.status == MagneticStatus.SANGAT_TINGGI)
            if (isHighOrExtreme && preferencesRepository.isVibrationEnabled.value) {
                if (now - lastVibrateTime >= 3000L) {
                    vibratorHelper.tickPulse()
                    lastVibrateTime = now
                    lastVibratedStatus = rawSensor.status
                }
            } else if (!isHighOrExtreme) {
                lastVibratedStatus = rawSensor.status
            }
        }

        val stats = _statsFlow.value

        LiveScanUiState(
            sensorState = effectiveSensor,
            displayUnit = unit,
            isFrozen = frozen,
            frozenValueUt = effectiveSensor.magneticUt,
            history10s = history,
            minUtSinceStart = if (sessionMin == Float.MAX_VALUE) effectiveSensor.magneticUt else stats.first,
            maxUtSinceStart = stats.second,
            averageUtSinceStart = stats.third,
            sampleCountSinceStart = runningCount,
            roomsList = rooms,
            isSaveModalVisible = modalVisible,
            selectedRoomId = selectedRoom ?: rooms.firstOrNull()?.id,
            pointNotesInput = notes,
            isPointSavedSuccess = _isPointSavedSuccess.value
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
            // Bekukan nilai saat ini
            _frozenState.value = uiState.value.sensorState
            _isFrozen.value = true
        } else {
            // Lepas bekuan
            _frozenState.value = null
            _isFrozen.value = false
        }
    }

    fun resetStatistics() {
        runningSum = 0.0
        runningCount = 0
        sessionMin = Float.MAX_VALUE
        sessionMax = 0f
        _statsFlow.value = Triple(0f, 0f, 0f)
        _historyWindow.clear()
        _historyList.value = emptyList()
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

    fun savePointToRoom() {
        val state = uiState.value
        val targetRoomId = state.selectedRoomId ?: return
        val currentSensor = state.sensorState

        viewModelScope.launch {
            val measurement = Measurement(
                roomId = targetRoomId,
                timestamp = System.currentTimeMillis(),
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
