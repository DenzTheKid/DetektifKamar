package com.detektifkamar.app.ui.screens.roomdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomDetailUiState(
    val isLoading: Boolean = false,
    val room: Room? = null,
    val measurements: List<Measurement> = emptyList(),
    val averageUt: Float? = null,
    val maxUt: Float? = null,
    val minUt: Float? = null,
    val selectedMeasurement: Measurement? = null
)

class RoomDetailViewModel(
    private val roomId: Long,
    private val roomRepository: RoomRepository,
    private val measurementRepository: MeasurementRepository
) : ViewModel() {

    private val _selectedPoint = MutableStateFlow<Measurement?>(null)

    val uiState: StateFlow<RoomDetailUiState> = combine(
        roomRepository.getAllRoomsFlow(),
        measurementRepository.getMeasurementsForRoomFlow(roomId),
        _selectedPoint
    ) { allRooms, measurements, selected ->
        val currentRoom = allRooms.firstOrNull { it.id == roomId }

        val avg = if (measurements.isNotEmpty()) measurements.map { it.magnitude }.average().toFloat() else null
        val max = measurements.maxOfOrNull { it.magnitude }
        val min = measurements.minOfOrNull { it.magnitude }

        RoomDetailUiState(
            isLoading = false,
            room = currentRoom,
            measurements = measurements,
            averageUt = avg,
            maxUt = max,
            minUt = min,
            selectedMeasurement = selected ?: measurements.firstOrNull()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RoomDetailUiState(isLoading = true)
    )

    fun onPointSelected(measurement: Measurement) {
        _selectedPoint.value = measurement
    }

    fun deleteMeasurement(measurementId: Long) {
        viewModelScope.launch {
            measurementRepository.deleteMeasurement(measurementId)
        }
    }
}
