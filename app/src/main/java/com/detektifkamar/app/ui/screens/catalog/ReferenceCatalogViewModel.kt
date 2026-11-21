package com.detektifkamar.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.domain.model.DeviceCategory
import com.detektifkamar.app.domain.model.DeviceReference
import com.detektifkamar.app.domain.repository.DeviceReferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CatalogUiState(
    val searchQuery: String = "",
    val selectedCategory: DeviceCategory? = null,
    val devices: List<DeviceReference> = emptyList(),
    val selectedDeviceDetail: DeviceReference? = null
)

class ReferenceCatalogViewModel(
    private val deviceReferenceRepository: DeviceReferenceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<DeviceCategory?>(null)
    private val _selectedDeviceDetail = MutableStateFlow<DeviceReference?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CatalogUiState> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category ->
        Pair(query, category)
    }.flatMapLatest { (query, category) ->
        deviceReferenceRepository.getReferencesFlow(query, category).combine(_selectedDeviceDetail) { list, detail ->
            CatalogUiState(
                searchQuery = query,
                selectedCategory = category,
                devices = list,
                selectedDeviceDetail = detail
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CatalogUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: DeviceCategory?) {
        _selectedCategory.value = category
    }

    fun onDeviceClicked(device: DeviceReference) {
        _selectedDeviceDetail.value = device
    }

    fun closeDetailDialog() {
        _selectedDeviceDetail.value = null
    }
}
