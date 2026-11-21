package com.detektifkamar.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.detektifkamar.app.data.local.DetektifDbHelper
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PreferencesRepository : UserPreferencesRepository {
    val defaultBaselineUt: StateFlow<Float>
    val isVibrationEnabled: StateFlow<Boolean>
    val themeMode: StateFlow<Int> // 0: Sistem, 1: Terang, 2: Gelap
    val isSimulationMode: StateFlow<Boolean>
    val hasShownGuide: StateFlow<Boolean>
    val selectedUnitState: StateFlow<UnitType>

    suspend fun setDefaultBaselineUt(value: Float)
    override suspend fun setThemeMode(mode: Int)
    override suspend fun setThemeMode(mode: String)
    suspend fun setSimulationMode(enabled: Boolean)
    suspend fun setHasShownGuide(shown: Boolean)
}

class PreferencesRepositoryImpl(
    private val context: Context,
    private val dbHelper: DetektifDbHelper
) : PreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("detektif_kamar_prefs", Context.MODE_PRIVATE)

    private val _defaultBaselineUt = MutableStateFlow(prefs.getFloat(KEY_BASELINE, 45.0f))
    override val defaultBaselineUt: StateFlow<Float> = _defaultBaselineUt.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    override val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()
    override val vibrationEnabledFlow: Flow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME, 0))
    override val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

    private val _themeModeString = MutableStateFlow(
        when (_themeMode.value) {
            1 -> "LIGHT"
            2 -> "DARK"
            else -> "SYSTEM"
        }
    )
    override val themeModeFlow: Flow<String> = _themeModeString.asStateFlow()

    private val _selectedUnit = MutableStateFlow(
        try {
            UnitType.valueOf(prefs.getString(KEY_UNIT, UnitType.MICROTESLA.name) ?: UnitType.MICROTESLA.name)
        } catch (_: Exception) {
            UnitType.MICROTESLA
        }
    )
    override val selectedUnitState: StateFlow<UnitType> = _selectedUnit.asStateFlow()
    override val selectedUnitFlow: Flow<UnitType> = _selectedUnit.asStateFlow()

    private val _isSimulationMode = MutableStateFlow(prefs.getBoolean(KEY_SIMULATION, false))
    override val isSimulationMode: StateFlow<Boolean> = _isSimulationMode.asStateFlow()

    private val _hasShownGuide = MutableStateFlow(prefs.getBoolean(KEY_SHOWN_GUIDE, false))
    override val hasShownGuide: StateFlow<Boolean> = _hasShownGuide.asStateFlow()

    override suspend fun setDefaultBaselineUt(value: Float) {
        prefs.edit().putFloat(KEY_BASELINE, value).apply()
        _defaultBaselineUt.value = value
    }

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
        _isVibrationEnabled.value = enabled
    }

    override suspend fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME, mode).apply()
        _themeMode.value = mode
        _themeModeString.value = when (mode) {
            1 -> "LIGHT"
            2 -> "DARK"
            else -> "SYSTEM"
        }
    }

    override suspend fun setThemeMode(mode: String) {
        val intMode = when (mode) {
            "LIGHT" -> 1
            "DARK" -> 2
            else -> 0
        }
        setThemeMode(intMode)
    }

    override suspend fun setSelectedUnit(unit: UnitType) {
        prefs.edit().putString(KEY_UNIT, unit.name).apply()
        _selectedUnit.value = unit
    }

    override suspend fun setSimulationMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIMULATION, enabled).apply()
        _isSimulationMode.value = enabled
    }

    override suspend fun setHasShownGuide(shown: Boolean) {
        prefs.edit().putBoolean(KEY_SHOWN_GUIDE, shown).apply()
        _hasShownGuide.value = shown
    }

    override suspend fun resetAllData() {
        val db = dbHelper.writableDatabase
        db.delete("measurements", null, null)
        db.delete("rooms", null, null)
        db.delete("calibration_bias", null, null)
        prefs.edit().clear().apply()
        _defaultBaselineUt.value = 45.0f
        _isVibrationEnabled.value = true
        _themeMode.value = 0
        _themeModeString.value = "SYSTEM"
        _selectedUnit.value = UnitType.MICROTESLA
        _isSimulationMode.value = false
        _hasShownGuide.value = false
    }

    companion object {
        private const val KEY_BASELINE = "default_baseline_ut"
        private const val KEY_VIBRATION = "vibration_enabled"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_UNIT = "selected_unit"
        private const val KEY_SIMULATION = "simulation_mode"
        private const val KEY_SHOWN_GUIDE = "has_shown_guide"
    }
}
