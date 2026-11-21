package com.detektifkamar.app.ui.screens.detailtitik

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SuspectRepository
import com.detektifkamar.app.domain.logic.DisturbanceClassifier
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePoint
import com.detektifkamar.app.domain.model.PointReading
import com.detektifkamar.app.domain.model.Suspect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailTitikUiState(
    val point: EvidencePoint? = null,
    val readings: List<PointReading> = emptyList(),
    val latestReading: PointReading? = null,
    val deltaUt: Float = 0f,
    val category: DisturbanceCategory = DisturbanceCategory.TENANG,
    val likelySuspects: List<Suspect> = emptyList(),
    val linkedSuspects: List<Suspect> = emptyList(),
    val customSuspectLabel: String = "",
    val isLoading: Boolean = true
)

class DetailTitikViewModel(
    private val pointId: Long,
    private val caseRepository: CaseRepository,
    private val suspectRepository: SuspectRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailTitikUiState())
    val uiState: StateFlow<DetailTitikUiState> = _uiState.asStateFlow()

    init {
        loadPointDetails()
    }

    private fun loadPointDetails() {
        viewModelScope.launch {
            val point = caseRepository.getPointById(pointId)
            val readings = caseRepository.getReadingsForPoint(pointId)
            val latest = readings.lastOrNull()

            val baseline = if (point != null) {
                val case = caseRepository.getCaseById(point.caseId)
                case?.baselineUt ?: preferencesRepository.defaultBaselineUt.value
            } else preferencesRepository.defaultBaselineUt.value

            val delta = if (latest != null) {
                DisturbanceClassifier.calculateDelta(latest.meanUt, baseline)
            } else 0f

            val category = DisturbanceClassifier.classify(delta)
            val likely = suspectRepository.getLikelySuspectsForDelta(delta)
            val linked = suspectRepository.getSuspectsForPoint(pointId)
            val custom = suspectRepository.getCustomLabelForPoint(pointId).orEmpty()

            _uiState.update {
                it.copy(
                    point = point,
                    readings = readings,
                    latestReading = latest,
                    deltaUt = delta,
                    category = category,
                    likelySuspects = likely,
                    linkedSuspects = linked,
                    customSuspectLabel = custom,
                    isLoading = false
                )
            }
        }
    }

    fun toggleLinkSuspect(suspect: Suspect) {
        viewModelScope.launch {
            val isLinked = _uiState.value.linkedSuspects.any { it.id == suspect.id }
            if (isLinked) {
                suspectRepository.unlinkSuspectFromPoint(pointId, suspect.id)
            } else {
                suspectRepository.linkSuspectToPoint(pointId, suspect.id)
            }
            loadPointDetails()
        }
    }

    fun updateCustomLabel(label: String) {
        viewModelScope.launch {
            suspectRepository.setCustomLabelForPoint(pointId, label.trim().ifBlank { null })
            _uiState.update { it.copy(customSuspectLabel = label) }
        }
    }

    companion object {
        fun provideFactory(
            pointId: Long,
            caseRepository: CaseRepository,
            suspectRepository: SuspectRepository,
            preferencesRepository: PreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailTitikViewModel(
                    pointId,
                    caseRepository,
                    suspectRepository,
                    preferencesRepository
                ) as T
            }
        }
    }
}
