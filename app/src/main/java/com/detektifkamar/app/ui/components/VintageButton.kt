package com.detektifkamar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@Composable
fun VintageButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
) {
    val colors = DetectiveTheme.colors
    val btnShape = RoundedCornerShape(10.dp)

    val bgColor = when {
        !enabled -> colors.surfaceSecondary
        isPrimary -> colors.cyanotypeBlue
        else -> colors.surface
    }

    val textColor = when {
        !enabled -> colors.textMuted
        isPrimary -> Color.White
        else -> colors.inkText
    }

    val borderColor = when {
        !enabled -> colors.borderOutline.copy(alpha = 0.5f)
        isPrimary -> Color.Transparent
        else -> colors.borderOutline
    }

    Box(
        modifier = modifier
            .clip(btnShape)
            .background(bgColor)
            .border(1.dp, borderColor, btnShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = IbmPlexSansFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
