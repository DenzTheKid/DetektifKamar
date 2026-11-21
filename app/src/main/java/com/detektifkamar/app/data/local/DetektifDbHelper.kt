package com.detektifkamar.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.detektifkamar.app.data.local.DatabaseContract.CaseTable
import com.detektifkamar.app.data.local.DatabaseContract.DATABASE_NAME
import com.detektifkamar.app.data.local.DatabaseContract.DATABASE_VERSION
import com.detektifkamar.app.data.local.DatabaseContract.EvidencePointTable
import com.detektifkamar.app.data.local.DatabaseContract.FurnitureTable
import com.detektifkamar.app.data.local.DatabaseContract.PointSuspectTable
import com.detektifkamar.app.data.local.DatabaseContract.ReadingTable
import com.detektifkamar.app.data.local.DatabaseContract.SuspectCatalogTable
import com.detektifkamar.app.domain.model.SuspectCategory

class DetektifDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Wajib mengaktifkan foreign key constraints di SQLite Android
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Tabel Kasus Penyelidikan
        db.execSQL("""
            CREATE TABLE ${CaseTable.TABLE_NAME} (
                ${CaseTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${CaseTable.COLUMN_TITLE} TEXT NOT NULL,
                ${CaseTable.COLUMN_ROOM_TYPE} TEXT NOT NULL,
                ${CaseTable.COLUMN_WIDTH_M} REAL NOT NULL,
                ${CaseTable.COLUMN_LENGTH_M} REAL NOT NULL,
                ${CaseTable.COLUMN_NOTE} TEXT,
                ${CaseTable.COLUMN_BASELINE_UT} REAL,
                ${CaseTable.COLUMN_CREATED_AT} INTEGER NOT NULL,
                ${CaseTable.COLUMN_UPDATED_AT} INTEGER NOT NULL
            );
        """.trimIndent())

        // 2. Tabel Perabot / Furnitur TKP
        db.execSQL("""
            CREATE TABLE ${FurnitureTable.TABLE_NAME} (
                ${FurnitureTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${FurnitureTable.COLUMN_CASE_ID} INTEGER NOT NULL,
                ${FurnitureTable.COLUMN_LABEL} TEXT NOT NULL,
                ${FurnitureTable.COLUMN_KIND} TEXT NOT NULL,
                ${FurnitureTable.COLUMN_X_M} REAL NOT NULL,
                ${FurnitureTable.COLUMN_Y_M} REAL NOT NULL,
                ${FurnitureTable.COLUMN_W_M} REAL NOT NULL,
                ${FurnitureTable.COLUMN_H_M} REAL NOT NULL,
                FOREIGN KEY (${FurnitureTable.COLUMN_CASE_ID})
                    REFERENCES ${CaseTable.TABLE_NAME}(${CaseTable.COLUMN_ID})
                    ON DELETE CASCADE
            );
        """.trimIndent())

        // 3. Tabel Titik Bukti Penyelidikan
        db.execSQL("""
            CREATE TABLE ${EvidencePointTable.TABLE_NAME} (
                ${EvidencePointTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${EvidencePointTable.COLUMN_CASE_ID} INTEGER NOT NULL,
                ${EvidencePointTable.COLUMN_LABEL} TEXT NOT NULL,
                ${EvidencePointTable.COLUMN_X_M} REAL NOT NULL,
                ${EvidencePointTable.COLUMN_Y_M} REAL NOT NULL,
                ${EvidencePointTable.COLUMN_NOTE} TEXT,
                ${EvidencePointTable.COLUMN_CREATED_AT} INTEGER NOT NULL,
                FOREIGN KEY (${EvidencePointTable.COLUMN_CASE_ID})
                    REFERENCES ${CaseTable.TABLE_NAME}(${CaseTable.COLUMN_ID})
                    ON DELETE CASCADE
            );
        """.trimIndent())

        // 4. Tabel Hasil Pembacaan Sensor Titik
        db.execSQL("""
            CREATE TABLE ${ReadingTable.TABLE_NAME} (
                ${ReadingTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ReadingTable.COLUMN_POINT_ID} INTEGER NOT NULL,
                ${ReadingTable.COLUMN_MEAN_UT} REAL NOT NULL,
                ${ReadingTable.COLUMN_MIN_UT} REAL NOT NULL,
                ${ReadingTable.COLUMN_MAX_UT} REAL NOT NULL,
                ${ReadingTable.COLUMN_STD_UT} REAL NOT NULL,
                ${ReadingTable.COLUMN_BX} REAL NOT NULL,
                ${ReadingTable.COLUMN_BY} REAL NOT NULL,
                ${ReadingTable.COLUMN_BZ} REAL NOT NULL,
                ${ReadingTable.COLUMN_LUX_MEAN} REAL,
                ${ReadingTable.COLUMN_AZIMUTH_DEG} REAL,
                ${ReadingTable.COLUMN_TILT_DEG} REAL NOT NULL,
                ${ReadingTable.COLUMN_ACCURACY} INTEGER NOT NULL,
                ${ReadingTable.COLUMN_QUALITY} TEXT NOT NULL,
                ${ReadingTable.COLUMN_SAMPLE_COUNT} INTEGER NOT NULL,
                ${ReadingTable.COLUMN_MEASURED_AT} INTEGER NOT NULL,
                FOREIGN KEY (${ReadingTable.COLUMN_POINT_ID})
                    REFERENCES ${EvidencePointTable.TABLE_NAME}(${EvidencePointTable.COLUMN_ID})
                    ON DELETE CASCADE
            );
        """.trimIndent())

        // 5. Tabel Katalog Tersangka Gangguan
        db.execSQL("""
            CREATE TABLE ${SuspectCatalogTable.TABLE_NAME} (
                ${SuspectCatalogTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${SuspectCatalogTable.COLUMN_NAME} TEXT NOT NULL,
                ${SuspectCatalogTable.COLUMN_CATEGORY} TEXT NOT NULL,
                ${SuspectCatalogTable.COLUMN_TYPICAL_MIN_UT} REAL NOT NULL,
                ${SuspectCatalogTable.COLUMN_TYPICAL_MAX_UT} REAL NOT NULL,
                ${SuspectCatalogTable.COLUMN_TYPICAL_DISTANCE_CM} INTEGER NOT NULL,
                ${SuspectCatalogTable.COLUMN_NOTE} TEXT NOT NULL,
                ${SuspectCatalogTable.COLUMN_HOW_TO_CONFIRM} TEXT NOT NULL
            );
        """.trimIndent())

        // 6. Tabel Relasi Titik Bukti dengan Tersangka
        db.execSQL("""
            CREATE TABLE ${PointSuspectTable.TABLE_NAME} (
                ${PointSuspectTable.COLUMN_POINT_ID} INTEGER NOT NULL,
                ${PointSuspectTable.COLUMN_SUSPECT_ID} INTEGER NOT NULL,
                ${PointSuspectTable.COLUMN_CUSTOM_LABEL} TEXT,
                PRIMARY KEY (${PointSuspectTable.COLUMN_POINT_ID}, ${PointSuspectTable.COLUMN_SUSPECT_ID}),
                FOREIGN KEY (${PointSuspectTable.COLUMN_POINT_ID})
                    REFERENCES ${EvidencePointTable.TABLE_NAME}(${EvidencePointTable.COLUMN_ID})
                    ON DELETE CASCADE,
                FOREIGN KEY (${PointSuspectTable.COLUMN_SUSPECT_ID})
                    REFERENCES ${SuspectCatalogTable.TABLE_NAME}(${SuspectCatalogTable.COLUMN_ID})
                    ON DELETE CASCADE
            );
        """.trimIndent())

        // Buat Indeks untuk kecepatan query join & filter
        db.execSQL("CREATE INDEX idx_furniture_case_id ON ${FurnitureTable.TABLE_NAME}(${FurnitureTable.COLUMN_CASE_ID});")
        db.execSQL("CREATE INDEX idx_evidence_point_case_id ON ${EvidencePointTable.TABLE_NAME}(${EvidencePointTable.COLUMN_CASE_ID});")
        db.execSQL("CREATE INDEX idx_reading_point_id ON ${ReadingTable.TABLE_NAME}(${ReadingTable.COLUMN_POINT_ID});")
        db.execSQL("CREATE INDEX idx_reading_measured_at ON ${ReadingTable.TABLE_NAME}(${ReadingTable.COLUMN_MEASURED_AT});")
        db.execSQL("CREATE INDEX idx_suspect_catalog_category ON ${SuspectCatalogTable.TABLE_NAME}(${SuspectCatalogTable.COLUMN_CATEGORY});")

        // Isi data awal (seed catalog) minimal 26 entri realistis
        seedSuspectCatalog(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Kerangka upgrade per versi (saat ini DB_VERSION = 1)
        var currentVersion = oldVersion
        while (currentVersion < newVersion) {
            when (currentVersion) {
                1 -> {
                    // Contoh migrasi versi 2 di masa depan tanpa drop data
                    currentVersion = 2
                }
                else -> currentVersion++
            }
        }
    }

    private fun seedSuspectCatalog(db: SQLiteDatabase) {
        val suspects = listOf(
            SuspectSeed("Speaker Woofer", SuspectCategory.AUDIO, 40f, 150f, 10, "Mengandung magnet ferit/neodymium kuat pada cone suara.", "Jauhkan speaker 30 cm, nilai harus turun mendekati acuan."),
            SuspectSeed("Headphone Over-Ear", SuspectCategory.AUDIO, 15f, 60f, 5, "Driver neodymium di kedua earcup menghasilkan medan lokal terkonsentrasi.", "Jauhkan headphone sejauh 15 cm dari titik ukur."),
            SuspectSeed("Speaker Bluetooth Portable", SuspectCategory.AUDIO, 25f, 90f, 8, "Kombinasi magnet speaker ganda dan sirkuit baterai internal.", "Matikan perangkat dan posisikan 25 cm lebih jauh."),
            SuspectSeed("Magnet Pintu Kulkas", SuspectCategory.DAPUR, 50f, 200f, 3, "Strip magnetik fleksibel di sepanjang segel karet pintu kulkas.", "Buka pintu atau geser sensor 15 cm dari karet pintu."),
            SuspectSeed("Kulkas (Motor Kompresor)", SuspectCategory.DAPUR, 20f, 80f, 25, "Motor kompresor pendingin di bagian belakang bawah kulkas.", "Ukur saat motor kompresor mati/tidak berdengung."),
            SuspectSeed("Kompor Induksi", SuspectCategory.DAPUR, 30f, 140f, 20, "Kumparan kawat elektromagnet daya tinggi untuk memanaskan panci.", "Matikan saklar kompor induksi, medan akan langsung drop."),
            SuspectSeed("Rice Cooker / Magic Com", SuspectCategory.DAPUR, 10f, 45f, 10, "Elemen pemanas resistif dan relay elektromagnetik di dasar unit.", "Cabut colokan listrik rice cooker dari stopkontak."),
            SuspectSeed("Microwave", SuspectCategory.DAPUR, 35f, 120f, 30, "Trafo tegangan tinggi dan magnetron pelepas gelombang mikro.", "Pastikan oven microwave dalam kondisi mati standby."),
            SuspectSeed("Charger Wireless (Qi/MagSafe)", SuspectCategory.DAYA_KABEL, 25f, 100f, 5, "Kumparan pengirim daya induksi frekuensi tinggi nirkabel.", "Angkat ponsel dan cabut pad charger dari sumber daya."),
            SuspectSeed("Casing Ponsel MagSafe / Ring Magnet", SuspectCategory.AKSESORI_MAGNETIK, 30f, 160f, 2, "Cincin magnet neodymium tipis di bodi belakang casing ponsel.", "Lepaskan casing magnetik dari ponsel saat mengukur."),
            SuspectSeed("Adaptor Laptop / Power Brick", SuspectCategory.DAYA_KABEL, 15f, 55f, 15, "Trafo switching ferit penurun tegangan AC ke DC.", "Geser adaptor sejauh minimal 40 cm dari meja kerja."),
            SuspectSeed("Power Bank Kapasitas Besar", SuspectCategory.DAYA_KABEL, 8f, 30f, 5, "Sirkuit induktor boost converter dan kemasan baterai lithium.", "Jauhkan power bank sejauh 20 cm dari titik pengukuran."),
            SuspectSeed("Kabel Listrik Beban Besar", SuspectCategory.DAYA_KABEL, 12f, 45f, 15, "Kabel perpanjangan yang menyuplai pemanas air, AC, atau setrika.", "Cabut beban listrik berdaya besar yang tersambung."),
            SuspectSeed("Kotak Sekring / MCB Kamar", SuspectCategory.DAYA_KABEL, 20f, 75f, 25, "Terminal saklar bimetal dan arus listrik utama satu ruangan.", "Mundur 50 cm dari dinding yang memuat kotak MCB."),
            SuspectSeed("Kipas Angin Meja / Lantai", SuspectCategory.PERANGKAT, 20f, 70f, 15, "Stator dan rotor kumparan tembaga motor listrik berputar.", "Matikan kipas angin dan amati perubahan simpangan jarum."),
            SuspectSeed("Blower Unit Indoor AC", SuspectCategory.PERANGKAT, 15f, 50f, 35, "Motor penggerak blower dan relay switch pada unit AC indoor.", "Matikan pendingin ruangan sementara waktu."),
            SuspectSeed("Televisi LED / Smart TV", SuspectCategory.PERANGKAT, 10f, 40f, 25, "Power supply internal dan magnet speaker bawaan di bawah layar.", "Posisikan titik ukur mundur minimal 50 cm dari panel TV."),
            SuspectSeed("Monitor Komputer Desktop", SuspectCategory.PERANGKAT, 8f, 35f, 20, "Komponen catu daya internal dan panel display komputer.", "Matikan saklar monitor untuk memverifikasi gangguan."),
            SuspectSeed("Router Wi-Fi & Modem Fiber", SuspectCategory.PERANGKAT, 8f, 28f, 10, "Regulator daya DC dan choke koil induksi frekuensi radio.", "Jauhkan router sejauh 30 cm dari tempat duduk/tidur."),
            SuspectSeed("Driver Lampu Meja LED", SuspectCategory.PERANGKAT, 12f, 40f, 12, "Konverter daya switching AC-DC murah tanpa pelindung ferit.", "Matikan lampu meja dan lihat pembacaan pada layar."),
            SuspectSeed("Rangka Besi Ranjang Tidur", SuspectCategory.STRUKTUR_BANGUNAN, 15f, 60f, 10, "Pipa baja yang mengalami magnetisasi remanen statis dari pabrik.", "Geser kasur atau ukur di tengah kasur menjauhi rangka."),
            SuspectSeed("Rak / Lemari Arsip Besi", SuspectCategory.STRUKTUR_BANGUNAN, 20f, 80f, 10, "Lembaran plat baja berukuran besar yang menangkap fluks magnet.", "Ukur dengan jarak minimal 30 cm dari dinding lemari."),
            SuspectSeed("Tulangan Baja Beton Dinding", SuspectCategory.STRUKTUR_BANGUNAN, 12f, 45f, 8, "Besi cor tulangan penguat konstruksi di dalam kolom dinding.", "Geser titik 20 cm ke samping sepanjang permukaan dinding."),
            SuspectSeed("Kusen Pintu / Jendela Baja", SuspectCategory.STRUKTUR_BANGUNAN, 10f, 38f, 5, "Profil baja hollow galvanis penopang daun pintu atau jendela.", "Pindahkan sensor ke arah tengah daun pintu."),
            SuspectSeed("Jam Dinding Mesin Kuarsa", SuspectCategory.PERANGKAT, 10f, 35f, 5, "Motor stepper mini dengan rotor magnet yang berdetik tiap detik.", "Lepaskan baterai AA jam dinding untuk memastikan."),
            SuspectSeed("Mainan Kubus Magnet Neodymium", SuspectCategory.AKSESORI_MAGNETIK, 60f, 350f, 5, "Bola-bola magnet neodymium berkekuatan sangat tinggi.", "Simpan magnet mainan di dalam wadah logam atau laci jauh.")
        )

        db.beginTransaction()
        try {
            for (s in suspects) {
                val cv = ContentValues().apply {
                    put(SuspectCatalogTable.COLUMN_NAME, s.name)
                    put(SuspectCatalogTable.COLUMN_CATEGORY, s.category.name)
                    put(SuspectCatalogTable.COLUMN_TYPICAL_MIN_UT, s.minUt)
                    put(SuspectCatalogTable.COLUMN_TYPICAL_MAX_UT, s.maxUt)
                    put(SuspectCatalogTable.COLUMN_TYPICAL_DISTANCE_CM, s.distCm)
                    put(SuspectCatalogTable.COLUMN_NOTE, s.note)
                    put(SuspectCatalogTable.COLUMN_HOW_TO_CONFIRM, s.confirm)
                }
                db.insert(SuspectCatalogTable.TABLE_NAME, null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private data class SuspectSeed(
        val name: String,
        val category: SuspectCategory,
        val minUt: Float,
        val maxUt: Float,
        val distCm: Int,
        val note: String,
        val confirm: String
    )
}
