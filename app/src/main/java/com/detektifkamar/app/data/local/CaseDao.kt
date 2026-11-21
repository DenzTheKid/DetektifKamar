package com.detektifkamar.app.data.local

import android.content.ContentValues
import android.database.Cursor
import com.detektifkamar.app.data.local.DatabaseContract.CaseTable
import com.detektifkamar.app.data.local.DatabaseContract.EvidencePointTable
import com.detektifkamar.app.data.local.DatabaseContract.FurnitureTable
import com.detektifkamar.app.data.local.DatabaseContract.PointSuspectTable
import com.detektifkamar.app.data.local.DatabaseContract.ReadingTable
import com.detektifkamar.app.data.local.DatabaseContract.SuspectCatalogTable
import com.detektifkamar.app.domain.logic.DisturbanceClassifier
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePoint
import com.detektifkamar.app.domain.model.EvidencePointWithReading
import com.detektifkamar.app.domain.model.Furniture
import com.detektifkamar.app.domain.model.InvestigationCase
import com.detektifkamar.app.domain.model.PointReading
import com.detektifkamar.app.domain.model.Suspect
import com.detektifkamar.app.domain.model.SuspectCategory

data class CaseSummary(
    val pointCount: Int,
    val maxDeltaUt: Float,
    val avgMeanUt: Float,
    val highestCategory: DisturbanceCategory
)

class CaseDao(private val dbHelper: DetektifDbHelper) {

    fun getAllCases(): List<InvestigationCase> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<InvestigationCase>()
        val sql = "SELECT * FROM ${CaseTable.TABLE_NAME} ORDER BY ${CaseTable.COLUMN_UPDATED_AT} DESC"
        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToCase(cursor))
            }
        }
        return list
    }

    fun getCaseById(id: Long): InvestigationCase? {
        val db = dbHelper.readableDatabase
        val sql = "SELECT * FROM ${CaseTable.TABLE_NAME} WHERE ${CaseTable.COLUMN_ID} = ?"
        db.rawQuery(sql, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToNext()) {
                return cursorToCase(cursor)
            }
        }
        return null
    }

    fun insertCase(case: InvestigationCase): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(CaseTable.COLUMN_TITLE, case.title)
            put(CaseTable.COLUMN_ROOM_TYPE, case.roomType)
            put(CaseTable.COLUMN_WIDTH_M, case.widthM)
            put(CaseTable.COLUMN_LENGTH_M, case.lengthM)
            put(CaseTable.COLUMN_NOTE, case.note)
            put(CaseTable.COLUMN_BASELINE_UT, case.baselineUt)
            put(CaseTable.COLUMN_CREATED_AT, case.createdAt)
            put(CaseTable.COLUMN_UPDATED_AT, case.updatedAt)
        }
        return db.insert(CaseTable.TABLE_NAME, null, cv)
    }

    fun updateCase(case: InvestigationCase): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(CaseTable.COLUMN_TITLE, case.title)
            put(CaseTable.COLUMN_ROOM_TYPE, case.roomType)
            put(CaseTable.COLUMN_WIDTH_M, case.widthM)
            put(CaseTable.COLUMN_LENGTH_M, case.lengthM)
            put(CaseTable.COLUMN_NOTE, case.note)
            put(CaseTable.COLUMN_BASELINE_UT, case.baselineUt)
            put(CaseTable.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        return db.update(CaseTable.TABLE_NAME, cv, "${CaseTable.COLUMN_ID} = ?", arrayOf(case.id.toString()))
    }

    fun updateBaseline(caseId: Long, baselineUt: Float): Int {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(CaseTable.COLUMN_BASELINE_UT, baselineUt)
            put(CaseTable.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        return db.update(CaseTable.TABLE_NAME, cv, "${CaseTable.COLUMN_ID} = ?", arrayOf(caseId.toString()))
    }

    fun deleteCase(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(CaseTable.TABLE_NAME, "${CaseTable.COLUMN_ID} = ?", arrayOf(id.toString()))
    }

    // --- Perabot ---
    fun getFurnitureByCaseId(caseId: Long): List<Furniture> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<Furniture>()
        val sql = "SELECT * FROM ${FurnitureTable.TABLE_NAME} WHERE ${FurnitureTable.COLUMN_CASE_ID} = ?"
        db.rawQuery(sql, arrayOf(caseId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(
                    Furniture(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_ID)),
                        caseId = cursor.getLong(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_CASE_ID)),
                        label = cursor.getString(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_LABEL)),
                        kind = cursor.getString(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_KIND)),
                        xM = cursor.getFloat(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_X_M)),
                        yM = cursor.getFloat(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_Y_M)),
                        wM = cursor.getFloat(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_W_M)),
                        hM = cursor.getFloat(cursor.getColumnIndexOrThrow(FurnitureTable.COLUMN_H_M))
                    )
                )
            }
        }
        return list
    }

    fun insertFurniture(furniture: Furniture): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(FurnitureTable.COLUMN_CASE_ID, furniture.caseId)
            put(FurnitureTable.COLUMN_LABEL, furniture.label)
            put(FurnitureTable.COLUMN_KIND, furniture.kind)
            put(FurnitureTable.COLUMN_X_M, furniture.xM)
            put(FurnitureTable.COLUMN_Y_M, furniture.yM)
            put(FurnitureTable.COLUMN_W_M, furniture.wM)
            put(FurnitureTable.COLUMN_H_M, furniture.hM)
        }
        return db.insert(FurnitureTable.TABLE_NAME, null, cv)
    }

    fun deleteFurniture(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(FurnitureTable.TABLE_NAME, "${FurnitureTable.COLUMN_ID} = ?", arrayOf(id.toString()))
    }

    // --- Titik Bukti & Pembacaan ---
    fun insertPoint(point: EvidencePoint): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(EvidencePointTable.COLUMN_CASE_ID, point.caseId)
            put(EvidencePointTable.COLUMN_LABEL, point.label)
            put(EvidencePointTable.COLUMN_X_M, point.xM)
            put(EvidencePointTable.COLUMN_Y_M, point.yM)
            put(EvidencePointTable.COLUMN_NOTE, point.note)
            put(EvidencePointTable.COLUMN_CREATED_AT, point.createdAt)
        }
        return db.insert(EvidencePointTable.TABLE_NAME, null, cv)
    }

    fun deletePoint(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(EvidencePointTable.TABLE_NAME, "${EvidencePointTable.COLUMN_ID} = ?", arrayOf(id.toString()))
    }

    fun getPointById(id: Long): EvidencePoint? {
        val db = dbHelper.readableDatabase
        val sql = "SELECT * FROM ${EvidencePointTable.TABLE_NAME} WHERE ${EvidencePointTable.COLUMN_ID} = ?"
        db.rawQuery(sql, arrayOf(id.toString())).use { cursor ->
            if (cursor.moveToNext()) {
                return cursorToPoint(cursor)
            }
        }
        return null
    }

    fun insertReading(reading: PointReading): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put(ReadingTable.COLUMN_POINT_ID, reading.pointId)
            put(ReadingTable.COLUMN_MEAN_UT, reading.meanUt)
            put(ReadingTable.COLUMN_MIN_UT, reading.minUt)
            put(ReadingTable.COLUMN_MAX_UT, reading.maxUt)
            put(ReadingTable.COLUMN_STD_UT, reading.stdUt)
            put(ReadingTable.COLUMN_BX, reading.bx)
            put(ReadingTable.COLUMN_BY, reading.by)
            put(ReadingTable.COLUMN_BZ, reading.bz)
            put(ReadingTable.COLUMN_LUX_MEAN, reading.luxMean)
            put(ReadingTable.COLUMN_AZIMUTH_DEG, reading.azimuthDeg)
            put(ReadingTable.COLUMN_TILT_DEG, reading.tiltDeg)
            put(ReadingTable.COLUMN_ACCURACY, reading.accuracy)
            put(ReadingTable.COLUMN_QUALITY, reading.quality)
            put(ReadingTable.COLUMN_SAMPLE_COUNT, reading.sampleCount)
            put(ReadingTable.COLUMN_MEASURED_AT, reading.measuredAt)
        }
        return db.insert(ReadingTable.TABLE_NAME, null, cv)
    }

    fun getReadingsForPoint(pointId: Long): List<PointReading> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<PointReading>()
        val sql = "SELECT * FROM ${ReadingTable.TABLE_NAME} WHERE ${ReadingTable.COLUMN_POINT_ID} = ? ORDER BY ${ReadingTable.COLUMN_MEASURED_AT} ASC"
        db.rawQuery(sql, arrayOf(pointId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToReading(cursor))
            }
        }
        return list
    }

    /**
     * Query JOIN: Mengambil titik bukti beserta pembacaan terakhir per titik menggunakan subquery MAX(measured_at).
     */
    fun getPointsWithLatestReadings(caseId: Long, baselineUt: Float?): List<EvidencePointWithReading> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<EvidencePointWithReading>()

        val sql = """
            SELECT 
                p.${EvidencePointTable.COLUMN_ID} AS p_id,
                p.${EvidencePointTable.COLUMN_CASE_ID} AS p_case_id,
                p.${EvidencePointTable.COLUMN_LABEL} AS p_label,
                p.${EvidencePointTable.COLUMN_X_M} AS p_x_m,
                p.${EvidencePointTable.COLUMN_Y_M} AS p_y_m,
                p.${EvidencePointTable.COLUMN_NOTE} AS p_note,
                p.${EvidencePointTable.COLUMN_CREATED_AT} AS p_created_at,
                r.${ReadingTable.COLUMN_ID} AS r_id,
                r.${ReadingTable.COLUMN_POINT_ID} AS r_point_id,
                r.${ReadingTable.COLUMN_MEAN_UT} AS r_mean_ut,
                r.${ReadingTable.COLUMN_MIN_UT} AS r_min_ut,
                r.${ReadingTable.COLUMN_MAX_UT} AS r_max_ut,
                r.${ReadingTable.COLUMN_STD_UT} AS r_std_ut,
                r.${ReadingTable.COLUMN_BX} AS r_bx,
                r.${ReadingTable.COLUMN_BY} AS r_by,
                r.${ReadingTable.COLUMN_BZ} AS r_bz,
                r.${ReadingTable.COLUMN_LUX_MEAN} AS r_lux_mean,
                r.${ReadingTable.COLUMN_AZIMUTH_DEG} AS r_azimuth_deg,
                r.${ReadingTable.COLUMN_TILT_DEG} AS r_tilt_deg,
                r.${ReadingTable.COLUMN_ACCURACY} AS r_accuracy,
                r.${ReadingTable.COLUMN_QUALITY} AS r_quality,
                r.${ReadingTable.COLUMN_SAMPLE_COUNT} AS r_sample_count,
                r.${ReadingTable.COLUMN_MEASURED_AT} AS r_measured_at
            FROM ${EvidencePointTable.TABLE_NAME} p
            LEFT JOIN ${ReadingTable.TABLE_NAME} r ON r.${ReadingTable.COLUMN_POINT_ID} = p.${EvidencePointTable.COLUMN_ID}
                AND r.${ReadingTable.COLUMN_MEASURED_AT} = (
                    SELECT MAX(r2.${ReadingTable.COLUMN_MEASURED_AT})
                    FROM ${ReadingTable.TABLE_NAME} r2
                    WHERE r2.${ReadingTable.COLUMN_POINT_ID} = p.${EvidencePointTable.COLUMN_ID}
                )
            WHERE p.${EvidencePointTable.COLUMN_CASE_ID} = ?
            ORDER BY p.${EvidencePointTable.COLUMN_CREATED_AT} ASC
        """.trimIndent()

        db.rawQuery(sql, arrayOf(caseId.toString())).use { cursor ->
            while (cursor.moveToNext()) {
                val point = EvidencePoint(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("p_id")),
                    caseId = cursor.getLong(cursor.getColumnIndexOrThrow("p_case_id")),
                    label = cursor.getString(cursor.getColumnIndexOrThrow("p_label")),
                    xM = cursor.getFloat(cursor.getColumnIndexOrThrow("p_x_m")),
                    yM = cursor.getFloat(cursor.getColumnIndexOrThrow("p_y_m")),
                    note = cursor.getString(cursor.getColumnIndexOrThrow("p_note")),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("p_created_at"))
                )

                val readingIdCol = cursor.getColumnIndexOrThrow("r_id")
                val reading = if (!cursor.isNull(readingIdCol)) {
                    PointReading(
                        id = cursor.getLong(readingIdCol),
                        pointId = cursor.getLong(cursor.getColumnIndexOrThrow("r_point_id")),
                        meanUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_mean_ut")),
                        minUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_min_ut")),
                        maxUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_max_ut")),
                        stdUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_std_ut")),
                        bx = cursor.getFloat(cursor.getColumnIndexOrThrow("r_bx")),
                        by = cursor.getFloat(cursor.getColumnIndexOrThrow("r_by")),
                        bz = cursor.getFloat(cursor.getColumnIndexOrThrow("r_bz")),
                        luxMean = if (cursor.isNull(cursor.getColumnIndexOrThrow("r_lux_mean"))) null else cursor.getFloat(cursor.getColumnIndexOrThrow("r_lux_mean")),
                        azimuthDeg = if (cursor.isNull(cursor.getColumnIndexOrThrow("r_azimuth_deg"))) null else cursor.getFloat(cursor.getColumnIndexOrThrow("r_azimuth_deg")),
                        tiltDeg = cursor.getFloat(cursor.getColumnIndexOrThrow("r_tilt_deg")),
                        accuracy = cursor.getInt(cursor.getColumnIndexOrThrow("r_accuracy")),
                        quality = cursor.getString(cursor.getColumnIndexOrThrow("r_quality")),
                        sampleCount = cursor.getInt(cursor.getColumnIndexOrThrow("r_sample_count")),
                        measuredAt = cursor.getLong(cursor.getColumnIndexOrThrow("r_measured_at"))
                    )
                } else null

                val deltaUt = if (reading != null && baselineUt != null) {
                    DisturbanceClassifier.calculateDelta(reading.meanUt, baselineUt)
                } else null

                val category = if (deltaUt != null) {
                    DisturbanceClassifier.classify(deltaUt)
                } else DisturbanceCategory.TENANG

                list.add(
                    EvidencePointWithReading(
                        point = point,
                        latestReading = reading,
                        deltaUt = deltaUt,
                        category = category
                    )
                )
            }
        }
        return list
    }

    /**
     * Query untuk layar Arsip (semua titik bukti lintas kasus).
     */
    fun getAllEvidencePointsWithLatestReading(
        searchQuery: String? = null,
        categoryFilter: DisturbanceCategory? = null,
        sortByLargestDelta: Boolean = false
    ): List<EvidencePointWithReading> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<EvidencePointWithReading>()

        val whereClauses = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (!searchQuery.isNullOrBlank()) {
            whereClauses.add("(p.${EvidencePointTable.COLUMN_LABEL} LIKE ? OR c.${CaseTable.COLUMN_TITLE} LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        val whereSql = if (whereClauses.isNotEmpty()) "WHERE " + whereClauses.joinToString(" AND ") else ""

        val sql = """
            SELECT 
                p.${EvidencePointTable.COLUMN_ID} AS p_id,
                p.${EvidencePointTable.COLUMN_CASE_ID} AS p_case_id,
                p.${EvidencePointTable.COLUMN_LABEL} AS p_label,
                p.${EvidencePointTable.COLUMN_X_M} AS p_x_m,
                p.${EvidencePointTable.COLUMN_Y_M} AS p_y_m,
                p.${EvidencePointTable.COLUMN_NOTE} AS p_note,
                p.${EvidencePointTable.COLUMN_CREATED_AT} AS p_created_at,
                c.${CaseTable.COLUMN_BASELINE_UT} AS c_baseline_ut,
                r.${ReadingTable.COLUMN_ID} AS r_id,
                r.${ReadingTable.COLUMN_POINT_ID} AS r_point_id,
                r.${ReadingTable.COLUMN_MEAN_UT} AS r_mean_ut,
                r.${ReadingTable.COLUMN_MIN_UT} AS r_min_ut,
                r.${ReadingTable.COLUMN_MAX_UT} AS r_max_ut,
                r.${ReadingTable.COLUMN_STD_UT} AS r_std_ut,
                r.${ReadingTable.COLUMN_BX} AS r_bx,
                r.${ReadingTable.COLUMN_BY} AS r_by,
                r.${ReadingTable.COLUMN_BZ} AS r_bz,
                r.${ReadingTable.COLUMN_LUX_MEAN} AS r_lux_mean,
                r.${ReadingTable.COLUMN_AZIMUTH_DEG} AS r_azimuth_deg,
                r.${ReadingTable.COLUMN_TILT_DEG} AS r_tilt_deg,
                r.${ReadingTable.COLUMN_ACCURACY} AS r_accuracy,
                r.${ReadingTable.COLUMN_QUALITY} AS r_quality,
                r.${ReadingTable.COLUMN_SAMPLE_COUNT} AS r_sample_count,
                r.${ReadingTable.COLUMN_MEASURED_AT} AS r_measured_at
            FROM ${EvidencePointTable.TABLE_NAME} p
            JOIN ${CaseTable.TABLE_NAME} c ON c.${CaseTable.COLUMN_ID} = p.${EvidencePointTable.COLUMN_CASE_ID}
            LEFT JOIN ${ReadingTable.TABLE_NAME} r ON r.${ReadingTable.COLUMN_POINT_ID} = p.${EvidencePointTable.COLUMN_ID}
                AND r.${ReadingTable.COLUMN_MEASURED_AT} = (
                    SELECT MAX(r2.${ReadingTable.COLUMN_MEASURED_AT})
                    FROM ${ReadingTable.TABLE_NAME} r2
                    WHERE r2.${ReadingTable.COLUMN_POINT_ID} = p.${EvidencePointTable.COLUMN_ID}
                )
            $whereSql
            ORDER BY p.${EvidencePointTable.COLUMN_CREATED_AT} DESC
        """.trimIndent()

        db.rawQuery(sql, if (args.isNotEmpty()) args.toTypedArray() else null).use { cursor ->
            while (cursor.moveToNext()) {
                val point = EvidencePoint(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow("p_id")),
                    caseId = cursor.getLong(cursor.getColumnIndexOrThrow("p_case_id")),
                    label = cursor.getString(cursor.getColumnIndexOrThrow("p_label")),
                    xM = cursor.getFloat(cursor.getColumnIndexOrThrow("p_x_m")),
                    yM = cursor.getFloat(cursor.getColumnIndexOrThrow("p_y_m")),
                    note = cursor.getString(cursor.getColumnIndexOrThrow("p_note")),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("p_created_at"))
                )

                val baselineCol = cursor.getColumnIndexOrThrow("c_baseline_ut")
                val baselineUt = if (cursor.isNull(baselineCol)) 45.0f else cursor.getFloat(baselineCol)

                val readingIdCol = cursor.getColumnIndexOrThrow("r_id")
                val reading = if (!cursor.isNull(readingIdCol)) {
                    PointReading(
                        id = cursor.getLong(readingIdCol),
                        pointId = cursor.getLong(cursor.getColumnIndexOrThrow("r_point_id")),
                        meanUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_mean_ut")),
                        minUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_min_ut")),
                        maxUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_max_ut")),
                        stdUt = cursor.getFloat(cursor.getColumnIndexOrThrow("r_std_ut")),
                        bx = cursor.getFloat(cursor.getColumnIndexOrThrow("r_bx")),
                        by = cursor.getFloat(cursor.getColumnIndexOrThrow("r_by")),
                        bz = cursor.getFloat(cursor.getColumnIndexOrThrow("r_bz")),
                        luxMean = if (cursor.isNull(cursor.getColumnIndexOrThrow("r_lux_mean"))) null else cursor.getFloat(cursor.getColumnIndexOrThrow("r_lux_mean")),
                        azimuthDeg = if (cursor.isNull(cursor.getColumnIndexOrThrow("r_azimuth_deg"))) null else cursor.getFloat(cursor.getColumnIndexOrThrow("r_azimuth_deg")),
                        tiltDeg = cursor.getFloat(cursor.getColumnIndexOrThrow("r_tilt_deg")),
                        accuracy = cursor.getInt(cursor.getColumnIndexOrThrow("r_accuracy")),
                        quality = cursor.getString(cursor.getColumnIndexOrThrow("r_quality")),
                        sampleCount = cursor.getInt(cursor.getColumnIndexOrThrow("r_sample_count")),
                        measuredAt = cursor.getLong(cursor.getColumnIndexOrThrow("r_measured_at"))
                    )
                } else null

                val deltaUt = if (reading != null) {
                    DisturbanceClassifier.calculateDelta(reading.meanUt, baselineUt)
                } else null

                val category = if (deltaUt != null) {
                    DisturbanceClassifier.classify(deltaUt)
                } else DisturbanceCategory.TENANG

                if (categoryFilter == null || category == categoryFilter) {
                    list.add(
                        EvidencePointWithReading(
                            point = point,
                            latestReading = reading,
                            deltaUt = deltaUt,
                            category = category
                        )
                    )
                }
            }
        }

        return if (sortByLargestDelta) {
            list.sortedByDescending { it.deltaUt ?: 0f }
        } else {
            list
        }
    }

    /**
     * Query Agregat: menghitung COUNT titik bukti, MAX simpangan, AVG mean_ut per kasus.
     */
    fun getCaseSummary(caseId: Long, baselineUt: Float): CaseSummary {
        val points = getPointsWithLatestReadings(caseId, baselineUt)
        if (points.isEmpty()) {
            return CaseSummary(0, 0f, 0f, DisturbanceCategory.TENANG)
        }

        val deltas = points.mapNotNull { it.deltaUt }
        val maxDelta = if (deltas.isNotEmpty()) deltas.maxOrNull() ?: 0f else 0f

        val validMeans = points.mapNotNull { it.latestReading?.meanUt }
        val avgMean = if (validMeans.isNotEmpty()) validMeans.average().toFloat() else 0f

        val highestCategory = DisturbanceClassifier.classify(maxDelta)

        return CaseSummary(
            pointCount = points.size,
            maxDeltaUt = maxDelta,
            avgMeanUt = avgMean,
            highestCategory = highestCategory
        )
    }

    private fun cursorToCase(cursor: Cursor): InvestigationCase {
        val baselineCol = cursor.getColumnIndexOrThrow(CaseTable.COLUMN_BASELINE_UT)
        val baseline = if (cursor.isNull(baselineCol)) null else cursor.getFloat(baselineCol)
        return InvestigationCase(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_TITLE)),
            roomType = cursor.getString(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_ROOM_TYPE)),
            widthM = cursor.getFloat(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_WIDTH_M)),
            lengthM = cursor.getFloat(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_LENGTH_M)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_NOTE)),
            baselineUt = baseline,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(CaseTable.COLUMN_UPDATED_AT))
        )
    }

    private fun cursorToPoint(cursor: Cursor): EvidencePoint {
        return EvidencePoint(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_ID)),
            caseId = cursor.getLong(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_CASE_ID)),
            label = cursor.getString(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_LABEL)),
            xM = cursor.getFloat(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_X_M)),
            yM = cursor.getFloat(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_Y_M)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_NOTE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(EvidencePointTable.COLUMN_CREATED_AT))
        )
    }

    private fun cursorToReading(cursor: Cursor): PointReading {
        val luxCol = cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_LUX_MEAN)
        val azCol = cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_AZIMUTH_DEG)
        return PointReading(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_ID)),
            pointId = cursor.getLong(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_POINT_ID)),
            meanUt = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_MEAN_UT)),
            minUt = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_MIN_UT)),
            maxUt = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_MAX_UT)),
            stdUt = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_STD_UT)),
            bx = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_BX)),
            by = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_BY)),
            bz = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_BZ)),
            luxMean = if (cursor.isNull(luxCol)) null else cursor.getFloat(luxCol),
            azimuthDeg = if (cursor.isNull(azCol)) null else cursor.getFloat(azCol),
            tiltDeg = cursor.getFloat(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_TILT_DEG)),
            accuracy = cursor.getInt(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_ACCURACY)),
            quality = cursor.getString(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_QUALITY)),
            sampleCount = cursor.getInt(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_SAMPLE_COUNT)),
            measuredAt = cursor.getLong(cursor.getColumnIndexOrThrow(ReadingTable.COLUMN_MEASURED_AT))
        )
    }
}
