package com.detektifkamar.app.ui.screens.arsip

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.HeatmapColors
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArsipScreen(
    viewModel: ArsipViewModel,
    onNavigateToPointDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

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
                        text = "RIWAYAT PENGUKURAN",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Text(
                        text = "Rekapitulasi seluruh titik ukur lintas semua ruangan",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }

                TextButton(onClick = viewModel::toggleSort) {
                    Text(
                        text = if (uiState.sortByLargestDelta) "Urut: Δ Tertinggi" else "Urut: Terbaru",
                        fontFamily = IbmPlexSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.cyanotypeBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kolom Pencarian
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Cari nama titik atau nama ruangan...") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip("Semua", uiState.categoryFilter == null) { viewModel.onCategoryFilterSelected(null) }
                DisturbanceCategory.entries.forEach { cat ->
                    CategoryChip(cat.label, uiState.categoryFilter == cat) { viewModel.onCategoryFilterSelected(cat) }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daftar Titik Ukur
            if (uiState.points.isEmpty() && !uiState.isLoading) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada titik ukur yang cocok dengan pencarian.",
                        fontFamily = IbmPlexSansFamily,
                        color = colors.textMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.points, key = { it.point.id }) { p ->
                        val reading = p.latestReading
                        val cardShape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(cardShape)
                                .background(colors.surface)
                                .border(1.dp, colors.borderOutline.copy(alpha = 0.6f), cardShape)
                                .clickable { onNavigateToPointDetail(p.point.id) }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.point.label,
                                        fontFamily = IbmPlexSansFamily,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.inkText
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Posisi: (%.1f, %.1f) m • ${reading?.let { dateFormat.format(Date(it.measuredAt)) } ?: "Belum diukur"}".format(
                                            Locale("id", "ID"),
                                            p.point.xM,
                                            p.point.yM
                                        ),
                                        fontFamily = IbmPlexMonoFamily,
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    if (reading != null) {
                                        Text(
                                            text = "%.1f µT".format(reading.meanUt),
                                            fontFamily = IbmPlexMonoFamily,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.inkText
                                        )
                                        p.deltaUt?.let { delta ->
                                            Text(
                                                text = "Δ %.1f µT".format(delta),
                                                fontFamily = IbmPlexMonoFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when (p.category) {
                                                    DisturbanceCategory.TENANG -> colors.labGreen
                                                    DisturbanceCategory.GANGGUAN_RINGAN -> colors.highlighterYellow
                                                    DisturbanceCategory.GANGGUAN_KUAT -> HeatmapColors.GangguanKuat
                                                    DisturbanceCategory.SUMBER_DEKAT -> colors.stampRed
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = DetectiveTheme.colors
    val bg = if (isSelected) colors.cyanotypeBlue else colors.surface
    val text = if (isSelected) Color.White else colors.inkText
    val borderColor = if (isSelected) colors.cyanotypeBlue else colors.borderOutline
    val chipShape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(bg)
            .border(1.dp, borderColor, chipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = label, fontFamily = IbmPlexSansFamily, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, fontSize = 11.sp, color = text)
    }
}
