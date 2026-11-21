package com.detektifkamar.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.domain.model.UnitType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedUnit: UnitType = UnitType.MICROTESLA,
    val themeMode: Int = 0, // 0: Sistem, 1: Terang, 2: Gelap
    val isVibrationEnabled: Boolean = true,
    val isSimulationMode: Boolean = false,
    val isResetConfirmDialogVisible: Boolean = false,
    val isResetSuccess: Boolean = false
)

private data class SettingsDialogState(
    val isConfirmVisible: Boolean = false,
    val isSuccess: Boolean = false
)

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _isResetConfirmVisible = MutableStateFlow(false)
    private val _isResetSuccess = MutableStateFlow(false)

    private val dialogStateFlow = combine(
        _isResetConfirmVisible,
        _isResetSuccess
    ) { confirm, success ->
        SettingsDialogState(confirm, success)
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.selectedUnitFlow,
        preferencesRepository.themeMode,
        preferencesRepository.isVibrationEnabled,
        preferencesRepository.isSimulationMode,
        dialogStateFlow
    ) { unit, theme, vibration, simulation, dialog ->
        SettingsUiState(
            selectedUnit = unit,
            themeMode = theme,
            isVibrationEnabled = vibration,
            isSimulationMode = simulation,
            isResetConfirmDialogVisible = dialog.isConfirmVisible,
            isResetSuccess = dialog.isSuccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun onUnitSelected(unit: UnitType) {
        viewModelScope.launch {
            preferencesRepository.setSelectedUnit(unit)
        }
    }

    fun onThemeSelected(theme: Int) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(theme)
        }
    }

    fun onVibrationToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setVibrationEnabled(enabled)
        }
    }

    fun onSimulationModeToggled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSimulationMode(enabled)
        }
    }

    fun showResetConfirmDialog() {
        _isResetConfirmVisible.value = true
    }

    fun hideResetConfirmDialog() {
        _isResetConfirmVisible.value = false
    }

    fun resetAllData() {
        viewModelScope.launch {
            preferencesRepository.resetAllData()
            _isResetConfirmVisible.value = false
            _isResetSuccess.value = true
        }
    }

    fun clearResetSuccess() {
        _isResetSuccess.value = false
    }
}
