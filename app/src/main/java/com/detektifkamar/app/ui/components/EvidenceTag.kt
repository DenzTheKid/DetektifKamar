package com.detektifkamar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@Composable
fun EvidenceTag(
    number: Int,
    label: String,
    modifier: Modifier = Modifier,
    tagColor: Color = DetectiveTheme.colors.surfaceSecondary
) {
    val colors = DetectiveTheme.colors
    val tagShape = RoundedCornerShape(6.dp)

    Box(
        modifier = modifier
            .background(tagColor, tagShape)
            .border(1.dp, colors.borderOutline.copy(alpha = 0.6f), tagShape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(colors.cyanotypeBlue, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "TITIK #%02d".format(number),
                fontFamily = IbmPlexMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = colors.cyanotypeBlue
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "• $label",
                fontFamily = IbmPlexSansFamily,
                fontSize = 11.sp,
                color = colors.inkText
            )
        }
    }
}
