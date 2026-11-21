package com.detektifkamar.app.ui.screens.detailtitik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.TraceGraph
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.HeatmapColors
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailTitikScreen(
    viewModel: DetailTitikViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val scrollState = rememberScrollState()

    var customInput by remember(uiState.customSuspectLabel) { mutableStateOf(uiState.customSuspectLabel) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.paperBackground)
            .millimeterBackground(colors.gridLine)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = DetectiveIcons.Kembali, contentDescription = "Kembali", tint = colors.inkText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = uiState.point?.label ?: "Detail Titik Ukur",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Text(
                        text = "Koordinat: (%.2f, %.2f) meter".format(Locale("id", "ID"), uiState.point?.xM ?: 0f, uiState.point?.yM ?: 0f),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Kartu Ringkasan Pembacaan Terakhir
            val latest = uiState.latestReading
            if (latest != null) {
                val cardShape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                        .background(colors.surface)
                        .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), cardShape)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "%.1f µT".format(latest.meanUt),
                                    fontFamily = IbmPlexMonoFamily,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.inkText
                                )
                                Text(
                                    text = "Simpangan dari baseline: Δ %.1f µT".format(uiState.deltaUt),
                                    fontFamily = IbmPlexMonoFamily,
                                    fontSize = 12.sp,
                                    color = colors.textMuted
                                )
                            }
                            DetectiveStamp(
                                text = uiState.category.label,
                                color = when (uiState.category) {
                                    DisturbanceCategory.TENANG -> colors.labGreen
                                    DisturbanceCategory.GANGGUAN_RINGAN -> colors.highlighterYellow
                                    DisturbanceCategory.GANGGUAN_KUAT -> HeatmapColors.GangguanKuat
                                    DisturbanceCategory.SUMBER_DEKAT -> colors.stampRed
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rincian Per-Sumbu X, Y, Z
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Bx: %.1f".format(latest.bx), fontFamily = IbmPlexMonoFamily, fontSize = 12.sp, color = colors.inkText)
                            Text("By: %.1f".format(latest.by), fontFamily = IbmPlexMonoFamily, fontSize = 12.sp, color = colors.inkText)
                            Text("Bz: %.1f".format(latest.bz), fontFamily = IbmPlexMonoFamily, fontSize = 12.sp, color = colors.inkText)
                            Text("Lux: ${latest.luxMean?.let { "%.0f".format(it) } ?: "-"}", fontFamily = IbmPlexMonoFamily, fontSize = 12.sp, color = colors.inkText)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Arah: ${latest.azimuthDeg?.let { "%.0f°".format(it) } ?: "-"} • Kemiringan: %.1f° • Kualitas: ${latest.quality}".format(latest.tiltDeg),
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grafik Riwayat Pembacaan
            Text(
                text = "GRAFIK RIWAYAT PEMBACAAN TITIK",
                fontFamily = IbmPlexSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            val samples = uiState.readings.map { it.meanUt }
            TraceGraph(
                samples = if (samples.isEmpty()) listOf(0f, 0f) else samples,
                minY = 10f,
                maxY = 120f
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Perangkat Referensi Terkait
            Text(
                text = "ESTIMASI PERANGKAT TERKAIT",
                fontFamily = IbmPlexSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.inkText
            )
            Text(
                text = "Perangkat dengan kisaran emisi medan mendekati Δ %.1f µT:".format(uiState.deltaUt),
                fontFamily = IbmPlexSansFamily,
                fontSize = 12.sp,
                color = colors.textMuted
            )
            Spacer(modifier = Modifier.height(8.dp))

            uiState.likelySuspects.forEach { s ->
                val isLinked = uiState.linkedSuspects.any { it.id == s.id }
                val itemShape = RoundedCornerShape(10.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(itemShape)
                        .background(colors.surface)
                        .border(1.dp, if (isLinked) colors.cyanotypeBlue else colors.borderOutline.copy(alpha = 0.5f), itemShape)
                        .clickable { viewModel.toggleLinkSuspect(s) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isLinked,
                        onCheckedChange = { viewModel.toggleLinkSuspect(s) },
                        colors = CheckboxDefaults.colors(checkedColor = colors.cyanotypeBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = s.name,
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.inkText
                        )
                        Text(
                            text = "${s.category.label} • Tipikal: %.0f-%.0f µT (%d cm)".format(s.typicalMinUt, s.typicalMaxUt, s.typicalDistanceCm),
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Catatan Perangkat Tambahan
            Text(
                text = "CATATAN PERANGKAT LOKAL",
                fontFamily = IbmPlexSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customInput,
                onValueChange = {
                    customInput = it
                    viewModel.updateCustomLabel(it)
                },
                placeholder = { Text("Nama alat elektronik di titik ini (opsional)...") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
