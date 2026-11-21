package com.detektifkamar.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.domain.logic.DisturbanceClassifier
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.HeatmapColors
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pengukur dial analog-digital modern dengan presisi tinggi dan respons animasi pegas halus.
 */
@Composable
fun VintageAnalogMeter(
    valueUt: Float,
    baselineUt: Float = 45.0f,
    modifier: Modifier = Modifier,
    maxScaleUt: Float = 120.0f
) {
    val colors = DetectiveTheme.colors
    val deltaUt = DisturbanceClassifier.calculateDelta(valueUt, baselineUt)
    val category = DisturbanceClassifier.classify(deltaUt)

    val animatedValue by animateFloatAsState(
        targetValue = valueUt.coerceIn(0f, maxScaleUt),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "MeterNeedleAnimation"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
                val width = size.width
                val height = size.height

                val pivotX = width / 2f
                val pivotY = height * 0.90f
                val radius = height * 0.78f

                val startAngle = 190f
                val sweepAngle = 160f

                // Latar belakang busur rel skala
                drawArc(
                    color = colors.borderOutline.copy(alpha = 0.35f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(pivotX - radius, pivotY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Segmen warna ambang presisi
                val deltaToAngle = sweepAngle / maxScaleUt
                val angleTenang = (15f * deltaToAngle).coerceAtMost(sweepAngle)
                val angleRingan = (25f * deltaToAngle).coerceAtMost(sweepAngle - angleTenang)
                val angleKuat = (40f * deltaToAngle).coerceAtMost(sweepAngle - angleTenang - angleRingan)
                val angleDekat = (sweepAngle - angleTenang - angleRingan - angleKuat).coerceAtLeast(0f)

                var currentArcStart = startAngle
                drawArc(
                    color = colors.labGreen,
                    startAngle = currentArcStart,
                    sweepAngle = angleTenang,
                    useCenter = false,
                    topLeft = Offset(pivotX - radius, pivotY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                currentArcStart += angleTenang

                drawArc(
                    color = colors.highlighterYellow,
                    startAngle = currentArcStart,
                    sweepAngle = angleRingan,
                    useCenter = false,
                    topLeft = Offset(pivotX - radius, pivotY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8.dp.toPx())
                )
                currentArcStart += angleRingan

                drawArc(
                    color = HeatmapColors.GangguanKuat,
                    startAngle = currentArcStart,
                    sweepAngle = angleKuat,
                    useCenter = false,
                    topLeft = Offset(pivotX - radius, pivotY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8.dp.toPx())
                )
                currentArcStart += angleKuat

                drawArc(
                    color = colors.stampRed,
                    startAngle = currentArcStart,
                    sweepAngle = angleDekat,
                    useCenter = false,
                    topLeft = Offset(pivotX - radius, pivotY - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )

                // Garis centang skala (ticks)
                val tickCount = 16
                for (i in 0..tickCount) {
                    val tickFraction = i.toFloat() / tickCount
                    val tickAngle = startAngle + tickFraction * sweepAngle
                    val tickRad = Math.toRadians(tickAngle.toDouble())

                    val isMajor = i % 4 == 0
                    val innerR = if (isMajor) radius - 16.dp.toPx() else radius - 10.dp.toPx()
                    val outerR = radius - 4.dp.toPx()

                    val startX = pivotX + (innerR * cos(tickRad)).toFloat()
                    val startY = pivotY + (innerR * sin(tickRad)).toFloat()
                    val endX = pivotX + (outerR * cos(tickRad)).toFloat()
                    val endY = pivotY + (outerR * sin(tickRad)).toFloat()

                    drawLine(
                        color = if (isMajor) colors.inkText else colors.textMuted.copy(alpha = 0.6f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = if (isMajor) 2.dp.toPx() else 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Gambar Jarum Meter Presisi
                val needleFraction = (animatedValue / maxScaleUt).coerceIn(0f, 1f)
                val needleAngle = startAngle + needleFraction * sweepAngle
                val needleRad = Math.toRadians(needleAngle.toDouble())

                val needleLength = radius * 0.90f
                val needleTipX = pivotX + (needleLength * cos(needleRad)).toFloat()
                val needleTipY = pivotY + (needleLength * sin(needleRad)).toFloat()

                // Jarum
                drawLine(
                    color = colors.cyanotypeBlue,
                    start = Offset(pivotX, pivotY),
                    end = Offset(needleTipX, needleTipY),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Poros Jarum (Center Pivot)
                drawCircle(
                    color = colors.surface,
                    radius = 9.dp.toPx(),
                    center = Offset(pivotX, pivotY)
                )
                drawCircle(
                    color = colors.cyanotypeBlue,
                    radius = 5.dp.toPx(),
                    center = Offset(pivotX, pivotY)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Nilai Digital Besar
        Text(
            text = "%.1f µT".format(valueUt),
            fontFamily = IbmPlexMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 36.sp,
            color = colors.inkText,
            letterSpacing = (-0.5).sp
        )
    }
}
