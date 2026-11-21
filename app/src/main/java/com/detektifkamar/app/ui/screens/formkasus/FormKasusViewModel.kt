package com.detektifkamar.app.ui.screens.formkasus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.data.repository.CaseRepository
import com.detektifkamar.app.domain.logic.NumberInputParser
import com.detektifkamar.app.domain.model.InvestigationCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormKasusUiState(
    val caseId: Long = 0L,
    val title: String = "",
    val titleError: String? = null,
    val roomType: String = "Kamar tidur",
    val widthInput: String = "3,0",
    val widthError: String? = null,
    val lengthInput: String = "4,0",
    val lengthError: String? = null,
    val note: String = "",
    val isFormValid: Boolean = true,
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false
)

class FormKasusViewModel(
    private val caseRepository: CaseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val caseIdArg: Long = savedStateHandle.get<String>("caseId")?.toLongOrNull() ?: 0L

    private val _uiState = MutableStateFlow(FormKasusUiState(caseId = caseIdArg))
    val uiState: StateFlow<FormKasusUiState> = _uiState.asStateFlow()

    init {
        if (caseIdArg > 0L) {
            viewModelScope.launch {
                val existing = caseRepository.getCaseById(caseIdArg)
                if (existing != null) {
                    _uiState.update {
                        it.copy(
                            title = existing.title,
                            roomType = existing.roomType,
                            widthInput = "%.1f".format(java.util.Locale("id", "ID"), existing.widthM),
                            lengthInput = "%.1f".format(java.util.Locale("id", "ID"), existing.lengthM),
                            note = existing.note.orEmpty()
                        )
                    }
                    validateForm()
                }
            }
        } else {
            validateForm()
        }
    }

    fun onTitleChanged(newTitle: String) {
        val trimmed = if (newTitle.length > 40) newTitle.take(40) else newTitle
        _uiState.update { it.copy(title = trimmed) }
        validateForm()
    }

    fun onRoomTypeSelected(type: String) {
        _uiState.update { it.copy(roomType = type) }
    }

    fun onWidthChanged(input: String) {
        _uiState.update { it.copy(widthInput = input) }
        validateForm()
    }

    fun onLengthChanged(input: String) {
        _uiState.update { it.copy(lengthInput = input) }
        validateForm()
    }

    fun onNoteChanged(newNote: String) {
        _uiState.update { it.copy(note = newNote) }
    }

    private fun validateForm() {
        val current = _uiState.value

        val titleErr = when {
            current.title.isBlank() -> "Judul kasus tidak boleh kosong"
            current.title.length > 40 -> "Maksimal 40 karakter"
            else -> null
        }

        val widthRes = NumberInputParser.parseDimension(current.widthInput)
        val widthErr = widthRes.exceptionOrNull()?.message

        val lengthRes = NumberInputParser.parseDimension(current.lengthInput)
        val lengthErr = lengthRes.exceptionOrNull()?.message

        val isValid = titleErr == null && widthErr == null && lengthErr == null

        _uiState.update {
            it.copy(
                titleError = titleErr,
                widthError = widthErr,
                lengthError = lengthErr,
                isFormValid = isValid
            )
        }
    }

    fun saveCase() {
        validateForm()
        val current = _uiState.value
        if (!current.isFormValid || current.isSaving) return

        val width = NumberInputParser.parseDimension(current.widthInput).getOrNull() ?: 3.0f
        val length = NumberInputParser.parseDimension(current.lengthInput).getOrNull() ?: 4.0f

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            if (current.caseId > 0L) {
                val existing = caseRepository.getCaseById(current.caseId)
                if (existing != null) {
                    val updated = existing.copy(
                        title = current.title.trim(),
                        roomType = current.roomType,
                        widthM = width,
                        lengthM = length,
                        note = current.note.trim().ifBlank { null }
                    )
                    caseRepository.updateCase(updated)
                }
            } else {
                val newCase = InvestigationCase(
                    title = current.title.trim(),
                    roomType = current.roomType,
                    widthM = width,
                    lengthM = length,
                    note = current.note.trim().ifBlank { null },
                    baselineUt = null
                )
                caseRepository.createCase(newCase)
            }

            _uiState.update { it.copy(isSaving = false, saveCompleted = true) }
        }
    }

    companion object {
        fun provideFactory(
            caseRepository: CaseRepository,
            savedStateHandle: SavedStateHandle
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FormKasusViewModel(caseRepository, savedStateHandle) as T
            }
        }
    }
}
