package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.MagneticStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class MagneticClassifierTest {

    @Test
    fun testNormalField() {
        val status = MagneticClassifier.classify(45.0f, 2000f)
        assertEquals(MagneticStatus.NORMAL, status)
    }

    @Test
    fun testSedangField() {
        val status = MagneticClassifier.classify(120.0f, 2000f)
        assertEquals(MagneticStatus.SEDANG, status)
    }

    @Test
    fun testTinggiField() {
        val status = MagneticClassifier.classify(350.0f, 2000f)
        assertEquals(MagneticStatus.TINGGI, status)
    }

    @Test
    fun testSangatTinggiField() {
        val status = MagneticClassifier.classify(850.0f, 2000f)
        assertEquals(MagneticStatus.SANGAT_TINGGI, status)
    }

    @Test
    fun testSaturasiField() {
        // Sensor maximumRange is 2000 µT, magnitude is 1990 µT (>= 98%)
        val status = MagneticClassifier.classify(1990.0f, 2000f)
        assertEquals(MagneticStatus.SATURASI, status)
    }
}
