package com.detektifkamar.app.data.repository

import com.detektifkamar.app.data.local.DatabaseChangeNotifier
import com.detektifkamar.app.data.local.RoomDao
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.repository.RoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class RoomRepositoryImpl(
    private val roomDao: RoomDao,
    private val changeNotifier: DatabaseChangeNotifier
) : RoomRepository {

    override fun getAllRoomsFlow(): Flow<List<Room>> {
        return changeNotifier.changeEvents.map {
            withContext(Dispatchers.IO) {
                roomDao.getAllRooms()
            }
        }
    }

    override suspend fun getRoomById(id: Long): Room? {
        return withContext(Dispatchers.IO) {
            roomDao.getRoomById(id)
        }
    }

    override suspend fun insertRoom(room: Room): Long {
        return withContext(Dispatchers.IO) {
            val id = roomDao.insertRoom(room)
            changeNotifier.notifyChange()
            id
        }
    }

    override suspend fun updateRoom(room: Room) {
        withContext(Dispatchers.IO) {
            roomDao.updateRoom(room)
            changeNotifier.notifyChange()
        }
    }

    override suspend fun deleteRoom(id: Long) {
        withContext(Dispatchers.IO) {
            roomDao.deleteRoom(id)
            changeNotifier.notifyChange()
        }
    }
}
