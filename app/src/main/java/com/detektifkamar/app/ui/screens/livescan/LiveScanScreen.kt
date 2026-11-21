package com.detektifkamar.app.ui.screens.livescan

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.ui.components.ArcGauge240
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.components.OscilloscopeGraph
import com.detektifkamar.app.ui.components.StatusStampBadge
import com.detektifkamar.app.ui.components.TiltWarningBanner
import com.detektifkamar.app.ui.components.VectorComponentBars
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScanScreen(
    viewModel: LiveScanViewModel,
    onNavigateToCreateRoom: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isPointSavedSuccess) {
        if (uiState.isPointSavedSuccess) {
            snackbarHostState.showSnackbar("Titik ukur berhasil disimpan ke ruangan!")
        }
    }

    if (!uiState.sensorState.isMagnetometerAvailable) {
        SensorUnavailableScreen(colors = colors)
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.paperBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PINDAI LANGSUNG",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.inkText
                        )
                        StatusStampBadge(status = uiState.sensorState.status)
                    }
                },
                actions = {
                    // Tombol Ganti Satuan (µT -> mT -> G)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accentCyan.copy(alpha = 0.15f))
                            .clickable { viewModel.toggleUnit() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = uiState.displayUnit.symbol,
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = colors.accentCyan
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Tombol Bekukan (Freeze)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (uiState.isFrozen) colors.warningAmber else colors.surfaceSecondary
                            )
                            .clickable { viewModel.toggleFreeze() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (uiState.isFrozen) "TERTAHAN" else "BEKUKAN",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (uiState.isFrozen) colors.paperBackground else colors.inkText
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Peringatan Kemiringan (TiltDetector)
            TiltWarningBanner(
                isTilted = uiState.sensorState.tiltState.isTilted,
                tiltAngleDeg = uiState.sensorState.tiltState.tiltAngle
            )

            // 2. Gauge Analog Melengkung 240° dengan Nilai Monospace Besar
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArcGauge240(
                        valueUt = uiState.sensorState.magneticUt,
                        displayUnit = uiState.displayUnit,
                        sensorMaxRangeUt = uiState.sensorState.sensorMaxRangeUt,
                        status = uiState.sensorState.status
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Penjelasan Fisika Dipol Singkat
                    Text(
                        text = "Fluks magnet meluruh drastis sebanding 1/r³ terhadap jarak.",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }

            // 3. Komponen Vektor Sumbu X, Y, Z
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KOMPONEN VEKTOR MEDAN (X, Y, Z)",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.textMuted
                        )
                        if (uiState.sensorState.lux != null) {
                            Text(
                                text = String.format(Locale.US, "Cahaya: %.0f Lux", uiState.sensorState.lux),
                                fontFamily = PlexMonoFontFamily,
                                fontSize = 11.sp,
                                color = colors.accentCyan
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    VectorComponentBars(
                        bx = uiState.sensorState.bx,
                        by = uiState.sensorState.by,
                        bz = uiState.sensorState.bz
                    )
                }
            }

            // 4. Grafik Osiloskop Sinyal 10 Detik
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "GRAFIK JEJAK OSILOSKOP (10 DETIK)",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OscilloscopeGraph(
                        samples = uiState.history10s,
                        maxUt = (uiState.sensorState.magneticUt * 1.5f).coerceAtLeast(120f)
                    )
                }
            }

            // 5. Kartu Statistik Sesi (Min, Max, Avg)
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STATISTIK SESI PENGUKURAN",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.textMuted
                        )
                        IconButton(
                            onClick = viewModel::resetStatistics,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reset Statistik",
                                tint = colors.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatColumn(
                            label = "MINIMUM",
                            value = uiState.displayUnit.convertFromMicrotesla(uiState.minUtSinceStart),
                            unit = uiState.displayUnit.symbol,
                            color = colors.accentCyan
                        )
                        StatColumn(
                            label = "RATA-RATA",
                            value = uiState.displayUnit.convertFromMicrotesla(uiState.averageUtSinceStart),
                            unit = uiState.displayUnit.symbol,
                            color = colors.inkText
                        )
                        StatColumn(
                            label = "MAKSIMUM",
                            value = uiState.displayUnit.convertFromMicrotesla(uiState.maxUtSinceStart),
                            unit = uiState.displayUnit.symbol,
                            color = colors.warningAmber
                        )
                    }
                }
            }

            // 6. Tombol Aksi Simpan ke Ruangan
            Button(
                onClick = viewModel::openSaveModal,
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
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simpan ke Titik Ruangan",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Modal Bottom Sheet untuk Menyimpan Titik ke Ruangan
        if (uiState.isSaveModalVisible) {
            SavePointBottomSheet(
                uiState = uiState,
                onDismiss = viewModel::closeSaveModal,
                onSelectRoom = viewModel::onRoomSelected,
                onNotesChange = viewModel::onNotesInputChanged,
                onConfirmSave = viewModel::savePointToRoom,
                onCreateRoom = {
                    viewModel.closeSaveModal()
                    onNavigateToCreateRoom()
                }
            )
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: Float,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontFamily = PlexSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = colors.textMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = String.format(Locale.US, "%.1f", value),
            fontFamily = PlexMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = color
        )
        Text(
            text = unit,
            fontFamily = PlexSansFontFamily,
            fontSize = 10.sp,
            color = colors.textMuted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavePointBottomSheet(
    uiState: LiveScanUiState,
    onDismiss: () -> Unit,
    onSelectRoom: (Long) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirmSave: () -> Unit,
    onCreateRoom: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = DetectiveTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SIMPAN TITIK UKUR",
                    fontFamily = PlexMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = colors.inkText
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = colors.textMuted)
                }
            }

            // Ringkasan nilai yang akan disimpan
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kekuatan Medan",
                            fontFamily = PlexSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f µT", uiState.sensorState.magneticUt),
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.accentCyan
                        )
                    }
                    StatusStampBadge(status = uiState.sensorState.status)
                }
            }

            // Pilihan Ruangan Target
            Column {
                Text(
                    text = "PILIH RUANGAN TARGET",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (uiState.roomsList.isEmpty()) {
                    Text(
                        text = "Belum ada berkas ruangan. Buat ruangan terlebih dahulu.",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 13.sp,
                        color = colors.dangerRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onCreateRoom,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accentCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Buat Ruangan Baru")
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        uiState.roomsList.forEach { room ->
                            val isSelected = (uiState.selectedRoomId == room.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accentCyan.copy(alpha = 0.15f) else colors.surfaceSecondary)
                                    .border(
                                        1.dp,
                                        if (isSelected) colors.accentCyan else colors.borderOutline,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { onSelectRoom(room.id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${room.name} (${room.roomType.displayName})",
                                    fontFamily = PlexSansFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = colors.inkText
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.accentCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Catatan Titik Ukur
            Column {
                Text(
                    text = "IDENTIFIKASI / CATATAN TITIK",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.pointNotesInput,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("mis. Dekat stop kontak kasur, adaptor laptop") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.borderOutline,
                        focusedTextColor = colors.inkText,
                        unfocusedTextColor = colors.inkText
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onConfirmSave,
                enabled = uiState.selectedRoomId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentCyan,
                    contentColor = colors.paperBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Konfirmasi Simpan Data",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SensorUnavailableScreen(
    colors: com.detektifkamar.app.ui.theme.DetectiveColors
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paperBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(colors.dangerRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⚠️", fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sensor Magnetometer Tidak Tersedia",
                fontFamily = PlexSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Perangkat ini tidak memiliki perangkat keras sensor medan magnet (TYPE_MAGNETIC_FIELD) bawaan. Anda dapat mengaktifkan Mode Simulasi di menu Pengaturan untuk menguji aplikasi.",
                fontFamily = PlexSansFontFamily,
                fontSize = 13.sp,
                color = colors.textMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
