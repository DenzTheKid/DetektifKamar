package com.detektifkamar.app.ui.screens.selidik

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.sensor.HotColdTrend
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.data.sensor.VibratorHelper
import com.detektifkamar.app.domain.model.InvestigationCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.LinkedList

data class SelidikUiState(
    val liveSensor: LiveSensorState = LiveSensorState(),
    val recentSamples: List<Float> = emptyList(),
    val cases: List<InvestigationCase> = emptyList(),
    val showSelectCaseDialog: Boolean = false,
    val isVibrationActive: Boolean = true
)

class SelidikViewModel(
    private val sensorRepository: SensorRepository,
    private val caseRepository: CaseRepository,
    private val preferencesRepository: PreferencesRepository,
    private val vibratorHelper: VibratorHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelidikUiState())
    val uiState: StateFlow<SelidikUiState> = _uiState.asStateFlow()

    private val samplesWindow = LinkedList<Float>()
    private val maxSamples = 60 // ~10 detik pada 6 sample/detik

    private var lastVibrateTime = 0L

    init {
        // Ambil daftar kasus untuk dialog tombol cepat
        viewModelScope.launch {
            caseRepository.getAllCases().collect { caseList ->
                _uiState.update { it.copy(cases = caseList) }
            }
        }

        // Ambil data sensor live
        viewModelScope.launch {
            sensorRepository.getLiveSensorState().collect { state ->
                samplesWindow.add(state.magneticUt)
                if (samplesWindow.size > maxSamples) {
                    samplesWindow.removeFirst()
                }

                // Cek getar haptik jika makin panas dan getar aktif
                if (preferencesRepository.isVibrationEnabled.value) {
                    val now = System.currentTimeMillis()
                    if (state.hotColdTrend == HotColdTrend.MAKIN_PANAS && (now - lastVibrateTime > 400L)) {
                        vibratorHelper.vibrateShortPulse(35L)
                        lastVibrateTime = now
                    }
                }

                _uiState.update {
                    it.copy(
                        liveSensor = state,
                        recentSamples = samplesWindow.toList(),
                        isVibrationActive = preferencesRepository.isVibrationEnabled.value
                    )
                }
            }
        }
    }

    fun openSelectCaseDialog() {
        _uiState.update { it.copy(showSelectCaseDialog = true) }
    }

    fun dismissSelectCaseDialog() {
        _uiState.update { it.copy(showSelectCaseDialog = false) }
    }

    companion object {
        fun provideFactory(
            sensorRepository: SensorRepository,
            caseRepository: CaseRepository,
            preferencesRepository: PreferencesRepository,
            vibratorHelper: VibratorHelper
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SelidikViewModel(
                    sensorRepository,
                    caseRepository,
                    preferencesRepository,
                    vibratorHelper
                ) as T
            }
        }
    }
}
