package com.detektifkamar.app.domain.model

enum class SuspectCategory(val label: String) {
    AUDIO("Audio"),
    DAPUR("Dapur"),
    DAYA_KABEL("Daya & kabel"),
    PERANGKAT("Perangkat"),
    STRUKTUR_BANGUNAN("Struktur bangunan"),
    AKSESORI_MAGNETIK("Aksesori magnetik");

    companion object {
        fun fromString(str: String): SuspectCategory {
            return entries.find { it.name.equals(str, ignoreCase = true) || it.label.equals(str, ignoreCase = true) }
                ?: PERANGKAT
        }
    }
}
