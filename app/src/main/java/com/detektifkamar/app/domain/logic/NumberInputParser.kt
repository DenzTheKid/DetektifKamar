package com.detektifkamar.app.domain.logic

/**
 * Pengurai angka desimal yang mendukung tanda koma (standar Indonesia) maupun titik.
 */
object NumberInputParser {

    const val MIN_DIMENSION_M = 0.5f
    const val MAX_DIMENSION_M = 30.0f

    fun parseDimension(input: String): Result<Float> {
        val trimmed = input.trim().replace(',', '.')
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Ukuran tidak boleh kosong"))
        }

        val parsed = trimmed.toFloatOrNull()
            ?: return Result.failure(IllegalArgumentException("Format angka tidak valid. Gunakan angka desimal seperti 3.5 atau 3,5"))

        if (parsed < MIN_DIMENSION_M) {
            return Result.failure(IllegalArgumentException("Ukuran minimal adalah 0,5 meter"))
        }
        if (parsed > MAX_DIMENSION_M) {
            return Result.failure(IllegalArgumentException("Ukuran maksimal adalah 30,0 meter"))
        }

        return Result.success(parsed)
    }
}
