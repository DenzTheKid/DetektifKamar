package com.detektifkamar.app.domain.repository

import com.detektifkamar.app.domain.model.DeviceCategory
import com.detektifkamar.app.domain.model.DeviceReference
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk katalog referensi perangkat sumber medan magnet.
 */
interface DeviceReferenceRepository {
    fun getReferencesFlow(
        searchQuery: String = "",
        category: DeviceCategory? = null
    ): Flow<List<DeviceReference>>

    suspend fun getReferenceById(id: Long): DeviceReference?
}
