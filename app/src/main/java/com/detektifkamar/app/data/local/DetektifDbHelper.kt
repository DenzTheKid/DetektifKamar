package com.detektifkamar.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.detektifkamar.app.data.local.DatabaseContract.CalibrationTable
import com.detektifkamar.app.data.local.DatabaseContract.DATABASE_NAME
import com.detektifkamar.app.data.local.DatabaseContract.DATABASE_VERSION
import com.detektifkamar.app.data.local.DatabaseContract.DeviceCatalogTable
import com.detektifkamar.app.data.local.DatabaseContract.MeasurementTable
import com.detektifkamar.app.data.local.DatabaseContract.RoomTable
import com.detektifkamar.app.domain.model.DeviceCategory

/**
 * SQLiteOpenHelper murni untuk mengelola pembuatan dan migrasi skema database Detektif Kamar.
 */
class DetektifDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Wajib mengaktifkan foreign key constraints di SQLite Android
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        createIndices(db)
        seedDeviceCatalog(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrasi terstruktur dari versi 1 ke versi 2
        if (oldVersion < 2) {
            // Jika ada tabel lama dari versi 1, kita buat tabel baru
            db.execSQL("CREATE TABLE IF NOT EXISTS ${RoomTable.TABLE_NAME} (" +
                    "${RoomTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${RoomTable.COLUMN_NAME} TEXT NOT NULL, " +
                    "${RoomTable.COLUMN_ROOM_TYPE} TEXT NOT NULL, " +
                    "${RoomTable.COLUMN_WIDTH_METERS} REAL, " +
                    "${RoomTable.COLUMN_LENGTH_METERS} REAL, " +
                    "${RoomTable.COLUMN_NOTES} TEXT, " +
                    "${RoomTable.COLUMN_CREATED_AT} INTEGER NOT NULL);")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${MeasurementTable.TABLE_NAME} (" +
                    "${MeasurementTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${MeasurementTable.COLUMN_ROOM_ID} INTEGER NOT NULL, " +
                    "${MeasurementTable.COLUMN_TIMESTAMP} INTEGER NOT NULL, " +
                    "${MeasurementTable.COLUMN_X} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_Y} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_Z} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_MAGNITUDE} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_SENSOR_ACCURACY} INTEGER NOT NULL, " +
                    "${MeasurementTable.COLUMN_BIAS_X} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_BIAS_Y} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_BIAS_Z} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_LUX} REAL, " +
                    "${MeasurementTable.COLUMN_TILT_ANGLE} REAL NOT NULL, " +
                    "${MeasurementTable.COLUMN_IS_TILTED} INTEGER NOT NULL, " +
                    "${MeasurementTable.COLUMN_STATUS} TEXT NOT NULL, " +
                    "${MeasurementTable.COLUMN_NOTES} TEXT, " +
                    "${MeasurementTable.COLUMN_POINT_X} REAL, " +
                    "${MeasurementTable.COLUMN_POINT_Y} REAL, " +
                    "FOREIGN KEY (${MeasurementTable.COLUMN_ROOM_ID}) REFERENCES ${RoomTable.TABLE_NAME}(${RoomTable.COLUMN_ID}) ON DELETE CASCADE);")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${CalibrationTable.TABLE_NAME} (" +
                    "${CalibrationTable.COLUMN_ID} INTEGER PRIMARY KEY, " +
                    "${CalibrationTable.COLUMN_BIAS_X} REAL NOT NULL, " +
                    "${CalibrationTable.COLUMN_BIAS_Y} REAL NOT NULL, " +
                    "${CalibrationTable.COLUMN_BIAS_Z} REAL NOT NULL, " +
                    "${CalibrationTable.COLUMN_QUALITY} TEXT NOT NULL, " +
                    "${CalibrationTable.COLUMN_SAMPLE_COUNT} INTEGER NOT NULL, " +
                    "${CalibrationTable.COLUMN_CALIBRATED_AT} INTEGER NOT NULL);")

            db.execSQL("CREATE TABLE IF NOT EXISTS ${DeviceCatalogTable.TABLE_NAME} (" +
                    "${DeviceCatalogTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "${DeviceCatalogTable.COLUMN_NAME} TEXT NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_CATEGORY} TEXT NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_MIN_UT} REAL NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_MAX_UT} REAL NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_MEASUREMENT_DISTANCE} TEXT NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_PHYSICAL_EXPLANATION} TEXT NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_SAFETY_NOTE} TEXT NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_VERIFICATION_SOURCE} TEXT NOT NULL, " +
                    "${DeviceCatalogTable.COLUMN_IS_SATURATING} INTEGER NOT NULL);")

            createIndices(db)
            seedDeviceCatalog(db)
        }
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE ${RoomTable.TABLE_NAME} (
                ${RoomTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${RoomTable.COLUMN_NAME} TEXT NOT NULL,
                ${RoomTable.COLUMN_ROOM_TYPE} TEXT NOT NULL,
                ${RoomTable.COLUMN_WIDTH_METERS} REAL,
                ${RoomTable.COLUMN_LENGTH_METERS} REAL,
                ${RoomTable.COLUMN_NOTES} TEXT,
                ${RoomTable.COLUMN_CREATED_AT} INTEGER NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE ${MeasurementTable.TABLE_NAME} (
                ${MeasurementTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${MeasurementTable.COLUMN_ROOM_ID} INTEGER NOT NULL,
                ${MeasurementTable.COLUMN_TIMESTAMP} INTEGER NOT NULL,
                ${MeasurementTable.COLUMN_X} REAL NOT NULL,
                ${MeasurementTable.COLUMN_Y} REAL NOT NULL,
                ${MeasurementTable.COLUMN_Z} REAL NOT NULL,
                ${MeasurementTable.COLUMN_MAGNITUDE} REAL NOT NULL,
                ${MeasurementTable.COLUMN_SENSOR_ACCURACY} INTEGER NOT NULL,
                ${MeasurementTable.COLUMN_BIAS_X} REAL NOT NULL,
                ${MeasurementTable.COLUMN_BIAS_Y} REAL NOT NULL,
                ${MeasurementTable.COLUMN_BIAS_Z} REAL NOT NULL,
                ${MeasurementTable.COLUMN_LUX} REAL,
                ${MeasurementTable.COLUMN_TILT_ANGLE} REAL NOT NULL,
                ${MeasurementTable.COLUMN_IS_TILTED} INTEGER NOT NULL,
                ${MeasurementTable.COLUMN_STATUS} TEXT NOT NULL,
                ${MeasurementTable.COLUMN_NOTES} TEXT,
                ${MeasurementTable.COLUMN_POINT_X} REAL,
                ${MeasurementTable.COLUMN_POINT_Y} REAL,
                FOREIGN KEY (${MeasurementTable.COLUMN_ROOM_ID})
                    REFERENCES ${RoomTable.TABLE_NAME}(${RoomTable.COLUMN_ID})
                    ON DELETE CASCADE
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE ${CalibrationTable.TABLE_NAME} (
                ${CalibrationTable.COLUMN_ID} INTEGER PRIMARY KEY,
                ${CalibrationTable.COLUMN_BIAS_X} REAL NOT NULL,
                ${CalibrationTable.COLUMN_BIAS_Y} REAL NOT NULL,
                ${CalibrationTable.COLUMN_BIAS_Z} REAL NOT NULL,
                ${CalibrationTable.COLUMN_QUALITY} TEXT NOT NULL,
                ${CalibrationTable.COLUMN_SAMPLE_COUNT} INTEGER NOT NULL,
                ${CalibrationTable.COLUMN_CALIBRATED_AT} INTEGER NOT NULL
            );
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE ${DeviceCatalogTable.TABLE_NAME} (
                ${DeviceCatalogTable.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${DeviceCatalogTable.COLUMN_NAME} TEXT NOT NULL,
                ${DeviceCatalogTable.COLUMN_CATEGORY} TEXT NOT NULL,
                ${DeviceCatalogTable.COLUMN_MIN_UT} REAL NOT NULL,
                ${DeviceCatalogTable.COLUMN_MAX_UT} REAL NOT NULL,
                ${DeviceCatalogTable.COLUMN_MEASUREMENT_DISTANCE} TEXT NOT NULL,
                ${DeviceCatalogTable.COLUMN_PHYSICAL_EXPLANATION} TEXT NOT NULL,
                ${DeviceCatalogTable.COLUMN_SAFETY_NOTE} TEXT NOT NULL,
                ${DeviceCatalogTable.COLUMN_VERIFICATION_SOURCE} TEXT NOT NULL,
                ${DeviceCatalogTable.COLUMN_IS_SATURATING} INTEGER NOT NULL
            );
        """.trimIndent())
    }

    private fun createIndices(db: SQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_measurements_room_id ON ${MeasurementTable.TABLE_NAME}(${MeasurementTable.COLUMN_ROOM_ID});")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_measurements_timestamp ON ${MeasurementTable.TABLE_NAME}(${MeasurementTable.COLUMN_TIMESTAMP});")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_rooms_created_at ON ${RoomTable.TABLE_NAME}(${RoomTable.COLUMN_CREATED_AT});")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_device_category ON ${DeviceCatalogTable.TABLE_NAME}(${DeviceCatalogTable.COLUMN_CATEGORY});")
    }

    private fun seedDeviceCatalog(db: SQLiteDatabase) {
        db.execSQL("DELETE FROM ${DeviceCatalogTable.TABLE_NAME};")

        val seeds = listOf(
            // Kategori: ELEKTRONIK
            SeedDevice(
                name = "Adaptor Laptop (65W - 100W)",
                category = DeviceCategory.ELEKTRONIK,
                minUt = 30f,
                maxUt = 160f,
                distance = "Jarak 3 – 5 cm",
                explanation = "Transformator switching frekuensi tinggi (SMPS) menghasilkan fluks magnetik bolak-balik di sekitar kumparan inti ferit saat konversi tegangan AC ke DC.",
                safety = "Jauhkan minimal 30 cm dari bantal atau tempat tidur agar paparan fluks kembali ke tingkat alami bumi.",
                source = "WHO Environmental Health Criteria 238: Extremely Low Frequency Fields",
                isSaturating = false
            ),
            SeedDevice(
                name = "Charger Nirkabel (Wireless Qi 15W)",
                category = DeviceCategory.ELEKTRONIK,
                minUt = 80f,
                maxUt = 320f,
                distance = "Jarak 1 – 3 cm",
                explanation = "Memancarkan medan magnet bolak-balik frekuensi 110–205 kHz melalui kumparan primer untuk mentransfer daya induksi elektromagnetik ke ponsel.",
                safety = "Medan turun drastis sebanding 1/r³; pada jarak 20 cm emisi sudah mendekati fluks alami bumi.",
                source = "ICNIRP Guidelines for Limiting Exposure to Electromagnetic Fields (100 kHz - 300 GHz)",
                isSaturating = false
            ),
            SeedDevice(
                name = "Monitor Komputer & Layar TV",
                category = DeviceCategory.ELEKTRONIK,
                minUt = 10f,
                maxUt = 45f,
                distance = "Jarak 10 – 20 cm",
                explanation = "Berasal dari sirkuit backlight LED driver dan catu daya internal pada panel belakang perangkat.",
                safety = "Sangat aman pada jarak pandang normal (> 40 cm).",
                source = "IEEE Std C95.6: Safety Levels with Respect to Human Exposure to Electromagnetic Fields",
                isSaturating = false
            ),
            SeedDevice(
                name = "Unit Catu Daya PC (Power Supply / PSU)",
                category = DeviceCategory.ELEKTRONIK,
                minUt = 40f,
                maxUt = 180f,
                distance = "Jarak 5 – 10 cm",
                explanation = "Arus bolak-balik besar pada trafo step-down utama dan choke induktif koreksi faktor daya (PFC).",
                safety = "Casing logam PC berbahan baja memberikan sedikit pelindung feromagnetik.",
                source = "WHO EMF Worldwide Database",
                isSaturating = false
            ),

            // Kategori: AUDIO
            SeedDevice(
                name = "Subwoofer Audio Aktif",
                category = DeviceCategory.AUDIO,
                minUt = 250f,
                maxUt = 950f,
                distance = "Jarak 5 cm dari magnet driver",
                explanation = "Menggunakan magnet permanen keramik/stronsium ferit besar pada voice coil untuk menggerakkan konus bass secara mekanis.",
                safety = "Jangan meletakkan kartu magnetik atau hard disk mekanik langsung menempel pada bodi subwoofer.",
                source = "Audio Engineering Society: Magnetic Field Radiation in Loudspeaker Design",
                isSaturating = false
            ),
            SeedDevice(
                name = "Speaker Desktop / Soundbar",
                category = DeviceCategory.AUDIO,
                minUt = 60f,
                maxUt = 280f,
                distance = "Jarak 5 – 10 cm",
                explanation = "Kombinasi fluks magnet permanen pada driver kerucut dan trafo catu daya internal.",
                safety = "Pada jarak dengar kerja normal (50 cm), medan magnet telah meluruh di bawah 5 µT.",
                source = "ICNIRP EMF Guidelines",
                isSaturating = false
            ),
            SeedDevice(
                name = "Headphone Over-Ear (Driver Planar/Dinamik)",
                category = DeviceCategory.AUDIO,
                minUt = 25f,
                maxUt = 120f,
                distance = "Jarak 1 – 2 cm dari bantalan telinga",
                explanation = "Susunan magnet neodymium berdaya tarik tinggi mengapit membran suara tipis.",
                safety = "Meskipun dekat kepala, paparan magnet adalah medan statis dan bolak-balik yang masih dalam batas aman paparan konsumen.",
                source = "ICNIRP Statement on Medical Magnetic Exposure Limits",
                isSaturating = false
            ),

            // Kategori: DAPUR
            SeedDevice(
                name = "Kompor Induksi",
                category = DeviceCategory.DAPUR,
                minUt = 200f,
                maxUt = 800f,
                distance = "Jarak 5 – 10 cm di atas permukaan panci",
                explanation = "Kumparan tembaga osilasi frekuensi 20–40 kHz menghasilkan fluks magnet kuat untuk memicu arus eddy di dasar wajan feromagnetik.",
                safety = "Penderita alat pacu jantung (pacemaker) disarankan menjaga jarak minimal 30 cm saat kompor beroperasi penuh.",
                source = "BfS (Federal Office for Radiation Protection Germany): Induction Hobs EMF Study",
                isSaturating = false
            ),
            SeedDevice(
                name = "Oven Microwave (Operasi Aktif)",
                category = DeviceCategory.DAPUR,
                minUt = 80f,
                maxUt = 400f,
                distance = "Jarak 10 – 30 cm dari pintu",
                explanation = "Transformator tegangan tinggi berdaya 1.000W dan magnetron yang memancarkan gelombang mikro.",
                safety = "Jaga jarak 50 cm saat microwave sedang memanaskan makanan.",
                source = "US EPA: EMF Radiation from Home Appliances",
                isSaturating = false
            ),
            SeedDevice(
                name = "Kompresor Kulkas (Motor Belakang)",
                category = DeviceCategory.DAPUR,
                minUt = 45f,
                maxUt = 220f,
                distance = "Jarak 10 – 20 cm dari bawah belakang",
                explanation = "Motor listrik induksi satu fasa berputar memompa freon dengan lilitan kawat berarus bolak-balik.",
                safety = "Di dalam ruang pendingin atau bagian depan kulkas, medan magnet sudah turun drastis.",
                source = "WHO Environmental Health Criteria 238",
                isSaturating = false
            ),
            SeedDevice(
                name = "Blender / Mixer Dapur",
                category = DeviceCategory.DAPUR,
                minUt = 90f,
                maxUt = 380f,
                distance = "Jarak 5 – 10 cm dari motor",
                explanation = "Motor universal berkecepatan tinggi menghasilkan medan magnet bolak-balik dan percikan komutator.",
                safety = "Alat hanya digunakan dalam durasi singkat sehingga dosis paparan kumulatif rendah.",
                source = "USGS Geomagnetism & Consumer EMF Surveys",
                isSaturating = false
            ),

            // Kategori: KABEL DAN LISTRIK
            SeedDevice(
                name = "Panel Sekring / MCB Rumah",
                category = DeviceCategory.KABEL_DAN_LISTRIK,
                minUt = 50f,
                maxUt = 350f,
                distance = "Jarak 10 – 30 cm dari boks panel",
                explanation = "Titik konvergensi arus utama rumah tangga; arus bolak-balik total 50 Hz mengalir melalui pemutus sirkuit.",
                safety = "Hindari menempatkan kepala ranjang tepat di balik dinding yang dipasangi boks MCB.",
                source = "ICNIRP Guidelines for ELF EMF Exposure",
                isSaturating = false
            ),
            SeedDevice(
                name = "Kabel Rol / Sambungan Kusut Berarus Besar",
                category = DeviceCategory.KABEL_DAN_LISTRIK,
                minUt = 25f,
                maxUt = 140f,
                distance = "Jarak 5 – 10 cm saat terbeban pemanas/setrika",
                explanation = "Ketika kabel digulung, lilitan kabel membentuk selenoida mini yang memperkuat induktansi medan magnet lokal.",
                safety = "Selalu bentangkan kabel rol saat digunakan untuk beban daya tinggi agar tidak panas dan mengurangi konsentrasi fluks.",
                source = "IEEE Transactions on Power Delivery: Magnetic Fields Around Domestic Wiring",
                isSaturating = false
            ),
            SeedDevice(
                name = "Stop Kontak Dinding Tunggal",
                category = DeviceCategory.KABEL_DAN_LISTRIK,
                minUt = 5f,
                maxUt = 35f,
                distance = "Jarak 5 cm saat beban tersambung",
                explanation = "Kabel fase dan netral yang berjalan berdekatan sebagian besar saling menghilangkan medan magnet satu sama lain.",
                safety = "Emisi sangat rendah dan kembali ke level medan bumi pada jarak 15 cm.",
                source = "WHO ELF Field Measurements in Residences",
                isSaturating = false
            ),

            // Kategori: STRUKTUR BANGUNAN
            SeedDevice(
                name = "Kolom Beton Bertulang (Besi Tulangan)",
                category = DeviceCategory.STRUKTUR_BANGUNAN,
                minUt = 15f,
                maxUt = 75f,
                distance = "Menempel pada dinding kolom",
                explanation = "Besi beton (rebar) memiliki permeabilitas magnetik tinggi yang membelokkan atau menahan remanensi geomagnetik bumi lokal.",
                safety = "Medan statis murni yang tidak berbahaya bagi kesehatan biologis.",
                source = "USGS National Geomagnetic Survey: Structural Iron Anomalies",
                isSaturating = false
            ),
            SeedDevice(
                name = "Kusen Jendela & Pintu Baja/Besi",
                category = DeviceCategory.STRUKTUR_BANGUNAN,
                minUt = 10f,
                maxUt = 60f,
                distance = "Jarak 1 – 3 cm dari rangka",
                explanation = "Baja struktural termagnetisasi secara alami oleh medan geomagnetik bumi selama proses fabrikasi dan pendinginan.",
                safety = "Biasa menyebabkan sedikit deviasi pada kompas ponsel saat berjalan melewati pintu.",
                source = "USGS Geomagnetism Program",
                isSaturating = false
            ),

            // Kategori: LAINNYA
            SeedDevice(
                name = "Magnet Neodymium / Casing Magnetik Tebal",
                category = DeviceCategory.LAINNYA,
                minUt = 1200f,
                maxUt = 5000f,
                distance = "Menempel langsung",
                explanation = "Bahan paduan Neodymium-Besi-Boron (NdFeB) menghasilkan rapat fluks remanensi sangat tinggi (B_r hingga 1,4 Tesla).",
                safety = "Dapat membuat sensor magnetometer ponsel jenuh (saturasi) dan mengacaukan kalibrasi sensor kompas secara sementara.",
                source = "ICNIRP Guidelines on Limits of Exposure to Static Magnetic Fields",
                isSaturating = true
            ),
            SeedDevice(
                name = "Magnet Kulkas Dekoratif",
                category = DeviceCategory.LAINNYA,
                minUt = 400f,
                maxUt = 1500f,
                distance = "Jarak 0 – 1 cm",
                explanation = "Ferit barium/stronsium fleksibel dengan strip multipol bolak-balik di permukaan belakangnya.",
                safety = "Medan meluruh sangat tajam dalam hitungan beberapa milimeter.",
                source = "American Journal of Physics: Magnetic Fields of Refrigerator Magnets",
                isSaturating = false
            ),
            SeedDevice(
                name = "Motor Kipas Angin Dinding/Langit-langit",
                category = DeviceCategory.LAINNYA,
                minUt = 40f,
                maxUt = 170f,
                distance = "Jarak 15 – 30 cm dari rumah motor",
                explanation = "Motor kapasitor AC menghasilkan fluks putar stator untuk memutar baling-baling.",
                safety = "Karena terpasang tinggi di plafon atau dinding, jarak aman (> 1 meter) selalu terpenuhi.",
                source = "WHO Environmental Health Criteria 238",
                isSaturating = false
            )
        )

        for (s in seeds) {
            val cv = ContentValues().apply {
                put(DeviceCatalogTable.COLUMN_NAME, s.name)
                put(DeviceCatalogTable.COLUMN_CATEGORY, s.category.name)
                put(DeviceCatalogTable.COLUMN_MIN_UT, s.minUt)
                put(DeviceCatalogTable.COLUMN_MAX_UT, s.maxUt)
                put(DeviceCatalogTable.COLUMN_MEASUREMENT_DISTANCE, s.distance)
                put(DeviceCatalogTable.COLUMN_PHYSICAL_EXPLANATION, s.explanation)
                put(DeviceCatalogTable.COLUMN_SAFETY_NOTE, s.safety)
                put(DeviceCatalogTable.COLUMN_VERIFICATION_SOURCE, s.source)
                put(DeviceCatalogTable.COLUMN_IS_SATURATING, if (s.isSaturating) 1 else 0)
            }
            db.insert(DeviceCatalogTable.TABLE_NAME, null, cv)
        }
    }

    private data class SeedDevice(
        val name: String,
        val category: DeviceCategory,
        val minUt: Float,
        val maxUt: Float,
        val distance: String,
        val explanation: String,
        val safety: String,
        val source: String,
        val isSaturating: Boolean
    )
}
