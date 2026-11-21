package com.detektifkamar.app.ui.screens.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.domain.logic.CsvExporter
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.repository.MeasurementRepository
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HistoryUiState(
    val isLoading: Boolean = false,
    val measurements: List<Measurement> = emptyList(),
    val rooms: List<Room> = emptyList(),
    val roomMap: Map<Long, Room> = emptyMap(),
    val selectedRoomId: Long? = null,
    val selectedStatus: MagneticStatus? = null,
    val recentlyDeletedMeasurement: Measurement? = null,
    val isExportSuccess: Boolean = false,
    val exportErrorMessage: String? = null
) {
    val groupedByDay: Map<String, List<Measurement>>
        get() {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L
            val df = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

            return measurements.groupBy { m ->
                val diffDays = (now - m.timestamp) / dayMillis
                when {
                    diffDays == 0L -> "Hari Ini"
                    diffDays == 1L -> "Kemarin"
                    else -> df.format(Date(m.timestamp))
                }
            }
        }
}

private data class HistoryFilterCriteria(
    val roomId: Long? = null,
    val status: MagneticStatus? = null,
    val recentlyDeleted: Measurement? = null,
    val exportStatus: Pair<Boolean, String?> = Pair(false, null)
)

class HistoryViewModel(
    private val measurementRepository: MeasurementRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _selectedRoomId = MutableStateFlow<Long?>(null)
    private val _selectedStatus = MutableStateFlow<MagneticStatus?>(null)
    private val _recentlyDeleted = MutableStateFlow<Measurement?>(null)
    private val _exportStatus = MutableStateFlow<Pair<Boolean, String?>>(Pair(false, null))

    private val filterCriteriaFlow = combine(
        _selectedRoomId,
        _selectedStatus,
        _recentlyDeleted,
        _exportStatus
    ) { roomId, status, deleted, export ->
        HistoryFilterCriteria(
            roomId = roomId,
            status = status,
            recentlyDeleted = deleted,
            exportStatus = export
        )
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        measurementRepository.getAllMeasurementsFlow(),
        roomRepository.getAllRoomsFlow(),
        filterCriteriaFlow
    ) { allMeasurements, allRooms, filter ->
        val roomMap = allRooms.associateBy { it.id }

        val filtered = allMeasurements.filter { m ->
            (filter.roomId == null || m.roomId == filter.roomId) &&
            (filter.status == null || m.status == filter.status)
        }

        HistoryUiState(
            isLoading = false,
            measurements = filtered,
            rooms = allRooms,
            roomMap = roomMap,
            selectedRoomId = filter.roomId,
            selectedStatus = filter.status,
            recentlyDeletedMeasurement = filter.recentlyDeleted,
            isExportSuccess = filter.exportStatus.first,
            exportErrorMessage = filter.exportStatus.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = true)
    )

    fun onRoomFilterSelected(roomId: Long?) {
        _selectedRoomId.value = roomId
    }

    fun onStatusFilterSelected(status: MagneticStatus?) {
        _selectedStatus.value = status
    }

    fun deleteMeasurement(measurement: Measurement) {
        viewModelScope.launch {
            _recentlyDeleted.value = measurement
            measurementRepository.deleteMeasurement(measurement.id)
        }
    }

    fun undoDelete() {
        val toRestore = _recentlyDeleted.value ?: return
        viewModelScope.launch {
            measurementRepository.restoreMeasurement(toRestore)
            _recentlyDeleted.value = null
        }
    }

    fun clearRecentlyDeleted() {
        _recentlyDeleted.value = null
    }

    fun exportToCsvUri(context: Context, destinationUri: Uri) {
        val measurements = uiState.value.measurements
        val roomMap = uiState.value.roomMap

        viewModelScope.launch {
            try {
                val csvContent = withContext(Dispatchers.Default) {
                    CsvExporter.exportToCsv(measurements, roomMap)
                }

                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                        outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }
                }
                _exportStatus.value = Pair(true, null)
            } catch (e: Exception) {
                _exportStatus.value = Pair(false, e.localizedMessage ?: "Gagal mengekspor berkas CSV")
            }
        }
    }

    fun clearExportStatus() {
        _exportStatus.value = Pair(false, null)
    }
}
