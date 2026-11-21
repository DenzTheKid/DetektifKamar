package com.detektifkamar.app.domain.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NumberInputParserTest {

    @Test
    fun testValidInputsWithDotAndComma() {
        assertEquals(3.5f, NumberInputParser.parseDimension("3.5").getOrThrow(), 0.001f)
        assertEquals(3.5f, NumberInputParser.parseDimension("3,5").getOrThrow(), 0.001f)
        assertEquals(10.0f, NumberInputParser.parseDimension("  10  ").getOrThrow(), 0.001f)
    }

    @Test
    fun testOutOfRangeInputs() {
        assertTrue(NumberInputParser.parseDimension("0.3").isFailure) // < 0.5m
        assertTrue(NumberInputParser.parseDimension("35").isFailure)  // > 30m
        assertTrue(NumberInputParser.parseDimension("").isFailure)
        assertTrue(NumberInputParser.parseDimension("abc").isFailure)
    }
}
