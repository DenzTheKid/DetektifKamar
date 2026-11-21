package com.detektifkamar.app.domain.logic

import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

data class CanvasBounds(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val roomWidthPx: Float,
    val roomLengthPx: Float
)

/**
 * Transformasi koordinat berskala antara ruang fisis (meter) dan layar Canvas (piksel).
 * Mempertahankan rasio aspek dan memusatkan denah di area gambar.
 */
object CoordinateTransform {

    fun calculateBounds(
        availableWidthPx: Float,
        availableHeightPx: Float,
        roomWidthM: Float,
        roomLengthM: Float,
        marginPx: Float = 32f
    ): CanvasBounds {
        val usableWidth = max(10f, availableWidthPx - 2 * marginPx)
        val usableHeight = max(10f, availableHeightPx - 2 * marginPx)

        val safeWidthM = max(0.1f, roomWidthM)
        val safeLengthM = max(0.1f, roomLengthM)

        val scale = min(usableWidth / safeWidthM, usableHeight / safeLengthM)
        val roomWidthPx = safeWidthM * scale
        val roomLengthPx = safeLengthM * scale

        val offsetX = (availableWidthPx - roomWidthPx) / 2f
        val offsetY = (availableHeightPx - roomLengthPx) / 2f

        return CanvasBounds(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            roomWidthPx = roomWidthPx,
            roomLengthPx = roomLengthPx
        )
    }

    fun meterToPixelX(meterX: Float, bounds: CanvasBounds): Float {
        return bounds.offsetX + meterX * bounds.scale
    }

    fun meterToPixelY(meterY: Float, bounds: CanvasBounds): Float {
        return bounds.offsetY + meterY * bounds.scale
    }

    fun pixelToMeterX(pixelX: Float, bounds: CanvasBounds, roomWidthM: Float): Float {
        if (bounds.scale <= 0f) return 0f
        val rawMeter = (pixelX - bounds.offsetX) / bounds.scale
        return rawMeter.coerceIn(0f, roomWidthM)
    }

    fun pixelToMeterY(pixelY: Float, bounds: CanvasBounds, roomLengthM: Float): Float {
        if (bounds.scale <= 0f) return 0f
        val rawMeter = (pixelY - bounds.offsetY) / bounds.scale
        return rawMeter.coerceIn(0f, roomLengthM)
    }

    fun isPointHit(
        touchX: Float,
        touchY: Float,
        pointX: Float,
        pointY: Float,
        tolerancePx: Float
    ): Boolean {
        return hypot(touchX - pointX, touchY - pointY) <= tolerancePx
    }
}
