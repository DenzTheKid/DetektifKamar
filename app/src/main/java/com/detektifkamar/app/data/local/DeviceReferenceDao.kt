package com.detektifkamar.app.data.local

import android.database.Cursor
import com.detektifkamar.app.data.local.DatabaseContract.DeviceCatalogTable
import com.detektifkamar.app.domain.model.DeviceCategory
import com.detektifkamar.app.domain.model.DeviceReference

class DeviceReferenceDao(private val dbHelper: DetektifDbHelper) {

    fun search(searchQuery: String = "", category: DeviceCategory? = null): List<DeviceReference> {
        val db = dbHelper.readableDatabase
        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (searchQuery.isNotBlank()) {
            conditions.add("(${DeviceCatalogTable.COLUMN_NAME} LIKE ? OR ${DeviceCatalogTable.COLUMN_PHYSICAL_EXPLANATION} LIKE ?)")
            val pattern = "%${searchQuery.trim()}%"
            args.add(pattern)
            args.add(pattern)
        }

        if (category != null) {
            conditions.add("${DeviceCatalogTable.COLUMN_CATEGORY} = ?")
            args.add(category.name)
        }

        val selection = if (conditions.isNotEmpty()) conditions.joinToString(" AND ") else null
        val selectionArgs = if (args.isNotEmpty()) args.toTypedArray() else null

        val list = mutableListOf<DeviceReference>()
        db.query(
            DeviceCatalogTable.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${DeviceCatalogTable.COLUMN_MIN_UT} ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                list.add(cursorToDeviceReference(cursor))
            }
        }
        return list
    }

    fun getById(id: Long): DeviceReference? {
        val db = dbHelper.readableDatabase
        db.query(
            DeviceCatalogTable.TABLE_NAME,
            null,
            "${DeviceCatalogTable.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToNext()) {
                return cursorToDeviceReference(cursor)
            }
        }
        return null
    }

    private fun cursorToDeviceReference(cursor: Cursor): DeviceReference {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_NAME))
        val catStr = cursor.getString(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_CATEGORY))
        val category = try {
            DeviceCategory.valueOf(catStr)
        } catch (_: Exception) {
            DeviceCategory.fromDisplayName(catStr)
        }
        val minUt = cursor.getFloat(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_MIN_UT))
        val maxUt = cursor.getFloat(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_MAX_UT))
        val distance = cursor.getString(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_MEASUREMENT_DISTANCE))
        val explanation = cursor.getString(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_PHYSICAL_EXPLANATION))
        val safety = cursor.getString(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_SAFETY_NOTE))
        val source = cursor.getString(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_VERIFICATION_SOURCE))
        val isSaturating = cursor.getInt(cursor.getColumnIndexOrThrow(DeviceCatalogTable.COLUMN_IS_SATURATING)) == 1

        return DeviceReference(
            id = id,
            name = name,
            category = category,
            minUt = minUt,
            maxUt = maxUt,
            measurementDistance = distance,
            physicalExplanation = explanation,
            safetyNote = safety,
            verificationSource = source,
            isSaturatingSensor = isSaturating
        )
    }
}
