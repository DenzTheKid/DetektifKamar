package com.detektifkamar.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily

/**
 * Lencana status berbentuk stempel cap teknis berotasi -3° dengan border tipis.
 */
@Composable
fun StatusStampBadge(
    status: MagneticStatus,
    modifier: Modifier = Modifier,
    rotateDeg: Float = -3f
) {
    val targetColor = DetectiveTheme.colors.colorForStatus(status)
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 200),
        label = "BadgeColorAnimation"
    )

    Box(
        modifier = modifier
            .rotate(rotateDeg)
            .border(
                width = 1.5.dp,
                color = animatedColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.label,
            fontFamily = PlexMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.2.sp,
            color = animatedColor
        )
    }
}
