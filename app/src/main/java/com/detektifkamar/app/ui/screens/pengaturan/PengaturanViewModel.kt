package com.detektifkamar.app.ui.screens.pengaturan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PengaturanUiState(
    val defaultBaselineUt: Float = 45.0f,
    val isVibrationEnabled: Boolean = true,
    val themeMode: Int = 0, // 0: Sistem, 1: Terang, 2: Gelap
    val isSimulationMode: Boolean = false,
    val showGuideDialog: Boolean = false
)

class PengaturanViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _showGuide = MutableStateFlow(false)

    val uiState: StateFlow<PengaturanUiState> = combine(
        preferencesRepository.defaultBaselineUt,
        preferencesRepository.isVibrationEnabled,
        preferencesRepository.themeMode,
        preferencesRepository.isSimulationMode,
        _showGuide
    ) { baseline, vibration, theme, sim, guide ->
        PengaturanUiState(
            defaultBaselineUt = baseline,
            isVibrationEnabled = vibration,
            themeMode = theme,
            isSimulationMode = sim,
            showGuideDialog = guide
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = PengaturanUiState()
    )

    init {
        // Cek apakah panduan belum pernah ditampilkan saat pertama kali pasang aplikasi
        viewModelScope.launch {
            if (!preferencesRepository.hasShownGuide.value) {
                _showGuide.value = true
                preferencesRepository.setHasShownGuide(true)
            }
        }
    }

    fun setDefaultBaseline(value: Float) {
        viewModelScope.launch {
            preferencesRepository.setDefaultBaselineUt(value)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVibrationEnabled(enabled)
        }
    }

    fun setThemeMode(mode: Int) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setSimulationMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSimulationMode(enabled)
        }
    }

    fun openGuideDialog() {
        _showGuide.value = true
    }

    fun dismissGuideDialog() {
        _showGuide.value = false
    }

    companion object {
        fun provideFactory(preferencesRepository: PreferencesRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PengaturanViewModel(preferencesRepository) as T
                }
            }
    }
}
