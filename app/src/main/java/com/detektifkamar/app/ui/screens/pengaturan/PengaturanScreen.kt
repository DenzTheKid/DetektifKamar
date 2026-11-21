package com.detektifkamar.app.ui.screens.pengaturan

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.SegmentedTabControl
import com.detektifkamar.app.ui.components.VintageButton
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@Composable
fun PengaturanScreen(
    viewModel: PengaturanViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val scrollState = rememberScrollState()

    var baselineInput by remember(uiState.defaultBaselineUt) {
        mutableStateOf("%.1f".format(uiState.defaultBaselineUt))
    }

    // Dialog Panduan Pengukuran (3 Langkah Praktis)
    if (uiState.showGuideDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissGuideDialog,
            title = {
                Text("PANDUAN PENGUKURAN MEDAN MAGNET", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, color = colors.inkText)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GuideStepItem("1. JAUHKAN AKSESORI MAGNETIK", "Lepaskan casing HP bertrik MagSafe atau jam tangan logam agar pembacaan sensor magnetometer murni mencatat medan ruangan.")
                    GuideStepItem("2. KALIBRASI SENSOR (ANGKA 8)", "Putar HP membentuk pola angka 8 di udara beberapa detik sebelum memulai pengukuran agar akurasi sensor mencapai status tinggi.")
                    GuideStepItem("3. TAHAN PERANGKAT TETAP DIAM", "Pegang atau letakkan HP dengan stabil saat menekan tombol rekam selama 4 detik untuk hasil yang akurat.")
                }
            },
            confirmButton = {
                TextButton(onClick = viewModel::dismissGuideDialog) {
                    Text("MENGERTI", fontFamily = IbmPlexSansFamily, color = colors.cyanotypeBlue, fontWeight = FontWeight.Bold)
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = DetectiveIcons.Kembali, contentDescription = "Kembali", tint = colors.inkText)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PENGATURAN",
                    fontFamily = IbmPlexSansFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.inkText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val cardShape = RoundedCornerShape(14.dp)

            // 1. Acuan Bawaan (µT)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), cardShape)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "BASELINE MEDAN MAGNET DEFAULT (µT)",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Digunakan sebagai pembanding jika ruangan belum memiliki nilai baseline khusus (default: 45.0 µT).",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = baselineInput,
                            onValueChange = { baselineInput = it },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        VintageButton(
                            text = "Terapkan",
                            onClick = {
                                val parsed = baselineInput.replace(',', '.').toFloatOrNull()
                                if (parsed != null && parsed in 20f..80f) {
                                    viewModel.setDefaultBaseline(parsed)
                                }
                            },
                            isPrimary = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Tema Tampilan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), cardShape)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "TEMA TAMPILAN",
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SegmentedTabControl(
                        tabs = listOf("Sistem", "Terang", "Gelap"),
                        selectedIndex = uiState.themeMode,
                        onTabSelected = viewModel::setThemeMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Saklar Getar Haptik
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), cardShape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GETAR HAPTIK SCANNER",
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.inkText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Memberikan getaran halus saat mendekati area dengan fluktuasi medan tinggi.",
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                    Switch(
                        checked = uiState.isVibrationEnabled,
                        onCheckedChange = viewModel::setVibrationEnabled,
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.cyanotypeBlue, checkedTrackColor = colors.surfaceSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Mode Simulasi Sensor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(cardShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), cardShape)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MODE SIMULASI SENSOR",
                                fontFamily = IbmPlexSansFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.inkText
                            )
                            if (uiState.isSimulationMode) {
                                Spacer(modifier = Modifier.width(6.dp))
                                DetectiveStamp(text = "AKTIF", color = colors.stampRed)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Digunakan saat pengujian pada emulator tanpa sensor magnetometer fisik.",
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                    Switch(
                        checked = uiState.isSimulationMode,
                        onCheckedChange = viewModel::setSimulationMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.cyanotypeBlue, checkedTrackColor = colors.surfaceSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Panduan Pengukuran
            VintageButton(
                text = "Buka Panduan Pengukuran",
                onClick = viewModel::openGuideDialog,
                isPrimary = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info Aplikasi
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "PINDAI MAGNET v1.0.0", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.inkText)
                Text(text = "100% Offline • Raw SQLite • Jetpack Compose", fontFamily = IbmPlexMonoFamily, fontSize = 11.sp, color = colors.textMuted)
                Text(text = "Pemetaan dan Analisis Medan Magnet Ruangan", fontFamily = IbmPlexSansFamily, fontSize = 11.sp, color = colors.textMuted)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GuideStepItem(title: String, desc: String) {
    val colors = DetectiveTheme.colors
    Column {
        Text(text = title, fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = colors.cyanotypeBlue)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = desc, fontFamily = IbmPlexSansFamily, fontSize = 12.sp, color = colors.inkText)
    }
}
