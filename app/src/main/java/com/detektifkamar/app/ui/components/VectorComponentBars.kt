package com.detektifkamar.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import java.util.Locale
import kotlin.math.abs

/**
 * Komponen bar horizontal untuk menampilkan komponen vektor X, Y, Z.
 * Menampilkan skala dari -100 µT sampai +100 µT dengan titik nol di tengah.
 */
@Composable
fun VectorComponentBars(
    bx: Float,
    by: Float,
    bz: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AxisBarRow(axisLabel = "X", value = bx, color = DetectiveTheme.colors.accentCyan)
        AxisBarRow(axisLabel = "Y", value = by, color = DetectiveTheme.colors.normalGreen)
        AxisBarRow(axisLabel = "Z", value = bz, color = DetectiveTheme.colors.warningAmber)
    }
}

@Composable
private fun AxisBarRow(
    axisLabel: String,
    value: Float,
    color: Color,
    maxRange: Float = 100f
) {
    val colors = DetectiveTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = axisLabel,
            fontFamily = PlexMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = color,
            modifier = Modifier.width(18.dp)
        )

        // Bar Simetris (tengah adalah 0)
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
        ) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f

            // Garis latar belakang
            drawRoundRect(
                color = colors.borderOutline.copy(alpha = 0.35f),
                size = Size(w, h),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Garis batas tengah (nol)
            drawLine(
                color = colors.textMuted.copy(alpha = 0.5f),
                start = Offset(centerX, 0f),
                end = Offset(centerX, h),
                strokeWidth = 1.dp.toPx()
            )

            // Hitung lebar bar terisi
            val fraction = (value / maxRange).coerceIn(-1f, 1f)
            val barWidth = abs(fraction) * centerX

            if (fraction >= 0) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(centerX, 0f),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            } else {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(centerX - barWidth, 0f),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Nilai Numerik Monospace (lebar tetap agar tidak goyang)
        Text(
            text = String.format(Locale.US, "%+6.1f µT", value),
            fontFamily = PlexMonoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = colors.inkText,
            modifier = Modifier.width(72.dp)
        )
    }
}
