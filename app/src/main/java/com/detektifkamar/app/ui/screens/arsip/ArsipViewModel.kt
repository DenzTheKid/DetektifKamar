package com.detektifkamar.app.ui.screens.arsip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePointWithReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ArsipUiState(
    val searchQuery: String = "",
    val categoryFilter: DisturbanceCategory? = null,
    val sortByLargestDelta: Boolean = false,
    val points: List<EvidencePointWithReading> = emptyList(),
    val isLoading: Boolean = true
)

class ArsipViewModel(
    private val caseRepository: CaseRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow<DisturbanceCategory?>(null)
    private val _sortByLargest = MutableStateFlow(false)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ArsipUiState> = combine(
        _searchQuery,
        _categoryFilter,
        _sortByLargest
    ) { query, category, sort ->
        Triple(query, category, sort)
    }.flatMapLatest { (query, category, sort) ->
        caseRepository.getAllArchivedPoints(query, category, sort)
    }.combine(_searchQuery) { points, query ->
        ArsipUiState(
            searchQuery = query,
            categoryFilter = _categoryFilter.value,
            sortByLargestDelta = _sortByLargest.value,
            points = points,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = ArsipUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategoryFilterSelected(category: DisturbanceCategory?) {
        _categoryFilter.value = category
    }

    fun toggleSort() {
        _sortByLargest.value = !_sortByLargest.value
    }

    companion object {
        fun provideFactory(caseRepository: CaseRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ArsipViewModel(caseRepository) as T
                }
            }
    }
}
