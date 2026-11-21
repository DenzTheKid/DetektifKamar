package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IdwInterpolatorTest {

    @Test
    fun testLessThanThreePointsReturnsNull() {
        val points = listOf(
            SamplePoint(1.0f, 1.0f, 10.0f),
            SamplePoint(2.0f, 2.0f, 20.0f)
        )
        val result = IdwInterpolator.computeGrid(4.0f, 4.0f, points)
        assertNull("IDW interpolator should return null when points count < 3", result)
    }

    @Test
    fun testThreePointsInterpolationGeneratesGrid() {
        val points = listOf(
            SamplePoint(1.0f, 1.0f, 10.0f),
            SamplePoint(2.0f, 2.0f, 20.0f),
            SamplePoint(1.0f, 2.0f, 15.0f)
        )
        val result = IdwInterpolator.computeGrid(3.0f, 3.0f, points)
        assertNotNull(result)
        assertTrue(result!!.cols > 0)
        assertTrue(result.rows > 0)
        assertEquals(result.cols * result.rows, result.values.size)
    }

    @Test
    fun testPointsFartherThanRadiusAreMarkedNaN() {
        // Ruangan 10x10m dengan 3 titik terkumpul di pojok kiri bawah (0.5, 0.5)
        val points = listOf(
            SamplePoint(0.5f, 0.5f, 10.0f),
            SamplePoint(0.6f, 0.6f, 12.0f),
            SamplePoint(0.5f, 0.7f, 11.0f)
        )
        val result = IdwInterpolator.computeGrid(10.0f, 10.0f, points)
        assertNotNull(result)
        // Pojok kanan atas (9.5, 9.5) berjarak > 1.5m dari semua titik, harus NaN
        val lastIndex = result!!.values.size - 1
        assertTrue("Cell far away from all points should be NaN", result.values[lastIndex].isNaN())
    }
}
