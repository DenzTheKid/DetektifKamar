package com.detektifkamar.app.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.ui.theme.DetectiveTheme
import java.util.Locale

/**
 * Grafik osiloskop sinyal medan magnet selama 10 detik.
 *
 * @param samples Daftar sampel data terbaru (maksimal 100 sampel ~10 detik pada sampling 10Hz)
 * @param maxUt Batas atas tampilan sumbu vertikal
 */
@Composable
fun OscilloscopeGraph(
    samples: List<Float>,
    modifier: Modifier = Modifier,
    maxUt: Float = 150f
) {
    val colors = DetectiveTheme.colors

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val w = size.width
        val h = size.height
        val paddingLeft = 32.dp.toPx()
        val paddingBottom = 18.dp.toPx()
        val graphW = w - paddingLeft
        val graphH = h - paddingBottom

        // 1. Grid Horizontal dan Sumbu Nilai
        val gridLines = 4
        val textPaint = Paint().apply {
            color = colors.textMuted.toArgb()
            textSize = 9.sp.toPx()
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        for (i in 0..gridLines) {
            val y = graphH - (i * graphH / gridLines)
            val gridVal = (i * maxUt / gridLines)

            // Garis kisi horizontal tipis
            drawLine(
                color = colors.borderOutline.copy(alpha = 0.4f),
                start = Offset(paddingLeft, y),
                end = Offset(w, y),
                strokeWidth = 0.8.dp.toPx()
            )

            // Label angka di sumbu Y
            drawContext.canvas.nativeCanvas.drawText(
                String.format(Locale.US, "%.0f", gridVal),
                paddingLeft - 6.dp.toPx(),
                y + 3.dp.toPx(),
                textPaint
            )
        }

        // 2. Garis Ambang Batas Medan Normal / Geomagnetik Bumi (65 µT)
        if (65f <= maxUt) {
            val earthY = graphH - ((65f / maxUt) * graphH)
            drawLine(
                color = colors.accentCyan.copy(alpha = 0.5f),
                start = Offset(paddingLeft, earthY),
                end = Offset(w, earthY),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (samples.size < 2) return@Canvas

        // 3. Path Garis Sinyal dan Gradient Fill (<15% opacity)
        val strokePath = Path()
        val fillPath = Path()

        val stepX = graphW / (samples.size - 1).coerceAtLeast(1)

        val firstY = graphH - ((samples[0] / maxUt).coerceIn(0f, 1f) * graphH)
        strokePath.moveTo(paddingLeft, firstY)
        fillPath.moveTo(paddingLeft, graphH)
        fillPath.lineTo(paddingLeft, firstY)

        for (idx in 1 until samples.size) {
            val x = paddingLeft + (idx * stepX)
            val y = graphH - ((samples[idx] / maxUt).coerceIn(0f, 1f) * graphH)
            strokePath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        val lastX = paddingLeft + ((samples.size - 1) * stepX)
        fillPath.lineTo(lastX, graphH)
        fillPath.close()

        // Gambar Area Gradasi di Bawah Kurva (<15% opacity)
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.accentCyan.copy(alpha = 0.14f),
                    colors.accentCyan.copy(alpha = 0.01f)
                ),
                startY = 0f,
                endY = graphH
            )
        )

        // Gambar Garis Sinyal Tipis 2dp
        drawPath(
            path = strokePath,
            color = colors.accentCyan,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // Sumbu Waktu (Teks "-10s" dan "Sekarang")
        val timePaint = Paint().apply {
            color = colors.textMuted.toArgb()
            textSize = 9.sp.toPx()
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText("-10 dtk", paddingLeft, h - 2.dp.toPx(), timePaint)
        timePaint.textAlign = Paint.Align.RIGHT
        drawContext.canvas.nativeCanvas.drawText("0 dtk (Live)", w, h - 2.dp.toPx(), timePaint)
    }
}
