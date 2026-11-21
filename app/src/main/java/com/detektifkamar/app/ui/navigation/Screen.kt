package com.detektifkamar.app.ui.navigation

sealed class Screen(val route: String) {
    // 4 Tab Utama di Navigasi Bawah
    object HomeRooms : Screen("ruangan")
    object LiveScan : Screen("pindai")
    object Catalog : Screen("referensi")
    object History : Screen("riwayat")

    // Layar Pendukung / Detail
    object Calibration : Screen("kalibrasi")
    object Settings : Screen("pengaturan")

    object RoomDetail : Screen("room_detail/{roomId}") {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }

    object RoomForm : Screen("room_form?roomId={roomId}") {
        fun createRoute(roomId: Long = 0L) = "room_form?roomId=$roomId"
    }
}
