# DETEKTIF KAMAR (EMF Room Surveyor)

> **"Seberapa kuat medan magnet di sini, dan dari mana asalnya?"**

Aplikasi Android Native berbasis **Kotlin + Jetpack Compose** yang dirancang sebagai instrumen lapangan presisi untuk mendeteksi, mengukur, memetakan, dan menganalisis intensitas medan magnet (Electromagnetic Field / EMF) di lingkungan tempat tinggal (kamar tidur, ruang kerja, dapur, dan laboratorium).

Aplikasi ini dikembangkan untuk keperluan akademik dan tugas kuliah dengan standar rekayasa perangkat lunak tinggi: arsitektur MVVM bersih, SQLite native murni dengan migrasi skema terstruktur, pemrosesan sensor fisik secara nyata, serta desain visual khas *"Field Detective Instrument"*.

---

## 1. Deskripsi Singkat & Latar Belakang Tema

Di era modern, ruang hunian kita dipenuhi oleh berbagai perangkat elektromagnetik: transformator catu daya laptop, kompor induksi, motor kompresor pendingin, pengisi daya nirkabel (*wireless charger*), dan kabel daya berarus bolak-balik. Sebagian perangkat memancarkan fluks medan magnet lokal yang cukup kuat di sekitarnya.

Aplikasi **DETEKTIF KAMAR** mengubah smartphone menjadi alat ukur ilmiah (*Gaussmeter/EMF Surveyor*) mandiri tanpa memerlukan alat laboratorium khusus bernilai jutaan rupiah. Pengguna dapat:
- Mengetahui kekuatan medan magnet ($\mu\text{T}$) di titik-titik krusial kamar (misalnya bantal kasur tidur atau area kerja).
- Memetakan titik ukur pada denah kamar.
- Mengidentifikasi sumber medan anomali melalui katalog referensi perangkat elektronik yang berbasis literatur fisika resmi.

---

## 2. Fitur Utama

1. **Pindai Medan Langsung (Live Scanner)**:
   - Gauge analog lengkung 240° dengan jarum beranimasi pegas halus (*spring damping ratio 0.75*).
   - Penanda visual garis referensi fluks medan geomagnetik alami bumi ($25 - 65\ \mu\text{T}$).
   - Animasi garis pemindai (*scanner radar line*) di latar gauge.
   - Pembacaan digital angka besar dengan font monospace lebar tetap (*fixed width*) agar tidak goyang.
   - Toggle satuan instan: Mikrotesla ($\mu\text{T}$), Militesla ($\text{mT}$), dan Gauss ($\text{G}$).
   - Penguraian vektor 3-sumbu ($B_x, B_y, B_z$) dalam horizontal bar simetris.
   - Grafik jejak osiloskop 10 detik dengan gradasi transparan di bawah kurva ($< 15\%$ opacity).
   - Kartu statistik sesi: Minimum, Rata-rata, dan Maksimum.
   - Tombol **Bekukan (Freeze)** untuk menahan nilai pengukuran di titik tertentu.
   - Deteksi kemiringan ponsel dari akselerometer dengan peringatan *"Pegang ponsel mendatar"*.
   - Badge status dinamis: `NORMAL`, `SEDANG`, `TINGGI`, `SANGAT TINGGI`, `SATURASI`.
   - Tombol cepat simpan ke titik ruangan via Modal Bottom Sheet.
2. **Pemetaan Ruangan (Berkas Kasus Ruangan)**:
   - Pembuatan dan manajemen ruangan dengan tipe (Kamar Tidur, Ruang Kerja, Dapur, Ruang Tamu, Lainnya).
   - Pengaturan dimensi panjang dan lebar dalam meter.
   - Kartu padat berornamen crosshair teknis dengan kode identifikasi kasus (misal: `KASUS #001 · KAMAR TIDUR`).
3. **Detail Ruangan & Peta Titik Denah**:
   - Peta denah kotak 2D dengan titik ukur berkilau interaktif per status warna.
   - Grafik batang komparasi intensitas antar titik ukur dalam satu ruangan.
   - Ringkasan rata-rata, nilai puncak, dan jumlah titik ukur.
4. **Kalibrasi Magnetometer 15 Detik**:
   - Sesi gerakan angka 8 (*3D figure-8 motion*) selama 15 detik dengan progress ring melingkar.
   - Estimasi offset bias hard-iron ($B_x, B_y, B_z$) dan skor kualitas (*Baik*, *Cukup*, *Ulangi*).
   - Penyimpanan bias kalibrasi ke database lokal dan teraplikasi secara transparan ke sensor.
5. **Riwayat Pengukuran & Ekspor CSV**:
   - Filter chip horizontal scrollable per ruangan dan per kategori status.
   - Pengelompokan data per hari ("Hari Ini", "Kemarin", tanggal kalender).
   - Hapus titik dengan konfirmasi dan fitur **Undo (Urungkan)** via Snackbar.
   - **Ekspor Data ke CSV** standar RFC 4180 melalui Storage Access Framework (SAF) tanpa izin penyimpanan umum/berbahaya.
6. **Katalog Referensi Sumber Medan (26 Perangkat Terverifikasi)**:
   - Katalog perangkat rumah tangga dengan rentang $\mu\text{T}$ tipikal, jarak pengukuran, prinsip fisika timbulnya medan magnet, catatan keamanan, dan indikasi saturasi sensor.
   - Pencarian real-time dan filter kategori: Elektronik, Audio, Dapur, Kabel dan Listrik, Struktur Bangunan, Lainnya.
   - Kartu proporsional dan dialog detail komprehensif.
7. **Pengaturan & Keandalan**:
   - Pilihan satuan default ($\mu\text{T}$, $\text{mT}$, $\text{G}$).
   - Pilihan tema (Ikuti Sistem, Terang, Gelap).
   - Mode Simulasi Sensor untuk pengujian di Emulator Android.
   - Fitur Reset Data dengan konfirmasi ganda.

---

## 3. Teknologi yang Digunakan

| Komponen | Spesifikasi & Alasan Pemilihan |
| :--- | :--- |
| **Bahasa Pemrograman** | **Kotlin 2.0.20** (modern, ekspresif, type-safe, null-safety). |
| **UI Toolkit** | **Jetpack Compose (BOM 2024.09.03) + Material 3** sebagai fondasi, dengan custom styling token, Canvas custom, dan tipografi khusus. |
| **Pola Arsitektur** | **MVVM (Model-View-ViewModel) + Clean Architecture**. Pemisahan ketat antara domain (logika murni), data (database & sensor), dan UI (Composable + UiState). |
| **State Management** | **Kotlin Coroutines 1.8.1 & StateFlow**. Single immutable UiState per layar, dikonsumsi via `collectAsStateWithLifecycle()`. |
| **Database** | **SQLite murni (SQLiteOpenHelper)** dengan versi migrasi terstruktur (`onUpgrade`). Alasan: membuktikan penguasaan native SQL, DDL, agregasi JOIN, dan transaksi kaskade tanpa layer abstraksi ORM pihak ketiga. |
| **Reaktivitas DB** | **DatabaseChangeNotifier** berbasis `MutableStateFlow` yang memicu pembaruan Flow secara otomatis saat terjadi operasi tulis (`INSERT`/`UPDATE`/`DELETE`). |
| **Preferensi** | **SharedPreferences** untuk menyimpan preferensi satuan, tema tampilan, haptik, dan flag panduan. |
| **Dependency Injection** | **Manual DI (AppContainer)** tingkat `Application` tanpa framework eksternal (Hilt/Koin), menjaga ukuran biner kecil dan kompilasi cepat. |
| **Build System** | Gradle 8.7, AGP 8.5.2, minSdk 26 (Java 8/Time API native tanpa desugaring), targetSdk 34, compileSdk 34. |
| **Keamanan R8** | Lulus build release dengan `isMinifyEnabled = true` dan ProGuard rules terkonfigurasi. |

---

## 4. Sensor Fisik yang Digunakan

Aplikasi menggunakan kombinasi sensor perangkat keras asli Android:

| Sensor Android | Tipe Sensor | Fungsi Nyata di Aplikasi | Sumber Data | Batasan Fisika Hardware |
| :--- | :--- | :--- | :--- | :--- |
| **Magnetometer** | `Sensor.TYPE_MAGNETIC_FIELD` | Mengukur fluks medan magnet 3-sumbu ($B_x, B_y, B_z$) dan magnitudo total $\|B\| = \sqrt{x^2 + y^2 + z^2}$ dalam $\mu\text{T}$. | `SensorEvent.values[0..2]` | Terdistorsi oleh material feromagnetik ponsel (baut, speaker, casing magnetik), serta saturasi jika medan melebihi `sensor.maximumRange`. |
| **Akselerometer** | `Sensor.TYPE_ACCELEROMETER` | Menghitung sudut kemiringan total ponsel (*tilt angle*) terhadap vektor gravitasi bumi ($9.81\ \text{m/s}^2$). | `SensorEvent.values[0..2]` | Memberikan peringatan *"Pegang ponsel mendatar"* saat sudut kemiringan $> 45^\circ$ agar pengukuran konsisten pada bidang horizontal. |
| **Sensor Cahaya (Ambient Light)** | `Sensor.TYPE_LIGHT` | Mengukur tingkat pencahayaan kamar dalam satuan Lux untuk korelasi kondisi ruangan. | `SensorEvent.values[0]` | Opsional; jika perangkat tidak memiliki sensor cahaya, aplikasi tetap berjalan normal dengan fallback nilai `null`. |

---

## 5. Diagram Arsitektur Aplikasi

```
+-----------------------------------------------------------------------------------+
|                                 PRESENTATION LAYER                                |
|                                                                                   |
|  [HomeRoomsScreen]      [LiveScanScreen]     [ReferenceCatalog]   [HistoryScreen] |
|         ^                      ^                     ^                   ^        |
|         |                      |                     |                   |        |
|  (HomeRoomsViewModel)  (LiveScanViewModel)  (CatalogViewModel)   (HistoryViewModel)|
|                                                                                   |
|                      UI STATE (StateFlow<UiState> Immutable)                      |
|                           Unidirectional Data Flow (UDF)                          |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+------------------------------------------+----------------------------------------+
|                                    DOMAIN LAYER                                   |
|                                                                                   |
|  Logika Murni (Pure Kotlin):                                                      |
|   - UnitConverter (µT <-> mT <-> Gauss)                                           |
|   - MagneticClassifier (NORMAL, SEDANG, TINGGI, SANGAT TINGGI, SATURASI)          |
|   - TiltDetector (vektor gravitasi akselerometer -> tilt angle)                   |
|   - CalibrationCalculator (estimasi hard-iron offset & kualitas)                  |
|   - CsvExporter (RFC 4180 serialization)                                          |
|                                                                                   |
|  Repository Interfaces:                                                           |
|   - RoomRepository, MeasurementRepository, CalibrationRepository, ...            |
+------------------------------------------+----------------------------------------+
                                           |
                                           v
+------------------------------------------+----------------------------------------+
|                                     DATA LAYER                                    |
|                                                                                   |
|       Repository Implementations (RoomRepoImpl, MeasurementRepoImpl, ...)         |
|                          ^                                    ^                   |
|                          |                                    |                   |
|         +----------------+---------------+   +----------------+---------------+   |
|         |    LOCAL DATA SOURCE (SQLite)  |   |      SENSOR DATA SOURCE        |   |
|         |                                |   |                                |   |
|         | - DetektifDbHelper             |   | - RealSensorSource             |   |
|         |   (SQLiteOpenHelper native)    |   |   (SensorManager callbacks)    |   |
|         | - RoomDao, MeasurementDao      |   | - SimulationSensorSource       |   |
|         | - CalibrationDao, CatalogDao   |   | - CalibrationEngine            |   |
|         | - DatabaseChangeNotifier       |   | - Low-pass filter (alpha=0.15) |   |
|         +--------------------------------+   +--------------------------------+   |
+-----------------------------------------------------------------------------------+
```

---

## 6. Tinjauan Antarmuka (Layar Utama)

| Layar | Deskripsi & Komponen Kunci |
| :--- | :--- |
| **A. Pemetaan Ruangan (Beranda)** | Kartu padat berornamen crosshair teknis, kode berkas kasus (`KASUS #001 · KAMAR TIDUR`), jumlah titik ukur, status cap karet miring, dan FAB Tambah Ruangan. |
| **B. Tambah / Edit Ruangan** | Formulir validasi nama (maks 40 karakter dengan counter), chip seleksi tipe ruangan, input dimensi meter (lebar x panjang), dan catatan. |
| **C. Pindai Medan Langsung** | Gauge analog lengkung 240° dengan animasi spring, pembacaan digital monospace besar, grafik sinyal osiloskop 10 detik, bar vektor X/Y/Z, kartu statistik sesi, banner tilt warning, dan tombol bekukan. |
| **D. Kalibrasi Magnetik** | Panduan gerakan angka 8 selama 15 detik dengan countdown timer, visualisasi sampel 3D real-time, peringatan pengaruh casing magnet MagSafe, dan estimasi offset bias hard-iron. |
| **E. Riwayat Pengukuran** | Filter chip horizontal per ruangan & status, pengelompokan hari ("Hari Ini", "Kemarin"), swipe to delete dengan Undo Snackbar, dan ekspor CSV via Storage Access Framework. |
| **F. Detail Ruangan** | Ringkasan intensitas medan, canvas denah kotak interaktif berskala dengan titik-titik pengukuran, dan grafik batang intensitas antar titik. |
| **G. Katalog Referensi Perangkat** | 26 entri sumber medan elektromagnetik, pencarian real-time, filter kategori (Elektronik, Audio, Dapur, Kabel, Struktur, Lainnya), kartu proporsional, dan dialog detail fisika induksi. |
| **H. Pengaturan & Tentang** | Toggle satuan default ($\mu\text{T}$, $\text{mT}$, $\text{G}$), tema tampilan, haptik getar, mode simulasi sensor untuk emulator, dan tombol reset seluruh database. |

---

## 7. Panduan Menjalankan Aplikasi

### Kebutuhan Lingkungan:
- **Android Studio**: Android Studio Koala / Ladybug / Jellyfish (disarankan versi 2024.1+).
- **JDK**: JDK 17 (misalnya JetBrains Runtime / OpenJDK 17).
- **Perangkat Uji**: Smartphone fisik Android (minSdk 26 / Android 8.0 Oreo ke atas) dengan sensor magnetometer terpasang, atau Emulator Android dengan Virtual Sensors aktif.

### Langkah Build & Instalasi:

1. **Clone atau Buka Proyek**:
   Buka folder proyek `DetektifKamar` di Android Studio.
2. **Jalankan Unit Test**:
   ```powershell
   ./gradlew testDebugUnitTest
   ```
   *(Seluruh pengujian unit test pada logika matematika, klasifikasi, konversi satuan, dan kalibrasi akan dieksekusi)*.
3. **Build APK Debug**:
   ```powershell
   ./gradlew assembleDebug
   ```
   File biner APK dihasilkan di: `app/build/outputs/apk/debug/app-debug.apk`.
4. **Build APK Release (Minified)**:
   ```powershell
   ./gradlew assembleRelease
   ```
   File biner APK teroptimasi dihasilkan di: `app/build/outputs/apk/release/app-release.apk`.
5. **Instalasi ke Perangkat**:
   Hubungkan smartphone dengan mode USB Debugging aktif, lalu jalankan:
   ```powershell
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## 8. Keterbatasan Fisika & Sensor Smartphone

1. **Pengaruh Bahan Feromagnetik Internal & Eksternal**:
   - Magnetometer smartphone mengukur total rapat fluks magnetik lokal. Bodi logam ponsel, modul kamera magnetik, speaker internal, dan terutama **casing dengan cincin magnetik (seperti casing MagSafe)** akan mendistorsi pembacaan secara konstan. Pengguna disarankan melepas casing tebal saat melakukan survei ruangan.
2. **Saturasi Sensor**:
   - Sensor magnetometer smartphone memiliki jangkauan pembacaan maksimum (`sensor.maximumRange`, umumnya berkisar $1.000 - 2.000\ \mu\text{T}$). Jika didekatkan langsung ke magnet permanen kuat (seperti magnet neodymium atau motor trafo besar), sensor akan mengalami kejenuhan (*saturasi*). Aplikasi menangani kondisi ini dengan menampilkan status `SATURASI`.
3. **Pergeseran Bias (*Hard-Iron Distortion*)**:
   - Komponen logam permanen pada sirkuit ponsel menyebabkan pergeseran titik nol (*zero-offset bias*). Sesi kalibrasi gerakan angka 8 disediakan untuk memperkirakan dan mengurangi offset ini dari data mentah sensor.
4. **Data Katalog adalah Nilai Tipikal**:
   - Angka emisi pada Katalog Referensi merupakan rentang tipikal yang diambil dari literatur ilmiah pada jarak tertentu. Fluks magnetik nyata sangat dipengaruhi oleh daya beban perangkat, kualitas pelindung kabel (*shielding*), dan jarak ($1/r^3$).

---

## 9. Sumber Rujukan Ilmiah Terverifikasi

Nilai batas ambang dan rentang fluks medan magnet dalam aplikasi ini didasarkan pada literatur ilmiah dan panduan keselamatan elektromagnetik resmi:

1. **World Health Organization (WHO)**:
   - *WHO Environmental Health Criteria 238: Extremely Low Frequency Fields* (2007).
   - Membahas paparan medan magnet bolak-balik frekuensi rendah ($50/60\text{ Hz}$) pada perangkat rumah tangga dan pedoman batas paparan konsumen.
2. **International Commission on Non-Ionizing Radiation Protection (ICNIRP)**:
   - *ICNIRP Guidelines for Limiting Exposure to Time-Varying Electric and Magnetic Fields (1 Hz - 100 kHz)*, Health Physics 99(6):818-836 (2010).
   - Menetapkan batas referensi medan magnet untuk keselamatan masyarakat umum.
3. **U.S. Geological Survey (USGS)**:
   - *USGS Geomagnetism Program: Surface Magnetic Field Surveys & Global Field Models*.
   - Rujukan standar fluks medan geomagnetik alami permukaan bumi ($25 - 65\ \mu\text{T}$ tergantung lintang geografis).
4. **IEEE Standards Association**:
   - *IEEE Std C95.6: Safety Levels with Respect to Human Exposure to Electromagnetic Fields (0 to 3 kHz)*.
   - Analisis medan magnet di sekitar transformator step-down, motor listrik induksi, dan instalasi kabel hunian.
