package com.detektifkamar.app.ui.screens.selidik

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.R
import com.detektifkamar.app.data.sensor.HotColdTrend
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.TraceGraph
import com.detektifkamar.app.ui.components.VintageAnalogMeter
import com.detektifkamar.app.ui.components.VintageButton
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@Composable
fun SelidikScreen(
    viewModel: SelidikViewModel,
    onNavigateToUkur: (caseId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val scrollState = rememberScrollState()

    // Dialog Pilih Ruangan untuk Tombol Cepat "Jadikan Titik Ukur"
    if (uiState.showSelectCaseDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissSelectCaseDialog,
            title = {
                Text("PILIH RUANGAN TUJUAN", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, color = colors.inkText)
            },
            text = {
                if (uiState.cases.isEmpty()) {
                    Text("Belum ada ruangan yang dibuat. Buat ruangan terlebih dahulu di tab Ruangan.", fontFamily = IbmPlexSansFamily)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.cases) { c ->
                            val itemShape = RoundedCornerShape(8.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(itemShape)
                                    .background(colors.surfaceSecondary)
                                    .border(1.dp, colors.borderOutline.copy(alpha = 0.5f), itemShape)
                                    .clickable {
                                        viewModel.dismissSelectCaseDialog()
                                        onNavigateToUkur(c.id)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${c.title} (${c.roomType})",
                                    fontFamily = IbmPlexSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = colors.inkText
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::dismissSelectCaseDialog) {
                    Text("BATAL", color = colors.textMuted)
                }
            }
        )
    }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.pindai_title),
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Text(
                        text = "Pemantauan real-time fluktuasi medan magnet ruangan",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }

                if (uiState.liveSensor.isSimulation) {
                    DetectiveStamp(text = stringResource(R.string.simulasi_badge), color = colors.stampRed)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Meter Dial Presisi
            VintageAnalogMeter(valueUt = uiState.liveSensor.magneticUt)

            Spacer(modifier = Modifier.height(10.dp))

            // Indikator Tren Intensitas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val trend = uiState.liveSensor.hotColdTrend
                val trendColor = when (trend) {
                    HotColdTrend.MAKIN_PANAS -> colors.stampRed
                    HotColdTrend.MAKIN_DINGIN -> colors.cyanotypeBlue
                    HotColdTrend.STABIL -> colors.labGreen
                }
                DetectiveStamp(
                    text = "STATUS: ${trend.label.uppercase()}",
                    color = trendColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Komponen Vektor Sumbu X, Y, Z
            val vectorBoxShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(vectorBoxShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), vectorBoxShape)
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "KOMPONEN VEKTOR MEDAN (µT)",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AxisProgressBar("Sumbu X", uiState.liveSensor.bx, colors.cyanotypeBlue)
                    Spacer(modifier = Modifier.height(6.dp))
                    AxisProgressBar("Sumbu Y", uiState.liveSensor.by, colors.labGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    AxisProgressBar("Sumbu Z", uiState.liveSensor.bz, colors.stampRed)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grafik Osiloskop 10 Detik
            Text(
                text = "OSILASI SINYAL (10 DETIK)",
                fontFamily = IbmPlexSansFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            TraceGraph(
                samples = if (uiState.recentSamples.isEmpty()) listOf(45f, 45f) else uiState.recentSamples,
                minY = 15f,
                maxY = 110f
            )

            // Peringatan Kalibrasi jika akurasi rendah
            if (!uiState.liveSensor.isCalibrated) {
                Spacer(modifier = Modifier.height(12.dp))
                val alertShape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(alertShape)
                        .background(colors.stampRed.copy(alpha = 0.08f))
                        .border(1.dp, colors.stampRed.copy(alpha = 0.4f), alertShape)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Kalibrasi sensor diperlukan. Gerakkan ponsel membentuk angka 8 di udara beberapa kali.",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.stampRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Cepat "Jadikan Titik Ukur"
            VintageButton(
                text = "Simpan ke Denah Ruangan...",
                onClick = viewModel::openSelectCaseDialog,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AxisProgressBar(label: String, value: Float, color: androidx.compose.ui.graphics.Color) {
    val colors = DetectiveTheme.colors
    val normalized = (kotlin.math.abs(value) / 80f).coerceIn(0f, 1f)

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = "$label: %+.1f".format(value), fontFamily = IbmPlexMonoFamily, fontSize = 11.sp, color = colors.inkText, modifier = Modifier.width(90.dp))
        LinearProgressIndicator(
            progress = { normalized },
            modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = colors.surfaceSecondary
        )
    }
}
