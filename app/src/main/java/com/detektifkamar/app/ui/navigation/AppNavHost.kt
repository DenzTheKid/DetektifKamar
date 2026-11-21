package com.detektifkamar.app.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.detektifkamar.app.appContainer
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.screens.calibration.CalibrationScreen
import com.detektifkamar.app.ui.screens.calibration.CalibrationViewModel
import com.detektifkamar.app.ui.screens.catalog.ReferenceCatalogScreen
import com.detektifkamar.app.ui.screens.catalog.ReferenceCatalogViewModel
import com.detektifkamar.app.ui.screens.history.HistoryScreen
import com.detektifkamar.app.ui.screens.history.HistoryViewModel
import com.detektifkamar.app.ui.screens.homerooms.HomeRoomsScreen
import com.detektifkamar.app.ui.screens.homerooms.HomeRoomsViewModel
import com.detektifkamar.app.ui.screens.livescan.LiveScanScreen
import com.detektifkamar.app.ui.screens.livescan.LiveScanViewModel
import com.detektifkamar.app.ui.screens.roomdetail.RoomDetailScreen
import com.detektifkamar.app.ui.screens.roomdetail.RoomDetailViewModel
import com.detektifkamar.app.ui.screens.roomform.RoomFormScreen
import com.detektifkamar.app.ui.screens.roomform.RoomFormViewModel
import com.detektifkamar.app.ui.screens.settings.SettingsScreen
import com.detektifkamar.app.ui.screens.settings.SettingsViewModel
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.PlexSansFontFamily

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val container = context.appContainer
    val colors = DetectiveTheme.colors

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Triple(Screen.HomeRooms.route, "Ruangan", DetectiveIcons.Kasus),
        Triple(Screen.LiveScan.route, "Pindai", DetectiveIcons.Selidik),
        Triple(Screen.Catalog.route, "Referensi", DetectiveIcons.Tersangka),
        Triple(Screen.History.route, "Riwayat", DetectiveIcons.Arsip)
    )

    val showBottomBar = bottomNavItems.any { it.first == currentRoute }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = colors.surface,
                    modifier = Modifier.border(width = 1.dp, color = colors.borderOutline.copy(alpha = 0.5f))
                ) {
                    bottomNavItems.forEach { (route, label, icon) ->
                        val selected = currentRoute == route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (selected) colors.accentCyan else colors.textMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontFamily = PlexSansFontFamily,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = if (selected) colors.accentCyan else colors.textMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = colors.accentCyan.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.HomeRooms.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab 1: Pemetaan Ruangan / Beranda
            composable(Screen.HomeRooms.route) {
                val vm: HomeRoomsViewModel = viewModel {
                    HomeRoomsViewModel(container.roomRepository)
                }
                HomeRoomsScreen(
                    viewModel = vm,
                    onNavigateToRoomDetail = { roomId ->
                        navController.navigate(Screen.RoomDetail.createRoute(roomId))
                    },
                    onNavigateToCreateRoom = {
                        navController.navigate(Screen.RoomForm.createRoute())
                    },
                    onNavigateToCalibration = {
                        navController.navigate(Screen.Calibration.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }

            // Tab 2: Pindai Medan Langsung
            composable(Screen.LiveScan.route) {
                val vm: LiveScanViewModel = viewModel {
                    LiveScanViewModel(
                        sensorRepository = container.sensorRepository,
                        roomRepository = container.roomRepository,
                        measurementRepository = container.measurementRepository,
                        preferencesRepository = container.preferencesRepository,
                        vibratorHelper = container.vibratorHelper
                    )
                }
                LiveScanScreen(
                    viewModel = vm,
                    onNavigateToCreateRoom = {
                        navController.navigate(Screen.RoomForm.createRoute())
                    }
                )
            }

            // Tab 3: Katalog Referensi Perangkat
            composable(Screen.Catalog.route) {
                val vm: ReferenceCatalogViewModel = viewModel {
                    ReferenceCatalogViewModel(container.deviceReferenceRepository)
                }
                ReferenceCatalogScreen(viewModel = vm)
            }

            // Tab 4: Riwayat Pengukuran
            composable(Screen.History.route) {
                val vm: HistoryViewModel = viewModel {
                    HistoryViewModel(
                        measurementRepository = container.measurementRepository,
                        roomRepository = container.roomRepository
                    )
                }
                HistoryScreen(viewModel = vm)
            }

            // Layar D: Kalibrasi Magnetik 15 Detik
            composable(Screen.Calibration.route) {
                val vm: CalibrationViewModel = viewModel {
                    CalibrationViewModel(
                        sensorRepository = container.sensorRepository,
                        calibrationRepository = container.calibrationRepository
                    )
                }
                CalibrationScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Layar B: Form Ruangan (Tambah / Edit)
            composable(
                route = Screen.RoomForm.route,
                arguments = listOf(
                    navArgument("roomId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                val vm: RoomFormViewModel = viewModel {
                    RoomFormViewModel(container.roomRepository, roomId)
                }
                RoomFormScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Layar F: Detail Ruangan
            composable(
                route = Screen.RoomDetail.route,
                arguments = listOf(
                    navArgument("roomId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                val vm: RoomDetailViewModel = viewModel {
                    RoomDetailViewModel(
                        roomId = roomId,
                        roomRepository = container.roomRepository,
                        measurementRepository = container.measurementRepository
                    )
                }
                RoomDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEditRoom = { id ->
                        navController.navigate(Screen.RoomForm.createRoute(id))
                    },
                    onNavigateToScan = {
                        navController.navigate(Screen.LiveScan.route)
                    }
                )
            }

            // Layar H: Pengaturan & Tentang
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = viewModel {
                    SettingsViewModel(container.preferencesRepository)
                }
                SettingsScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
