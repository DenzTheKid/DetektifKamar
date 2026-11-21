package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AzimuthUtilsTest {

    @Test
    fun testCircularMeanNearNorth() {
        // 359 derajat dan 1 derajat seharusnya menghasilkan rata-rata mendekati 0 / 360 derajat, BUKAN 180 derajat!
        val angles = listOf(359f, 1f)
        val mean = AzimuthUtils.circularMean(angles)
        // mean harus mendekati 0 atau 360
        val isNearZero = mean < 1.0f || mean > 359.0f
        assertTrue("Circular mean of 359 and 1 should be around 0 deg but was $mean", isNearZero)
    }

    @Test
    fun testShortestAngleDiff() {
        // Dari 350 ke 10: selisih terpendek adalah +20 derajat, bukan -340
        val diff1 = AzimuthUtils.shortestAngleDiff(350f, 10f)
        assertEquals(20f, diff1, 0.01f)

        // Dari 10 ke 350: selisih terpendek adalah -20 derajat
        val diff2 = AzimuthUtils.shortestAngleDiff(10f, 350f)
        assertEquals(-20f, diff2, 0.01f)
    }

    @Test
    fun testTiltValidity() {
        assertTrue(AzimuthUtils.isAzimuthValid(30f))
        assertFalse(AzimuthUtils.isAzimuthValid(45f))
        assertFalse(AzimuthUtils.isAzimuthValid(60f))
    }
}
