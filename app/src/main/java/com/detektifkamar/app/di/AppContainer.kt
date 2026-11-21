package com.detektifkamar.app.di

import android.content.Context
import com.detektifkamar.app.data.local.CalibrationDao
import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.data.local.DetektifDbHelper
import com.detektifkamar.app.data.local.DeviceReferenceDao
import com.detektifkamar.app.data.local.MeasurementDao
import com.detektifkamar.app.data.local.RoomDao
import com.detektifkamar.app.data.repository.CalibrationRepositoryImpl
import com.detektifkamar.app.data.repository.DeviceReferenceRepositoryImpl
import com.detektifkamar.app.data.repository.MeasurementRepositoryImpl
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.PreferencesRepositoryImpl
import com.detektifkamar.app.data.repository.RoomRepositoryImpl
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.repository.SensorRepositoryImpl
import com.detektifkamar.app.data.sensor.RealSensorSource
import com.detektifkamar.app.data.sensor.SimulationSensorSource
import com.detektifkamar.app.data.sensor.VibratorHelper
import com.detektifkamar.app.domain.repository.CalibrationRepository
import com.detektifkamar.app.domain.repository.DeviceReferenceRepository
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository

/**
 * Kontrak Dependency Injection manual tingkat aplikasi.
 * Tanpa framework pihak ketiga (Hilt/Koin), menjaga dependensi bersih dan transparan.
 */
interface AppContainer {
    val dbHelper: DetektifDbHelper
    val databaseChangeNotifier: DatabaseChangeNotifier
    val roomRepository: RoomRepository
    val measurementRepository: MeasurementRepository
    val calibrationRepository: CalibrationRepository
    val deviceReferenceRepository: DeviceReferenceRepository
    val preferencesRepository: PreferencesRepository
    val sensorRepository: SensorRepository
    val vibratorHelper: VibratorHelper
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val dbHelper: DetektifDbHelper by lazy {
        DetektifDbHelper(context)
    }

    override val databaseChangeNotifier: DatabaseChangeNotifier by lazy {
        DatabaseChangeNotifier()
    }

    private val roomDao by lazy {
        RoomDao(dbHelper)
    }

    private val measurementDao by lazy {
        MeasurementDao(dbHelper)
    }

    private val calibrationDao by lazy {
        CalibrationDao(dbHelper)
    }

    private val deviceReferenceDao by lazy {
        DeviceReferenceDao(dbHelper)
    }

    override val roomRepository: RoomRepository by lazy {
        RoomRepositoryImpl(roomDao, databaseChangeNotifier)
    }

    override val measurementRepository: MeasurementRepository by lazy {
        MeasurementRepositoryImpl(measurementDao, databaseChangeNotifier)
    }

    override val calibrationRepository: CalibrationRepository by lazy {
        CalibrationRepositoryImpl(calibrationDao, databaseChangeNotifier)
    }

    override val deviceReferenceRepository: DeviceReferenceRepository by lazy {
        DeviceReferenceRepositoryImpl(deviceReferenceDao)
    }

    override val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(context, dbHelper)
    }

    private val realSensorSource by lazy {
        RealSensorSource(context)
    }

    private val simulationSensorSource by lazy {
        SimulationSensorSource()
    }

    override val sensorRepository: SensorRepository by lazy {
        SensorRepositoryImpl(
            realSensorSource = realSensorSource,
            simulationSensorSource = simulationSensorSource,
            preferencesRepository = preferencesRepository,
            calibrationRepository = calibrationRepository
        )
    }

    override val vibratorHelper: VibratorHelper by lazy {
        VibratorHelper(context)
    }
}
