package com.detektifkamar.app.data.local

import android.content.ContentValues
import com.detektifkamar.app.data.local.DatabaseContract.CalibrationTable
import com.detektifkamar.app.domain.model.CalibrationData
import com.detektifkamar.app.domain.model.CalibrationQuality

class CalibrationDao(private val dbHelper: DetektifDbHelper) {

    fun getLatestCalibration(): CalibrationData {
        val db = dbHelper.readableDatabase
        val query = "SELECT * FROM ${CalibrationTable.TABLE_NAME} ORDER BY ${CalibrationTable.COLUMN_ID} DESC LIMIT 1"
        db.rawQuery(query, null).use { cursor ->
            if (cursor.moveToNext()) {
                val bx = cursor.getFloat(cursor.getColumnIndexOrThrow(CalibrationTable.COLUMN_BIAS_X))
                val by = cursor.getFloat(cursor.getColumnIndexOrThrow(CalibrationTable.COLUMN_BIAS_Y))
                val bz = cursor.getFloat(cursor.getColumnIndexOrThrow(CalibrationTable.COLUMN_BIAS_Z))
                val qStr = cursor.getString(cursor.getColumnIndexOrThrow(CalibrationTable.COLUMN_QUALITY))
                val quality = try {
                    CalibrationQuality.valueOf(qStr)
                } catch (_: Exception) {
                    CalibrationQuality.CUKUP
                }
                val sampleCount = cursor.getInt(cursor.getColumnIndexOrThrow(CalibrationTable.COLUMN_SAMPLE_COUNT))
                val calAt = cursor.getLong(cursor.getColumnIndexOrThrow(CalibrationTable.COLUMN_CALIBRATED_AT))

                return CalibrationData(
                    biasX = bx,
                    biasY = by,
                    biasZ = bz,
                    quality = quality,
                    sampleCount = sampleCount,
                    calibratedAt = calAt
                )
            }
        }
        return CalibrationData()
    }

    fun saveCalibration(data: CalibrationData) {
        val db = dbHelper.writableDatabase
        db.delete(CalibrationTable.TABLE_NAME, null, null)
        val cv = ContentValues().apply {
            put(CalibrationTable.COLUMN_BIAS_X, data.biasX)
            put(CalibrationTable.COLUMN_BIAS_Y, data.biasY)
            put(CalibrationTable.COLUMN_BIAS_Z, data.biasZ)
            put(CalibrationTable.COLUMN_QUALITY, data.quality.name)
            put(CalibrationTable.COLUMN_SAMPLE_COUNT, data.sampleCount)
            put(CalibrationTable.COLUMN_CALIBRATED_AT, data.calibratedAt)
        }
        db.insert(CalibrationTable.TABLE_NAME, null, cv)
    }

    fun clearCalibration() {
        val db = dbHelper.writableDatabase
        db.delete(CalibrationTable.TABLE_NAME, null, null)
    }
}
