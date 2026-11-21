package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.EvidencePointWithReading
import com.detektifkamar.app.domain.model.InvestigationCase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pemformat laporan hasil survei dan pemetaan medan magnet ruangan.
 */
object InvestigationReportFormatter {

    fun formatReport(
        case: InvestigationCase,
        points: List<EvidencePointWithReading>
    ): String {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID"))
        val dateStr = dateFormat.format(Date(case.updatedAt))

        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("        LAPORAN PEMETAAN MEDAN MAGNET RUANGAN     ")
        sb.appendLine("==================================================")
        sb.appendLine("Ruangan        : ${case.title}")
        sb.appendLine("Tipe Ruangan   : ${case.roomType}")
        sb.appendLine("Dimensi        : %.1f x %.1f meter".format(Locale("id", "ID"), case.widthM, case.lengthM))
        val baselineText = case.baselineUt?.let { "%.1f µT".format(Locale("id", "ID"), it) } ?: "Belum Ditentukan"
        sb.appendLine("Nilai Acuan    : $baselineText")
        sb.appendLine("Waktu Survei   : $dateStr")
        if (!case.note.isNullOrBlank()) {
            sb.appendLine("Catatan        : ${case.note}")
        }
        sb.appendLine("--------------------------------------------------")
        sb.appendLine("RINGKASAN HASIL:")
        sb.appendLine("Total Titik Ukur  : ${points.size}")

        val deltas = points.mapNotNull { it.deltaUt }
        val maxDelta = if (deltas.isNotEmpty()) deltas.maxOrNull() ?: 0f else 0f
        val avgMean = if (points.isNotEmpty()) {
            val validMeans = points.mapNotNull { it.latestReading?.meanUt }
            if (validMeans.isNotEmpty()) validMeans.average().toFloat() else 0f
        } else 0f

        val hottestPoint = points.maxByOrNull { it.deltaUt ?: 0f }

        sb.appendLine("Simpangan Max     : %.1f µT".format(Locale("id", "ID"), maxDelta))
        sb.appendLine("Rata-rata Kuat    : %.1f µT".format(Locale("id", "ID"), avgMean))
        if (hottestPoint != null && (hottestPoint.deltaUt ?: 0f) > 0f) {
            sb.appendLine("Titik Tertinggi   : ${hottestPoint.point.label} (Δ %.1f µT - %s)".format(
                Locale("id", "ID"),
                hottestPoint.deltaUt,
                hottestPoint.category.label
            ))
        }

        sb.appendLine("--------------------------------------------------")
        sb.appendLine("DATA PENGUKURAN PER TITIK:")
        if (points.isEmpty()) {
            sb.appendLine("(Belum ada data titik ukur yang tercatat)")
        } else {
            points.forEachIndexed { index, p ->
                val reading = p.latestReading
                val deltaStr = p.deltaUt?.let { "%.1f µT".format(Locale("id", "ID"), it) } ?: "-"
                val meanStr = reading?.let { "%.1f µT".format(Locale("id", "ID"), it.meanUt) } ?: "Belum diukur"
                val luxStr = reading?.luxMean?.let { "%.0f lux".format(Locale("id", "ID"), it) } ?: "-"
                
                sb.appendLine("${index + 1}. [${p.point.label}] Koordinat (%.1f, %.1f m)".format(Locale("id", "ID"), p.point.xM, p.point.yM))
                sb.appendLine("   Kekuatan Medan : $meanStr | Simpangan: $deltaStr (${p.category.label})")
                sb.appendLine("   Intensitas Lux : $luxStr | Kualitas: ${reading?.quality ?: "-"}")
                if (p.linkedSuspects.isNotEmpty()) {
                    sb.appendLine("   Perangkat Terkait: " + p.linkedSuspects.joinToString(", ") { it.name })
                }
                if (!p.customSuspectLabel.isNullOrBlank()) {
                    sb.appendLine("   Catatan Sumber: ${p.customSuspectLabel}")
                }
            }
        }

        sb.appendLine("==================================================")
        sb.appendLine("Pindai Magnet - Analisis & Pemetaan EMF Ruangan")
        return sb.toString()
    }
}
