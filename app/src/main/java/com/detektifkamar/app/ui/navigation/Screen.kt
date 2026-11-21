package com.detektifkamar.app.ui.navigation

sealed class Screen(val route: String) {
    // 4 Tab Utama
    object Kasus : Screen("kasus")
    object Selidik : Screen("selidik")
    object Tersangka : Screen("tersangka")
    object Arsip : Screen("arsip")

    // Sub-layar
    object FormKasus : Screen("form_kasus?caseId={caseId}") {
        fun createRoute(caseId: Long = 0L) = "form_kasus?caseId=$caseId"
    }

    object DetailKasus : Screen("detail_kasus/{caseId}") {
        fun createRoute(caseId: Long) = "detail_kasus/$caseId"
    }

    object UkurTitik : Screen("ukur_titik/{caseId}?pointId={pointId}&isBaseline={isBaseline}&x={x}&y={y}&label={label}") {
        fun createRoute(
            caseId: Long,
            pointId: Long = 0L,
            isBaseline: Boolean = false,
            x: Float = 0f,
            y: Float = 0f,
            label: String = ""
        ) = "ukur_titik/$caseId?pointId=$pointId&isBaseline=$isBaseline&x=$x&y=$y&label=${java.net.URLEncoder.encode(label, "UTF-8")}"
    }

    object DetailTitik : Screen("detail_titik/{pointId}") {
        fun createRoute(pointId: Long) = "detail_titik/$pointId"
    }

    object Pengaturan : Screen("pengaturan")
}
