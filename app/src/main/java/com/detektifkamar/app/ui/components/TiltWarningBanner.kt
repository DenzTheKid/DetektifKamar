package com.detektifkamar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import kotlin.math.roundToInt

/**
 * Banner peringatan saat ponsel miring melebihi batas aman posisi mendatar.
 * Menggunakan transisi halus dan pembulatan sudut untuk mencegah layout jitter.
 */
@Composable
fun TiltWarningBanner(
    isTilted: Boolean,
    tiltAngleDeg: Float,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors

    // Pembulatan ke kelipatan 5 derajat agar layout teks tidak re-measure berlebihan
    val displayAngle = remember((tiltAngleDeg / 5f).roundToInt()) {
        ((tiltAngleDeg / 5f).roundToInt() * 5).coerceAtLeast(0)
    }

    AnimatedVisibility(
        visible = isTilted,
        enter = fadeIn(animationSpec = tween(250)) + expandVertically(animationSpec = tween(250)),
        exit = fadeOut(animationSpec = tween(200)) + shrinkVertically(animationSpec = tween(200)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.warningAmber.copy(alpha = 0.12f))
                .border(1.dp, colors.warningAmber.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colors.warningAmber)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Pegang ponsel mendatar (Kemiringan: $displayAngle°). Pengukuran optimal pada posisi datar.",
                fontFamily = PlexSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = colors.warningAmber
            )
        }
    }
}
