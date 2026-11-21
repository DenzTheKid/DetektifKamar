package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testConvertMicrotesla() {
        val ut = 150.5f
        assertEquals(150.5f, UnitConverter.convert(ut, UnitType.MICROTESLA), 0.001f)
        assertEquals("150.5 µT", UnitConverter.format(ut, UnitType.MICROTESLA))
    }

    @Test
    fun testConvertMillitesla() {
        val ut = 2500f
        // 1 mT = 1000 µT -> 2500 µT = 2.5 mT
        assertEquals(2.5f, UnitConverter.convert(ut, UnitType.MILLITESLA), 0.001f)
        assertEquals("2.500 mT", UnitConverter.format(ut, UnitType.MILLITESLA))
    }

    @Test
    fun testConvertGauss() {
        val ut = 100f
        // 1 G = 100 µT -> 100 µT = 1.0 G
        assertEquals(1.0f, UnitConverter.convert(ut, UnitType.GAUSS), 0.001f)
        assertEquals("1.00 G", UnitConverter.format(ut, UnitType.GAUSS))
    }

    @Test
    fun testRoundTripConversion() {
        val originalUt = 48.7f
        val inGauss = UnitConverter.convert(originalUt, UnitType.GAUSS)
        val backToUt = UnitConverter.convertToUt(inGauss, UnitType.GAUSS)
        assertEquals(originalUt, backToUt, 0.01f)
    }
}
