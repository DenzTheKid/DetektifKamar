package com.detektifkamar.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PreferencesRepository {
    val defaultBaselineUt: StateFlow<Float>
    val isVibrationEnabled: StateFlow<Boolean>
    val themeMode: StateFlow<Int> // 0: Sistem, 1: Terang, 2: Gelap
    val isSimulationMode: StateFlow<Boolean>
    val hasShownGuide: StateFlow<Boolean>

    suspend fun setDefaultBaselineUt(value: Float)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setThemeMode(mode: Int)
    suspend fun setSimulationMode(enabled: Boolean)
    suspend fun setHasShownGuide(shown: Boolean)
}

class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("detektif_kamar_prefs", Context.MODE_PRIVATE)

    private val _defaultBaselineUt = MutableStateFlow(prefs.getFloat(KEY_BASELINE, 45.0f))
    override val defaultBaselineUt: StateFlow<Float> = _defaultBaselineUt.asStateFlow()

    private val _isVibrationEnabled = MutableStateFlow(prefs.getBoolean(KEY_VIBRATION, true))
    override val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getInt(KEY_THEME, 0))
    override val themeMode: StateFlow<Int> = _themeMode.asStateFlow()

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
    }

    override suspend fun setSimulationMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SIMULATION, enabled).apply()
        _isSimulationMode.value = enabled
    }

    override suspend fun setHasShownGuide(shown: Boolean) {
        prefs.edit().putBoolean(KEY_SHOWN_GUIDE, shown).apply()
        _hasShownGuide.value = shown
    }

    companion object {
        private const val KEY_BASELINE = "key_baseline_ut"
        private const val KEY_VIBRATION = "key_vibration"
        private const val KEY_THEME = "key_theme_mode"
        private const val KEY_SIMULATION = "key_simulation_mode"
        private const val KEY_SHOWN_GUIDE = "key_has_shown_guide"
    }
}
