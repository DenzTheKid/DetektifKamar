package com.detektifkamar.app.domain.repository

import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk entitas Pengukuran titik medan magnet.
 */
interface MeasurementRepository {
    fun getMeasurementsForRoomFlow(roomId: Long): Flow<List<Measurement>>
    fun getAllMeasurementsFlow(): Flow<List<Measurement>>
    fun getFilteredMeasurementsFlow(
        roomId: Long? = null,
        status: MagneticStatus? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<Measurement>>

    suspend fun getMeasurementById(id: Long): Measurement?
    suspend fun insertMeasurement(measurement: Measurement): Long
    suspend fun deleteMeasurement(id: Long)
    suspend fun restoreMeasurement(measurement: Measurement)
}
