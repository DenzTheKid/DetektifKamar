package com.detektifkamar.app.ui.screens.catalog

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.detektifkamar.app.domain.model.DeviceCategory
import com.detektifkamar.app.domain.model.DeviceReference
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferenceCatalogScreen(
    viewModel: ReferenceCatalogViewModel,
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
                    Column {
                        Text(
                            text = "KATALOG SUMBER MEDAN",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.inkText
                        )
                        Text(
                            text = "Referensi rentang tipikal & fisika perangkat",
                            fontFamily = PlexSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
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
            // 1. Kolom Pencarian
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Cari perangkat, trafo, motor...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cari",
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Hapus",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.borderOutline,
                        focusedTextColor = colors.inkText,
                        unfocusedTextColor = colors.inkText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 2. Chip Kategori Horizontal Scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogCategoryChip(
                    label = "Semua",
                    isSelected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) },
                    colors = colors
                )
                DeviceCategory.entries.forEach { cat ->
                    CatalogCategoryChip(
                        label = cat.displayName,
                        isSelected = uiState.selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) },
                        colors = colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3. Daftar Kartu Referensi
            if (uiState.devices.isEmpty()) {
                EmptyCatalogView(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.devices, key = { it.id }) { device ->
                        DeviceCatalogItemCard(
                            device = device,
                            onClick = { viewModel.onDeviceClicked(device) }
                        )
                    }
                }
            }
        }

        // Dialog Detail Perangkat Interaktif
        if (uiState.selectedDeviceDetail != null) {
            DeviceDetailDialog(
                device = uiState.selectedDeviceDetail!!,
                onDismiss = viewModel::closeDetailDialog
            )
        }
    }
}

@Composable
private fun CatalogCategoryChip(
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
private fun DeviceCatalogItemCard(
    device: DeviceReference,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors

    DetectiveCrosshairCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label Kategori
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceSecondary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = device.category.displayName.uppercase(),
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = colors.textMuted
                    )
                }

                if (device.isSaturatingSensor) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.dangerRed.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SATURASI SENSOR",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = colors.dangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = device.name,
                fontFamily = PlexSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colors.inkText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "RENTANG TIPIKAL",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp,
                        color = colors.textMuted
                    )
                    Text(
                        text = device.rangeLabel,
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.accentCyan
                    )
                }

                Text(
                    text = device.measurementDistance,
                    fontFamily = PlexSansFontFamily,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceDetailDialog(
    device: DeviceReference,
    onDismiss: () -> Unit
) {
    val colors = DetectiveTheme.colors

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.borderOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.category.displayName.uppercase(),
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp,
                            color = colors.accentCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = device.name,
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = colors.inkText
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = colors.textMuted)
                    }
                }

                // Rentang & Jarak
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
                                text = "Intensitas Fluks Tipikal",
                                fontFamily = PlexSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                            Text(
                                text = device.rangeLabel,
                                fontFamily = PlexMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = colors.accentCyan
                            )
                        }
                        Text(
                            text = device.measurementDistance,
                            fontFamily = PlexSansFontFamily,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                }

                // Bagian: Penjelasan Fisika
                Column {
                    Text(
                        text = "MENGAPA MEDAN MAGNET TIMBUL?",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = device.physicalExplanation,
                        fontFamily = PlexSansFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = colors.inkText
                    )
                }

                // Bagian: Catatan Keamanan
                Column {
                    Text(
                        text = "CATATAN KEAMANAN & PENGUKURAN",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = device.safetyNote,
                        fontFamily = PlexSansFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = colors.inkText
                    )
                }

                // Bagian: Sumber Rujukan Ilmiah
                Column {
                    Text(
                        text = "SUMBER RUJUKAN ILMIAH",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = device.verificationSource,
                        fontFamily = PlexMonoFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = colors.accentCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCatalogView(modifier: Modifier = Modifier) {
    val colors = DetectiveTheme.colors
    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(colors.accentCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔍", fontSize = 26.sp)
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Perangkat Tidak Ditemukan",
            fontFamily = PlexSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = colors.inkText
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Coba ubah kata kunci pencarian atau pilih kategori perangkat lainnya.",
            fontFamily = PlexSansFontFamily,
            fontSize = 12.sp,
            color = colors.textMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
