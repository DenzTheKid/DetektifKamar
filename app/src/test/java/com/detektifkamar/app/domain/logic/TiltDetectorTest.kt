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

    @Test
    fun testHysteresisPreventsJitterInDeadband() {
        // 42 degrees tilt: between 38° exit bound and 48° enter bound
        val ay = 6.56f
        val az = 7.29f
        
        // 1. If previously flat (wasTilted = false), 42° does not trigger tilted
        val stateWhenFlat = TiltDetector.detectTilt(0f, ay, az, wasTilted = false)
        assertTrue(stateWhenFlat.tiltAngle in 41f..43f)
        assertFalse(stateWhenFlat.isTilted)

        // 2. If previously tilted (wasTilted = true), 42° remains tilted until under 38°
        val stateWhenTilted = TiltDetector.detectTilt(0f, ay, az, wasTilted = true)
        assertTrue(stateWhenTilted.tiltAngle in 41f..43f)
        assertTrue(stateWhenTilted.isTilted)

        // 3. When tilt drops below 38° (e.g. ~35°), it safely exits tilted state
        val ayFlat = 5.63f
        val azFlat = 8.04f
        val stateRecovered = TiltDetector.detectTilt(0f, ayFlat, azFlat, wasTilted = true)
        assertTrue(stateRecovered.tiltAngle < 38f)
        assertFalse(stateRecovered.isTilted)
    }
}
