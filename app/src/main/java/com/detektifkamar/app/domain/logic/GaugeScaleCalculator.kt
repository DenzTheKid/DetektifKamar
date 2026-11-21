package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.UnitType
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

data class GaugeTick(
    val fraction: Float,
    val valueDisplay: Float,
    val isMajor: Boolean,
    val label: String?
)

data class GaugeScale(
    val maxScaleUt: Float,
    val maxScaleDisplay: Float,
    val majorStepDisplay: Float,
    val minorStepDisplay: Float,
    val ticks: List<GaugeTick>
)

/**
 * Kalkulator skala murni (pure Kotlin) untuk ArcGauge240.
 * - Menjaga rentang dinamis bertingkat (200 / 500 / 1000 / sensorMaxRangeUt) dengan histeresis ~15%.
 * - Menyinkronkan batas atas dengan animatedValueUt agar jarum tidak mentok saat medan turun drastis.
 * - Memilih langkah mayor "nice number" ({1, 2, 2.5, 5} * 10^k) sesuai displayUnit (µT / mT / G),
 *   menjamin jumlah label mayor selalu terjaga (6..11 label) tanpa ledakan angka.
 */
object GaugeScaleCalculator {

    const val DEFAULT_MAX_RANGE_UT = 2000f
    private const val HYSTERESIS_FACTOR = 0.85f
    private val NICE_MANTISSAS = doubleArrayOf(1.0, 2.0, 2.5, 5.0)

    fun sanitizeValue(valueUt: Float): Float {
        return if (valueUt.isFinite()) valueUt.coerceAtLeast(0f) else 0f
    }

    fun sanitizeMaxRange(sensorMaxRangeUt: Float): Float {
        return if (sensorMaxRangeUt.isFinite() && sensorMaxRangeUt > 0f) {
            sensorMaxRangeUt
        } else {
            DEFAULT_MAX_RANGE_UT
        }
    }

    fun computeMaxScaleUt(
        valueUt: Float,
        animatedValueUt: Float = valueUt,
        sensorMaxRangeUt: Float = DEFAULT_MAX_RANGE_UT,
        previousMaxScaleUt: Float? = null
    ): Float {
        val safeValue = sanitizeValue(valueUt)
        val safeAnimated = sanitizeValue(animatedValueUt)
        val safeMaxRange = sanitizeMaxRange(sensorMaxRangeUt)
        val effectiveValue = maxOf(safeValue, safeAnimated)

        val tier1 = minOf(200f, safeMaxRange)
        val tier2 = minOf(500f, safeMaxRange)
        val tier3 = minOf(1000f, safeMaxRange)
        val tier4 = safeMaxRange

        val upwardTier = when {
            effectiveValue <= tier1 -> tier1
            effectiveValue <= tier2 -> tier2
            effectiveValue <= tier3 -> tier3
            else -> tier4
        }

        if (previousMaxScaleUt != null && previousMaxScaleUt.isFinite() && previousMaxScaleUt > 0f) {
            val clampedPrev = previousMaxScaleUt.coerceAtMost(safeMaxRange)
            if (upwardTier < clampedPrev) {
                val heldTier = when {
                    clampedPrev >= tier4 - 0.01f && tier4 > tier3 && effectiveValue >= 1000f * HYSTERESIS_FACTOR -> tier4
                    abs(clampedPrev - tier3) <= 0.01f && tier3 > tier2 && effectiveValue >= 500f * HYSTERESIS_FACTOR -> tier3
                    abs(clampedPrev - tier2) <= 0.01f && tier2 > tier1 && effectiveValue >= 200f * HYSTERESIS_FACTOR -> tier2
                    else -> upwardTier
                }
                return maxOf(upwardTier, heldTier)
            }
        }

        return upwardTier
    }

    fun calculate(
        valueUt: Float,
        animatedValueUt: Float = valueUt,
        sensorMaxRangeUt: Float = DEFAULT_MAX_RANGE_UT,
        displayUnit: UnitType = UnitType.MICROTESLA,
        previousMaxScaleUt: Float? = null
    ): GaugeScale {
        val maxScaleUt = computeMaxScaleUt(
            valueUt = valueUt,
            animatedValueUt = animatedValueUt,
            sensorMaxRangeUt = sensorMaxRangeUt,
            previousMaxScaleUt = previousMaxScaleUt
        )

        val maxDisplayD = displayUnit.convertFromMicrotesla(maxScaleUt).toDouble()
            .coerceAtLeast(1e-6)
        val majorStepD = findNiceMajorStep(maxDisplayD)
        val minorStepD = majorStepD / 2.0

        val totalTicks = floor((maxDisplayD / minorStepD) + 1e-6).toInt().coerceIn(2, 40)
        val ticks = ArrayList<GaugeTick>(totalTicks + 1)

        for (i in 0..totalTicks) {
            val tickDisplayD = i * minorStepD
            val tickDisplay = tickDisplayD.toFloat()
            val tickUt = displayUnit.convertToMicrotesla(tickDisplay)
            val fraction = (tickUt / maxScaleUt).coerceIn(0f, 1f)
            val isMajor = (i % 2 == 0)
            ticks.add(
                GaugeTick(
                    fraction = fraction,
                    valueDisplay = tickDisplay,
                    isMajor = isMajor,
                    label = if (isMajor) formatTickLabel(tickDisplay) else null
                )
            )
        }

        return GaugeScale(
            maxScaleUt = maxScaleUt,
            maxScaleDisplay = maxDisplayD.toFloat(),
            majorStepDisplay = majorStepD.toFloat(),
            minorStepDisplay = minorStepD.toFloat(),
            ticks = ticks
        )
    }

    private fun findNiceMajorStep(maxDisplay: Double): Double {
        val baseExp = floor(log10(maxDisplay)).toInt()
        for (exp in (baseExp - 2)..(baseExp + 2)) {
            val factor = 10.0.pow(exp)
            for (mantissa in NICE_MANTISSAS) {
                val candidate = mantissa * factor
                if (maxDisplay / candidate <= 10.0001) {
                    return candidate
                }
            }
        }
        return maxDisplay / 5.0
    }

    fun formatTickLabel(value: Float): String {
        if (!value.isFinite() || abs(value) < 1e-6f) return "0"
        val roundedInt = round(value.toDouble())
        if (abs(value.toDouble() - roundedInt) < 1e-4) {
            return roundedInt.toLong().toString()
        }
        val formatted = String.format(Locale.US, "%.4f", value)
            .trimEnd('0')
            .trimEnd('.')
        return if (formatted == "-0" || formatted.isEmpty()) "0" else formatted
    }
}
