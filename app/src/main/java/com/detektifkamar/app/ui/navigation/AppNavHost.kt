package com.detektifkamar.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.detektifkamar.app.R
import com.detektifkamar.app.appContainer
import com.detektifkamar.app.ui.components.DetectiveIcons
import com.detektifkamar.app.ui.screens.arsip.ArsipScreen
import com.detektifkamar.app.ui.screens.arsip.ArsipViewModel
import com.detektifkamar.app.ui.screens.detailkasus.DetailKasusScreen
import com.detektifkamar.app.ui.screens.detailkasus.DetailKasusViewModel
import com.detektifkamar.app.ui.screens.detailtitik.DetailTitikScreen
import com.detektifkamar.app.ui.screens.detailtitik.DetailTitikViewModel
import com.detektifkamar.app.ui.screens.formkasus.FormKasusScreen
import com.detektifkamar.app.ui.screens.formkasus.FormKasusViewModel
import com.detektifkamar.app.ui.screens.kasus.KasusScreen
import com.detektifkamar.app.ui.screens.kasus.KasusViewModel
import com.detektifkamar.app.ui.screens.pengaturan.PengaturanScreen
import com.detektifkamar.app.ui.screens.pengaturan.PengaturanViewModel
import com.detektifkamar.app.ui.screens.selidik.SelidikScreen
import com.detektifkamar.app.ui.screens.selidik.SelidikViewModel
import com.detektifkamar.app.ui.screens.tersangka.TersangkaScreen
import com.detektifkamar.app.ui.screens.tersangka.TersangkaViewModel
import com.detektifkamar.app.ui.screens.ukurtitik.UkurTitikScreen
import com.detektifkamar.app.ui.screens.ukurtitik.UkurTitikViewModel
import com.detektifkamar.app.ui.theme.DetectiveTheme
import com.detektifkamar.app.ui.theme.IbmPlexSansFamily

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val container = context.appContainer
    val colors = DetectiveTheme.colors

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Triple(Screen.Kasus.route, stringResource(R.string.nav_ruangan), DetectiveIcons.Kasus),
        Triple(Screen.Selidik.route, stringResource(R.string.nav_pindai), DetectiveIcons.Selidik),
        Triple(Screen.Arsip.route, stringResource(R.string.nav_riwayat), DetectiveIcons.Arsip),
        Triple(Screen.Tersangka.route, stringResource(R.string.nav_referensi), DetectiveIcons.Tersangka)
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
                                    tint = if (selected) colors.cyanotypeBlue else colors.textMuted
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontFamily = IbmPlexSansFamily,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) colors.cyanotypeBlue else colors.textMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Kasus.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // TAB 1: RUANGAN
            composable(Screen.Kasus.route) {
                val vm: KasusViewModel = viewModel(
                    factory = KasusViewModel.provideFactory(
                        container.caseRepository,
                        container.preferencesRepository
                    )
                )
                KasusScreen(
                    viewModel = vm,
                    onNavigateToNewCase = { navController.navigate(Screen.FormKasus.createRoute()) },
                    onNavigateToDetailCase = { caseId -> navController.navigate(Screen.DetailKasus.createRoute(caseId)) },
                    onNavigateToSettings = { navController.navigate(Screen.Pengaturan.route) }
                )
            }

            // FORM RUANGAN
            composable(
                route = Screen.FormKasus.route,
                arguments = listOf(navArgument("caseId") { type = NavType.StringType; defaultValue = "0" })
            ) { backStackEntry ->
                val vm: FormKasusViewModel = viewModel(
                    factory = FormKasusViewModel.provideFactory(
                        container.caseRepository,
                        backStackEntry.savedStateHandle
                    )
                )
                FormKasusScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // DETAIL RUANGAN
            composable(
                route = Screen.DetailKasus.route,
                arguments = listOf(navArgument("caseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getLong("caseId") ?: 0L
                val vm: DetailKasusViewModel = viewModel(
                    factory = DetailKasusViewModel.provideFactory(
                        caseId,
                        container.caseRepository,
                        container.suspectRepository,
                        container.preferencesRepository
                    )
                )
                DetailKasusScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToUkur = { cid, pid, isBase, x, y, label ->
                        navController.navigate(Screen.UkurTitik.createRoute(cid, pid, isBase, x, y, label))
                    },
                    onNavigateToPointDetail = { pid ->
                        navController.navigate(Screen.DetailTitik.createRoute(pid))
                    }
                )
            }

            // UKUR TITIK RUANGAN
            composable(
                route = Screen.UkurTitik.route,
                arguments = listOf(
                    navArgument("caseId") { type = NavType.LongType },
                    navArgument("pointId") { type = NavType.LongType; defaultValue = 0L },
                    navArgument("isBaseline") { type = NavType.BoolType; defaultValue = false },
                    navArgument("x") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("y") { type = NavType.FloatType; defaultValue = 0f },
                    navArgument("label") { type = NavType.StringType; defaultValue = "" }
                )
            ) { backStackEntry ->
                val cid = backStackEntry.arguments?.getLong("caseId") ?: 0L
                val pid = backStackEntry.arguments?.getLong("pointId") ?: 0L
                val isBase = backStackEntry.arguments?.getBoolean("isBaseline") ?: false
                val x = backStackEntry.arguments?.getFloat("x") ?: 0f
                val y = backStackEntry.arguments?.getFloat("y") ?: 0f
                val label = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("label") ?: "", "UTF-8")

                val vm: UkurTitikViewModel = viewModel(
                    factory = UkurTitikViewModel.provideFactory(
                        cid,
                        pid,
                        isBase,
                        x,
                        y,
                        label,
                        container.caseRepository,
                        container.sensorRepository,
                        container.preferencesRepository
                    )
                )
                UkurTitikScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // DETAIL TITIK UKUR
            composable(
                route = Screen.DetailTitik.route,
                arguments = listOf(navArgument("pointId") { type = NavType.LongType })
            ) { backStackEntry ->
                val pointId = backStackEntry.arguments?.getLong("pointId") ?: 0L
                val vm: DetailTitikViewModel = viewModel(
                    factory = DetailTitikViewModel.provideFactory(
                        pointId,
                        container.caseRepository,
                        container.suspectRepository,
                        container.preferencesRepository
                    )
                )
                DetailTitikScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // TAB 2: PINDAI LANGSUNG
            composable(Screen.Selidik.route) {
                val vm: SelidikViewModel = viewModel(
                    factory = SelidikViewModel.provideFactory(
                        container.sensorRepository,
                        container.caseRepository,
                        container.preferencesRepository,
                        container.vibratorHelper
                    )
                )
                SelidikScreen(
                    viewModel = vm,
                    onNavigateToUkur = { cid ->
                        navController.navigate(Screen.UkurTitik.createRoute(cid, 0L, false, 0f, 0f, "Titik Pindai"))
                    }
                )
            }

            // TAB 3: RIWAYAT PENGUKURAN
            composable(Screen.Arsip.route) {
                val vm: ArsipViewModel = viewModel(
                    factory = ArsipViewModel.provideFactory(container.caseRepository)
                )
                ArsipScreen(
                    viewModel = vm,
                    onNavigateToPointDetail = { pid ->
                        navController.navigate(Screen.DetailTitik.createRoute(pid))
                    }
                )
            }

            // TAB 4: REFERENSI PERANGKAT
            composable(Screen.Tersangka.route) {
                val vm: TersangkaViewModel = viewModel(
                    factory = TersangkaViewModel.provideFactory(container.suspectRepository)
                )
                TersangkaScreen(viewModel = vm)
            }

            // PENGATURAN
            composable(Screen.Pengaturan.route) {
                val vm: PengaturanViewModel = viewModel(
                    factory = PengaturanViewModel.provideFactory(container.preferencesRepository)
                )
                PengaturanScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
