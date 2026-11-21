package com.detektifkamar.app.data.local

import android.content.ContentValues
import android.database.Cursor
import com.detektifkamar.app.data.local.DatabaseContract.MeasurementTable
import com.detektifkamar.app.data.local.DatabaseContract.RoomTable
import com.detektifkamar.app.domain.logic.MagneticClassifier
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.RoomType

class RoomDao(private val dbHelper: DetektifDbHelper) {

    fun getAllRooms(): List<Room> {
        val db = dbHelper.readableDatabase
        // Query ruangan dengan agregasi titik pengukuran langsung via LEFT JOIN
        val query = """
            SELECT 
                r.${RoomTable.COLUMN_ID},
                r.${RoomTable.COLUMN_NAME},
                r.${RoomTable.COLUMN_ROOM_TYPE},
                r.${RoomTable.COLUMN_WIDTH_METERS},
                r.${RoomTable.COLUMN_LENGTH_METERS},
                r.${RoomTable.COLUMN_NOTES},
                r.${RoomTable.COLUMN_CREATED_AT},
                COUNT(m.${MeasurementTable.COLUMN_ID}) AS point_count,
                AVG(m.${MeasurementTable.COLUMN_MAGNITUDE}) AS avg_magnitude,
                MAX(m.${MeasurementTable.COLUMN_MAGNITUDE}) AS max_magnitude
            FROM ${RoomTable.TABLE_NAME} r
            LEFT JOIN ${MeasurementTable.TABLE_NAME} m ON r.${RoomTable.COLUMN_ID} = m.${MeasurementTable.COLUMN_ROOM_ID}
            GROUP BY r.${RoomTable.COLUMN_ID}
            ORDER BY r.${RoomTable.COLUMN_CREATED_AT} DESC
        """.trimIndent()

        val list = mutableListOf<Room>()
        db.rawQuery(query, null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToRoom(cursor))
            }
        }
        return list
    }

    fun getRoomById(id: Long): Room? {
        val db = dbHelper.readableDatabase
        val query = """
            SELECT 
                r.${RoomTable.COLUMN_ID},
                r.${RoomTable.COLUMN_NAME},
                r.${RoomTable.COLUMN_ROOM_TYPE},
                r.${RoomTable.COLUMN_WIDTH_METERS},
                r.${RoomTable.COLUMN_LENGTH_METERS},
                r.${RoomTable.COLUMN_NOTES},
                r.${RoomTable.COLUMN_CREATED_AT},
                COUNT(m.${MeasurementTable.COLUMN_ID}) AS point_count,
                AVG(m.${MeasurementTable.COLUMN_MAGNITUDE}) AS avg_magnitude,
                MAX(m.${MeasurementTable.COLUMN_MAGNITUDE}) AS max_magnitude
            FROM ${RoomTable.TABLE_NAME} r
            LEFT JOIN ${MeasurementTable.TABLE_NAME} m ON r.${RoomTable.COLUMN_ID} = m.${MeasurementTable.COLUMN_ROOM_ID}
            WHERE r.${RoomTable.COLUMN_ID} = ?
            GROUP BY r.${RoomTable.COLUMN_ID}
        """.trimIndent()

        db.rawQuery(query, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToNext()) {
                return cursorToRoom(cursor)
            }
        }
        return null
    }

    fun insertRoom(room: Room): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(RoomTable.COLUMN_NAME, room.name)
            put(RoomTable.COLUMN_ROOM_TYPE, room.roomType.name)
            if (room.widthMeters != null) put(RoomTable.COLUMN_WIDTH_METERS, room.widthMeters) else putNull(RoomTable.COLUMN_WIDTH_METERS)
            if (room.lengthMeters != null) put(RoomTable.COLUMN_LENGTH_METERS, room.lengthMeters) else putNull(RoomTable.COLUMN_LENGTH_METERS)
            put(RoomTable.COLUMN_NOTES, room.notes)
            put(RoomTable.COLUMN_CREATED_AT, room.createdAt)
        }
        return db.insert(RoomTable.TABLE_NAME, null, cv)
    }

    fun updateRoom(room: Room) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(RoomTable.COLUMN_NAME, room.name)
            put(RoomTable.COLUMN_ROOM_TYPE, room.roomType.name)
            if (room.widthMeters != null) put(RoomTable.COLUMN_WIDTH_METERS, room.widthMeters) else putNull(RoomTable.COLUMN_WIDTH_METERS)
            if (room.lengthMeters != null) put(RoomTable.COLUMN_LENGTH_METERS, room.lengthMeters) else putNull(RoomTable.COLUMN_LENGTH_METERS)
            put(RoomTable.COLUMN_NOTES, room.notes)
        }
        db.update(RoomTable.TABLE_NAME, cv, "${RoomTable.COLUMN_ID} = ?", arrayOf(room.id.toString()))
    }

    fun deleteRoom(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(RoomTable.TABLE_NAME, "${RoomTable.COLUMN_ID} = ?", arrayOf(id.toString()))
    }

    private fun cursorToRoom(cursor: Cursor): Room {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(RoomTable.COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(RoomTable.COLUMN_NAME))
        val roomTypeStr = cursor.getString(cursor.getColumnIndexOrThrow(RoomTable.COLUMN_ROOM_TYPE))
        val roomType = try {
            RoomType.valueOf(roomTypeStr)
        } catch (_: Exception) {
            RoomType.fromDisplayName(roomTypeStr)
        }

        val widthCol = cursor.getColumnIndexOrThrow(RoomTable.COLUMN_WIDTH_METERS)
        val widthM = if (cursor.isNull(widthCol)) null else cursor.getFloat(widthCol)

        val lengthCol = cursor.getColumnIndexOrThrow(RoomTable.COLUMN_LENGTH_METERS)
        val lengthM = if (cursor.isNull(lengthCol)) null else cursor.getFloat(lengthCol)

        val notes = cursor.getString(cursor.getColumnIndexOrThrow(RoomTable.COLUMN_NOTES)) ?: ""
        val createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(RoomTable.COLUMN_CREATED_AT))

        val pointCount = cursor.getInt(cursor.getColumnIndexOrThrow("point_count"))
        val avgCol = cursor.getColumnIndexOrThrow("avg_magnitude")
        val avgMagnitude = if (cursor.isNull(avgCol)) null else cursor.getFloat(avgCol)

        val maxCol = cursor.getColumnIndexOrThrow("max_magnitude")
        val maxMagnitude = if (cursor.isNull(maxCol)) null else cursor.getFloat(maxCol)

        val dominantStatus = if (maxMagnitude != null) {
            MagneticClassifier.classify(maxMagnitude)
        } else {
            MagneticStatus.NORMAL
        }

        return Room(
            id = id,
            name = name,
            roomType = roomType,
            widthMeters = widthM,
            lengthMeters = lengthM,
            notes = notes,
            createdAt = createdAt,
            pointCount = pointCount,
            latestAverageUt = avgMagnitude,
            dominantStatus = dominantStatus
        )
    }
}
