package com.detektifkamar.app.ui.screens.tersangka

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.SuspectCategory
import com.detektifkamar.app.ui.components.DetectiveStamp
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexMonoFamily
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@Composable
fun TersangkaScreen(
    viewModel: TersangkaViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors

    // Dialog Detail Referensi Perangkat
    val detail = uiState.selectedSuspectDetail
    if (detail != null) {
        AlertDialog(
            onDismissRequest = { viewModel.selectSuspectForDetail(null) },
            title = {
                Text(
                    text = detail.name,
                    fontFamily = IbmPlexSansFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.inkText
                )
            },
            text = {
                Column {
                    DetectiveStamp(text = detail.category.label, color = colors.cyanotypeBlue)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Rentang Tipikal: %.0f - %.0f µT".format(detail.typicalMinUt, detail.typicalMaxUt),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.cyanotypeBlue
                    )
                    Text(
                        text = "Jarak Pengukuran: ~%d cm".format(detail.typicalDistanceCm),
                        fontFamily = IbmPlexMonoFamily,
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Karakteristik Sumber:",
                        fontFamily = IbmPlexSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.inkText
                    )
                    Text(
                        text = detail.note,
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.inkText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Saran Verifikasi / Pengurangan:",
                        fontFamily = IbmPlexSansFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = colors.cyanotypeBlue
                    )
                    Text(
                        text = detail.howToConfirm,
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 12.sp,
                        color = colors.inkText
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.selectSuspectForDetail(null) }) {
                    Text("TUTUP", fontFamily = IbmPlexSansFamily, fontWeight = FontWeight.Bold, color = colors.cyanotypeBlue)
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Header Top Bar
            Text(
                text = "KATALOG REFERENSI PERANGKAT",
                fontFamily = IbmPlexSansFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.inkText
            )
            Text(
                text = "Daftar emisi medan elektromagnetik tipikal pada benda di dalam ruangan",
                fontFamily = IbmPlexSansFamily,
                fontSize = 12.sp,
                color = colors.textMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Kolom Pencarian
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text("Cari nama perangkat atau jenis komponen...") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Kategori
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryFilterChip(
                    label = "Semua",
                    isSelected = uiState.selectedCategory == null,
                    onClick = { viewModel.onCategorySelected(null) }
                )
                SuspectCategory.entries.forEach { cat ->
                    CategoryFilterChip(
                        label = cat.label,
                        isSelected = uiState.selectedCategory == cat,
                        onClick = { viewModel.onCategorySelected(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daftar Katalog
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.suspects, key = { it.id }) { s ->
                    val cardShape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(cardShape)
                            .background(colors.surface)
                            .border(1.dp, colors.borderOutline.copy(alpha = 0.6f), cardShape)
                            .clickable { viewModel.selectSuspectForDetail(s) }
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = s.name,
                                    fontFamily = IbmPlexSansFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.inkText
                                )
                                DetectiveStamp(
                                    text = s.category.label,
                                    color = colors.cyanotypeBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kisaran: %.0f - %.0f µT (pada jarak %d cm)".format(s.typicalMinUt, s.typicalMaxUt, s.typicalDistanceCm),
                                fontFamily = IbmPlexMonoFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.cyanotypeBlue
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = s.note,
                                fontFamily = IbmPlexSansFamily,
                                fontSize = 12.sp,
                                color = colors.textMuted,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CategoryFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = DetectiveTheme.colors
    val bg = if (isSelected) colors.cyanotypeBlue else colors.surface
    val textColor = if (isSelected) Color.White else colors.inkText
    val borderColor = if (isSelected) colors.cyanotypeBlue else colors.borderOutline
    val chipShape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .clip(chipShape)
            .background(bg)
            .border(1.dp, borderColor, chipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontFamily = IbmPlexSansFamily,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 11.sp,
            color = textColor
        )
    }
}
