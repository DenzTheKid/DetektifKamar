package com.detektifkamar.app.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Gauge analog melengkung 240° dengan spesifikasi instrumen presisi:
 * - Busur 240 derajat (mulai dari 150° ke 390°)
 * - Jarum dengan animasi pegas halus (spring: dampingRatio = 0.75, stiffness = StiffnessMediumLow)
 * - Tick mark tipis tiap 10, tebal tiap 50, dan garis rentang medan bumi (25–65 µT)
 * - Garis pemindai (scanner line) yang bergerak perlahan di latar gauge
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

    // Tentukan batas atas skala gauge yang proporsional untuk visualisasi (dinamis hingga 300 µT, atau lebih jika tinggi)
    val maxScaleUt = remember(valueUt) {
        when {
            valueUt <= 200f -> 200f
            valueUt <= 500f -> 500f
            valueUt <= 1000f -> 1000f
            else -> sensorMaxRangeUt.coerceAtLeast(1000f)
        }
    }

    // Animasi pergerakan jarum dengan spesifikasi Spring (damping 0.75, medium-low stiffness)
    val animatedValue = remember { Animatable(valueUt) }
    LaunchedEffect(valueUt) {
        animatedValue.animateTo(
            targetValue = valueUt,
            animationSpec = spring(
                dampingRatio = 0.75f,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

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

    // Konversi nilai utama ke satuan yang dipilih
    val convertedValue = displayUnit.convertFromMicrotesla(animatedValue.value)
    val formattedValue = String.format(Locale.US, "%.${displayUnit.decimalPlaces}f", convertedValue)

    Box(
        modifier = modifier.fillMaxWidth(),
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

            // 4. Tick Marks (Setiap 10 satuan dan 50 satuan)
            val step = if (maxScaleUt <= 200f) 10 else if (maxScaleUt <= 500f) 25 else 50
            val totalTicks = (maxScaleUt / step).toInt()

            val textPaint = Paint().apply {
                color = colors.textMuted.toArgb()
                textSize = 9.sp.toPx()
                typeface = Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }

            for (i in 0..totalTicks) {
                val tickVal = i * step
                val fraction = tickVal.toFloat() / maxScaleUt
                val angleDeg = startAngle + (fraction * sweepAngle)
                val rad = Math.toRadians(angleDeg.toDouble())

                val isMajor = (tickVal % (step * 2) == 0)
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

                // Label numerik untuk tick mayor
                if (isMajor) {
                    val labelRadius = radius - 20.dp.toPx()
                    val pLabel = Offset(
                        center.x + labelRadius * cos(rad).toFloat(),
                        center.y + labelRadius * sin(rad).toFloat() + 3.dp.toPx()
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        tickVal.toString(),
                        pLabel.x,
                        pLabel.y,
                        textPaint
                    )
                }
            }

            // 5. Gambar Jarum Penunjuk (Needle) dengan Ujung Runcing
            val valueFraction = (animatedValue.value / maxScaleUt).coerceIn(0f, 1.05f)
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

        // Tampilan Angka Digital Utama di Bawah Titik Tumpu Busur
        Column(
            modifier = Modifier
                .offset(y = 65.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedValue,
                fontFamily = PlexMonoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 58.sp,
                lineHeight = 60.sp,
                color = colors.inkText
            )
            Text(
                text = displayUnit.symbol,
                fontFamily = PlexSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = colors.textMuted,
                letterSpacing = 1.sp
            )
        }
    }
}
