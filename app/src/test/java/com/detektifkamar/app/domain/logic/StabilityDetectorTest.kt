package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StabilityDetectorTest {

    @Test
    fun testIsStableWithLowStdDev() {
        // Data akselerometer yang sangat stabil di sekitar gravitasi 9.81
        val stableSamples = listOf(9.81f, 9.80f, 9.82f, 9.81f, 9.80f)
        assertTrue(StabilityDetector.isStable(stableSamples))
    }

    @Test
    fun testIsUnstableWithHighStdDev() {
        // Data akselerometer yang banyak bergerak
        val unstableSamples = listOf(9.81f, 11.5f, 8.2f, 12.0f, 7.5f)
        assertFalse(StabilityDetector.isStable(unstableSamples))
    }

    @Test
    fun testUnstableRatioCalculation() {
        val windowResults = listOf(true, true, false, true, false) // 2 dari 5 tidak stabil = 40%
        val ratio = StabilityDetector.calculateUnstableRatio(windowResults)
        assertTrue(ratio > StabilityDetector.UNSTABLE_RATIO_REJECT_THRESHOLD)
    }
}
