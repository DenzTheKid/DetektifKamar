package com.detektifkamar.app.data.local

/**
 * Kontrak skema database SQLite murni untuk aplikasi Detektif Kamar.
 */
object DatabaseContract {
    const val DATABASE_NAME = "detektif_kamar.db"
    const val DATABASE_VERSION = 2

    object RoomTable {
        const val TABLE_NAME = "rooms"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_ROOM_TYPE = "room_type"
        const val COLUMN_WIDTH_METERS = "width_meters"
        const val COLUMN_LENGTH_METERS = "length_meters"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_CREATED_AT = "created_at"
    }

    object MeasurementTable {
        const val TABLE_NAME = "measurements"
        const val COLUMN_ID = "id"
        const val COLUMN_ROOM_ID = "room_id"
        const val COLUMN_TIMESTAMP = "timestamp"
        const val COLUMN_X = "x"
        const val COLUMN_Y = "y"
        const val COLUMN_Z = "z"
        const val COLUMN_MAGNITUDE = "magnitude"
        const val COLUMN_SENSOR_ACCURACY = "sensor_accuracy"
        const val COLUMN_BIAS_X = "bias_x"
        const val COLUMN_BIAS_Y = "bias_y"
        const val COLUMN_BIAS_Z = "bias_z"
        const val COLUMN_LUX = "lux"
        const val COLUMN_TILT_ANGLE = "tilt_angle"
        const val COLUMN_IS_TILTED = "is_tilted"
        const val COLUMN_STATUS = "status"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_POINT_X = "point_x"
        const val COLUMN_POINT_Y = "point_y"
    }

    object CalibrationTable {
        const val TABLE_NAME = "calibration_bias"
        const val COLUMN_ID = "id"
        const val COLUMN_BIAS_X = "bias_x"
        const val COLUMN_BIAS_Y = "bias_y"
        const val COLUMN_BIAS_Z = "bias_z"
        const val COLUMN_QUALITY = "quality"
        const val COLUMN_SAMPLE_COUNT = "sample_count"
        const val COLUMN_CALIBRATED_AT = "calibrated_at"
    }

    object DeviceCatalogTable {
        const val TABLE_NAME = "device_catalog"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_MIN_UT = "min_ut"
        const val COLUMN_MAX_UT = "max_ut"
        const val COLUMN_MEASUREMENT_DISTANCE = "measurement_distance"
        const val COLUMN_PHYSICAL_EXPLANATION = "physical_explanation"
        const val COLUMN_SAFETY_NOTE = "safety_note"
        const val COLUMN_VERIFICATION_SOURCE = "verification_source"
        const val COLUMN_IS_SATURATING = "is_saturating"
    }
}
