package com.detektifkamar.app.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

/**
 * Modifier kustom untuk menggambar kisi presisi instrumen modern.
 * Ringan, elegan, dan tidak membuat tampilan menjadi terlalu ramai.
 */
fun Modifier.millimeterBackground(
    gridColor: Color,
    majorGridColor: Color = gridColor.copy(alpha = 0.6f),
    gridSpacingDp: Float = 28f
): Modifier = this.drawBehind {
    val step = gridSpacingDp * density
    val dotRadius = 1.0f * density

    // Gambar titik grid presisi (dot matrix grid)
    var x = step
    while (x < size.width) {
        var y = step
        while (y < size.height) {
            drawCircle(
                color = majorGridColor,
                radius = dotRadius,
                center = Offset(x, y)
            )
            y += step
        }
        x += step
    }
}
