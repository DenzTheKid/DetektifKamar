package com.detektifkamar.app.ui.screens.roomform

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.RoomType
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomFormScreen(
    viewModel: RoomFormViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = DetectiveTheme.colors

    LaunchedEffect(uiState.isSavedSuccess) {
        if (uiState.isSavedSuccess) {
            onNavigateBack()
        }
    }

    val isEditMode = uiState.roomId > 0L
    val titleText = if (isEditMode) "Edit Berkas Ruangan" else "Buka Berkas Ruangan"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.paperBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = titleText,
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
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
            // Field 1: Nama Ruangan
            Column {
                Text(
                    text = "NAMA RUANGAN *",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("mis. Kamar Tidur Utama, Kos No. 12") },
                    isError = uiState.nameError != null,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = uiState.nameError ?: "Wajib diisi",
                                color = if (uiState.nameError != null) colors.dangerRed else colors.textMuted
                            )
                            Text(
                                text = "${uiState.name.length}/40",
                                color = colors.textMuted
                            )
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

            // Field 2: Tipe Ruangan (Chips)
            Column {
                Text(
                    text = "TIPE RUANGAN",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoomType.entries.forEach { type ->
                        val isSelected = (uiState.selectedType == type)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) colors.accentCyan.copy(alpha = 0.18f) else colors.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) colors.accentCyan else colors.borderOutline
                            ),
                            modifier = Modifier.clickable { viewModel.onTypeSelected(type) }
                        ) {
                            Text(
                                text = type.displayName,
                                fontFamily = PlexSansFontFamily,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 13.sp,
                                color = if (isSelected) colors.accentCyan else colors.inkText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Field 3: Dimensi Ruangan (Opsional)
            Column {
                Text(
                    text = "UKURAN RUANGAN (METER, OPSIONAL)",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.widthInput,
                        onValueChange = viewModel::onWidthChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Lebar (m)") },
                        placeholder = { Text("mis. 3.5") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.widthError != null,
                        supportingText = uiState.widthError?.let { { Text(it, color = colors.dangerRed) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentCyan,
                            unfocusedBorderColor = colors.borderOutline,
                            focusedTextColor = colors.inkText,
                            unfocusedTextColor = colors.inkText
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.lengthInput,
                        onValueChange = viewModel::onLengthChanged,
                        modifier = Modifier.weight(1f),
                        label = { Text("Panjang (m)") },
                        placeholder = { Text("mis. 4.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = uiState.lengthError != null,
                        supportingText = uiState.lengthError?.let { { Text(it, color = colors.dangerRed) } },
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
            }

            // Field 4: Catatan Tambahan (Opsional)
            Column {
                Text(
                    text = "CATATAN PENYELIDIKAN (OPSIONAL)",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = { Text("mis. Ruangan lantai 2 dekat tiang listrik / boks MCB") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accentCyan,
                        unfocusedBorderColor = colors.borderOutline,
                        focusedTextColor = colors.inkText,
                        unfocusedTextColor = colors.inkText
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Simpan
            Button(
                onClick = viewModel::saveRoom,
                enabled = uiState.isValid && !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentCyan,
                    contentColor = colors.paperBackground,
                    disabledContainerColor = colors.borderOutline,
                    disabledContentColor = colors.textMuted
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditMode) "Perbarui Berkas" else "Simpan Berkas Ruangan",
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
