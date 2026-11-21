package com.detektifkamar.app.ui.screens.kasus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.local.CaseSummary
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.InvestigationCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CaseItemUi(
    val case: InvestigationCase,
    val pointCount: Int,
    val maxDeltaUt: Float,
    val highestCategory: DisturbanceCategory
)

data class KasusUiState(
    val cases: List<CaseItemUi> = emptyList(),
    val isLoading: Boolean = true,
    val isSimulation: Boolean = false
)

class KasusViewModel(
    private val caseRepository: CaseRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val uiState: StateFlow<KasusUiState> = combine(
        caseRepository.getAllCases(),
        preferencesRepository.isSimulationMode,
        preferencesRepository.defaultBaselineUt
    ) { casesList, isSimulation, defaultBaseline ->
        val items = casesList.map { c ->
            val baseline = c.baselineUt ?: defaultBaseline
            val summary = caseRepository.getCaseSummary(c.id, baseline)
            CaseItemUi(
                case = c,
                pointCount = summary.pointCount,
                maxDeltaUt = summary.maxDeltaUt,
                highestCategory = summary.highestCategory
            )
        }
        KasusUiState(
            cases = items,
            isLoading = false,
            isSimulation = isSimulation
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = KasusUiState()
    )

    fun deleteCase(id: Long) {
        viewModelScope.launch {
            caseRepository.deleteCase(id)
        }
    }

    companion object {
        fun provideFactory(
            caseRepository: CaseRepository,
            preferencesRepository: PreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return KasusViewModel(caseRepository, preferencesRepository) as T
            }
        }
    }
}
