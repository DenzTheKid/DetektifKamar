package com.detektifkamar.app.data.local

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pengganti reaktivitas Room untuk SQLite murni.
 * Memancarkan pembaruan waktu saat ada operasi tulis (INSERT, UPDATE, DELETE)
 * agar Flow di repository dapat memicu pembaruan data secara otomatis.
 */
class DatabaseChangeNotifier {
    private val _changeEvents = MutableStateFlow(System.currentTimeMillis())
    val changeEvents: StateFlow<Long> = _changeEvents.asStateFlow()

    fun notifyChange() {
        _changeEvents.value = System.currentTimeMillis()
    }
}
