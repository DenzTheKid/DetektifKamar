package com.detektifkamar.app.ui.screens.tersangka

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.SuspectRepository
import com.detektifkamar.app.domain.model.Suspect
import com.detektifkamar.app.domain.model.SuspectCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class TersangkaUiState(
    val searchQuery: String = "",
    val selectedCategory: SuspectCategory? = null,
    val suspects: List<Suspect> = emptyList(),
    val selectedSuspectDetail: Suspect? = null
)

class TersangkaViewModel(
    private val suspectRepository: SuspectRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<SuspectCategory?>(null)
    private val _selectedDetail = MutableStateFlow<Suspect?>(null)

    val uiState: StateFlow<TersangkaUiState> = combine(
        _searchQuery,
        _selectedCategory,
        _selectedDetail
    ) { query, category, selectedDetail ->
        Triple(query, category, selectedDetail)
    }.combine(suspectRepository.getAllSuspects(null, null)) { triple, allList ->
        val (query, category, selectedDetail) = triple
        val filtered = allList.filter { s ->
            val matchQuery = query.isBlank() || s.name.contains(query, ignoreCase = true) || s.note.contains(query, ignoreCase = true)
            val matchCat = category == null || s.category == category
            matchQuery && matchCat
        }
        TersangkaUiState(
            searchQuery = query,
            selectedCategory = category,
            suspects = filtered,
            selectedSuspectDetail = selectedDetail
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3000),
        initialValue = TersangkaUiState()
    )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onCategorySelected(category: SuspectCategory?) {
        _selectedCategory.value = category
    }

    fun selectSuspectForDetail(suspect: Suspect?) {
        _selectedDetail.value = suspect
    }

    companion object {
        fun provideFactory(suspectRepository: SuspectRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TersangkaViewModel(suspectRepository) as T
                }
            }
    }
}
