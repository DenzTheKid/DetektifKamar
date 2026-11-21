package com.detektifkamar.app.domain.repository

import com.detektifkamar.app.domain.model.UnitType
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk preferensi pengguna (satuan default, tema, reset data).
 */
interface UserPreferencesRepository {
    val selectedUnitFlow: Flow<UnitType>
    val themeModeFlow: Flow<String> // "SYSTEM", "LIGHT", "DARK"
    val vibrationEnabledFlow: Flow<Boolean>

    suspend fun setSelectedUnit(unit: UnitType)
    suspend fun setThemeMode(mode: Int)
    suspend fun setThemeMode(mode: String)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun resetAllData()
}
