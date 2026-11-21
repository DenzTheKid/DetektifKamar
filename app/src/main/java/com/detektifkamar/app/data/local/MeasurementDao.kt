package com.detektifkamar.app.data.local

import android.content.ContentValues
import android.database.Cursor
import com.detektifkamar.app.data.local.DatabaseContract.MeasurementTable
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement

class MeasurementDao(private val dbHelper: DetektifDbHelper) {

    fun getMeasurementsForRoom(roomId: Long): List<Measurement> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Measurement>()
        val selection = "${MeasurementTable.COLUMN_ROOM_ID} = ?"
        val selectionArgs = arrayOf(roomId.toString())

        db.query(
            MeasurementTable.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${MeasurementTable.COLUMN_TIMESTAMP} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToMeasurement(cursor))
            }
        }
        return list
    }

    fun getAllMeasurements(): List<Measurement> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Measurement>()
        db.query(
            MeasurementTable.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "${MeasurementTable.COLUMN_TIMESTAMP} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToMeasurement(cursor))
            }
        }
        return list
    }

    fun getFilteredMeasurements(
        roomId: Long? = null,
        status: MagneticStatus? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): List<Measurement> {
        val db = dbHelper.readableDatabase
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (roomId != null) {
            conditions.add("${MeasurementTable.COLUMN_ROOM_ID} = ?")
            args.add(roomId.toString())
        }
        if (status != null) {
            conditions.add("${MeasurementTable.COLUMN_STATUS} = ?")
            args.add(status.name)
        }
        if (startDate != null) {
            conditions.add("${MeasurementTable.COLUMN_TIMESTAMP} >= ?")
            args.add(startDate.toString())
        }
        if (endDate != null) {
            conditions.add("${MeasurementTable.COLUMN_TIMESTAMP} <= ?")
            args.add(endDate.toString())
        }

        val selection = if (conditions.isNotEmpty()) conditions.joinToString(" AND ") else null
        val selectionArgs = if (args.isNotEmpty()) args.toTypedArray() else null

        val list = mutableListOf<Measurement>()
        db.query(
            MeasurementTable.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${MeasurementTable.COLUMN_TIMESTAMP} DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToMeasurement(cursor))
            }
        }
        return list
    }

    fun getMeasurementById(id: Long): Measurement? {
        val db = dbHelper.readableDatabase
        db.query(
            MeasurementTable.TABLE_NAME,
            null,
            "${MeasurementTable.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToNext()) {
                return cursorToMeasurement(cursor)
            }
        }
        return null
    }

    fun insertMeasurement(m: Measurement): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            if (m.id > 0) put(MeasurementTable.COLUMN_ID, m.id)
            put(MeasurementTable.COLUMN_ROOM_ID, m.roomId)
            put(MeasurementTable.COLUMN_TIMESTAMP, m.timestamp)
            put(MeasurementTable.COLUMN_X, m.x)
            put(MeasurementTable.COLUMN_Y, m.y)
            put(MeasurementTable.COLUMN_Z, m.z)
            put(MeasurementTable.COLUMN_MAGNITUDE, m.magnitude)
            put(MeasurementTable.COLUMN_SENSOR_ACCURACY, m.sensorAccuracy)
            put(MeasurementTable.COLUMN_BIAS_X, m.biasX)
            put(MeasurementTable.COLUMN_BIAS_Y, m.biasY)
            put(MeasurementTable.COLUMN_BIAS_Z, m.biasZ)
            if (m.lux != null) put(MeasurementTable.COLUMN_LUX, m.lux) else putNull(MeasurementTable.COLUMN_LUX)
            put(MeasurementTable.COLUMN_TILT_ANGLE, m.tiltAngle)
            put(MeasurementTable.COLUMN_IS_TILTED, if (m.isTilted) 1 else 0)
            put(MeasurementTable.COLUMN_STATUS, m.status.name)
            put(MeasurementTable.COLUMN_NOTES, m.notes)
            if (m.pointX != null) put(MeasurementTable.COLUMN_POINT_X, m.pointX) else putNull(MeasurementTable.COLUMN_POINT_X)
            if (m.pointY != null) put(MeasurementTable.COLUMN_POINT_Y, m.pointY) else putNull(MeasurementTable.COLUMN_POINT_Y)
        }
        return db.insertWithOnConflict(
            MeasurementTable.TABLE_NAME,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun deleteMeasurement(id: Long) {
        val db = dbHelper.writableDatabase
        db.delete(MeasurementTable.TABLE_NAME, "${MeasurementTable.COLUMN_ID} = ?", arrayOf(id.toString()))
    }

    private fun cursorToMeasurement(cursor: Cursor): Measurement {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_ID))
        val roomId = cursor.getLong(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_ROOM_ID))
        val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_TIMESTAMP))
        val x = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_X))
        val y = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_Y))
        val z = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_Z))
        val magnitude = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_MAGNITUDE))
        val accuracy = cursor.getInt(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_SENSOR_ACCURACY))
        val biasX = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_BIAS_X))
        val biasY = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_BIAS_Y))
        val biasZ = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_BIAS_Z))

        val luxCol = cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_LUX)
        val lux = if (cursor.isNull(luxCol)) null else cursor.getFloat(luxCol)

        val tiltAngle = cursor.getFloat(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_TILT_ANGLE))
        val isTilted = cursor.getInt(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_IS_TILTED)) == 1

        val statusStr = cursor.getString(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_STATUS))
        val status = try {
            MagneticStatus.valueOf(statusStr)
        } catch (_: Exception) {
            MagneticStatus.NORMAL
        }

        val notes = cursor.getString(cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_NOTES)) ?: ""

        val pxCol = cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_POINT_X)
        val pointX = if (cursor.isNull(pxCol)) null else cursor.getFloat(pxCol)

        val pyCol = cursor.getColumnIndexOrThrow(MeasurementTable.COLUMN_POINT_Y)
        val pointY = if (cursor.isNull(pyCol)) null else cursor.getFloat(pyCol)

        return Measurement(
            id = id,
            roomId = roomId,
            timestamp = timestamp,
            x = x,
            y = y,
            z = z,
            magnitude = magnitude,
            sensorAccuracy = accuracy,
            biasX = biasX,
            biasY = biasY,
            biasZ = biasZ,
            lux = lux,
            tiltAngle = tiltAngle,
            isTilted = isTilted,
            status = status,
            notes = notes,
            pointX = pointX,
            pointY = pointY
        )
    }
}
