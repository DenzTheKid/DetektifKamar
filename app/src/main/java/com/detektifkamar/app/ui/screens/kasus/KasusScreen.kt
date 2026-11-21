package com.detektifkamar.app.ui.screens.kasus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.R
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.FileTabCard
import com.detektifkamar.app.ui.components.VintageButton
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.HeatmapColors
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun KasusScreen(
    viewModel: KasusViewModel,
    onNavigateToNewCase: () -> Unit,
    onNavigateToDetailCase: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paperBackground)
            .millimeterBackground(colors.gridLine)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.ruangan_title),
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = stringResource(R.string.ruangan_subtitle),
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.isSimulation) {
                        DetectiveStamp(
                            text = stringResource(R.string.simulasi_badge),
                            color = colors.stampRed
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = DetectiveIcons.Pengaturan,
                            contentDescription = stringResource(R.string.nav_pengaturan),
                            tint = colors.inkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Konten: List atau Empty State
            if (uiState.cases.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        DetectiveStamp(
                            text = "DATA KOSONG",
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.ruangan_empty_title),
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.inkText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.ruangan_empty_desc),
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 14.sp,
                            color = colors.textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        VintageButton(
                            text = stringResource(R.string.ruangan_btn_tambah),
                            onClick = onNavigateToNewCase,
                            isPrimary = true
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.cases, key = { it.case.id }) { item ->
                        val case = item.case
                        FileTabCard(
                            fileNumberText = "DENAH #%02d".format(case.id),
                            onClick = { onNavigateToDetailCase(case.id) }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = case.title,
                                            fontFamily = IbmPlexSansFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.inkText
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "${case.roomType} • %.1f x %.1f m".format(
                                                Locale("id", "ID"),
                                                case.widthM,
                                                case.lengthM
                                            ),
                                            fontFamily = IbmPlexMonoFamily,
                                            fontSize = 13.sp,
                                            color = colors.textMuted
                                        )
                                    }

                                    // Lencana Kategori Fluktuasi
                                    val badgeColor = when (item.highestCategory) {
                                        DisturbanceCategory.TENANG -> colors.labGreen
                                        DisturbanceCategory.GANGGUAN_RINGAN -> colors.highlighterYellow
                                        DisturbanceCategory.GANGGUAN_KUAT -> HeatmapColors.GangguanKuat
                                        DisturbanceCategory.SUMBER_DEKAT -> colors.stampRed
                                    }
                                    DetectiveStamp(
                                        text = item.highestCategory.label,
                                        color = badgeColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.pointCount} Titik Terdata",
                                        fontFamily = IbmPlexSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = colors.cyanotypeBlue
                                    )

                                    if (case.baselineUt == null) {
                                        DetectiveStamp(
                                            text = stringResource(R.string.stamp_belum_ada_acuan),
                                            color = colors.stampRed
                                        )
                                    } else {
                                        Text(
                                            text = "Baseline: %.1f µT".format(Locale("id", "ID"), case.baselineUt),
                                            fontFamily = IbmPlexMonoFamily,
                                            fontSize = 12.sp,
                                            color = colors.textMuted
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Dibuat: ${dateFormat.format(Date(case.createdAt))}",
                                    fontFamily = IbmPlexMonoFamily,
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tombol Mengambang Tambah Ruangan Baru
        if (uiState.cases.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                VintageButton(
                    text = "+ Tambah Ruangan",
                    onClick = onNavigateToNewCase,
                    isPrimary = true
                )
            }
        }
    }
}
