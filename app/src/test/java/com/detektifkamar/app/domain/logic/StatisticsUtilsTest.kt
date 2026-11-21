package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class StatisticsUtilsTest {

    @Test
    fun testMeanAndStdDev() {
        val values = listOf(10f, 20f, 30f, 40f, 50f)
        val mean = StatisticsUtils.calculateMean(values)
        assertEquals(30.0f, mean, 0.001f)

        val min = StatisticsUtils.calculateMin(values)
        assertEquals(10.0f, min, 0.001f)

        val max = StatisticsUtils.calculateMax(values)
        assertEquals(50.0f, max, 0.001f)

        val stdDev = StatisticsUtils.calculateStdDev(values, mean)
        assertEquals(15.811f, stdDev, 0.01f)
    }

    @Test
    fun testEmptyList() {
        assertEquals(0f, StatisticsUtils.calculateMean(emptyList()), 0.001f)
        assertEquals(0f, StatisticsUtils.calculateStdDev(emptyList()), 0.001f)
    }
}
