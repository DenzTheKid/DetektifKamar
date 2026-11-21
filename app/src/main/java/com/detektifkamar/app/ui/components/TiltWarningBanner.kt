package com.detektifkamar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import kotlin.math.roundToInt

/**
 * Banner peringatan saat ponsel miring melebihi batas aman posisi mendatar.
 * Menempati slot tinggi tetap yang menskalakan diri dengan fontScale agar teks
 * tidak terpotong di font besar namun tetap memiliki 0 dp pergeseran layout.
 */
@Composable
fun TiltWarningBanner(
    isTilted: Boolean,
    tiltAngleDeg: Float,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors

    // Sanitasi sudut non-finite dan bulatkan ke kelipatan 5 derajat agar teks stabil
    val safeAngle = if (tiltAngleDeg.isFinite()) tiltAngleDeg else 0f
    val angleStep = (safeAngle / 5f).roundToInt()
    val displayAngle = remember(angleStep) {
        (angleStep * 5).coerceAtLeast(0)
    }
    val bannerTextColor = if (colors.isDark) colors.warningAmber else Color(0xFF92400E)
    val slotHeight = with(LocalDensity.current) {
        (18.dp + (16.sp * 3).toDp() + 8.dp).coerceAtLeast(58.dp)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(slotHeight)
            .testTag("tilt_warning_slot"),
        contentAlignment = Alignment.Center
    ) {
        if (!isTilted) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary.copy(alpha = 0.55f))
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.accentCyan)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Posisi mendatar optimal • Sensor stabil",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = colors.textMuted
                )
            }
        }

        AnimatedVisibility(
            visible = isTilted,
            enter = fadeIn(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tilt_warning_banner")
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.paperBackground)
                    .background(colors.warningAmber.copy(alpha = 0.14f))
                    .border(1.dp, colors.warningAmber.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
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
                    text = "Ponsel miring $displayAngle°. Posisikan datar agar akurat.",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = bannerTextColor
                )
            }
        }
    }
}
