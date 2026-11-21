package com.detektifkamar.app.ui.screens.ukurtitik

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SensorRepository
import com.detektifkamar.app.data.sensor.LiveSensorState
import com.detektifkamar.app.domain.logic.AzimuthUtils
import com.detektifkamar.app.domain.logic.DisturbanceClassifier
import com.detektifkamar.app.domain.logic.StabilityDetector
import com.detektifkamar.app.domain.logic.StatisticsUtils
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePoint
import com.detektifkamar.app.domain.model.PointReading
import com.detektifkamar.app.domain.model.ReadingQuality
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface MeasurementSessionState {
    object Idle : MeasurementSessionState
    object MenungguStabil : MeasurementSessionState
    data class Mengambil(val progress: Float) : MeasurementSessionState
    data class Selesai(
        val reading: PointReading,
        val deltaUt: Float,
        val category: DisturbanceCategory
    ) : MeasurementSessionState
    data class Gagal(val alasan: String) : MeasurementSessionState
}

data class UkurTitikUiState(
    val caseId: Long = 0L,
    val pointId: Long = 0L,
    val isBaselineMode: Boolean = false,
    val pointX: Float = 0f,
    val pointY: Float = 0f,
    val pointLabel: String = "",
    val liveSensor: LiveSensorState = LiveSensorState(),
    val sessionState: MeasurementSessionState = MeasurementSessionState.Idle,
    val baselineUt: Float = 45.0f,
    val saveCompleted: Boolean = false
)

class UkurTitikViewModel(
    private val caseId: Long,
    private val pointId: Long,
    private val isBaselineMode: Boolean,
    private val pointX: Float,
    private val pointY: Float,
    private val pointLabel: String,
    private val caseRepository: CaseRepository,
    private val sensorRepository: SensorRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UkurTitikUiState(
            caseId = caseId,
            pointId = pointId,
            isBaselineMode = isBaselineMode,
            pointX = pointX,
            pointY = pointY,
            pointLabel = pointLabel
        )
    )
    val uiState: StateFlow<UkurTitikUiState> = _uiState.asStateFlow()

    private var measurementJob: Job? = null

    init {
        // Ambil data acuan kasus
        viewModelScope.launch {
            val case = caseRepository.getCaseById(caseId)
            val baseline = case?.baselineUt ?: preferencesRepository.defaultBaselineUt.value
            _uiState.update { it.copy(baselineUt = baseline) }
        }

        // Langganan sensor live
        viewModelScope.launch {
            sensorRepository.getLiveSensorState().collect { liveState ->
                _uiState.update { it.copy(liveSensor = liveState) }
            }
        }
    }

    fun startMeasurementSession() {
        if (measurementJob?.isActive == true) return

        measurementJob = viewModelScope.launch {
            val liveState = _uiState.value.liveSensor
            if (!liveState.isStable) {
                _uiState.update { it.copy(sessionState = MeasurementSessionState.MenungguStabil) }
                return@launch
            }

            // Sesi pengambilan data selama 4 detik (40 langkah interval 100ms)
            val durationMs = 4000L
            val intervalMs = 100L
            val totalSteps = (durationMs / intervalMs).toInt()

            val magSamples = mutableListOf<Float>()
            val bxSamples = mutableListOf<Float>()
            val bySamples = mutableListOf<Float>()
            val bzSamples = mutableListOf<Float>()
            val luxSamples = mutableListOf<Float>()
            val azimuthSamples = mutableListOf<Float>()
            val stabilitySamples = mutableListOf<Boolean>()
            var lowestAccuracy = 3

            for (step in 1..totalSteps) {
                delay(intervalMs)
                val current = _uiState.value.liveSensor
                magSamples.add(current.magneticUt)
                bxSamples.add(current.bx)
                bySamples.add(current.by)
                bzSamples.add(current.bz)
                current.lux?.let { luxSamples.add(it) }
                current.azimuthDeg?.let { azimuthSamples.add(it) }
                stabilitySamples.add(current.isStable)
                if (current.accuracy < lowestAccuracy) {
                    lowestAccuracy = current.accuracy
                }

                val progress = step.toFloat() / totalSteps
                _uiState.update { it.copy(sessionState = MeasurementSessionState.Mengambil(progress)) }
            }

            // Hitung Metrik Statistik Sesi
            val meanUt = StatisticsUtils.calculateMean(magSamples)
            val minUt = StatisticsUtils.calculateMin(magSamples)
            val maxUt = StatisticsUtils.calculateMax(magSamples)
            val stdUt = StatisticsUtils.calculateStdDev(magSamples, meanUt)
            val meanBx = StatisticsUtils.calculateMean(bxSamples)
            val meanBy = StatisticsUtils.calculateMean(bySamples)
            val meanBz = StatisticsUtils.calculateMean(bzSamples)
            val meanLux = if (luxSamples.isNotEmpty()) StatisticsUtils.calculateMean(luxSamples) else null
            val meanAzimuth = if (azimuthSamples.isNotEmpty()) AzimuthUtils.circularMean(azimuthSamples) else null

            // Evaluasi Kualitas Pembacaan
            val unstableRatio = StabilityDetector.calculateUnstableRatio(stabilitySamples)
            val qualityList = mutableListOf<String>()
            if (unstableRatio > StabilityDetector.UNSTABLE_RATIO_REJECT_THRESHOLD) {
                qualityList.add(ReadingQuality.BERGERAK.value)
            }
            if (lowestAccuracy < 2) {
                qualityList.add(ReadingQuality.AKURASI_SENSOR_RENDAH.value)
            }
            if (stdUt > 2.0f) {
                qualityList.add(ReadingQuality.DERAU_TINGGI.value)
            }
            val qualityString = if (qualityList.isEmpty()) ReadingQuality.BAIK.value else qualityList.joinToString(", ")

            val baseline = _uiState.value.baselineUt
            val delta = DisturbanceClassifier.calculateDelta(meanUt, baseline)
            val category = DisturbanceClassifier.classify(delta)

            val calculatedReading = PointReading(
                pointId = pointId,
                meanUt = meanUt,
                minUt = minUt,
                maxUt = maxUt,
                stdUt = stdUt,
                bx = meanBx,
                by = meanBy,
                bz = meanBz,
                luxMean = meanLux,
                azimuthDeg = meanAzimuth,
                tiltDeg = _uiState.value.liveSensor.tiltDeg,
                accuracy = lowestAccuracy,
                quality = qualityString,
                sampleCount = magSamples.size,
                measuredAt = System.currentTimeMillis()
            )

            _uiState.update {
                it.copy(
                    sessionState = MeasurementSessionState.Selesai(
                        reading = calculatedReading,
                        deltaUt = delta,
                        category = category
                    )
                )
            }
        }
    }

    fun resetSession() {
        measurementJob?.cancel()
        _uiState.update { it.copy(sessionState = MeasurementSessionState.Idle) }
    }

    fun saveResult() {
        val session = _uiState.value.sessionState
        if (session !is MeasurementSessionState.Selesai) return

        viewModelScope.launch {
            if (isBaselineMode) {
                // Simpan sebagai nilai acuan kasus
                caseRepository.updateCaseBaseline(caseId, session.reading.meanUt)
            } else {
                val targetPointId = if (pointId > 0L) {
                    pointId
                } else {
                    // Buat titik baru di database terlebih dahulu
                    caseRepository.addPoint(
                        EvidencePoint(
                            caseId = caseId,
                            label = pointLabel.ifBlank { "Titik Baru" },
                            xM = pointX,
                            yM = pointY
                        )
                    )
                }

                // Simpan hasil pembacaan
                val readingToSave = session.reading.copy(pointId = targetPointId)
                caseRepository.addReading(readingToSave)
            }

            _uiState.update { it.copy(saveCompleted = true) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        measurementJob?.cancel()
    }

    companion object {
        fun provideFactory(
            caseId: Long,
            pointId: Long,
            isBaselineMode: Boolean,
            pointX: Float,
            pointY: Float,
            pointLabel: String,
            caseRepository: CaseRepository,
            sensorRepository: SensorRepository,
            preferencesRepository: PreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UkurTitikViewModel(
                    caseId,
                    pointId,
                    isBaselineMode,
                    pointX,
                    pointY,
                    pointLabel,
                    caseRepository,
                    sensorRepository,
                    preferencesRepository
                ) as T
            }
        }
    }
}
