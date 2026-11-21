package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.DisturbanceCategory
import com.detektifkamar.app.domain.model.EvidencePoint
import com.detektifkamar.app.domain.model.EvidencePointWithReading
import com.detektifkamar.app.domain.model.InvestigationCase
import com.detektifkamar.app.domain.model.PointReading
import org.junit.Assert.assertTrue
import org.junit.Test

class InvestigationReportFormatterTest {

    @Test
    fun testReportFormatting() {
        val testCase = InvestigationCase(
            id = 101L,
            title = "Kamar Utama",
            roomType = "Kamar tidur",
            widthM = 3.0f,
            lengthM = 4.0f,
            baselineUt = 45.0f
        )

        val point = EvidencePoint(
            id = 1L,
            caseId = 101L,
            label = "Dekat Colokan",
            xM = 1.0f,
            yM = 1.5f
        )
        val reading = PointReading(
            id = 1L,
            pointId = 1L,
            meanUt = 62.0f,
            minUt = 60.0f,
            maxUt = 64.0f,
            stdUt = 0.5f,
            bx = 10f, by = 20f, bz = 57f
        )

        val pointWithReading = EvidencePointWithReading(
            point = point,
            latestReading = reading,
            deltaUt = 17.0f,
            category = DisturbanceCategory.GANGGUAN_KUAT
        )

        val report = InvestigationReportFormatter.formatReport(testCase, listOf(pointWithReading))

        assertTrue(report.contains("LAPORAN PEMETAAN MEDAN MAGNET RUANGAN"))
        assertTrue(report.contains("Kamar Utama"))
        assertTrue(report.contains("45.0 µT") || report.contains("45,0 µT"))
        assertTrue(report.contains("Dekat Colokan"))
        assertTrue(report.contains("Medan Tinggi"))
    }
}
