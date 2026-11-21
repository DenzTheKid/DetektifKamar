package com.detektifkamar.app.domain.model

enum class ReadingQuality(val value: String, val label: String) {
    BAIK("baik", "Kualitas Baik"),
    BERGERAK("bergerak", "Terdeteksi Gerakan"),
    AKURASI_SENSOR_RENDAH("akurasi sensor rendah", "Akurasi Sensor Rendah"),
    DERAU_TINGGI("derau tinggi", "Derau Fluktuasi Tinggi");

    companion object {
        fun fromValue(value: String): ReadingQuality {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: BAIK
        }
    }
}
