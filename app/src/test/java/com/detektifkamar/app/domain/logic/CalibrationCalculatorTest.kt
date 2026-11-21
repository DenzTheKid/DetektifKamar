package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.CalibrationQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibrationCalculatorTest {

    @Test
    fun testInsufficientSamplesReturnsUlang() {
        val samples = listOf(
            CalibrationCalculator.Sample3D(10f, 20f, 30f),
            CalibrationCalculator.Sample3D(12f, 22f, 32f)
        )
        val result = CalibrationCalculator.calculate(samples)
        assertEquals(CalibrationQuality.ULANG, result.quality)
    }

    @Test
    fun testWellDistributedSphereSamplesReturnsBaik() {
        // Synthesize samples along a sphere radius 45 µT centered at offset (10, -15, 25)
        val trueOffsetX = 10f
        val trueOffsetY = -15f
        val trueOffsetZ = 25f
        val radius = 45f

        val samples = mutableListOf<CalibrationCalculator.Sample3D>()
        for (theta in 0..360 step 20) {
            val radT = Math.toRadians(theta.toDouble())
            for (phi in -60..60 step 30) {
                val radP = Math.toRadians(phi.toDouble())
                val x = (trueOffsetX + radius * Math.cos(radP) * Math.cos(radT)).toFloat()
                val y = (trueOffsetY + radius * Math.cos(radP) * Math.sin(radT)).toFloat()
                val z = (trueOffsetZ + radius * Math.sin(radP)).toFloat()
                samples.add(CalibrationCalculator.Sample3D(x, y, z))
            }
        }

        val result = CalibrationCalculator.calculate(samples)
        assertEquals(CalibrationQuality.BAIK, result.quality)
        assertEquals(trueOffsetX, result.biasX, 2.0f)
        assertEquals(trueOffsetY, result.biasY, 2.0f)
        assertEquals(trueOffsetZ, result.biasZ, 2.0f)
    }
}
