package com.detektifkamar.app.ui.screens.roomform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.RoomType
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoomFormUiState(
    val roomId: Long = 0L,
    val name: String = "",
    val nameError: String? = null,
    val selectedType: RoomType = RoomType.KAMAR_TIDUR,
    val widthInput: String = "",
    val widthError: String? = null,
    val lengthInput: String = "",
    val lengthError: String? = null,
    val notes: String = "",
    val isSaving: Boolean = false,
    val isSavedSuccess: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() && nameError == null && widthError == null && lengthError == null
}

class RoomFormViewModel(
    private val roomRepository: RoomRepository,
    private val editRoomId: Long = 0L
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomFormUiState(roomId = editRoomId))
    val uiState: StateFlow<RoomFormUiState> = _uiState.asStateFlow()

    init {
        if (editRoomId > 0L) {
            viewModelScope.launch {
                val room = roomRepository.getRoomById(editRoomId)
                if (room != null) {
                    _uiState.update { current ->
                        current.copy(
                            name = room.name,
                            selectedType = room.roomType,
                            widthInput = room.widthMeters?.toString() ?: "",
                            lengthInput = room.lengthMeters?.toString() ?: "",
                            notes = room.notes
                        )
                    }
                }
            }
        }
    }

    fun onNameChanged(newName: String) {
        val trimmed = if (newName.length > 40) newName.substring(0, 40) else newName
        val error = when {
            trimmed.isBlank() -> "Nama ruangan wajib diisi."
            trimmed.length > 40 -> "Maksimal 40 karakter."
            else -> null
        }
        _uiState.update { it.copy(name = trimmed, nameError = error) }
    }

    fun onTypeSelected(type: RoomType) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun onWidthChanged(input: String) {
        val sanitized = input.replace(',', '.')
        val error = if (sanitized.isNotBlank()) {
            val num = sanitized.toFloatOrNull()
            if (num == null || num <= 0f) "Harus berupa angka positif." else null
        } else null
        _uiState.update { it.copy(widthInput = sanitized, widthError = error) }
    }

    fun onLengthChanged(input: String) {
        val sanitized = input.replace(',', '.')
        val error = if (sanitized.isNotBlank()) {
            val num = sanitized.toFloatOrNull()
            if (num == null || num <= 0f) "Harus berupa angka positif." else null
        } else null
        _uiState.update { it.copy(lengthInput = sanitized, lengthError = error) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun saveRoom() {
        val state = _uiState.value
        if (!state.isValid) return

        val widthM = state.widthInput.toFloatOrNull()
        val lengthM = state.lengthInput.toFloatOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val room = Room(
                id = state.roomId,
                name = state.name.trim(),
                roomType = state.selectedType,
                widthMeters = widthM,
                lengthMeters = lengthM,
                notes = state.notes.trim()
            )

            if (state.roomId > 0L) {
                roomRepository.updateRoom(room)
            } else {
                roomRepository.insertRoom(room)
            }

            _uiState.update { it.copy(isSaving = false, isSavedSuccess = true) }
        }
    }
}
