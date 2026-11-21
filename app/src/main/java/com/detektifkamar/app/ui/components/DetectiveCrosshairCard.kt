package com.detektifkamar.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.detektifkamar.app.ui.theme.CardCornerRadius
import com.detektifkamar.app.ui.theme.DetectiveTheme

/**
 * Kartu instrumen lapangan dengan ornamen crosshair halus di sudut-sudutnya.
 */
@Composable
fun DetectiveCrosshairCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = DetectiveTheme.colors.surface,
    borderColor: Color = DetectiveTheme.colors.borderOutline,
    crosshairColor: Color = DetectiveTheme.colors.accentCyan.copy(alpha = 0.45f),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier.drawBehind {
        // Gambar tanda crosshair (+) teknis di pojok kiri atas dan kanan bawah kartu
        val armLength = 8.dp.toPx()
        val margin = 8.dp.toPx()
        val strokeW = 1.2.dp.toPx()

        // Pojok Kiri Atas
        val topLeftCenter = Offset(margin + armLength / 2, margin + armLength / 2)
        drawLine(
            color = crosshairColor,
            start = Offset(topLeftCenter.x - armLength / 2, topLeftCenter.y),
            end = Offset(topLeftCenter.x + armLength / 2, topLeftCenter.y),
            strokeWidth = strokeW
        )
        drawLine(
            color = crosshairColor,
            start = Offset(topLeftCenter.x, topLeftCenter.y - armLength / 2),
            end = Offset(topLeftCenter.x, topLeftCenter.y + armLength / 2),
            strokeWidth = strokeW
        )

        // Pojok Kanan Bawah
        val bottomRightCenter = Offset(size.width - margin - armLength / 2, size.height - margin - armLength / 2)
        drawLine(
            color = crosshairColor,
            start = Offset(bottomRightCenter.x - armLength / 2, bottomRightCenter.y),
            end = Offset(bottomRightCenter.x + armLength / 2, bottomRightCenter.y),
            strokeWidth = strokeW
        )
        drawLine(
            color = crosshairColor,
            start = Offset(bottomRightCenter.x, bottomRightCenter.y - armLength / 2),
            end = Offset(bottomRightCenter.x, bottomRightCenter.y + armLength / 2),
            strokeWidth = strokeW
        )
    }
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(CardCornerRadius)
    val border = BorderStroke(1.dp, borderColor)

    if (onClick != null) {
        Surface(
            modifier = cardModifier,
            shape = shape,
            color = backgroundColor,
            border = border,
            onClick = onClick
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    } else {
        Surface(
            modifier = cardModifier,
            shape = shape,
            color = backgroundColor,
            border = border
        ) {
            Box(modifier = Modifier.padding(14.dp)) {
                content()
            }
        }
    }
}
