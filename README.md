# Detektif Kamar

> *"Medan magnet tak terlihat, bukan tak terlacak."*

Aplikasi penyelidikan medan magnet di dalam ruangan berbasis **Android Native (Kotlin + Jetpack Compose)**. Dirancang khusus bagi pelajar dan mahasiswa Indonesia untuk mendeteksi anomali medan magnet di kamar, kos, ruang kerja, atau dapur menggunakan sensor fisik smartphone.

---

## 🔍 Fitur Utama

1. **Berkas Kasus TKP (Tempat Kejadian Perkara)**:
   - Buat kasus ruangan berskala meter (panjang x lebar).
   - Simpan catatan investigasi dan status acuan ruangan (*baseline*).
2. **Denah Interaktif & Peta Panas (Heatmap IDW)**:
   - Canvas denah presisi dengan kisi 0.5 meter.
   - Peta panas interpolasi IDW (*Inverse Distance Weighting* kuadrat) dengan batas jangkauan radius 1.5 meter.
   - Lapisan Peta Magnet, Peta Cahaya (lux), dan Polos.
   - Penempatan perabot berlabel (kotak proporsional).
   - Penandaan titik bukti langsung dengan mengetuk denah.
3. **Pengambilan Bukti Terkalibrasi**:
   - Gerbang kestabilan HP: hanya dapat merekam jika HP stabil (simpangan baku akselerometer < 0.25 m/s²).
   - Sesi perekaman 4 detik dengan kalkulasi statistik lengkap (rata-rata, min, maks, simpangan baku, delta acuan).
   - Evaluasi kualitas pembacaan (*baik*, *bergerak*, *akurasi rendah*, *derau tinggi*).
   - Mode pengukuran titik acuan (*baseline*) ruangan.
4. **Mode Selidik Langsung (Radar Hangat-Dingin)**:
   - Meter jarum analog mikrotesla (µT) bergaya instrumen laboratorium 70-an.
   - Indikator tren Hangat-Dingin (*Makin Panas!*, *Makin Dingin*, *Stabil*) dengan histeresis filter.
   - Grafik jejak osiloskop 10 detik real-time.
   - Penguraian vektor 3-sumbu ($B_x, B_y, B_z$).
   - Pulsa getar haptik adaptif yang berdenyut lebih cepat saat mendekati sumber gangguan.
5. **Katalog Tersangka (26 Entri)**:
   - Berisi 26 tersangka gangguan elektromagnetik tipikal (speaker woofer, magnet kulkas, charger wireless, adaptor laptop, kompor induksi, tulangan cor dinding, dsb.).
   - Dilengkapi rentang tipikal µT, jarak ukur, karakteristik fisik, dan prosedur verifikasi.
   - Pencarian cerdas dan pencocokan otomatis (*JOIN query*) terhadap simpangan titik ukur.
6. **Arsip Lintas Kasus**:
   - Rekapitulasi seluruh barang bukti dari semua berkas kasus dengan filter dan sortir simpangan.
7. **Desain Khas Laboratorium Fisika Tahun 70-an**:
   - Latar kertas milimeter ringan tanpa bitmap.
   - Tipografi mesin tik (*Special Elite*), teks teknis (*IBM Plex Sans*), dan angka sensor monospace (*IBM Plex Mono*).
   - Stempel status investigasi karet miring.
   - Mode Gelap (ruang gelap/papan tulis lab) dan Mode Terang.
   - 100% Offline tanpa izin berbahaya (hanya izin normal `VIBRATE`).

---

## ⚙️ Teknologi & Arsitektur

- **Bahasa**: Kotlin 2.0.20
- **UI Framework**: Jetpack Compose (Material 3 sebagai fondasi, dengan tema, palet token, tipografi, dan komponen buatan sendiri)
- **Arsitektur**: Clean MVVM (Model-View-ViewModel) dengan Unidirectional Data Flow (UDF)
  - `domain/`: Logika matematika murni (IDW, Trigonometri sirkular azimuth, statistik, kestabilan, klasifikasi ambang) tanpa dependensi Android SDK.
  - `data/`: SQLite murni via `SQLiteOpenHelper` dengan transaksi dan kaskade, `DatabaseChangeNotifier` untuk reaktivitas Flow.
  - `ui/`: Compose Screen + ViewModel + Immutable UiState yang dikonsumsi dengan `collectAsStateWithLifecycle`.
- **Injeksi Dependensi**: Manual Dependency Injection via `AppContainer` pada class `Application` (tanpa Hilt / Koin).
- **Navigasi**: Jetpack Navigation Compose (string routes).

---

## 📡 Sensor yang Digunakan

Aplikasi memanfaatkan 3 sensor fisik dari 3 kategori Android Sensor Hardware yang berbeda:

| Kategori Sensor | Tipe Sensor Android | Fungsi dalam Aplikasi Detektif Kamar |
| :--- | :--- | :--- |
| **POSITION** | `Sensor.TYPE_MAGNETIC_FIELD` | Mengukur fluks medan magnet total $\sqrt{x^2 + y^2 + z^2}$ dalam mikrotesla (µT) yang invarian terhadap orientasi HP; menghitung arah hadap kompas (azimuth) bersama akselerometer; memantau akurasi kalibrasi sensor; mendeteksi tren hangat-dingin. |
| **MOTION** | `Sensor.TYPE_ACCELEROMETER` | Menjadi **gerbang kestabilan** pengukuran (simpangan baku percepatan < 0.25 m/s²); menghitung kemiringan fisis (*tilt*) HP untuk komponen *bubble level* (waterpass); memvalidasi apakah sudut azimuth layak dicatat (kemiringan < 45°). |
| **ENVIRONMENT** | `Sensor.TYPE_LIGHT` | Mencatat intensitas cahaya ruangan (lux) di setiap titik bukti untuk membuat lapisan peta sekunder (Peta Cahaya) guna mengkorelasikan tingkat pencahayaan dengan gangguan magnetik. Ditangani secara anggun (*null-safe*) jika perangkat tidak memiliki sensor cahaya. |

---

## 🗄️ Skema Database SQLite

Database lokal murni menggunakan `detektif_kamar.db` (versi 1) dengan *Foreign Key Constraints* aktif:

```text
+--------------------+       1:N       +-------------------+
| investigation_case |<----------------|     furniture     |
+--------------------+                 +-------------------+
          |
          | 1:N
          v
+--------------------+       1:N       +-------------------+
|   evidence_point   |<----------------|      reading      |
+--------------------+                 +-------------------+
          |
          | N:M (via point_suspect)
          v
+--------------------+
|  suspect_catalog   |
+--------------------+
```

### Rincian Tabel:
1. `investigation_case`: Berkas kasus TKP (`id`, `title`, `room_type`, `width_m`, `length_m`, `note`, `baseline_ut`, `created_at`, `updated_at`).
2. `furniture`: Perabot di denah (`id`, `case_id` FK CASCADE, `label`, `kind`, `x_m`, `y_m`, `w_m`, `h_m`).
3. `evidence_point`: Titik bukti pengukuran (`id`, `case_id` FK CASCADE, `label`, `x_m`, `y_m`, `note`, `created_at`).
4. `reading`: Riwayat pembacaan sensor pada titik (`id`, `point_id` FK CASCADE, `mean_ut`, `min_ut`, `max_ut`, `std_ut`, `bx`, `by`, `bz`, `lux_mean`, `azimuth_deg`, `tilt_deg`, `accuracy`, `quality`, `sample_count`, `measured_at`).
5. `suspect_catalog`: Katalog tersangka gangguan (`id`, `name`, `category`, `typical_min_ut`, `typical_max_ut`, `typical_distance_cm`, `note`, `how_to_confirm`).
6. `point_suspect`: Tabel relasi relasi titik dengan tersangka (`point_id` FK CASCADE, `suspect_id` FK CASCADE, `custom_label`).

---

## 📸 Tangkapan Layar (Screenshots)

Letakkan tangkapan layar pengujian pada folder `docs/screenshots/`:

| 1. Daftar Kasus (Map Berkas) | 2. Denah & Peta Panas IDW | 3. Pengambilan Bukti |
| :---: | :---: | :---: |
| ![Kasus](docs/screenshots/01_kasus.png) | ![Denah](docs/screenshots/02_denah_heatmap.png) | ![Ukur](docs/screenshots/03_ukur_titik.png) |

| 4. Mode Selidik Langsung | 5. Detail Titik & Tersangka | 6. Mode Gelap (Lab Room) |
| :---: | :---: | :---: |
| ![Selidik](docs/screenshots/04_selidik.png) | ![Detail](docs/screenshots/05_detail_titik.png) | ![Dark Mode](docs/screenshots/06_dark_mode.png) |

---

## 🚀 Cara Menjalankan

1. **Kebutuhan Sistem**:
   - **Android Studio**: Android Studio Koala (2024.1.1) atau Ladybug (2024.2.1)
   - **JDK**: Java Development Kit 17 (JDK 17)
   - **Perangkat**: HP fisik Android (minSdk 26 / Android 8.0 Oreo ke atas) sangat direkomendasikan agar pembacaan sensor magnetometer dan akselerometer nyata.
2. **Menjalankan di Emulator Android**:
   - Jika menggunakan emulator, buka *Extended Controls* (`...`) $\rightarrow$ *Virtual sensors* untuk memanipulasi nilai medan magnet.
   - Atau aktifkan **Mode Simulasi Sensor** di layar *Pengaturan* aplikasi.
3. **Build & Install**:
   ```bash
   # Masuk ke folder proyek
   cd DetektifKamar

   # Kompilasi & Jalankan Unit Test
   ./gradlew testDebugUnitTest

   # Build APK Debug
   ./gradlew assembleDebug
   ```
   File APK hasil build berada di:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## ⚠️ Keterbatasan

1. **Akurasi Magnetometer**: Sensor magnetometer pada ponsel cerdas rentan terpengaruh oleh magnet casing MagSafe, gesper jam tangan, atau komponen internal ponsel saat panas. Lakukan kalibrasi gerakan angka 8 sebelum mengukur.
2. **Karakteristik Tersangka**: Nilai pada katalog tersangka merupakan estimasi tipikal empiris untuk keperluan edukasi dan penyelidikan amatir; nilai riil bergantung pada merk, daya watt, dan pelindung ferit masing-masing perangkat.
3. **Sensor Cahaya**: Beberapa tablet atau tipe ponsel kelas pemula tidak menyertakan sensor cahaya fisik (`TYPE_LIGHT`). Jika tidak tersedia, lapisan Peta Cahaya dinonaktifkan secara otomatis tanpa mengganggu fungsi medan magnet.

---

## 📦 Panduan Pengunggahan ke GitHub

1. **Inisialisasi Repositori Git**:
   ```bash
   git init
   git add .
   git commit -m "feat: inisialisasi lengkap aplikasi Detektif Kamar siap build"
   git branch -M main
   ```
2. **Hubungkan ke Remote GitHub (Pastikan Repositori Dibuat PUBLIC)**:
   ```bash
   git remote add origin https://github.com/<username-kamu>/DetektifKamar.git
   git push -u origin main
   ```
3. **Checklist Sebelum Pengumpulan Tugas**:
   - [x] Repositori disetel ke **Public** (dapat diakses tanpa login).
   - [x] Build Gradle sukses (`./gradlew assembleDebug` lulus).
   - [x] Seluruh unit test lokal lolos (`./gradlew testDebugUnitTest` lulus).
   - [x] Tidak ada file temporary atau folder `build/` yang ter-commit (terfilter oleh `.gitignore`).
   - [x] Tangkapan layar dimasukkan ke `docs/screenshots/`.
