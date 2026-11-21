package com.detektifkamar.app.data.local

/**
 * Kontrak skema database SQLite Detektif Kamar.
 * Menggunakan konstanta terpusat untuk mencegah typo string.
 */
object DatabaseContract {
    const val DATABASE_NAME = "detektif_kamar.db"
    const val DATABASE_VERSION = 1

    object CaseTable {
        const val TABLE_NAME = "investigation_case"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_ROOM_TYPE = "room_type"
        const val COLUMN_WIDTH_M = "width_m"
        const val COLUMN_LENGTH_M = "length_m"
        const val COLUMN_NOTE = "note"
        const val COLUMN_BASELINE_UT = "baseline_ut"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    object FurnitureTable {
        const val TABLE_NAME = "furniture"
        const val COLUMN_ID = "id"
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_LABEL = "label"
        const val COLUMN_KIND = "kind"
        const val COLUMN_X_M = "x_m"
        const val COLUMN_Y_M = "y_m"
        const val COLUMN_W_M = "w_m"
        const val COLUMN_H_M = "h_m"
    }

    object EvidencePointTable {
        const val TABLE_NAME = "evidence_point"
        const val COLUMN_ID = "id"
        const val COLUMN_CASE_ID = "case_id"
        const val COLUMN_LABEL = "label"
        const val COLUMN_X_M = "x_m"
        const val COLUMN_Y_M = "y_m"
        const val COLUMN_NOTE = "note"
        const val COLUMN_CREATED_AT = "created_at"
    }

    object ReadingTable {
        const val TABLE_NAME = "reading"
        const val COLUMN_ID = "id"
        const val COLUMN_POINT_ID = "point_id"
        const val COLUMN_MEAN_UT = "mean_ut"
        const val COLUMN_MIN_UT = "min_ut"
        const val COLUMN_MAX_UT = "max_ut"
        const val COLUMN_STD_UT = "std_ut"
        const val COLUMN_BX = "bx"
        const val COLUMN_BY = "by"
        const val COLUMN_BZ = "bz"
        const val COLUMN_LUX_MEAN = "lux_mean"
        const val COLUMN_AZIMUTH_DEG = "azimuth_deg"
        const val COLUMN_TILT_DEG = "tilt_deg"
        const val COLUMN_ACCURACY = "accuracy"
        const val COLUMN_QUALITY = "quality"
        const val COLUMN_SAMPLE_COUNT = "sample_count"
        const val COLUMN_MEASURED_AT = "measured_at"
    }

    object SuspectCatalogTable {
        const val TABLE_NAME = "suspect_catalog"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_TYPICAL_MIN_UT = "typical_min_ut"
        const val COLUMN_TYPICAL_MAX_UT = "typical_max_ut"
        const val COLUMN_TYPICAL_DISTANCE_CM = "typical_distance_cm"
        const val COLUMN_NOTE = "note"
        const val COLUMN_HOW_TO_CONFIRM = "how_to_confirm"
    }

    object PointSuspectTable {
        const val TABLE_NAME = "point_suspect"
        const val COLUMN_POINT_ID = "point_id"
        const val COLUMN_SUSPECT_ID = "suspect_id"
        const val COLUMN_CUSTOM_LABEL = "custom_label"
    }
}
