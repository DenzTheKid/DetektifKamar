package com.detektifkamar.app.ui.screens.formkasus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.R
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.components.VintageButton
import com.detektifkamar.app.ui.components.millimeterBackground
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FormKasusScreen(
    viewModel: FormKasusViewModel,
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

    val roomTypes = listOf(
        "Kamar tidur",
        "Kamar kos",
        "Ruang kerja",
        "Dapur",
        "Ruang tamu",
        "Lainnya"
    )

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
            // Header Form
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = DetectiveIcons.Kembali,
                        contentDescription = "Kembali",
                        tint = colors.inkText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.caseId > 0L) stringResource(R.string.form_ruangan_title_edit) else stringResource(R.string.form_ruangan_title_new),
                    fontFamily = IbmPlexSansFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.inkText
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Nama Ruangan
            Text(
                text = stringResource(R.string.form_ruangan_label_title),
                fontFamily = IbmPlexSansFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.title,
                onValueChange = viewModel::onTitleChanged,
                placeholder = {
                    Text(
                        text = stringResource(R.string.form_ruangan_hint_title),
                        fontFamily = IbmPlexSansFamily,
                        color = colors.textMuted
                    )
                },
                isError = uiState.titleError != null,
                supportingText = uiState.titleError?.let { { Text(it, color = colors.stampRed) } },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.cyanotypeBlue,
                    unfocusedBorderColor = colors.borderOutline,
                    focusedTextColor = colors.inkText,
                    unfocusedTextColor = colors.inkText,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tipe Ruangan
            Text(
                text = stringResource(R.string.form_ruangan_label_type),
                fontFamily = IbmPlexSansFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roomTypes.forEach { type ->
                    val isSelected = type == uiState.roomType
                    val chipBg = if (isSelected) colors.cyanotypeBlue else colors.surface
                    val chipTextColor = if (isSelected) Color.White else colors.inkText
                    val chipBorder = if (isSelected) colors.cyanotypeBlue else colors.borderOutline

                    Box(
                        modifier = Modifier
                            .background(chipBg, RoundedCornerShape(8.dp))
                            .border(1.dp, chipBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.onRoomTypeSelected(type) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = type,
                            color = chipTextColor,
                            fontFamily = IbmPlexSansFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Dimensi Ruangan (Lebar & Panjang)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.form_ruangan_label_width),
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.inkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.widthInput,
                        onValueChange = viewModel::onWidthChanged,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.widthError != null,
                        supportingText = uiState.widthError?.let { { Text(it, color = colors.stampRed, fontSize = 11.sp) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.cyanotypeBlue,
                            unfocusedBorderColor = colors.borderOutline,
                            focusedTextColor = colors.inkText,
                            unfocusedTextColor = colors.inkText,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        singleLine = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.form_ruangan_label_length),
                        fontFamily = IbmPlexSansFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.inkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = uiState.lengthInput,
                        onValueChange = viewModel::onLengthChanged,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.lengthError != null,
                        supportingText = uiState.lengthError?.let { { Text(it, color = colors.stampRed, fontSize = 11.sp) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.cyanotypeBlue,
                            unfocusedBorderColor = colors.borderOutline,
                            focusedTextColor = colors.inkText,
                            unfocusedTextColor = colors.inkText,
                            focusedContainerColor = colors.surface,
                            unfocusedContainerColor = colors.surface
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Catatan Ruangan (Opsional)
            Text(
                text = stringResource(R.string.form_ruangan_label_note),
                fontFamily = IbmPlexSansFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.inkText
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChanged,
                placeholder = {
                    Text(
                        text = "Misal: Dekat stopkontak utama, banyak peralatan elektronik...",
                        fontFamily = IbmPlexSansFamily,
                        color = colors.textMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.cyanotypeBlue,
                    unfocusedBorderColor = colors.borderOutline,
                    focusedTextColor = colors.inkText,
                    unfocusedTextColor = colors.inkText,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tombol Simpan
            VintageButton(
                text = stringResource(R.string.form_ruangan_btn_simpan),
                onClick = viewModel::saveCase,
                enabled = uiState.isFormValid && !uiState.isSaving,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
