package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinateTransformTest {

    @Test
    fun testMeterToPixelAndBackRoundtrip() {
        val roomWidthM = 4.0f
        val roomLengthM = 6.0f
        val canvasWidthPx = 800f
        val canvasHeightPx = 1200f
        val marginPx = 40f

        val bounds = CoordinateTransform.calculateBounds(
            availableWidthPx = canvasWidthPx,
            availableHeightPx = canvasHeightPx,
            roomWidthM = roomWidthM,
            roomLengthM = roomLengthM,
            marginPx = marginPx
        )

        val originalX = 2.5f
        val originalY = 4.2f

        val px = CoordinateTransform.meterToPixelX(originalX, bounds)
        val py = CoordinateTransform.meterToPixelY(originalY, bounds)

        val reconstructedX = CoordinateTransform.pixelToMeterX(px, bounds, roomWidthM)
        val reconstructedY = CoordinateTransform.pixelToMeterY(py, bounds, roomLengthM)

        assertEquals(originalX, reconstructedX, 0.001f)
        assertEquals(originalY, reconstructedY, 0.001f)
    }

    @Test
    fun testHitTest() {
        assertTrue(CoordinateTransform.isPointHit(100f, 100f, 105f, 102f, 24f))
        assertTrue(!CoordinateTransform.isPointHit(100f, 100f, 140f, 150f, 24f))
    }
}
