package com.detektifkamar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import kotlin.math.max

/**
 * Grafik gelombang real-time 10 detik dengan gradien area transparan dan kisi presisi.
 */
@Composable
fun TraceGraph(
    samples: List<Float>,
    modifier: Modifier = Modifier,
    minY: Float = 20f,
    maxY: Float = 100f
) {
    val colors = DetectiveTheme.colors
    val chartShape = RoundedCornerShape(12.dp)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(chartShape)
            .background(colors.surface)
            .border(1.dp, colors.borderOutline.copy(alpha = 0.6f), chartShape)
    ) {
        val width = size.width
        val height = size.height

        // Garis kisi horizontal
        val gridLines = 3
        for (i in 1..gridLines) {
            val y = (height / (gridLines + 1)) * i
            drawLine(
                color = colors.gridLine.copy(alpha = 0.7f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (samples.size < 2) return@Canvas

        val safeRange = max(1f, maxY - minY)
        val strokePath = Path()
        val fillPath = Path()

        val stepX = width / (samples.size - 1)

        samples.forEachIndexed { index, value ->
            val clampedVal = value.coerceIn(minY, maxY)
            val normalizedY = 1.0f - ((clampedVal - minY) / safeRange)
            val x = index * stepX
            val y = normalizedY * height

            if (index == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Area gradien di bawah kurva
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.cyanotypeBlue.copy(alpha = 0.25f),
                    colors.cyanotypeBlue.copy(alpha = 0.02f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Garis sinyal utama
        drawPath(
            path = strokePath,
            color = colors.cyanotypeBlue,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}
