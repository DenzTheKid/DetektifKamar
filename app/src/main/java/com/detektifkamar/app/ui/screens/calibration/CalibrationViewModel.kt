package com.detektifkamar.app.ui.screens.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.sensor.CalibrationEngine
import com.detektifkamar.app.data.sensor.CalibrationSessionState
import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.repository.CalibrationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalibrationUiState(
    val isSessionRunning: Boolean = false,
    val progress: Float = 0f,
    val remainingSeconds: Int = 15,
    val sampleCount: Int = 0,
    val currentX: Float = 0f,
    val currentY: Float = 0f,
    val currentZ: Float = 0f,
    val isCompleted: Boolean = false,
    val calibrationResult: CalibrationData? = null,
    val activeSavedCalibration: CalibrationData = CalibrationData()
)

class CalibrationViewModel(
    private val sensorRepository: SensorRepository,
    private val calibrationRepository: CalibrationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private var calibrationJob: Job? = null
    private val calibrationEngine: CalibrationEngine by lazy {
        sensorRepository.createCalibrationEngine()
    }

    init {
        viewModelScope.launch {
            calibrationRepository.getCalibrationFlow().collect { saved ->
                _uiState.update { it.copy(activeSavedCalibration = saved) }
            }
        }
    }

    fun startCalibration() {
        calibrationJob?.cancel()
        _uiState.update {
            it.copy(
                isSessionRunning = true,
                progress = 0f,
                remainingSeconds = 15,
                sampleCount = 0,
                isCompleted = false,
                calibrationResult = null
            )
        }

        calibrationJob = viewModelScope.launch {
            calibrationEngine.startSession().collect { sessionState ->
                _uiState.update { current ->
                    current.copy(
                        progress = sessionState.progress,
                        remainingSeconds = sessionState.remainingSeconds,
                        sampleCount = sessionState.sampleCount,
                        currentX = sessionState.currentX,
                        currentY = sessionState.currentY,
                        currentZ = sessionState.currentZ,
                        isCompleted = sessionState.isCompleted,
                        calibrationResult = sessionState.result,
                        isSessionRunning = !sessionState.isCompleted
                    )
                }
            }
        }
    }

    fun saveCalibrationResult() {
        val result = _uiState.value.calibrationResult ?: return
        viewModelScope.launch {
            calibrationRepository.saveCalibration(result)
        }
    }

    fun resetSavedCalibration() {
        viewModelScope.launch {
            calibrationRepository.resetCalibration()
        }
    }
}
