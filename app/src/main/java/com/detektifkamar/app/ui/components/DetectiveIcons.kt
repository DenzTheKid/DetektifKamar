package com.detektifkamar.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Kumpulan ikon vektor modern bergaris bersih (1.8dp stroke).
 */
object DetectiveIcons {

    // Tab 1: Ruangan / Denah
    val Kasus: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconRuangan",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Bingkai denah ruangan dengan pintu terbuka
            moveTo(3f, 3f)
            lineTo(21f, 3f)
            lineTo(21f, 21f)
            lineTo(13f, 21f)
            moveTo(9f, 21f)
            lineTo(3f, 21f)
            lineTo(3f, 3f)
            // Sekat ruangan internal
            moveTo(3f, 12f)
            lineTo(11f, 12f)
            moveTo(14f, 12f)
            lineTo(21f, 12f)
        }.build()
    }

    // Tab 2: Pindai / Scanner Langsung
    val Selidik: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconPindai",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Gelombang pulsa scanner
            moveTo(12f, 12f)
            moveTo(12f, 2f)
            curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
            curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
            curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
            curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
            // Lingkaran konsentris dalam
            moveTo(12f, 6f)
            curveTo(15.31f, 6f, 18f, 8.69f, 18f, 12f)
            curveTo(18f, 15.31f, 15.31f, 18f, 12f, 18f)
            curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
            curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
            // Titik pusat
            moveTo(12f, 12f)
            lineTo(12f, 12.01f)
        }.build()
    }

    // Tab 3: Referensi Perangkat (dulu Tersangka)
    val Tersangka: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconReferensi",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Ikon perangkat / modul chip elektronik
            moveTo(5f, 4f)
            lineTo(19f, 4f)
            lineTo(19f, 20f)
            lineTo(5f, 20f)
            close()
            moveTo(9f, 9f)
            lineTo(15f, 9f)
            lineTo(15f, 15f)
            lineTo(9f, 15f)
            close()
            moveTo(12f, 1f)
            lineTo(12f, 4f)
            moveTo(12f, 20f)
            lineTo(12f, 23f)
            moveTo(1f, 12f)
            lineTo(4f, 12f)
            moveTo(20f, 12f)
            lineTo(23f, 12f)
        }.build()
    }

    // Tab 4: Riwayat Pengukuran (dulu Arsip)
    val Arsip: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconRiwayat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            // Jam / log riwayat waktu
            moveTo(12f, 2f)
            curveTo(17.52f, 2f, 22f, 6.48f, 22f, 12f)
            curveTo(22f, 17.52f, 17.52f, 22f, 12f, 22f)
            curveTo(6.48f, 22f, 2f, 17.52f, 2f, 12f)
            curveTo(2f, 6.48f, 6.48f, 2f, 12f, 2f)
            moveTo(12f, 6f)
            lineTo(12f, 12f)
            lineTo(16f, 14f)
        }.build()
    }

    val Pengaturan: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconPengaturan",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(12f, 15f)
            curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
            curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
            curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
            curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
            moveTo(19.4f, 13f)
            lineTo(20.8f, 12f)
            lineTo(19.4f, 11f)
            lineTo(18.9f, 9.3f)
            lineTo(20.2f, 8f)
            lineTo(18f, 5.8f)
            lineTo(16.7f, 7.1f)
            lineTo(15f, 6.6f)
            lineTo(14f, 5.2f)
            lineTo(12f, 5.2f)
            lineTo(10f, 5.2f)
            lineTo(9f, 6.6f)
            lineTo(7.3f, 7.1f)
            lineTo(6f, 5.8f)
            lineTo(3.8f, 8f)
            lineTo(5.1f, 9.3f)
            lineTo(4.6f, 11f)
            lineTo(3.2f, 12f)
            lineTo(4.6f, 13f)
            lineTo(5.1f, 14.7f)
            lineTo(3.8f, 16f)
            lineTo(6f, 18.2f)
            lineTo(7.3f, 16.9f)
            lineTo(9f, 17.4f)
            lineTo(10f, 18.8f)
            lineTo(14f, 18.8f)
            lineTo(15f, 17.4f)
            lineTo(16.7f, 16.9f)
            lineTo(18f, 18.2f)
            lineTo(20.2f, 16f)
            lineTo(18.9f, 14.7f)
            close()
        }.build()
    }

    val Magnet: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconMagnet",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(5f, 4f)
            lineTo(5f, 13f)
            curveTo(5f, 16.87f, 8.13f, 20f, 12f, 20f)
            curveTo(15.87f, 20f, 19f, 16.87f, 19f, 13f)
            lineTo(19f, 4f)
            lineTo(15f, 4f)
            lineTo(15f, 13f)
            curveTo(15f, 14.66f, 13.66f, 16f, 12f, 16f)
            curveTo(10.34f, 16f, 9f, 14.66f, 9f, 13f)
            lineTo(9f, 4f)
            close()
        }.build()
    }

    val KacaPembesar: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconCari",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(11f, 4f)
            curveTo(14.87f, 4f, 18f, 7.13f, 18f, 11f)
            curveTo(18f, 14.87f, 14.87f, 18f, 11f, 18f)
            curveTo(7.13f, 18f, 4f, 14.87f, 4f, 11f)
            curveTo(4f, 7.13f, 7.13f, 4f, 11f, 4f)
            moveTo(16f, 16f)
            lineTo(21f, 21f)
        }.build()
    }

    val Tambah: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconTambah",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(12f, 5f)
            lineTo(12f, 19f)
            moveTo(5f, 12f)
            lineTo(19f, 12f)
        }.build()
    }

    val Kembali: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconKembali",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(19f, 12f)
            lineTo(5f, 12f)
            moveTo(12f, 19f)
            lineTo(5f, 12f)
            lineTo(12f, 5f)
        }.build()
    }

    val Bagikan: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconBagikan",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(18f, 5f)
            curveTo(19.1f, 5f, 20f, 4.1f, 20f, 3f)
            curveTo(20f, 1.9f, 19.1f, 1f, 18f, 1f)
            moveTo(6f, 14f)
            lineTo(18f, 8f)
            moveTo(6f, 10f)
            lineTo(18f, 16f)
            moveTo(18f, 17f)
            curveTo(19.1f, 17f, 20f, 17.9f, 20f, 19f)
            curveTo(20f, 20.1f, 19.1f, 21f, 18f, 21f)
            moveTo(6f, 9f)
            curveTo(4.9f, 9f, 4f, 9.9f, 4f, 11f)
            curveTo(4f, 12.1f, 4.9f, 13f, 6f, 13f)
        }.build()
    }

    val Hapus: ImageVector by lazy {
        ImageVector.Builder(
            name = "IconHapus",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 6f)
            lineTo(21f, 6f)
            moveTo(8f, 6f)
            lineTo(8f, 4f)
            lineTo(16f, 4f)
            lineTo(16f, 6f)
            moveTo(19f, 6f)
            lineTo(18f, 20f)
            lineTo(6f, 20f)
            lineTo(5f, 6f)
            moveTo(10f, 11f)
            lineTo(10f, 16f)
            moveTo(14f, 11f)
            lineTo(14f, 16f)
        }.build()
    }
}
