package com.detektifkamar.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Bentuk sudut instrumen detektif:
 * - Konsisten 12dp untuk kartu
 * - 8dp untuk chip dan badge
 * - 24dp untuk bottom sheet
 */
val DetectiveShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

val CardCornerRadius = 12.dp
val ChipCornerRadius = 8.dp
val BottomSheetCornerRadius = 24.dp
