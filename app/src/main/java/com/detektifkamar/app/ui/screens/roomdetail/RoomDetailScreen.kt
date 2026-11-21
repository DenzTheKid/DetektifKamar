package com.detektifkamar.app.ui.screens.roomdetail

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.components.StatusStampBadge
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    viewModel: RoomDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditRoom: (Long) -> Unit,
    onNavigateToScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val room = uiState.room

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.paperBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = room?.name ?: "Detail Ruangan",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.inkText
                        )
                        if (room != null) {
                            Text(
                                text = "${room.roomType.displayName} · ${room.dimensionsLabel}",
                                fontFamily = PlexSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }
                    }
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
                actions = {
                    if (room != null) {
                        IconButton(onClick = { onNavigateToEditRoom(room.id) }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Ruangan",
                                tint = colors.inkText
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.paperBackground
                )
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading || room == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accentCyan)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Ringkasan Kartu Statistik Ruangan
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RINGKASAN INTENSITAS MEDAN",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.textMuted
                        )
                        StatusStampBadge(status = room.dominantStatus)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetricItem(
                            label = "RATA-RATA",
                            value = uiState.averageUt?.let { String.format(Locale.US, "%.1f µT", it) } ?: "-",
                            color = colors.inkText,
                            colors = colors
                        )
                        MetricItem(
                            label = "MAKSIMUM",
                            value = uiState.maxUt?.let { String.format(Locale.US, "%.1f µT", it) } ?: "-",
                            color = colors.warningAmber,
                            colors = colors
                        )
                        MetricItem(
                            label = "TITIK UKUR",
                            value = "${uiState.measurements.size}",
                            color = colors.accentCyan,
                            colors = colors
                        )
                    }
                }
            }

            // 2. Peta Titik Sederhana (Denah Kotak Ruangan)
            DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PETA TITIK UKUR RUANGAN",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.textMuted
                        )
                        Text(
                            text = "Ketuk titik untuk detail",
                            fontFamily = PlexSansFontFamily,
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RoomPointsCanvas(
                        measurements = uiState.measurements,
                        selectedMeasurement = uiState.selectedMeasurement,
                        roomWidth = room.widthMeters ?: 4f,
                        roomLength = room.lengthMeters ?: 4f,
                        onPointClicked = viewModel::onPointSelected,
                        colors = colors
                    )
                }
            }

            // 3. Detail Titik yang Sedang Dipilih
            if (uiState.selectedMeasurement != null) {
                val selected = uiState.selectedMeasurement!!
                val timeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

                DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TITIK TERPILIH",
                                fontFamily = PlexSansFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = colors.accentCyan
                            )
                            StatusStampBadge(status = selected.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = String.format(Locale.US, "%.1f µT", selected.magnitude),
                                    fontFamily = PlexMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = colors.colorForStatus(selected.status)
                                )
                                Text(
                                    text = "Vektor: X ${String.format(Locale.US, "%+.1f", selected.x)} | Y ${String.format(Locale.US, "%+.1f", selected.y)} | Z ${String.format(Locale.US, "%+.1f", selected.z)}",
                                    fontFamily = PlexMonoFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteMeasurement(selected.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Hapus Titik",
                                    tint = colors.dangerRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        if (selected.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Catatan: ${selected.notes}",
                                fontFamily = PlexSansFontFamily,
                                fontSize = 12.sp,
                                color = colors.inkText
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Waktu ukur: ${timeFormat.format(Date(selected.timestamp))}",
                            fontFamily = PlexSansFontFamily,
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            // 4. Grafik Batang Per Titik Pengukuran
            if (uiState.measurements.isNotEmpty()) {
                DetectiveCrosshairCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SEBARAN INTENSITAS ANTAR TITIK",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.textMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        PointsBarChart(
                            measurements = uiState.measurements,
                            selectedId = uiState.selectedMeasurement?.id,
                            onSelect = viewModel::onPointSelected,
                            colors = colors
                        )
                    }
                }
            }

            // 5. Tombol Pindai & Tambah Titik Baru
            Button(
                onClick = onNavigateToScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentCyan,
                    contentColor = colors.paperBackground
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pindai & Tambah Titik Ukur",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun RoomPointsCanvas(
    measurements: List<Measurement>,
    selectedMeasurement: Measurement?,
    roomWidth: Float,
    roomLength: Float,
    onPointClicked: (Measurement) -> Unit,
    colors: com.detektifkamar.app.ui.theme.DetectiveColors,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .pointerInput(measurements) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val margin = 20.dp.toPx()
                    val planW = w - (margin * 2)
                    val planH = h - (margin * 2)

                    // Cari titik terdekat dengan toleransi 28dp
                    val hitRadius = 28.dp.toPx()
                    var closest: Measurement? = null
                    var closestDist = Float.MAX_VALUE

                    measurements.forEachIndexed { idx, m ->
                        // Jika koordinat manual tidak ada, sebarkan secara merata di sepanjang denah
                        val px = m.pointX ?: (margin + (planW * ((idx % 4 + 1) / 5f)))
                        val py = m.pointY ?: (margin + (planH * ((idx / 4 + 1) / 4f)))

                        val dx = tapOffset.x - px
                        val dy = tapOffset.y - py
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist <= hitRadius && dist < closestDist) {
                            closest = m
                            closestDist = dist
                        }
                    }

                    if (closest != null) {
                        onPointClicked(closest!!)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val margin = 20.dp.toPx()
        val planW = w - (margin * 2)
        val planH = h - (margin * 2)

        // Bingkai denah ruangan
        drawRoundRect(
            color = colors.borderOutline.copy(alpha = 0.5f),
            topLeft = Offset(margin, margin),
            size = Size(planW, planH),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )

        // Garis kisi millimeter
        val gridStep = 24.dp.toPx()
        var gx = margin + gridStep
        while (gx < margin + planW) {
            drawLine(
                color = colors.borderOutline.copy(alpha = 0.25f),
                start = Offset(gx, margin),
                end = Offset(gx, margin + planH),
                strokeWidth = 0.8.dp.toPx()
            )
            gx += gridStep
        }
        var gy = margin + gridStep
        while (gy < margin + planH) {
            drawLine(
                color = colors.borderOutline.copy(alpha = 0.25f),
                start = Offset(margin, gy),
                end = Offset(margin + planW, gy),
                strokeWidth = 0.8.dp.toPx()
            )
            gy += gridStep
        }

        // Gambar titik-titik ukur
        val textPaint = Paint().apply {
            color = colors.inkText.toArgb()
            textSize = 9.sp.toPx()
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        measurements.forEachIndexed { idx, m ->
            val px = m.pointX ?: (margin + (planW * ((idx % 4 + 1) / 5f)))
            val py = m.pointY ?: (margin + (planH * ((idx / 4 + 1) / 4f)))
            val ptColor = colors.colorForStatus(m.status)
            val isSelected = (selectedMeasurement?.id == m.id)

            if (isSelected) {
                // Ring lingkaran fokus
                drawCircle(
                    color = colors.accentCyan.copy(alpha = 0.35f),
                    radius = 16.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            drawCircle(
                color = ptColor,
                radius = 7.dp.toPx(),
                center = Offset(px, py)
            )

            drawCircle(
                color = colors.surface,
                radius = 3.dp.toPx(),
                center = Offset(px, py)
            )

            // Label nomor titik
            drawContext.canvas.nativeCanvas.drawText(
                "#${idx + 1}",
                px,
                py - 10.dp.toPx(),
                textPaint
            )
        }
    }
}

@Composable
private fun PointsBarChart(
    measurements: List<Measurement>,
    selectedId: Long?,
    onSelect: (Measurement) -> Unit,
    colors: com.detektifkamar.app.ui.theme.DetectiveColors,
    modifier: Modifier = Modifier
) {
    val maxVal = (measurements.maxOfOrNull { it.magnitude } ?: 100f).coerceAtLeast(65f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .pointerInput(measurements) {
                detectTapGestures { tapOffset ->
                    val w = size.width.toFloat()
                    val barWidth = (w / measurements.size.toFloat()).coerceAtMost(36.dp.toPx())
                    val totalWidth = barWidth * measurements.size
                    val startX = (w - totalWidth) / 2f

                    measurements.forEachIndexed { idx, m ->
                        val bx = startX + (idx * barWidth)
                        if (tapOffset.x in bx..(bx + barWidth)) {
                            onSelect(m)
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val paddingBottom = 16.dp.toPx()
        val chartH = h - paddingBottom

        val barSlotWidth = (w / measurements.size).coerceAtMost(36.dp.toPx())
        val barActualWidth = barSlotWidth * 0.65f
        val totalWidth = barSlotWidth * measurements.size
        val startX = (w - totalWidth) / 2f

        val textPaint = Paint().apply {
            color = colors.textMuted.toArgb()
            textSize = 8.sp.toPx()
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        measurements.forEachIndexed { idx, m ->
            val fraction = (m.magnitude / maxVal).coerceIn(0f, 1f)
            val barH = fraction * chartH
            val bx = startX + (idx * barSlotWidth) + ((barSlotWidth - barActualWidth) / 2f)
            val by = chartH - barH

            val isSelected = (selectedId == m.id)
            val barColor = if (isSelected) colors.accentCyan else colors.colorForStatus(m.status)

            drawRoundRect(
                color = barColor,
                topLeft = Offset(bx, by),
                size = Size(barActualWidth, barH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Label titik di bawah bar
            drawContext.canvas.nativeCanvas.drawText(
                "#${idx + 1}",
                bx + (barActualWidth / 2f),
                h - 2.dp.toPx(),
                textPaint
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color,
    colors: com.detektifkamar.app.ui.theme.DetectiveColors
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            text = value,
            fontFamily = PlexMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = color
        )
    }
}
