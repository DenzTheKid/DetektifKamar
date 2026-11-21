package com.detektifkamar.app.ui.screens.ukurtitik

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.ui.components.BubbleLevel
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.VintageAnalogMeter
import com.detektifkamar.app.ui.components.VintageButton
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.HeatmapColors
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily
import java.util.Locale

@Composable
fun UkurTitikScreen(
    viewModel: UkurTitikViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.saveCompleted) {
        if (uiState.saveCompleted) {
            onNavigateBack()
        }
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = DetectiveIcons.Kembali, contentDescription = "Kembali", tint = colors.inkText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (uiState.isBaselineMode) "UKUR BASELINE LINGKUNGAN" else "PENGUKURAN TITIK RUANGAN",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Text(
                        text = if (uiState.isBaselineMode) "Ukur di titik bebas perangkat elektronik & logam" else "${uiState.pointLabel} (%.1f, %.1f m)".format(Locale("id", "ID"), uiState.pointX, uiState.pointY),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pengukur Dial Presisi Real-Time
            VintageAnalogMeter(
                valueUt = uiState.liveSensor.magneticUt,
                baselineUt = uiState.baselineUt
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Panel Parameter Sensor: Kestabilan, Akurasi, Lux, Azimuth & Waterpass
            val panelShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(panelShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), panelShape)
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Status Posisi: ", fontFamily = IbmPlexSansFamily, fontSize = 12.sp, color = colors.textMuted)
                            if (uiState.liveSensor.isStable) {
                                DetectiveStamp(text = stringResource(R.string.ukur_stabil), color = colors.labGreen)
                            } else {
                                DetectiveStamp(text = stringResource(R.string.ukur_tidak_stabil), color = colors.stampRed)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val luxStr = uiState.liveSensor.lux?.let { "%.0f lux".format(it) } ?: "Tidak ada"
                        val azStr = uiState.liveSensor.azimuthDeg?.let { "%.0f°".format(it) } ?: "-"
                        Text(
                            text = "Akurasi: ${uiState.liveSensor.accuracy}/3 • Cahaya: $luxStr • Hadap: $azStr",
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }

                    BubbleLevel(tiltDeg = uiState.liveSensor.tiltDeg)
                }
            }

            // Peringatan Kalibrasi jika akurasi rendah
            if (!uiState.liveSensor.isCalibrated) {
                Spacer(modifier = Modifier.height(10.dp))
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
                        text = "Akurasi sensor magnetometer rendah. Putar ponsel membentuk pola angka 8 di udara untuk mengkalibrasi sensor.",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.stampRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sesi Pengambilan (Progress & Hasil)
            when (val session = uiState.sessionState) {
                is MeasurementSessionState.Idle -> {
                    VintageButton(
                        text = stringResource(R.string.ukur_btn_ambil),
                        onClick = viewModel::startMeasurementSession,
                        enabled = uiState.liveSensor.isStable,
                        isPrimary = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is MeasurementSessionState.MenungguStabil -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Tahan perangkat tetap diam dan stabil...",
                            fontFamily = IbmPlexSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = colors.stampRed
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        VintageButton(
                            text = "Coba Lagi",
                            onClick = viewModel::startMeasurementSession,
                            isPrimary = false
                        )
                    }
                }
                is MeasurementSessionState.Mengambil -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Merekam fluks medan magnet (durasi 4 detik)...",
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 13.sp,
                            color = colors.cyanotypeBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { session.progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = colors.cyanotypeBlue,
                            trackColor = colors.surfaceSecondary
                        )
                    }
                }
                is MeasurementSessionState.Selesai -> {
                    val resultShape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(resultShape)
                            .background(colors.surface)
                            .border(1.dp, colors.cyanotypeBlue.copy(alpha = 0.5f), resultShape)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HASIL PENGUKURAN 4 DETIK",
                                    fontFamily = IbmPlexSansFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.inkText
                                )
                                DetectiveStamp(
                                    text = session.category.label,
                                    color = when (session.category) {
                                        DisturbanceCategory.TENANG -> colors.labGreen
                                        DisturbanceCategory.GANGGUAN_RINGAN -> colors.highlighterYellow
                                        DisturbanceCategory.GANGGUAN_KUAT -> HeatmapColors.GangguanKuat
                                        DisturbanceCategory.SUMBER_DEKAT -> colors.stampRed
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Rata-rata: %.1f µT (Min: %.1f, Maks: %.1f)".format(
                                    session.reading.meanUt,
                                    session.reading.minUt,
                                    session.reading.maxUt
                                ),
                                fontFamily = IbmPlexMonoFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.inkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Simpangan Baku: %.2f µT • Delta Baseline: Δ %.1f µT".format(
                                    session.reading.stdUt,
                                    session.deltaUt
                                ),
                                fontFamily = IbmPlexMonoFamily,
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = session.category.microcopy,
                                fontFamily = IbmPlexSansFamily,
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VintageButton(
                            text = if (uiState.isBaselineMode) "Simpan Baseline" else "Simpan Titik",
                            onClick = viewModel::saveResult,
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )
                        VintageButton(
                            text = "Ulangi",
                            onClick = viewModel::resetSession,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MeasurementSessionState.Gagal -> {
                    Text(text = "Pengambilan gagal: ${session.alasan}", color = colors.stampRed)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
