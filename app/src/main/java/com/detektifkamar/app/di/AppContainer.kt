package com.detektifkamar.app.di

import android.content.Context
import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.data.local.DetektifDbHelper
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.data.repository.CaseRepositoryImpl
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.PreferencesRepositoryImpl
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.repository.SensorRepositoryImpl
import com.detektifkamar.app.data.repository.SuspectRepository
import com.detektifkamar.app.data.repository.SuspectRepositoryImpl
import com.detektifkamar.app.data.sensor.RealSensorSource
import com.detektifkamar.app.data.sensor.SimulationSensorSource
import com.detektifkamar.app.data.sensor.VibratorHelper

/**
 * Kontrak Dependency Injection manual tingkat aplikasi.
 * Tanpa framework pihak ketiga (Hilt/Koin), menjaga dependensi seminimal mungkin.
 */
interface AppContainer {
    val dbHelper: DetektifDbHelper
    val databaseChangeNotifier: DatabaseChangeNotifier
    val caseRepository: CaseRepository
    val suspectRepository: SuspectRepository
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

    override val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepositoryImpl(context)
    }

    override val caseRepository: CaseRepository by lazy {
        CaseRepositoryImpl(dbHelper, databaseChangeNotifier)
    }

    override val suspectRepository: SuspectRepository by lazy {
        SuspectRepositoryImpl(dbHelper, databaseChangeNotifier)
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
            preferencesRepository = preferencesRepository
        )
    }

    override val vibratorHelper: VibratorHelper by lazy {
        VibratorHelper(context)
    }
}
