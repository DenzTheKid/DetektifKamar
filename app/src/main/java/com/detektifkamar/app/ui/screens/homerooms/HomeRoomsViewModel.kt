package com.detektifkamar.app.ui.screens.homerooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeRoomsUiState(
    val isLoading: Boolean = false,
    val rooms: List<Room> = emptyList()
)

class HomeRoomsViewModel(
    private val roomRepository: RoomRepository
) : ViewModel() {

    val uiState: StateFlow<HomeRoomsUiState> = roomRepository.getAllRoomsFlow()
        .map { rooms ->
            HomeRoomsUiState(
                isLoading = false,
                rooms = rooms
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeRoomsUiState(isLoading = true)
        )

    fun deleteRoom(roomId: Long) {
        viewModelScope.launch {
            roomRepository.deleteRoom(roomId)
        }
    }
}
