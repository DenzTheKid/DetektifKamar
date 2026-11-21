package com.detektifkamar.app.data.local

import android.content.ContentValues
import android.database.Cursor
import com.detektifkamar.app.data.local.DatabaseContract.PointSuspectTable
import com.detektifkamar.app.data.local.DatabaseContract.SuspectCatalogTable
import com.detektifkamar.app.domain.model.Suspect
import com.detektifkamar.app.domain.model.SuspectCategory

class SuspectDao(private val dbHelper: DetektifDbHelper) {

    /**
     * Pencarian LIKE dan filter kategori pada katalog tersangka.
     */
    fun getAllSuspects(searchQuery: String? = null, category: SuspectCategory? = null): List<Suspect> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Suspect>()

        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!searchQuery.isNullOrBlank()) {
            whereClauses.add("(${SuspectCatalogTable.COLUMN_NAME} LIKE ? OR ${SuspectCatalogTable.COLUMN_NOTE} LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        if (category != null) {
            whereClauses.add("${SuspectCatalogTable.COLUMN_CATEGORY} = ?")
            args.add(category.name)
        }

        val whereSql = if (whereClauses.isNotEmpty()) "WHERE " + whereClauses.joinToString(" AND ") else ""
        val sql = "SELECT * FROM ${SuspectCatalogTable.TABLE_NAME} $whereSql ORDER BY ${SuspectCatalogTable.COLUMN_NAME} ASC"

        db.rawQuery(sql, if (args.isNotEmpty()) args.toTypedArray() else null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToSuspect(cursor))
            }
        }
        return list
    }

    fun getSuspectById(id: Long): Suspect? {
        val db = dbHelper.readableDatabase
        val sql = "SELECT * FROM ${SuspectCatalogTable.TABLE_NAME} WHERE ${SuspectCatalogTable.COLUMN_ID} = ?"
        db.rawQuery(sql, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToNext()) {
                return cursorToSuspect(cursor)
            }
        }
        return null
    }

    /**
     * Query JOIN: Mencocokkan simpangan titik ukur dengan rentang tipikal tiap tersangka di katalog,
     * diurutkan berdasarkan kedekatan selisih terkecil ke rentang tersangka.
     */
    fun getLikelySuspectsForDelta(deltaUt: Float, limit: Int = 6): List<Suspect> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Suspect>()

        // Urutkan berdasarkan jarak selisih delta ke rentang [min_ut, max_ut]
        val sql = """
            SELECT *,
                CASE 
                    WHEN ? BETWEEN ${SuspectCatalogTable.COLUMN_TYPICAL_MIN_UT} AND ${SuspectCatalogTable.COLUMN_TYPICAL_MAX_UT} THEN 0.0
                    WHEN ? < ${SuspectCatalogTable.COLUMN_TYPICAL_MIN_UT} THEN (${SuspectCatalogTable.COLUMN_TYPICAL_MIN_UT} - ?)
                    ELSE (? - ${SuspectCatalogTable.COLUMN_TYPICAL_MAX_UT})
                END AS distance_score
            FROM ${SuspectCatalogTable.TABLE_NAME}
            ORDER BY distance_score ASC, ${SuspectCatalogTable.COLUMN_NAME} ASC
            LIMIT ?
        """.trimIndent()

        val deltaStr = deltaUt.toString()
        db.rawQuery(sql, arrayOf(deltaStr, deltaStr, deltaStr, deltaStr, limit.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToSuspect(cursor))
            }
        }
        return list
    }

    fun getSuspectsForPoint(pointId: Long): List<Suspect> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Suspect>()
        val sql = """
            SELECT s.*
            FROM ${SuspectCatalogTable.TABLE_NAME} s
            JOIN ${PointSuspectTable.TABLE_NAME} ps ON ps.${PointSuspectTable.COLUMN_SUSPECT_ID} = s.${SuspectCatalogTable.COLUMN_ID}
            WHERE ps.${PointSuspectTable.COLUMN_POINT_ID} = ?
            ORDER BY s.${SuspectCatalogTable.COLUMN_NAME} ASC
        """.trimIndent()

        db.rawQuery(sql, arrayOf(pointId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToSuspect(cursor))
            }
        }
        return list
    }

    fun linkSuspectToPoint(pointId: Long, suspectId: Long, customLabel: String? = null) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(PointSuspectTable.COLUMN_POINT_ID, pointId)
            put(PointSuspectTable.COLUMN_SUSPECT_ID, suspectId)
            put(PointSuspectTable.COLUMN_CUSTOM_LABEL, customLabel)
        }
        db.insertWithOnConflict(
            PointSuspectTable.TABLE_NAME,
            null,
            cv,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun unlinkSuspectFromPoint(pointId: Long, suspectId: Long) {
        val db = dbHelper.writableDatabase
        db.delete(
            PointSuspectTable.TABLE_NAME,
            "${PointSuspectTable.COLUMN_POINT_ID} = ? AND ${PointSuspectTable.COLUMN_SUSPECT_ID} = ?",
            arrayOf(pointId.toString(), suspectId.toString())
        )
    }

    fun getCustomLabelForPoint(pointId: Long): String? {
        val db = dbHelper.readableDatabase
        val sql = "SELECT ${PointSuspectTable.COLUMN_CUSTOM_LABEL} FROM ${PointSuspectTable.TABLE_NAME} WHERE ${PointSuspectTable.COLUMN_POINT_ID} = ? AND ${PointSuspectTable.COLUMN_CUSTOM_LABEL} IS NOT NULL LIMIT 1"
        db.rawQuery(sql, arrayOf(pointId.toString())).use { cursor ->
            if (cursor.moveToNext()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun setCustomLabelForPoint(pointId: Long, customLabel: String?) {
        val db = dbHelper.writableDatabase
        // Jika belum ada relasi, link ke suspect ID 1 atau perbarui kolom label
        val cv = ContentValues().apply {
            put(PointSuspectTable.COLUMN_CUSTOM_LABEL, customLabel)
        }
        val rows = db.update(PointSuspectTable.TABLE_NAME, cv, "${PointSuspectTable.COLUMN_POINT_ID} = ?", arrayOf(pointId.toString()))
        if (rows == 0 && customLabel != null) {
            // Pasang dummy link suspect 1 dengan label kustom
            cv.put(PointSuspectTable.COLUMN_POINT_ID, pointId)
            cv.put(PointSuspectTable.COLUMN_SUSPECT_ID, 1L)
            db.insertWithOnConflict(PointSuspectTable.TABLE_NAME, null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    private fun cursorToSuspect(cursor: Cursor): Suspect {
        val catName = cursor.getString(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_CATEGORY))
        return Suspect(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_NAME)),
            category = SuspectCategory.fromString(catName),
            typicalMinUt = cursor.getFloat(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_TYPICAL_MIN_UT)),
            typicalMaxUt = cursor.getFloat(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_TYPICAL_MAX_UT)),
            typicalDistanceCm = cursor.getInt(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_TYPICAL_DISTANCE_CM)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_NOTE)),
            howToConfirm = cursor.getString(cursor.getColumnIndexOrThrow(SuspectCatalogTable.COLUMN_HOW_TO_CONFIRM))
        )
    }
}
