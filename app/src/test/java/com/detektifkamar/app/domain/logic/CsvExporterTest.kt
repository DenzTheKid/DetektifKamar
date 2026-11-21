package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.MagneticStatus
import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import com.detektifkamar.app.domain.model.RoomType
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {

    @Test
    fun testExportCsvContainsHeadersAndData() {
        val room = Room(
            id = 1L,
            name = "Kamar Utama",
            roomType = RoomType.KAMAR_TIDUR
        )
        val measurement = Measurement(
            id = 101L,
            roomId = 1L,
            timestamp = 1700000000000L,
            x = 12.5f,
            y = -18.2f,
            z = 40.1f,
            magnitude = 45.74f,
            sensorAccuracy = 3,
            status = MagneticStatus.NORMAL,
            notes = "Dekat Bantal"
        )

        val csv = CsvExporter.exportToCsv(
            measurements = listOf(measurement),
            roomMap = mapOf(1L to room)
        )

        assertTrue(csv.contains("ID,Nama_Ruangan,Tipe_Ruangan"))
        assertTrue(csv.contains("Kamar Utama"))
        assertTrue(csv.contains("45.74"))
        assertTrue(csv.contains("Dekat Bantal"))
        assertTrue(csv.contains("NORMAL"))
    }
}
