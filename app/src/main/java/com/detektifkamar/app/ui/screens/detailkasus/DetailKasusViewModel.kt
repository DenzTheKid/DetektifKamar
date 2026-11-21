package com.detektifkamar.app.ui.screens.detailkasus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.local.CaseSummary
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.data.repository.PreferencesRepository
import com.detektifkamar.app.data.repository.SuspectRepository
import com.detektifkamar.app.domain.logic.HeatmapGrid
import com.detektifkamar.app.domain.logic.IdwInterpolator
import com.detektifkamar.app.domain.logic.InvestigationReportFormatter
import com.detektifkamar.app.domain.logic.SamplePoint
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePointWithReading
import com.detektifkamar.app.domain.model.Furniture
import com.detektifkamar.app.domain.model.InvestigationCase
import com.detektifkamar.app.domain.model.Suspect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class MapLayer {
    MAGNET,
    CAHAYA,
    POLOS
}

data class DetailKasusUiState(
    val currentTab: Int = 0, // 0: Denah, 1: Bukti, 2: Laporan
    val activeLayer: MapLayer = MapLayer.MAGNET,
    val case: InvestigationCase? = null,
    val furnitureList: List<Furniture> = emptyList(),
    val pointsWithReadings: List<EvidencePointWithReading> = emptyList(),
    val selectedPoint: EvidencePointWithReading? = null,
    val magneticHeatmap: HeatmapGrid? = null,
    val lightHeatmap: HeatmapGrid? = null,
    val hasLightData: Boolean = false,
    val summary: CaseSummary = CaseSummary(0, 0f, 0f, DisturbanceCategory.TENANG),
    val allLinkedSuspects: List<Suspect> = emptyList(),
    val showAddFurnitureDialog: Boolean = false,
    val showNewPointDialog: Boolean = false,
    val pendingNewPointCoords: Pair<Float, Float>? = null,
    val formattedReportText: String = "",
    val isLoading: Boolean = true
)

class DetailKasusViewModel(
    private val caseId: Long,
    private val caseRepository: CaseRepository,
    private val suspectRepository: SuspectRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailKasusUiState())
    val uiState: StateFlow<DetailKasusUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                caseRepository.getCaseFlow(caseId),
                caseRepository.getFurnitureForCase(caseId),
                caseRepository.getPointsForCase(caseId, null),
                preferencesRepository.defaultBaselineUt
            ) { case, furniture, points, defaultBaseline ->
                processCaseData(case, furniture, points, defaultBaseline)
            }.collect { newState ->
                _uiState.update { current ->
                    newState.copy(
                        currentTab = current.currentTab,
                        activeLayer = current.activeLayer,
                        selectedPoint = if (current.selectedPoint != null) {
                            newState.pointsWithReadings.find { it.point.id == current.selectedPoint.point.id }
                        } else null,
                        showAddFurnitureDialog = current.showAddFurnitureDialog,
                        showNewPointDialog = current.showNewPointDialog,
                        pendingNewPointCoords = current.pendingNewPointCoords
                    )
                }
            }
        }
    }

    private suspend fun processCaseData(
        case: InvestigationCase?,
        furniture: List<Furniture>,
        pointsList: List<EvidencePointWithReading>,
        defaultBaseline: Float
    ): DetailKasusUiState = withContext(Dispatchers.Default) {
        val effectiveBaseline = case?.baselineUt ?: defaultBaseline

        val pointsWithDetails = pointsList.map { p ->
            val suspects = suspectRepository.getSuspectsForPoint(p.point.id)
            val customLabel = suspectRepository.getCustomLabelForPoint(p.point.id)
            p.copy(
                linkedSuspects = suspects,
                customSuspectLabel = customLabel
            )
        }

        val summary = if (case != null) {
            caseRepository.getCaseSummary(caseId, effectiveBaseline)
        } else CaseSummary(0, 0f, 0f, DisturbanceCategory.TENANG)

        val magneticSamples = pointsWithDetails.mapNotNull { p ->
            val delta = p.deltaUt
            if (delta != null) SamplePoint(p.point.xM, p.point.yM, delta) else null
        }

        val lightSamples = pointsWithDetails.mapNotNull { p ->
            val lux = p.latestReading?.luxMean
            if (lux != null) SamplePoint(p.point.xM, p.point.yM, lux) else null
        }

        val magGrid = if (case != null) {
            IdwInterpolator.computeGrid(case.widthM, case.lengthM, magneticSamples)
        } else null

        val lightGrid = if (case != null && lightSamples.size >= 3) {
            IdwInterpolator.computeGrid(case.widthM, case.lengthM, lightSamples)
        } else null

        val allSuspects = pointsWithDetails.flatMap { it.linkedSuspects }.distinctBy { it.id }

        val reportText = if (case != null) {
            InvestigationReportFormatter.formatReport(case, pointsWithDetails)
        } else ""

        DetailKasusUiState(
            case = case,
            furnitureList = furniture,
            pointsWithReadings = pointsWithDetails,
            magneticHeatmap = magGrid,
            lightHeatmap = lightGrid,
            hasLightData = lightSamples.isNotEmpty(),
            summary = summary,
            allLinkedSuspects = allSuspects,
            formattedReportText = reportText,
            isLoading = false
        )
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(currentTab = tabIndex) }
    }

    fun selectLayer(layer: MapLayer) {
        _uiState.update { it.copy(activeLayer = layer) }
    }

    fun selectPoint(point: EvidencePointWithReading?) {
        _uiState.update { it.copy(selectedPoint = point) }
    }

    fun onCanvasTapped(meterX: Float, meterY: Float) {
        _uiState.update {
            it.copy(
                pendingNewPointCoords = Pair(meterX, meterY),
                showNewPointDialog = true
            )
        }
    }

    fun dismissNewPointDialog() {
        _uiState.update {
            it.copy(
                showNewPointDialog = false,
                pendingNewPointCoords = null
            )
        }
    }

    fun openAddFurnitureDialog() {
        _uiState.update { it.copy(showAddFurnitureDialog = true) }
    }

    fun dismissAddFurnitureDialog() {
        _uiState.update { it.copy(showAddFurnitureDialog = false) }
    }

    fun addFurniture(label: String, kind: String, xM: Float, yM: Float, wM: Float, hM: Float) {
        val currentCase = _uiState.value.case ?: return
        val clampedX = xM.coerceIn(0f, currentCase.widthM - 0.2f)
        val clampedY = yM.coerceIn(0f, currentCase.lengthM - 0.2f)
        val clampedW = wM.coerceIn(0.2f, currentCase.widthM - clampedX)
        val clampedH = hM.coerceIn(0.2f, currentCase.lengthM - clampedY)

        viewModelScope.launch {
            caseRepository.addFurniture(
                Furniture(
                    caseId = caseId,
                    label = label.trim().ifBlank { "Perabot" },
                    kind = kind,
                    xM = clampedX,
                    yM = clampedY,
                    wM = clampedW,
                    hM = clampedH
                )
            )
            dismissAddFurnitureDialog()
        }
    }

    fun deletePoint(pointId: Long) {
        viewModelScope.launch {
            caseRepository.deletePoint(pointId)
            _uiState.update { it.copy(selectedPoint = null) }
        }
    }

    fun deleteFurniture(furnitureId: Long) {
        viewModelScope.launch {
            caseRepository.deleteFurniture(furnitureId)
        }
    }

    companion object {
        fun provideFactory(
            caseId: Long,
            caseRepository: CaseRepository,
            suspectRepository: SuspectRepository,
            preferencesRepository: PreferencesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DetailKasusViewModel(
                    caseId,
                    caseRepository,
                    suspectRepository,
                    preferencesRepository
                ) as T
            }
        }
    }
}
