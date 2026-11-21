package com.detektifkamar.app.domain.repository

import com.detektifkamar.app.domain.model.CalibrationData
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk penyimpanan nilai kalibrasi magnetometer.
 */
interface CalibrationRepository {
    fun getCalibrationFlow(): Flow<CalibrationData>
    suspend fun saveCalibration(data: CalibrationData)
    suspend fun resetCalibration()
}
