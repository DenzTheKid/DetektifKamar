package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.local.CalibrationDao
import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.repository.CalibrationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class CalibrationRepositoryImpl(
    private val calibrationDao: CalibrationDao,
    private val changeNotifier: DatabaseChangeNotifier
) : CalibrationRepository {

    override fun getCalibrationFlow(): Flow<CalibrationData> {
        return changeNotifier.changeEvents.map {
            withContext(Dispatchers.IO) {
                calibrationDao.getLatestCalibration()
            }
        }
    }

    override suspend fun saveCalibration(data: CalibrationData) {
        withContext(Dispatchers.IO) {
            calibrationDao.saveCalibration(data)
            changeNotifier.notifyChange()
        }
    }

    override suspend fun resetCalibration() {
        withContext(Dispatchers.IO) {
            calibrationDao.clearCalibration()
            changeNotifier.notifyChange()
        }
    }
}
