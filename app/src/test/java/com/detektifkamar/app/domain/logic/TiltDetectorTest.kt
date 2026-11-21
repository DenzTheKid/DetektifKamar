package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TiltDetectorTest {

    @Test
    fun testPerfectFlatPosition() {
        // Laying flat on table, gravity is entirely along Z axis (9.81 m/s²)
        val state = TiltDetector.detectTilt(0f, 0f, 9.81f)
        assertEquals(0f, state.tiltAngle, 0.1f)
        assertFalse(state.isTilted)
    }

    @Test
    fun testSlightTiltWithinSafeLimit() {
        // Tilted ~15 degrees
        val ax = 2.5f
        val ay = 0f
        val az = 9.49f
        val state = TiltDetector.detectTilt(ax, ay, az)
        assertTrue(state.tiltAngle < 45f)
        assertFalse(state.isTilted)
    }

    @Test
    fun testExcessiveTiltDetected() {
        // Phone held upright (gravity mainly along Y axis)
        val ax = 0f
        val ay = 9.81f
        val az = 0.5f
        val state = TiltDetector.detectTilt(ax, ay, az)
        assertTrue(state.tiltAngle > 45f)
        assertTrue(state.isTilted)
    }
}
