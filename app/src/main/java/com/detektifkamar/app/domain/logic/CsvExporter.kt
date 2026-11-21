package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.Measurement
import com.detektifkamar.app.domain.model.Room
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas ekspor data pengukuran ke format CSV standar (RFC 4180).
 */
object CsvExporter {

    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Menghasilkan teks CSV dari daftar pengukuran beserta data ruangan terkait.
     */
    fun exportToCsv(
        measurements: List<Measurement>,
        roomMap: Map<Long, Room>
    ): String {
        val sb = StringBuilder()
        // Header CSV
        sb.append("ID,Nama_Ruangan,Tipe_Ruangan,Waktu_UTC,Magnitude_uT,Sumbu_X_uT,Sumbu_Y_uT,Sumbu_Z_uT,Status,Akurasi,Bias_X,Bias_Y,Bias_Z,Kemiringan_Derajat,Ponsel_Miring,Cahaya_Lux,Catatan\r\n")

        for (m in measurements) {
            val room = roomMap[m.roomId]
            val roomName = escapeCsv(room?.name ?: "Ruangan #${m.roomId}")
            val roomType = escapeCsv(room?.roomType?.displayName ?: "Lainnya")
            val timeUtc = isoDateFormat.format(Date(m.timestamp))
            val notes = escapeCsv(m.notes)
            val luxStr = m.lux?.let { String.format(Locale.US, "%.1f", it) } ?: ""

            sb.append(m.id).append(",")
                .append(roomName).append(",")
                .append(roomType).append(",")
                .append(timeUtc).append(",")
                .append(String.format(Locale.US, "%.2f", m.magnitude)).append(",")
                .append(String.format(Locale.US, "%.2f", m.x)).append(",")
                .append(String.format(Locale.US, "%.2f", m.y)).append(",")
                .append(String.format(Locale.US, "%.2f", m.z)).append(",")
                .append(m.status.name).append(",")
                .append(m.sensorAccuracy).append(",")
                .append(String.format(Locale.US, "%.2f", m.biasX)).append(",")
                .append(String.format(Locale.US, "%.2f", m.biasY)).append(",")
                .append(String.format(Locale.US, "%.2f", m.biasZ)).append(",")
                .append(String.format(Locale.US, "%.1f", m.tiltAngle)).append(",")
                .append(if (m.isTilted) "YA" else "TIDAK").append(",")
                .append(luxStr).append(",")
                .append(notes)
                .append("\r\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(text: String): String {
        if (!text.contains(",") && !text.contains("\"") && !text.contains("\n") && !text.contains("\r")) {
            return text
        }
        val escaped = text.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
