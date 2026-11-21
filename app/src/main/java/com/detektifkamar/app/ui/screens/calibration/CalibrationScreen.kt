package com.detektifkamar.app.ui.screens.calibration

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.CalibrationQuality
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    viewModel: CalibrationViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.paperBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "KALIBRASI MAGNETIK",
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.inkText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = colors.inkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.paperBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Peringatan Pengaruh Fisik
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.warningAmber.copy(alpha = 0.12f))
                    .border(1.dp, colors.warningAmber.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "⚠️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lepaskan casing ponsel bermagnet (seperti MagSafe atau dompet flip berkancing magnet), dan jauhi speaker, stop kontak, atau rangka besi selama kalibrasi.",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = colors.warningAmber
                    )
                }
            }

            // 2. Lingkaran Hitung Mundur 15 Detik & Panduan Gerakan Angka 8
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GERAKAN ANGKA 8 (FIGURE-8)",
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        modifier = Modifier.size(170.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background track
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.fillMaxSize(),
                            color = colors.borderOutline.copy(alpha = 0.4f),
                            strokeWidth = 8.dp
                        )
                        // Active progress ring
                        CircularProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier.fillMaxSize(),
                            color = colors.accentCyan,
                            strokeWidth = 8.dp
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (uiState.isSessionRunning) "${uiState.remainingSeconds}" else "15",
                                fontFamily = PlexMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 48.sp,
                                color = colors.inkText
                            )
                            Text(
                                text = "DETIK",
                                fontFamily = PlexSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = colors.textMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (uiState.isSessionRunning) {
                            "Putar dan ayunkan ponsel membentuk angka 8 di udara..."
                        } else {
                            "Tekan tombol di bawah lalu gerakkan ponsel membentuk angka 8 penuh selama 15 detik."
                        },
                        fontFamily = PlexSansFontFamily,
                        fontSize = 13.sp,
                        color = colors.inkText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (uiState.isSessionRunning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sampel: ${uiState.sampleCount} · X: ${String.format(Locale.US, "%.0f", uiState.currentX)} Y: ${String.format(Locale.US, "%.0f", uiState.currentY)} Z: ${String.format(Locale.US, "%.0f", uiState.currentZ)}",
                            fontFamily = PlexMonoFontFamily,
                            fontSize = 11.sp,
                            color = colors.accentCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = viewModel::startCalibration,
                        enabled = !uiState.isSessionRunning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentCyan,
                            contentColor = colors.paperBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isSessionRunning) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isSessionRunning) "Sedang Mengukur..." else "Mulai Kalibrasi 15 Detik",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // 3. Hasil Perhitungan Kalibrasi Terakhir
            AnimatedVisibility(visible = uiState.calibrationResult != null) {
                val res = uiState.calibrationResult ?: return@AnimatedVisibility
                val qualityColor = when (res.quality) {
                    CalibrationQuality.BAIK -> colors.normalGreen
                    CalibrationQuality.CUKUP -> colors.warningAmber
                    CalibrationQuality.ULANG -> colors.dangerRed
                }

                DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HASIL ESTIMASI BIAS HARD-IRON",
                                fontFamily = PlexSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 1.2.sp,
                                color = colors.textMuted
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(qualityColor.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = res.quality.label.uppercase(),
                                    fontFamily = PlexMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = qualityColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = res.quality.description,
                            fontFamily = PlexSansFontFamily,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BiasItem(axis = "Bias X", value = res.biasX, colors = colors)
                            BiasItem(axis = "Bias Y", value = res.biasY, colors = colors)
                            BiasItem(axis = "Bias Z", value = res.biasZ, colors = colors)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = viewModel::saveCalibrationResult,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.normalGreen,
                                contentColor = colors.paperBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terapkan Offset Bias ke Sensor",
                                fontFamily = PlexSansFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 4. Status Kalibrasi yang Sedang Aktif
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "STATUS KALIBRASI AKTIF",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.activeSavedCalibration.isCalibrated) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "Offset Aktif: X: %+.1f µT | Y: %+.1f µT | Z: %+.1f µT",
                                uiState.activeSavedCalibration.biasX,
                                uiState.activeSavedCalibration.biasY,
                                uiState.activeSavedCalibration.biasZ
                            ),
                            fontFamily = PlexMonoFontFamily,
                            fontSize = 12.sp,
                            color = colors.accentCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = viewModel::resetSavedCalibration,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Reset ke Offset Bawaan (Nol)",
                                fontFamily = PlexSansFontFamily,
                                fontSize = 12.sp,
                                color = colors.dangerRed
                            )
                        }
                    } else {
                        Text(
                            text = "Sensor saat ini menggunakan data mentah hardware (tanpa offset kalibrasi tambahan).",
                            fontFamily = PlexSansFontFamily,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BiasItem(
    axis: String,
    value: Float,
    colors: com.detektifkamar.app.ui.theme.DetectiveColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = axis,
            fontFamily = PlexSansFontFamily,
            fontSize = 10.sp,
            color = colors.textMuted
        )
        Text(
            text = String.format(Locale.US, "%+.1f µT", value),
            fontFamily = PlexMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.inkText
        )
    }
}
