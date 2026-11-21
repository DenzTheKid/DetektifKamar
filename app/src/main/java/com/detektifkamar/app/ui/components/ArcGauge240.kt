package com.detektifkamar.app.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.domain.logic.GaugeScaleCalculator
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

private class ScaleTierHolder(var lastMaxUt: Float? = null)

/**
 * Gauge analog melengkung 240° dengan spesifikasi instrumen presisi:
 * - Busur 240 derajat (mulai dari 150° ke 390°)
 * - Jarum dengan loop frame-clock non-cancelling (aman pada update sensor >60 Hz)
 * - Pembacaan digital langsung dari nilai tersaring (tidak pernah macet oleh pembatalan animasi)
 * - Skala dinamis terkalibrasi (GaugeScaleCalculator) yang bersih untuk µT, mT, dan G
 */
@Composable
fun ArcGauge240(
    valueUt: Float,
    displayUnit: UnitType,
    sensorMaxRangeUt: Float = 2000f,
    status: MagneticStatus = MagneticStatus.NORMAL,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors
    val safeValueUt = GaugeScaleCalculator.sanitizeValue(valueUt)

    // Loop animasi jarum tunggal non-cancelling menggunakan frame clock Compose
    val latestTargetUt by rememberUpdatedState(safeValueUt)
    var animatedValueUt by remember { mutableFloatStateOf(safeValueUt) }

    LaunchedEffect(Unit) {
        var hasLastFrame = false
        var lastFrameNanos = 0L
        var velocity = 0f
        val omega = 14f
        while (true) {
            withInfiniteAnimationFrameNanos { frameNanos ->
                val target = latestTargetUt
                if (!hasLastFrame) {
                    hasLastFrame = true
                    lastFrameNanos = frameNanos
                    animatedValueUt = target
                    return@withInfiniteAnimationFrameNanos
                }
                val dt = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
                lastFrameNanos = frameNanos
                if (dt > 0f) {
                    val diff = animatedValueUt - target
                    if (abs(diff) < 0.05f && abs(velocity) < 0.05f) {
                        animatedValueUt = target
                        velocity = 0f
                    } else {
                        val expTerm = exp(-omega * dt)
                        val c2 = velocity + omega * diff
                        val nextDiff = (diff + c2 * dt) * expTerm
                        velocity = (velocity - omega * c2 * dt) * expTerm
                        animatedValueUt = (target + nextDiff).coerceAtLeast(0f)
                    }
                }
            }
        }
    }

    // Hitung skala dinamis dengan histeresis dan sinkronisasi nilai jarum
    val scaleTierHolder = remember { ScaleTierHolder() }
    val gaugeScale = remember(safeValueUt, animatedValueUt, sensorMaxRangeUt, displayUnit) {
        GaugeScaleCalculator.calculate(
            valueUt = safeValueUt,
            animatedValueUt = animatedValueUt,
            sensorMaxRangeUt = sensorMaxRangeUt,
            displayUnit = displayUnit,
            previousMaxScaleUt = scaleTierHolder.lastMaxUt
        ).also { scaleTierHolder.lastMaxUt = it.maxScaleUt }
    }
    val maxScaleUt = gaugeScale.maxScaleUt

    // Animasi garis pemindai (radar sweep) di latar belakang
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val scannerAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 240f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ScannerSweepAngle"
    )

    // Pembacaan angka digital langsung dari nilai tersaring (anti-freeze pada frekuensi tinggi)
    val convertedValue = displayUnit.convertFromMicrotesla(safeValueUt)
    val formattedValue = String.format(Locale.US, "%.${displayUnit.decimalPlaces}f", convertedValue)
    val readoutFontSize = when {
        formattedValue.length >= 8 -> 40.sp
        formattedValue.length >= 7 -> 46.sp
        formattedValue.length >= 6 -> 52.sp
        else -> 58.sp
    }

    val density = LocalDensity.current
    val labelTextSizePx = with(density) { 9.sp.toPx() }
    val labelColorArgb = colors.textMuted.toArgb()
    val textPaint = remember(labelColorArgb, labelTextSizePx) {
        Paint().apply {
            color = labelColorArgb
            textSize = labelTextSizePx
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    val majorLabelsText = remember(gaugeScale) {
        gaugeScale.ticks.mapNotNull { it.label }.joinToString(",")
    }
    val scaleMaxLabel = remember(gaugeScale) {
        GaugeScaleCalculator.formatTickLabel(gaugeScale.maxScaleDisplay)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("arc_gauge")
            .semantics {
                contentDescription = "Skala 0..$scaleMaxLabel ${displayUnit.symbol}; label=[$majorLabelsText]"
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(290.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f + 15.dp.toPx())
            val radius = size.width * 0.40f

            val startAngle = 150f
            val sweepAngle = 240f

            // 1. Garis Pemindai (Scanner Sweep Line) di Latar Belakang
            val sweepCurrentAngle = (startAngle + scannerAngle) % 360f
            val radSweep = Math.toRadians(sweepCurrentAngle.toDouble())
            val sweepEnd = Offset(
                center.x + (radius - 10.dp.toPx()) * cos(radSweep).toFloat(),
                center.y + (radius - 10.dp.toPx()) * sin(radSweep).toFloat()
            )
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(colors.accentCyan.copy(alpha = 0.25f), Color.Transparent),
                    center = center,
                    radius = radius
                ),
                start = center,
                end = sweepEnd,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 2. Busur Latar Belakang Gauge (Track)
            drawArc(
                color = colors.borderOutline.copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )

            // 3. Garis Penanda Zona Medan Magnet Bumi (25 - 65 µT)
            val earthStartFraction = (25f / maxScaleUt).coerceIn(0f, 1f)
            val earthEndFraction = (65f / maxScaleUt).coerceIn(0f, 1f)
            val earthStartAngle = startAngle + (earthStartFraction * sweepAngle)
            val earthSweep = (earthEndFraction - earthStartFraction) * sweepAngle

            drawArc(
                color = colors.accentCyan.copy(alpha = 0.5f),
                startAngle = earthStartAngle,
                sweepAngle = earthSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Butt)
            )

            // 4. Tick Marks & Label Skala Terbatas (dari GaugeScaleCalculator)
            val labelRadius = radius - 20.dp.toPx()
            for (tick in gaugeScale.ticks) {
                val angleDeg = startAngle + (tick.fraction * sweepAngle)
                val rad = Math.toRadians(angleDeg.toDouble())

                val isMajor = tick.isMajor
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickColor = if (isMajor) colors.inkText.copy(alpha = 0.7f) else colors.textMuted.copy(alpha = 0.4f)

                val pOuter = Offset(
                    center.x + (radius - 2.dp.toPx()) * cos(rad).toFloat(),
                    center.y + (radius - 2.dp.toPx()) * sin(rad).toFloat()
                )
                val pInner = Offset(
                    center.x + (radius - 2.dp.toPx() - tickLength) * cos(rad).toFloat(),
                    center.y + (radius - 2.dp.toPx() - tickLength) * sin(rad).toFloat()
                )

                drawLine(
                    color = tickColor,
                    start = pInner,
                    end = pOuter,
                    strokeWidth = if (isMajor) 1.8.dp.toPx() else 1.dp.toPx()
                )

                val label = tick.label
                if (isMajor && label != null) {
                    val pLabel = Offset(
                        center.x + labelRadius * cos(rad).toFloat(),
                        center.y + labelRadius * sin(rad).toFloat() + 3.dp.toPx()
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        pLabel.x,
                        pLabel.y,
                        textPaint
                    )
                }
            }

            // 5. Gambar Jarum Penunjuk (Needle) dengan Ujung Runcing
            val valueFraction = (animatedValueUt / maxScaleUt).coerceIn(0f, 1.05f)
            val needleAngleDeg = startAngle + (valueFraction * sweepAngle)
            val needleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleLength = radius - 8.dp.toPx()

            val tip = Offset(
                center.x + needleLength * cos(needleRad).toFloat(),
                center.y + needleLength * sin(needleRad).toFloat()
            )

            // Bentuk segitiga jarum tipis runcing
            val perpAngle1 = needleRad + Math.PI / 2
            val perpAngle2 = needleRad - Math.PI / 2
            val baseRadius = 5.dp.toPx()
            val base1 = Offset(
                center.x + baseRadius * cos(perpAngle1).toFloat(),
                center.y + baseRadius * sin(perpAngle1).toFloat()
            )
            val base2 = Offset(
                center.x + baseRadius * cos(perpAngle2).toFloat(),
                center.y + baseRadius * sin(perpAngle2).toFloat()
            )

            val needlePath = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(base1.x, base1.y)
                lineTo(base2.x, base2.y)
                close()
            }

            val needleColor = colors.colorForStatus(status)
            drawPath(
                path = needlePath,
                color = needleColor
            )

            // Titik Pusat Jarum (Pivot Hub)
            drawCircle(
                color = colors.surface,
                radius = 8.dp.toPx(),
                center = center
            )
            drawCircle(
                color = needleColor,
                radius = 5.dp.toPx(),
                center = center
            )
        }

        // Tampilan Angka Digital Utama di Bawah Titik Tumpu Busur (batasi fontScale berlebih agar tidak menabrak pivot hub)
        val readoutDensity = Density(
            density = density.density,
            fontScale = density.fontScale.coerceAtMost(1.25f)
        )
        CompositionLocalProvider(LocalDensity provides readoutDensity) {
            Column(
                modifier = Modifier.offset(y = 65.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formattedValue,
                    fontFamily = PlexMonoFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = readoutFontSize,
                    lineHeight = 60.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = colors.inkText,
                    modifier = Modifier.testTag("arc_gauge_reading")
                )
                Text(
                    text = displayUnit.symbol,
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = colors.textMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.testTag("arc_gauge_unit")
                )
            }
        }
    }
}
