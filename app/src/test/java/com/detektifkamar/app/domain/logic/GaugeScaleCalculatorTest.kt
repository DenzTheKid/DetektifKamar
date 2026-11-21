package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GaugeScaleCalculatorTest {

    @Test
    fun baselineMicrotesla_uses0To200ScaleWith11CleanMajorLabels() {
        val scale = GaugeScaleCalculator.calculate(
            valueUt = 45f,
            animatedValueUt = 45f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.MICROTESLA
        )

        assertEquals(200f, scale.maxScaleUt, 0.01f)
        val majorLabels = scale.ticks.filter { it.isMajor }.mapNotNull { it.label }
        assertEquals(
            listOf("0", "20", "40", "60", "80", "100", "120", "140", "160", "180", "200"),
            majorLabels
        )
    }

    @Test
    fun midTiers_500And1000_preserveClean11MajorLabels() {
        val scale500 = GaugeScaleCalculator.calculate(
            valueUt = 320f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.MICROTESLA
        )
        assertEquals(500f, scale500.maxScaleUt, 0.01f)
        assertEquals(
            listOf("0", "50", "100", "150", "200", "250", "300", "350", "400", "450", "500"),
            scale500.ticks.filter { it.isMajor }.mapNotNull { it.label }
        )

        val scale1000 = GaugeScaleCalculator.calculate(
            valueUt = 750f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.MICROTESLA
        )
        assertEquals(1000f, scale1000.maxScaleUt, 0.01f)
        assertEquals(
            listOf("0", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000"),
            scale1000.ticks.filter { it.isMajor }.mapNotNull { it.label }
        )
    }

    @Test
    fun highRangeSensors_neverOverflowLabelsAndStayWithinSensorMaxRange() {
        val maxRanges = listOf(2000f, 4912f, 8000f, 32768f)
        for (maxRange in maxRanges) {
            val scale = GaugeScaleCalculator.calculate(
                valueUt = 1500f,
                animatedValueUt = 1500f,
                sensorMaxRangeUt = maxRange,
                displayUnit = UnitType.MICROTESLA
            )

            assertEquals(maxRange, scale.maxScaleUt, 0.01f)
            val majorTicks = scale.ticks.filter { it.isMajor }
            assertTrue(
                "Major tick count ${majorTicks.size} out of bounds for $maxRange",
                majorTicks.size in 6..11
            )
            assertTrue(scale.ticks.all { it.fraction in 0f..1f })
            assertTrue(majorTicks.all { it.valueDisplay <= maxRange + 0.01f })
            for (i in 1 until majorTicks.size) {
                assertTrue(majorTicks[i].valueDisplay > majorTicks[i - 1].valueDisplay)
            }
        }
    }

    @Test
    fun unitConversions_produceBoundedCleanLabelsInMtAndGauss() {
        val mt200 = GaugeScaleCalculator.calculate(
            valueUt = 45f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.MILLITESLA
        )
        val mt200Labels = mt200.ticks.filter { it.isMajor }.mapNotNull { it.label }
        assertEquals("0", mt200Labels.first())
        assertEquals("0.2", mt200Labels.last())
        assertEquals(11, mt200Labels.size)

        val g200 = GaugeScaleCalculator.calculate(
            valueUt = 45f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.GAUSS
        )
        val g200Labels = g200.ticks.filter { it.isMajor }.mapNotNull { it.label }
        assertEquals("0", g200Labels.first())
        assertEquals("2", g200Labels.last())
        assertEquals(11, g200Labels.size)

        val mt4912 = GaugeScaleCalculator.calculate(
            valueUt = 1500f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.MILLITESLA
        )
        val mt4912Labels = mt4912.ticks.filter { it.isMajor }.mapNotNull { it.label }
        assertTrue(mt4912Labels.size in 6..11)
        assertEquals("4.5", mt4912Labels.last())

        val g4912 = GaugeScaleCalculator.calculate(
            valueUt = 1500f,
            sensorMaxRangeUt = 4912f,
            displayUnit = UnitType.GAUSS
        )
        val g4912Labels = g4912.ticks.filter { it.isMajor }.mapNotNull { it.label }
        assertTrue(g4912Labels.size in 6..11)
        assertEquals("45", g4912Labels.last())
    }

    @Test
    fun hysteresis_preventsFlickerNearTierBoundaries() {
        val up201 = GaugeScaleCalculator.computeMaxScaleUt(
            valueUt = 201f,
            sensorMaxRangeUt = 4912f,
            previousMaxScaleUt = 200f
        )
        assertEquals(500f, up201, 0.01f)

        val hold500At195 = GaugeScaleCalculator.computeMaxScaleUt(
            valueUt = 195f,
            sensorMaxRangeUt = 4912f,
            previousMaxScaleUt = up201
        )
        assertEquals(500f, hold500At195, 0.01f)

        val dropBelow170 = GaugeScaleCalculator.computeMaxScaleUt(
            valueUt = 160f,
            sensorMaxRangeUt = 4912f,
            previousMaxScaleUt = up201
        )
        assertEquals(200f, dropBelow170, 0.01f)

        val up1001 = GaugeScaleCalculator.computeMaxScaleUt(
            valueUt = 1001f,
            sensorMaxRangeUt = 4912f,
            previousMaxScaleUt = 1000f
        )
        assertEquals(4912f, up1001, 0.01f)

        val hold4912At950 = GaugeScaleCalculator.computeMaxScaleUt(
            valueUt = 950f,
            sensorMaxRangeUt = 4912f,
            previousMaxScaleUt = up1001
        )
        assertEquals(4912f, hold4912At950, 0.01f)
    }

    @Test
    fun animationSync_preventsNeedlePeggingWhenFieldDropsFromHighToAmbient() {
        val maxScale = GaugeScaleCalculator.computeMaxScaleUt(
            valueUt = 45f,
            animatedValueUt = 1000f,
            sensorMaxRangeUt = 4912f,
            previousMaxScaleUt = 4912f
        )
        assertTrue(maxScale >= 1000f)
        val needleFraction = 1000f / maxScale
        assertTrue(needleFraction <= 1.0f)
    }

    @Test
    fun invalidAndSmallRangeInputs_areSanitizedGracefully() {
        val nanScale = GaugeScaleCalculator.calculate(
            valueUt = Float.NaN,
            animatedValueUt = Float.NEGATIVE_INFINITY,
            sensorMaxRangeUt = Float.POSITIVE_INFINITY,
            displayUnit = UnitType.MICROTESLA
        )
        assertEquals(200f, nanScale.maxScaleUt, 0.01f)
        assertTrue(nanScale.ticks.size in 6..25)

        val smallRangeScale = GaugeScaleCalculator.calculate(
            valueUt = 180f,
            animatedValueUt = 180f,
            sensorMaxRangeUt = 150f,
            displayUnit = UnitType.MICROTESLA
        )
        assertEquals(150f, smallRangeScale.maxScaleUt, 0.01f)
        assertTrue(smallRangeScale.ticks.filter { it.isMajor }.size in 6..11)
    }
}
