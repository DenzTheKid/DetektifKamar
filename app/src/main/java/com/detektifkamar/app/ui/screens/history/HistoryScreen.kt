package com.detektifkamar.app.ui.screens.history

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.components.StatusStampBadge
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Storage Access Framework Launcher untuk Ekspor CSV tanpa izin berbahaya
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToCsvUri(context, uri)
        }
    }

    // Snackbar Undo saat data dihapus
    LaunchedEffect(uiState.recentlyDeletedMeasurement) {
        val deleted = uiState.recentlyDeletedMeasurement
        if (deleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Titik ukur (${String.format(Locale.US, "%.1f µT", deleted.magnitude)}) dihapus.",
                actionLabel = "URUNGKAN",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearRecentlyDeleted()
            }
        }
    }

    LaunchedEffect(uiState.isExportSuccess) {
        if (uiState.isExportSuccess) {
            snackbarHostState.showSnackbar("Berkas CSV berhasil diekspor!")
            viewModel.clearExportStatus()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.paperBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "RIWAYAT PENGUKURAN",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.inkText
                        )
                        Text(
                            text = "${uiState.measurements.size} titik data tersimpan",
                            fontFamily = PlexSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                },
                actions = {
                    // Tombol Ekspor CSV
                    IconButton(
                        onClick = {
                            val defaultFileName = "DetektifKamar_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())}.csv"
                            csvExportLauncher.launch(defaultFileName)
                        },
                        enabled = uiState.measurements.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Ekspor CSV",
                            tint = if (uiState.measurements.isNotEmpty()) colors.accentCyan else colors.textMuted
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
        ) {
            // Filter Horizontal Scrollable
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                // Baris Filter Ruangan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "Semua Ruangan",
                        isSelected = uiState.selectedRoomId == null,
                        onClick = { viewModel.onRoomFilterSelected(null) },
                        colors = colors
                    )
                    uiState.rooms.forEach { room ->
                        FilterChip(
                            label = room.name,
                            isSelected = uiState.selectedRoomId == room.id,
                            onClick = { viewModel.onRoomFilterSelected(room.id) },
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Baris Filter Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "Semua Status",
                        isSelected = uiState.selectedStatus == null,
                        onClick = { viewModel.onStatusFilterSelected(null) },
                        colors = colors
                    )
                    MagneticStatus.entries.forEach { status ->
                        FilterChip(
                            label = status.label,
                            isSelected = uiState.selectedStatus == status,
                            onClick = { viewModel.onStatusFilterSelected(status) },
                            colors = colors
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Daftar Pengukuran Dikelompokkan per Hari
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = colors.accentCyan
                        )
                    }
                    uiState.measurements.isEmpty() -> {
                        EmptyHistoryView(modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        val grouped = uiState.groupedByDay
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            grouped.forEach { (dayLabel, itemsInDay) ->
                                item(key = "header_$dayLabel") {
                                    Text(
                                        text = dayLabel.uppercase(Locale.getDefault()),
                                        fontFamily = PlexMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.2.sp,
                                        color = colors.textMuted,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                    )
                                }
                                items(itemsInDay, key = { it.id }) { measurement ->
                                    val roomName = uiState.roomMap[measurement.roomId]?.name ?: "Ruangan #${measurement.roomId}"
                                    MeasurementHistoryCard(
                                        measurement = measurement,
                                        roomName = roomName,
                                        onDelete = { viewModel.deleteMeasurement(measurement) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.detektifkamar.app.ui.theme.DetectiveColors
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) colors.accentCyan.copy(alpha = 0.18f) else colors.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) colors.accentCyan else colors.borderOutline
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontFamily = PlexSansFontFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 12.sp,
            color = if (isSelected) colors.accentCyan else colors.inkText,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun MeasurementHistoryCard(
    measurement: Measurement,
    roomName: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = timeFormat.format(Date(measurement.timestamp))

    DetectiveCrosshairCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeStr,
                        fontFamily = PlexMonoFontFamily,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = roomName,
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = colors.inkText
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f µT", measurement.magnitude),
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = colors.colorForStatus(measurement.status)
                    )
                    Text(
                        text = String.format(
                            Locale.US,
                            "(X: %+.0f, Y: %+.0f, Z: %+.0f)",
                            measurement.x,
                            measurement.y,
                            measurement.z
                        ),
                        fontFamily = PlexMonoFontFamily,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }

                if (measurement.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = measurement.notes,
                        fontFamily = PlexSansFontFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                StatusStampBadge(status = measurement.status)
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = colors.textMuted.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView(modifier: Modifier = Modifier) {
    val colors = DetectiveTheme.colors
    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.accentCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📜", fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Belum Ada Riwayat Pengukuran",
            fontFamily = PlexSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = colors.inkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Gunakan layar Pindai Langsung untuk mengukur dan menyimpan titik-titik medan magnet ke dalam berkas ruangan.",
            fontFamily = PlexSansFontFamily,
            fontSize = 12.sp,
            color = colors.textMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
