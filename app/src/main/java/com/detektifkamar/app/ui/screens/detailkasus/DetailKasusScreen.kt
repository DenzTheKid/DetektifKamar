package com.detektifkamar.app.ui.screens.detailkasus

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.R
import com.detektifkamar.app.domain.logic.CoordinateTransform
import com.detektifkamar.app.domain.logic.DisturbanceClassifier
import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePointWithReading
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.SegmentedTabControl
import com.detektifkamar.app.ui.components.VintageButton
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.HeatmapColors
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailKasusScreen(
    viewModel: DetailKasusViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToUkur: (caseId: Long, pointId: Long, isBaseline: Boolean, x: Float, y: Float, label: String) -> Unit,
    onNavigateToPointDetail: (pointId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors
    val context = LocalContext.current

    val case = uiState.case

    // Dialog Tambah Titik Ukur Baru
    var newPointName by remember { mutableStateOf("") }
    if (uiState.showNewPointDialog && uiState.pendingNewPointCoords != null) {
        val coords = uiState.pendingNewPointCoords!!
        AlertDialog(
            onDismissRequest = viewModel::dismissNewPointDialog,
            title = {
                Text("TANDAI TITIK UKUR BARU", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, color = colors.inkText)
            },
            text = {
                Column {
                    Text(
                        "Koordinat Denah: (%.2f, %.2f) meter".format(Locale("id", "ID"), coords.first, coords.second),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPointName,
                        onValueChange = { newPointName = it },
                        placeholder = { Text("Nama Titik (mis. Sudut Meja, Dekat TV)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val label = newPointName.ifBlank { "Titik ${uiState.pointsWithReadings.size + 1}" }
                        viewModel.dismissNewPointDialog()
                        newPointName = ""
                        if (case != null) {
                            onNavigateToUkur(case.id, 0L, false, coords.first, coords.second, label)
                        }
                    }
                ) {
                    Text("REKAM MEDAN", fontFamily = IbmPlexSansFamily, color = colors.cyanotypeBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissNewPointDialog) {
                    Text("BATAL", color = colors.textMuted)
                }
            }
        )
    }

    // Dialog Tambah Perabot
    var furnitureLabel by remember { mutableStateOf("Meja Kerja") }
    var furnitureKind by remember { mutableStateOf("Kayu/Logam") }
    var furnitureX by remember { mutableStateOf("0.5") }
    var furnitureY by remember { mutableStateOf("0.5") }
    var furnitureW by remember { mutableStateOf("1.2") }
    var furnitureH by remember { mutableStateOf("0.8") }

    if (uiState.showAddFurnitureDialog && case != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissAddFurnitureDialog,
            title = {
                Text("TAMBAH PERABOT RUANGAN", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, color = colors.inkText)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = furnitureLabel,
                        onValueChange = { furnitureLabel = it },
                        label = { Text("Nama Perabot") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = furnitureX,
                            onValueChange = { furnitureX = it },
                            label = { Text("Posisi X (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = furnitureY,
                            onValueChange = { furnitureY = it },
                            label = { Text("Posisi Y (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = furnitureW,
                            onValueChange = { furnitureW = it },
                            label = { Text("Lebar (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = furnitureH,
                            onValueChange = { furnitureH = it },
                            label = { Text("Panjang (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val x = furnitureX.replace(',', '.').toFloatOrNull() ?: 0f
                        val y = furnitureY.replace(',', '.').toFloatOrNull() ?: 0f
                        val w = furnitureW.replace(',', '.').toFloatOrNull() ?: 1f
                        val h = furnitureH.replace(',', '.').toFloatOrNull() ?: 1f
                        viewModel.addFurniture(furnitureLabel, furnitureKind, x, y, w, h)
                    }
                ) {
                    Text("PASANG", fontFamily = IbmPlexSansFamily, color = colors.cyanotypeBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissAddFurnitureDialog) {
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
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = DetectiveIcons.Kembali, contentDescription = "Kembali", tint = colors.inkText)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = case?.title ?: "Memuat Ruangan...",
                            fontFamily = IbmPlexSansFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.inkText
                        )
                        Text(
                            text = if (case != null) "${case.roomType} • %.1f x %.1f m".format(Locale("id", "ID"), case.widthM, case.lengthM) else "",
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                if (case?.baselineUt == null) {
                    DetectiveStamp(
                        text = stringResource(R.string.stamp_belum_ada_acuan),
                        color = colors.stampRed
                    )
                } else {
                    DetectiveStamp(
                        text = "%.1f µT".format(case.baselineUt),
                        color = colors.cyanotypeBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Segmented Tab Kontrol Modern (Denah | Titik Ukur | Ringkasan Analisis)
            SegmentedTabControl(
                tabs = listOf(
                    stringResource(R.string.tab_denah),
                    stringResource(R.string.tab_titik_ukur),
                    stringResource(R.string.tab_laporan)
                ),
                selectedIndex = uiState.currentTab,
                onTabSelected = viewModel::selectTab
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Isi Tab Berdasarkan Pilihan
            when (uiState.currentTab) {
                0 -> {
                    DenahTabContent(
                        uiState = uiState,
                        onCanvasTapped = viewModel::onCanvasTapped,
                        onPointSelected = viewModel::selectPoint,
                        onLayerSelected = viewModel::selectLayer,
                        onOpenAddFurniture = viewModel::openAddFurnitureDialog,
                        onNavigateToUkur = { p ->
                            if (case != null) {
                                onNavigateToUkur(case.id, p.point.id, false, p.point.xM, p.point.yM, p.point.label)
                            }
                        },
                        onNavigateToPointDetail = onNavigateToPointDetail,
                        onDeletePoint = viewModel::deletePoint
                    )
                }
                1 -> {
                    BuktiTabContent(
                        uiState = uiState,
                        onNavigateToPointDetail = onNavigateToPointDetail,
                        onUkurAcuan = {
                            if (case != null) {
                                onNavigateToUkur(case.id, 0L, true, 0f, 0f, "Titik Acuan Lingkungan")
                            }
                        }
                    )
                }
                2 -> {
                    LaporanTabContent(
                        uiState = uiState,
                        onShareReport = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, uiState.formattedReportText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Laporan Pemetaan")
                            context.startActivity(shareIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DenahTabContent(
    uiState: DetailKasusUiState,
    onCanvasTapped: (Float, Float) -> Unit,
    onPointSelected: (EvidencePointWithReading?) -> Unit,
    onLayerSelected: (MapLayer) -> Unit,
    onOpenAddFurniture: () -> Unit,
    onNavigateToUkur: (EvidencePointWithReading) -> Unit,
    onNavigateToPointDetail: (Long) -> Unit,
    onDeletePoint: (Long) -> Unit
) {
    val colors = DetectiveTheme.colors
    val case = uiState.case ?: return
    val tolerancePx = 24.dp.value * 2.5f

    Column(modifier = Modifier.fillMaxSize()) {
        // Lapisan selector modern
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    Pair(MapLayer.MAGNET, stringResource(R.string.layer_magnet)),
                    Pair(MapLayer.CAHAYA, stringResource(R.string.layer_cahaya)),
                    Pair(MapLayer.POLOS, stringResource(R.string.layer_polos))
                ).forEach { (layer, label) ->
                    val isSel = uiState.activeLayer == layer
                    val pillShape = RoundedCornerShape(8.dp)
                    Box(
                        modifier = Modifier
                            .clip(pillShape)
                            .background(if (isSel) colors.cyanotypeBlue else colors.surface)
                            .border(1.dp, if (isSel) colors.cyanotypeBlue else colors.borderOutline, pillShape)
                            .clickable { onLayerSelected(layer) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = IbmPlexSansFamily,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (isSel) Color.White else colors.inkText
                        )
                    }
                }
            }

            TextButton(onClick = onOpenAddFurniture) {
                Text("+ Perabot", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.SemiBold, color = colors.cyanotypeBlue, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Canvas Denah Berskala Modern
        val canvasShape = RoundedCornerShape(14.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(canvasShape)
                .background(colors.surface)
                .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), canvasShape)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(case.id, uiState.pointsWithReadings) {
                        detectTapGestures { offset ->
                            val bounds = CoordinateTransform.calculateBounds(
                                availableWidthPx = size.width.toFloat(),
                                availableHeightPx = size.height.toFloat(),
                                roomWidthM = case.widthM,
                                roomLengthM = case.lengthM,
                                marginPx = 28f
                            )

                            // Hit-test titik ukur (toleransi sentuh 24dp)
                            var clickedPoint: EvidencePointWithReading? = null
                            for (p in uiState.pointsWithReadings) {
                                val ptPxX = CoordinateTransform.meterToPixelX(p.point.xM, bounds)
                                val ptPxY = CoordinateTransform.meterToPixelY(p.point.yM, bounds)
                                if (CoordinateTransform.isPointHit(offset.x, offset.y, ptPxX, ptPxY, tolerancePx)) {
                                    clickedPoint = p
                                    break
                                }
                            }

                            if (clickedPoint != null) {
                                onPointSelected(clickedPoint)
                            } else {
                                val meterX = CoordinateTransform.pixelToMeterX(offset.x, bounds, case.widthM)
                                val meterY = CoordinateTransform.pixelToMeterY(offset.y, bounds, case.lengthM)
                                onCanvasTapped(meterX, meterY)
                            }
                        }
                    }
            ) {
                val bounds = CoordinateTransform.calculateBounds(
                    availableWidthPx = size.width,
                    availableHeightPx = size.height,
                    roomWidthM = case.widthM,
                    roomLengthM = case.lengthM,
                    marginPx = 28f
                )

                // 1. Gambar Batas Ruangan
                drawRect(
                    color = colors.paperBackground,
                    topLeft = Offset(bounds.offsetX, bounds.offsetY),
                    size = Size(bounds.roomWidthPx, bounds.roomLengthPx)
                )

                // 2. Garis Kisi Tiap 0.5 Meter
                var gx = 0.5f
                while (gx < case.widthM) {
                    val px = bounds.offsetX + gx * bounds.scale
                    drawLine(
                        color = colors.gridLine,
                        start = Offset(px, bounds.offsetY),
                        end = Offset(px, bounds.offsetY + bounds.roomLengthPx),
                        strokeWidth = 0.8.dp.toPx()
                    )
                    gx += 0.5f
                }
                var gy = 0.5f
                while (gy < case.lengthM) {
                    val py = bounds.offsetY + gy * bounds.scale
                    drawLine(
                        color = colors.gridLine,
                        start = Offset(bounds.offsetX, py),
                        end = Offset(bounds.offsetX + bounds.roomWidthPx, py),
                        strokeWidth = 0.8.dp.toPx()
                    )
                    gy += 0.5f
                }

                // 3. Lapisan Peta Panas (Interpolasi IDW)
                if (uiState.activeLayer == MapLayer.MAGNET && uiState.magneticHeatmap != null) {
                    val grid = uiState.magneticHeatmap
                    val cellWidthPx = grid.cellWidthM * bounds.scale
                    val cellHeightPx = grid.cellHeightM * bounds.scale

                    for (r in 0 until grid.rows) {
                        for (c in 0 until grid.cols) {
                            val valDelta = grid.values[r * grid.cols + c]
                            val cellX = bounds.offsetX + c * cellWidthPx
                            val cellY = bounds.offsetY + r * cellHeightPx

                            if (!valDelta.isNaN()) {
                                val cellColor = when {
                                    valDelta < 5.0f -> HeatmapColors.Tenang
                                    valDelta < 15.0f -> HeatmapColors.GangguanRingan
                                    valDelta < 50.0f -> HeatmapColors.GangguanKuat
                                    else -> HeatmapColors.SumberDekat
                                }
                                drawRect(
                                    color = cellColor,
                                    topLeft = Offset(cellX, cellY),
                                    size = Size(cellWidthPx + 0.5f, cellHeightPx + 0.5f)
                                )
                            }
                        }
                    }
                } else if (uiState.activeLayer == MapLayer.CAHAYA && uiState.lightHeatmap != null) {
                    val grid = uiState.lightHeatmap
                    val cellWidthPx = grid.cellWidthM * bounds.scale
                    val cellHeightPx = grid.cellHeightM * bounds.scale

                    for (r in 0 until grid.rows) {
                        for (c in 0 until grid.cols) {
                            val lux = grid.values[r * grid.cols + c]
                            val cellX = bounds.offsetX + c * cellWidthPx
                            val cellY = bounds.offsetY + r * cellHeightPx

                            if (!lux.isNaN()) {
                                val fraction = (lux / 800f).coerceIn(0.1f, 0.85f)
                                drawRect(
                                    color = colors.highlighterYellow.copy(alpha = fraction),
                                    topLeft = Offset(cellX, cellY),
                                    size = Size(cellWidthPx + 0.5f, cellHeightPx + 0.5f)
                                )
                            }
                        }
                    }
                }

                // 4. Gambar Perabot (Kotak berlabel)
                uiState.furnitureList.forEach { f ->
                    val fx = bounds.offsetX + f.xM * bounds.scale
                    val fy = bounds.offsetY + f.yM * bounds.scale
                    val fw = f.wM * bounds.scale
                    val fh = f.hM * bounds.scale

                    drawRect(
                        color = colors.surfaceSecondary.copy(alpha = 0.9f),
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh)
                    )
                    drawRect(
                        color = colors.borderOutline,
                        topLeft = Offset(fx, fy),
                        size = Size(fw, fh),
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Bingkai Dinding Ruangan
                drawRect(
                    color = colors.inkText,
                    topLeft = Offset(bounds.offsetX, bounds.offsetY),
                    size = Size(bounds.roomWidthPx, bounds.roomLengthPx),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 5. Gambar Titik Ukur Bernomor
                uiState.pointsWithReadings.forEachIndexed { index, p ->
                    val ptPxX = bounds.offsetX + p.point.xM * bounds.scale
                    val ptPxY = bounds.offsetY + p.point.yM * bounds.scale

                    val isSelected = uiState.selectedPoint?.point?.id == p.point.id

                    val dotColor = when (p.category) {
                        DisturbanceCategory.TENANG -> colors.labGreen
                        DisturbanceCategory.GANGGUAN_RINGAN -> colors.highlighterYellow
                        DisturbanceCategory.GANGGUAN_KUAT -> HeatmapColors.GangguanKuat
                        DisturbanceCategory.SUMBER_DEKAT -> colors.stampRed
                    }

                    if (isSelected) {
                        drawCircle(
                            color = colors.cyanotypeBlue,
                            radius = 16.dp.toPx(),
                            center = Offset(ptPxX, ptPxY),
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                    }

                    drawCircle(
                        color = dotColor,
                        radius = 8.dp.toPx(),
                        center = Offset(ptPxX, ptPxY)
                    )
                    drawCircle(
                        color = colors.surface,
                        radius = 4.dp.toPx(),
                        center = Offset(ptPxX, ptPxY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Panel Bawah: Detail Titik Terpilih atau Legenda
        if (uiState.selectedPoint != null) {
            val sel = uiState.selectedPoint
            val panelShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(panelShape)
                    .background(colors.surface)
                    .border(1.dp, colors.borderOutline.copy(alpha = 0.8f), panelShape)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = sel.point.label,
                                fontFamily = IbmPlexSansFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.inkText
                            )
                            Text(
                                text = "Koordinat: (%.2f, %.2f) m".format(Locale("id", "ID"), sel.point.xM, sel.point.yM),
                                fontFamily = IbmPlexMonoFamily,
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }

                        val reading = sel.latestReading
                        if (reading != null) {
                            Text(
                                text = "%.1f µT".format(reading.meanUt),
                                fontFamily = IbmPlexMonoFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.inkText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VintageButton(
                            text = "Ukur Ulang",
                            onClick = { onNavigateToUkur(sel) },
                            isPrimary = true,
                            modifier = Modifier.weight(1f),
                            paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                        )
                        VintageButton(
                            text = "Rincian Titik",
                            onClick = { onNavigateToPointDetail(sel.point.id) },
                            isPrimary = false,
                            modifier = Modifier.weight(1f),
                            paddingValues = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                        )
                        IconButton(onClick = { onDeletePoint(sel.point.id) }) {
                            Icon(imageVector = DetectiveIcons.Hapus, contentDescription = "Hapus", tint = colors.stampRed)
                        }
                    }
                }
            }
        } else {
            // Catatan Interpolasi & Legenda
            Column(modifier = Modifier.fillMaxWidth()) {
                if (uiState.pointsWithReadings.size < 3) {
                    Text(
                        text = stringResource(R.string.heatmap_min_points_notice),
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                } else {
                    Text(
                        text = stringResource(R.string.heatmap_disclaimer),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 10.sp,
                        color = colors.textMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem("Normal (<5)", colors.labGreen)
                    LegendItem("Rendah (5-15)", colors.highlighterYellow)
                    LegendItem("Tinggi (15-50)", HeatmapColors.GangguanKuat)
                    LegendItem("Kuat (≥50)", colors.stampRed)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    val colors = DetectiveTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontFamily = IbmPlexMonoFamily,
            fontSize = 10.sp,
            color = colors.textMuted
        )
    }
}

@Composable
private fun BuktiTabContent(
    uiState: DetailKasusUiState,
    onNavigateToPointDetail: (Long) -> Unit,
    onUkurAcuan: () -> Unit
) {
    val colors = DetectiveTheme.colors
    val dateFormat = SimpleDateFormat("HH:mm, dd MMM", Locale("id", "ID"))

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${uiState.pointsWithReadings.size} Titik Ukur Tercatat",
                fontFamily = IbmPlexSansFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.inkText
            )
            VintageButton(
                text = "Set Baseline Ruangan",
                onClick = onUkurAcuan,
                isPrimary = false,
                paddingValues = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.pointsWithReadings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada titik ukur. Buka tab 'Denah Ruangan' dan sentuh area ruangan untuk menandai titik baru.",
                    fontFamily = IbmPlexSansFamily,
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.pointsWithReadings) { p ->
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.inkText
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Posisi: (%.1f, %.1f) m • ${reading?.quality ?: "Belum diukur"}".format(
                                        Locale("id", "ID"),
                                        p.point.xM,
                                        p.point.yM
                                    ),
                                    fontFamily = IbmPlexMonoFamily,
                                    fontSize = 12.sp,
                                    color = colors.textMuted
                                )
                                if (reading != null) {
                                    Text(
                                        text = "Waktu: ${dateFormat.format(Date(reading.measuredAt))}",
                                        fontFamily = IbmPlexMonoFamily,
                                        fontSize = 11.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                if (reading != null) {
                                    Text(
                                        text = "%.1f µT".format(reading.meanUt),
                                        fontFamily = IbmPlexMonoFamily,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.inkText
                                    )
                                    val delta = p.deltaUt
                                    if (delta != null) {
                                        Text(
                                            text = "Δ %.1f µT (%s)".format(delta, p.category.label),
                                            fontFamily = IbmPlexMonoFamily,
                                            fontSize = 11.sp,
                                            color = when (p.category) {
                                                DisturbanceCategory.TENANG -> colors.labGreen
                                                DisturbanceCategory.GANGGUAN_RINGAN -> colors.highlighterYellow
                                                DisturbanceCategory.GANGGUAN_KUAT -> HeatmapColors.GangguanKuat
                                                DisturbanceCategory.SUMBER_DEKAT -> colors.stampRed
                                            }
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Belum Diukur",
                                        fontFamily = IbmPlexSansFamily,
                                        fontSize = 12.sp,
                                        color = colors.stampRed
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
private fun LaporanTabContent(
    uiState: DetailKasusUiState,
    onShareReport: () -> Unit
) {
    val colors = DetectiveTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // Kartu Ringkasan
        val summaryCardShape = RoundedCornerShape(14.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(summaryCardShape)
                .background(colors.surface)
                .border(1.dp, colors.borderOutline.copy(alpha = 0.7f), summaryCardShape)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "RINGKASAN PEMETAAN MEDAN",
                    fontFamily = IbmPlexSansFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.inkText
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ReportStatMetric("Total Titik", "${uiState.summary.pointCount}")
                    ReportStatMetric("Simpangan Max", "%.1f µT".format(uiState.summary.maxDeltaUt))
                    ReportStatMetric("Rata-rata Kuat", "%.1f µT".format(uiState.summary.avgMeanUt))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Perangkat Terkait
        Text(
            text = "PERANGKAT ELEKTRONIK TERKAIT (${uiState.allLinkedSuspects.size})",
            fontFamily = IbmPlexSansFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.inkText
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.allLinkedSuspects.isEmpty()) {
            Text(
                text = "Belum ada perangkat referensi yang dikaitkan pada titik ukur ruangan ini.",
                fontFamily = IbmPlexSansFamily,
                fontSize = 13.sp,
                color = colors.textMuted
            )
        } else {
            uiState.allLinkedSuspects.forEach { s ->
                val cardShape = RoundedCornerShape(10.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(cardShape)
                        .background(colors.surface)
                        .border(1.dp, colors.borderOutline.copy(alpha = 0.5f), cardShape)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = s.name,
                            fontFamily = IbmPlexSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.cyanotypeBlue
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rentang Tipikal: %.0f - %.0f µT pada jarak %d cm".format(s.typicalMinUt, s.typicalMaxUt, s.typicalDistanceCm),
                            fontFamily = IbmPlexMonoFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Tombol Bagikan Laporan (Intent.ACTION_SEND)
        VintageButton(
            text = "Bagikan Ringkasan Laporan",
            onClick = onShareReport,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ReportStatMetric(label: String, value: String) {
    val colors = DetectiveTheme.colors
    Column {
        Text(text = label, fontFamily = IbmPlexSansFamily, fontSize = 11.sp, color = colors.textMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontFamily = IbmPlexMonoFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = colors.inkText)
    }
}
