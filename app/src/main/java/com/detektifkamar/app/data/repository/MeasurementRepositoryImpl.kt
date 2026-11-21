package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.data.local.MeasurementDao
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.repository.MeasurementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MeasurementRepositoryImpl(
    private val measurementDao: MeasurementDao,
    private val changeNotifier: DatabaseChangeNotifier
) : MeasurementRepository {

    override fun getMeasurementsForRoomFlow(roomId: Long): Flow<List<Measurement>> {
        return changeNotifier.changeEvents.map {
            withContext(Dispatchers.IO) {
                measurementDao.getMeasurementsForRoom(roomId)
            }
        }
    }

    override fun getAllMeasurementsFlow(): Flow<List<Measurement>> {
        return changeNotifier.changeEvents.map {
            withContext(Dispatchers.IO) {
                measurementDao.getAllMeasurements()
            }
        }
    }

    override fun getFilteredMeasurementsFlow(
        roomId: Long?,
        status: MagneticStatus?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Measurement>> {
        return changeNotifier.changeEvents.map {
            withContext(Dispatchers.IO) {
                measurementDao.getFilteredMeasurements(roomId, status, startDate, endDate)
            }
        }
    }

    override suspend fun getMeasurementById(id: Long): Measurement? {
        return withContext(Dispatchers.IO) {
            measurementDao.getMeasurementById(id)
        }
    }

    override suspend fun insertMeasurement(measurement: Measurement): Long {
        return withContext(Dispatchers.IO) {
            val id = measurementDao.insertMeasurement(measurement)
            changeNotifier.notifyChange()
            id
        }
    }

    override suspend fun deleteMeasurement(id: Long) {
        withContext(Dispatchers.IO) {
            measurementDao.deleteMeasurement(id)
            changeNotifier.notifyChange()
        }
    }

    override suspend fun restoreMeasurement(measurement: Measurement) {
        withContext(Dispatchers.IO) {
            measurementDao.insertMeasurement(measurement)
            changeNotifier.notifyChange()
        }
    }
}
