package com.detektifkamar.app.ui.screens.homerooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.ui.components.DetectiveCrosshairCard
import com.detektifkamar.app.ui.components.StatusStampBadge
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexMonoFontFamily
import com.detektifkamar.app.ui.theme.PlexSansFontFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoomsScreen(
    viewModel: HomeRoomsViewModel,
    onNavigateToRoomDetail: (Long) -> Unit,
    onNavigateToCreateRoom: () -> Unit,
    onNavigateToCalibration: () -> Unit,
    onNavigateToSettings: () -> Unit,
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
                            text = "DETEKTIF KAMAR",
                            fontFamily = PlexMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            letterSpacing = 1.2.sp,
                            color = colors.inkText
                        )
                        Text(
                            text = "Berkas Pemetaan Ruangan",
                            fontFamily = PlexSansFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCalibration) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.accentCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "8",
                                fontFamily = PlexMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = colors.accentCyan
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = colors.inkText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.paperBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateRoom,
                containerColor = colors.accentCyan,
                contentColor = colors.paperBackground,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Ruangan"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ruangan Baru",
                        fontFamily = PlexSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colors.accentCyan
                    )
                }
                uiState.rooms.isEmpty() -> {
                    EmptyRoomsView(
                        onAddClick = onNavigateToCreateRoom,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.rooms, key = { it.id }) { room ->
                            RoomItemCard(
                                room = room,
                                onClick = { onNavigateToRoomDetail(room.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomItemCard(
    room: Room,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = DetectiveTheme.colors
    val caseCode = String.format(Locale.US, "KASUS #%03d · %S", room.id, room.roomType.displayName)

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
                Text(
                    text = caseCode,
                    fontFamily = PlexMonoFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp,
                    color = colors.accentCyan
                )
                StatusStampBadge(status = room.dominantStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = room.name,
                fontFamily = PlexSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = colors.inkText
            )

            if (room.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = room.notes,
                    fontFamily = PlexSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${room.dimensionsLabel} · ${room.pointCount} titik ukur",
                    fontFamily = PlexMonoFontFamily,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )

                if (room.latestAverageUt != null) {
                    Text(
                        text = String.format(Locale.US, "Rata-rata: %.1f µT", room.latestAverageUt),
                        fontFamily = PlexMonoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = colors.inkText
                    )
                } else {
                    Text(
                        text = "Belum diukur",
                        fontFamily = PlexSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyRoomsView(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.accentCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📁",
                fontSize = 28.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Belum Ada Berkas Ruangan",
            fontFamily = PlexSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = colors.inkText
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Buat ruangan baru untuk memetakan titik-titik pengukuran medan magnet dan melacak sumber gangguan elektromagnetik.",
            fontFamily = PlexSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = colors.textMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(colors.accentCyan)
                .clickable { onAddClick() }
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Buka Berkas Ruangan Pertama",
                fontFamily = PlexSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.paperBackground
            )
        }
    }
}
