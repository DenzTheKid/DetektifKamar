package com.detektifkamar.app.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.UnitType
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isResetSuccess) {
        if (uiState.isResetSuccess) {
            snackbarHostState.showSnackbar("Semua data dan pengaturan berhasil direset.")
            viewModel.clearResetSuccess()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.paperBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PENGATURAN & TENTANG",
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
            // 1. Pilihan Satuan Bawaan (µT / mT / G)
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SATUAN PENGUKURAN DEFAULT",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UnitType.entries.forEach { unit ->
                            val isSelected = (uiState.selectedUnit == unit)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.accentCyan.copy(alpha = 0.18f) else colors.surfaceSecondary,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.accentCyan else colors.borderOutline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onUnitSelected(unit) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = unit.symbol,
                                        fontFamily = PlexMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) colors.accentCyan else colors.inkText
                                    )
                                    Text(
                                        text = when (unit) {
                                            UnitType.MICROTESLA -> "Mikrotesla"
                                            UnitType.MILLITESLA -> "Militesla"
                                            UnitType.GAUSS -> "Gauss"
                                        },
                                        fontFamily = PlexSansFontFamily,
                                        fontSize = 10.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Tema Aplikasi (Sistem / Terang / Gelap)
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TEMA TAMPILAN",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val themes = listOf("Ikuti Sistem", "Terang", "Gelap")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        themes.forEachIndexed { idx, title ->
                            val isSelected = (uiState.themeMode == idx)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.accentCyan.copy(alpha = 0.18f) else colors.surfaceSecondary,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) colors.accentCyan else colors.borderOutline
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.onThemeSelected(idx) }
                            ) {
                                Text(
                                    text = title,
                                    fontFamily = PlexSansFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = if (isSelected) colors.accentCyan else colors.inkText,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 3. Preferensi Getar & Mode Simulasi
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Umpan Balik Getar (Haptik)",
                                fontFamily = PlexSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.inkText
                            )
                            Text(
                                text = "Pulsa getar saat mendeteksi medan medan tinggi",
                                fontFamily = PlexSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = uiState.isVibrationEnabled,
                            onCheckedChange = viewModel::onVibrationToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentCyan,
                                checkedTrackColor = colors.accentCyan.copy(alpha = 0.35f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mode Simulasi Sensor",
                                fontFamily = PlexSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.inkText
                            )
                            Text(
                                text = "Gunakan data simulasi realistis untuk pengujian di emulator",
                                fontFamily = PlexSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                        Switch(
                            checked = uiState.isSimulationMode,
                            onCheckedChange = viewModel::onSimulationModeToggled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.accentCyan,
                                checkedTrackColor = colors.accentCyan.copy(alpha = 0.35f)
                            )
                        )
                    }
                }
            }

            // 4. Keterbatasan Fisika Magnetometer Ponsel
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "KETERBATASAN FISIKA SENSOR PONSEL",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )

                    Text(
                        text = "1. Pengaruh Bahan Feromagnetik: Bodi logam ponsel, baut speaker, dan casing magnetik (MagSafe) dapat mendistorsi pembacaan arah dan magnitudo.\n" +
                                "2. Saturasi Sensor: Magnet kuat (neodymium) dapat melebihi batas jangkauan maksimum sensor (maximumRange).\n" +
                                "3. Pentingnya Kalibrasi: Lakukan kalibrasi gerakan angka 8 secara berkala untuk memperbarui offset bias hard-iron.",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = colors.inkText
                    )
                }
            }

            // 5. Sumber Rujukan Ilmiah
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "SUMBER RUJUKAN ILMIAH TERVERIFIKASI",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )

                    Text(
                        text = "• WHO Environmental Health Criteria 238: Extremely Low Frequency Fields (World Health Organization)\n" +
                                "• ICNIRP Guidelines for Limiting Exposure to Electromagnetic Fields (International Commission on Non-Ionizing Radiation Protection)\n" +
                                "• USGS Geomagnetism Program: Earth's Magnetic Field Surface Survey",
                        fontFamily = PlexMonoFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = colors.accentCyan
                    )
                }
            }

            // 6. Tombol Reset Semua Data
            OutlinedButton(
                onClick = viewModel::showResetConfirmDialog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                border = BorderStroke(1.dp, colors.dangerRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = colors.dangerRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset Semua Berkas & Data Pengukuran",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.dangerRed
                )
            }

            // 7. Info Aplikasi & Tugas Kuliah
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DETEKTIF KAMAR v2.0.0",
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = colors.inkText
                    )
                    Text(
                        text = "Tugas Kuliah: Aplikasi Survei Medan Magnet Ruangan (EMF)",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
        }

        // Dialog Konfirmasi Reset Data
        if (uiState.isResetConfirmDialogVisible) {
            AlertDialog(
                onDismissRequest = viewModel::hideResetConfirmDialog,
                title = {
                    Text(
                        text = "Reset Semua Data?",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = colors.inkText
                    )
                },
                text = {
                    Text(
                        text = "Tindakan ini akan menghapus seluruh berkas ruangan, seluruh titik pengukuran medan magnet, dan offset kalibrasi secara permanen dari database SQLite lokal.",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::resetAllData,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.dangerRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Ya, Hapus Semua")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::hideResetConfirmDialog) {
                        Text("Batal", color = colors.inkText)
                    }
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
