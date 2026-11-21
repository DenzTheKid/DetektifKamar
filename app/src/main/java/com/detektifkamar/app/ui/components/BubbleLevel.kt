package com.detektifkamar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Komponen waterpass / bubble level bulat untuk memeriksa kemiringan fisik HP saat mengukur.
 */
@Composable
fun BubbleLevel(
    tiltDeg: Float,
    modifier: Modifier = Modifier.size(54.dp),
    pitchDeg: Float = 0f,
    rollDeg: Float = 0f
) {
    val colors = DetectiveTheme.colors
    val isLevel = tiltDeg < 5.0f

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val outerRadius = size.minDimension / 2f - 2.dp.toPx()
        val innerRadius = outerRadius * 0.35f

        // Lingkaran luar waterpass
        drawCircle(
            color = colors.borderOutline,
            radius = outerRadius,
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Lingkaran target tengah
        drawCircle(
            color = if (isLevel) colors.labGreen else colors.textMuted,
            radius = innerRadius,
            center = center,
            style = Stroke(width = 1.2.dp.toPx())
        )

        // Garis bidik silang
        drawLine(
            color = colors.borderOutline,
            start = Offset(center.x - outerRadius, center.y),
            end = Offset(center.x + outerRadius, center.y),
            strokeWidth = 0.8.dp.toPx()
        )
        drawLine(
            color = colors.borderOutline,
            start = Offset(center.x, center.y - outerRadius),
            end = Offset(center.x, center.y + outerRadius),
            strokeWidth = 0.8.dp.toPx()
        )

        // Posisi gelembung (bubble)
        val maxOffset = outerRadius - 6.dp.toPx()
        val tiltFactor = (tiltDeg / 45f).coerceIn(0f, 1f) * maxOffset
        val angleRad = Math.toRadians(rollDeg.toDouble())

        val bubbleX = center.x + (tiltFactor * sin(angleRad)).toFloat()
        val bubbleY = center.y + (tiltFactor * cos(angleRad)).toFloat()

        drawCircle(
            color = if (isLevel) colors.labGreen else colors.highlighterYellow,
            radius = 5.dp.toPx(),
            center = Offset(bubbleX, bubbleY)
        )
    }
}
