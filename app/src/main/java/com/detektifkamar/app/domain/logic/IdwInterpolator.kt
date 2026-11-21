package com.detektifkamar.app.domain.logic

import kotlin.math.hypot
import kotlin.math.max

data class HeatmapGrid(
    val cols: Int,
    val rows: Int,
    val cellWidthM: Float,
    val cellHeightM: Float,
    val values: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HeatmapGrid) return false
        if (cols != other.cols || rows != other.rows) return false
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int {
        var result = cols
        result = 31 * result + rows
        result = 31 * result + values.contentHashCode()
        return result
    }
}

data class SamplePoint(
    val x: Float,
    val y: Float,
    val value: Float
)

/**
 * Logika murni interpolasi 2D Inverse Distance Weighting (IDW pangkat 2).
 */
object IdwInterpolator {

    const val MIN_POINTS_REQUIRED = 3
    const val MAX_CELLS_LONGEST_SIDE = 48
    const val MIN_DISTANCE_CLAMP_M = 0.05f
    const val MAX_REACH_RADIUS_M = 1.50f

    /**
     * Menghitung kisi peta panas untuk ruangan [roomWidthM] x [roomLengthM].
     * Mengembalikan null jika titik < 3.
     */
    fun computeGrid(
        roomWidthM: Float,
        roomLengthM: Float,
        points: List<SamplePoint>,
        maxReachRadiusM: Float = MAX_REACH_RADIUS_M
    ): HeatmapGrid? {
        if (points.size < MIN_POINTS_REQUIRED || roomWidthM <= 0f || roomLengthM <= 0f) {
            return null
        }

        val longestSide = max(roomWidthM, roomLengthM)
        val cellSize = longestSide / MAX_CELLS_LONGEST_SIDE

        val cols = max(1, (roomWidthM / cellSize).toInt())
        val rows = max(1, (roomLengthM / cellSize).toInt())

        val cellWidthM = roomWidthM / cols
        val cellHeightM = roomLengthM / rows

        val values = FloatArray(cols * rows)

        for (r in 0 until rows) {
            val cellCenterY = (r + 0.5f) * cellHeightM
            for (c in 0 until cols) {
                val cellCenterX = (c + 0.5f) * cellWidthM

                var minDistance = Float.MAX_VALUE
                var exactValue: Float? = null

                // Cek jarak ke setiap titik
                for (p in points) {
                    val dist = hypot(cellCenterX - p.x, cellCenterY - p.y)
                    if (dist < minDistance) {
                        minDistance = dist
                    }
                    if (dist < MIN_DISTANCE_CLAMP_M) {
                        exactValue = p.value
                        break
                    }
                }

                val index = r * cols + c

                when {
                    // Di luar jangkauan radius interpolasi 1.5 meter -> tandai NaN (arsir)
                    minDistance > maxReachRadiusM -> {
                        values[index] = Float.NaN
                    }
                    // Tepat di atas titik sampel
                    exactValue != null -> {
                        values[index] = exactValue
                    }
                    // Interpolasi IDW bobot jarak terbalik kuadrat (1 / d^2)
                    else -> {
                        var weightedSum = 0.0
                        var weightTotal = 0.0
                        for (p in points) {
                            val dist = max(MIN_DISTANCE_CLAMP_M, hypot(cellCenterX - p.x, cellCenterY - p.y))
                            val weight = 1.0 / (dist * dist)
                            weightedSum += weight * p.value
                            weightTotal += weight
                        }
                        values[index] = (weightedSum / weightTotal).toFloat()
                    }
                }
            }
        }

        return HeatmapGrid(
            cols = cols,
            rows = rows,
            cellWidthM = cellWidthM,
            cellHeightM = cellHeightM,
            values = values
        )
    }
}
