package com.detektifkamar.app.domain.repository

import com.detektifkamar.app.domain.model.Room
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak repositori untuk entitas Ruangan.
 */
interface RoomRepository {
    fun getAllRoomsFlow(): Flow<List<Room>>
    suspend fun getRoomById(id: Long): Room?
    suspend fun insertRoom(room: Room): Long
    suspend fun updateRoom(room: Room)
    suspend fun deleteRoom(id: Long)
}
