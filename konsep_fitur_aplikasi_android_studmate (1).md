# Konsep Produk, Alur Pengguna & Sistem Desain: StudMate (Android Kotlin)

## 1. Ringkasan Eksekutif & Visi Produk

* **Nama Aplikasi:** StudMate
* **Platform:** Native Android (Kotlin — Jetpack Compose & Material Design 3)
* **Kategori:** Centralized Campus Peer-Learning Marketplace (Khusus Mata Kuliah)
* **Target Pengguna:** Mahasiswa aktif di seluruh Indonesia yang membutuhkan pendampingan mata kuliah (*Learner*) maupun mahasiswa berprestasi/asisten dosen yang membuka kelas pendampingan mata kuliah (*Peer Tutor*).
* **Keterkaitan SDGs:** **SDG 4 — Quality Education** (Memperluas akses pendampingan akademik berkualitas yang setara dan terjangkau antar mahasiswa lintas kampus).

### Visi & Prinsip Utama
Menjadi satu platform terpusat berbasis Android yang menghubungkan mahasiswa yang ingin memahami **mata kuliah** tertentu dengan sesama mahasiswa yang menguasai mata kuliah tersebut—baik di dalam satu kampus maupun lintas kampus di seluruh Indonesia.

> **Aturan Standarisasi Judul Kelas (Strict Course Naming Policy):**
> Seluruh kelas di StudMate **wajib menggunakan Nama Mata Kuliah murni** tanpa kode mata kuliah (seperti *IF-201* atau *MTH102*) dan tanpa embel-embel promosi/clickbait (seperti *"Kelas Intensif"*, *"Bimbel UTS"*, atau *"Jago dalam 3 Hari"*).
> * **Benar:** `Kalkulus Dasar`, `Struktur Data`, `Basis Data`, `Statistika dan Probabilitas`, `Akuntansi Keuangan Menengah`, `Kimia Organik`
> * **Salah:** `[IF2110] Bootcamp Struktur Data Pemula`, `Kelas Kilat Kalkulus Dasar Auto A`

---

## 2. Pemetaan Masalah ke Solusi Fitur (*Problem-Solution Fit*)

| Core Problem | Akar Masalah | Solusi Fitur di StudMate (Android) |
| :--- | :--- | :--- |
| **1. Fragmented Information** | Info tutor mata kuliah tersebar di grup WA, LINE, Instagram, dan info mulut ke mulut sehingga sulit dicari saat mendekati kuis/UTS/UAS. | **Unified Mata Kuliah Catalog & Smart Filter** berdasarkan nama mata kuliah, universitas, program studi, jadwal, dan metode belajar. |
| **2. Limited Exposure for Small Organizers** | Mahasiswa pintar atau kelompok studi kecil yang ingin membuka kelas mata kuliah sulit menjangkau peserta di luar lingkaran pertemanannya. | **Fair Tutor Discovery** (*"Tutor Baru di Kampusmu"*) dan **Shareable Course Card + Deep Link** yang otomatis mengarahkan klik dari grup WA langsung ke halaman mata kuliah di aplikasi. |
| **3. No Centralized Peer-Learning Ecosystem** | Belum ada satu alur terpadu lintas kampus di Indonesia untuk mencari, membandingkan tutor mata kuliah yang sama, mendaftar, belajar, hingga memberi ulasan. | **End-to-End Academic Flow**: `Discover → Compare → Register → Join → Review` bagi peserta dan `Create → Publish → Manage → Payout` bagi Peer Tutor. |

---

## 3. Alur Pengguna Terperinci (*End-to-End User Flows*)

StudMate menggunakan arsitektur **One Account, Dual Role**. Mahasiswa cukup memiliki satu akun dan dapat berpindah antara mode **Learner** dan **Peer Tutor** melalui *Role Switcher* di halaman Profil.

### A. Alur 1: Onboarding & Verifikasi Identitas Kampus

```text
[Buka Aplikasi] 
      │
      ▼
[Login Cepat] ──► Masuk via Google Sign-In / Email
      │
      ▼
[Profil Akademik] ──► Pilih Universitas, Fakultas, Program Studi, & Semester
      │
      ▼
[Verifikasi Mahasiswa] ──► Verifikasi OTP Email Kampus (.ac.id) atau Unggah KTM
      │                    (Opsional untuk Learner, Wajib untuk Peer Tutor)
      ▼
[Beranda StudMate] ──► Menampilkan rekomendasi mata kuliah sesuai prodi & kampus
```

### B. Alur 2: Learner (`Discover → Compare → Register → Join → Review`)

```text
1. DISCOVER (Cari Mata Kuliah)
   ├── Ketik nama mata kuliah di kolom pencarian (misal: "Struktur Data")
   ├── Pilih cakupan radar: [Kampus Saya] atau [Seluruh Indonesia]
   └── Filter berdasarkan: Metode (Tatap Muka / Online), Hari, & Rentang Biaya
          │
          ▼
2. COMPARE (Bandingkan Tutor Mata Kuliah)
   ├── Lihat daftar tutor yang membuka mata kuliah "Struktur Data"
   ├── Tandai 2–3 tutor untuk membuka layar Perbandingan Berdampingan (Side-by-Side)
   └── Bandingkan: Topik bahasan, nilai/IPK tutor pada matkul terkait, rating, jadwal, & tarif
          │
          ▼
3. REGISTER (Pendaftaran & Pembayaran Escrow)
   ├── Pilih jadwal sesi mata kuliah yang sesuai
   ├── Konfirmasi ringkasan pendaftaran (1-Tap Enrollment dari data profil)
   └── Bayar via QRIS / E-Wallet / VA ──► Dana ditahan aman di Sistem Escrow StudMate
          │
          ▼
4. JOIN (Pelaksanaan Kelas Mata Kuliah)
   ├── Terima Push Notification pengingat (H-1 dan 15 menit sebelum mulai)
   ├── Buka Detail Kelas di tab "Kelasku":
   │    ├── Online: Klik tombol "Gabung Sesi" (Zoom/Google Meet)
   │    └── Tatap Muka: Lihat titik lokasi kampus (Google Maps) & tunjukkan Tiket QR
   └── Unduh rangkuman materi & diskusi di forum kelas
          │
          ▼
5. REVIEW (Ulasan & Penyelesaian)
   ├── Sesi selesai & kehadiran terkonfirmasi
   ├── Berikan rating (1–5 bintang) dan ulasan kualitas penjelasan tutor
   └── Dana Escrow diteruskan ke saldo Peer Tutor
```

### C. Alur 3: Peer Tutor (`Create → Publish → Manage → Payout`)

```text
1. AKTIVASI MODE TUTOR
   └── Buka Tab Profil ──► Aktifkan Toggle "Mode Peer Tutor" (Wajib Verified Student)
          │
          ▼
2. CREATE (Buat Kelas Mata Kuliah)
   ├── Langkah 1 — Pilih Mata Kuliah: Pilih/ketik Nama Mata Kuliah murni (tanpa kode/embel-embel)
   │               + Lampirkan bukti penguasaan (Transkrip nilai matkul / riwayat asisten dosen)
   ├── Langkah 2 — Rencana Pertemuan: Isi daftar topik/bab mata kuliah yang akan dibahas
   └── Langkah 3 — Jadwal & Kuota: Tentukan tanggal, jam, lokasi kampus/link, kuota, & tarif
          │
          ▼
3. PUBLISH (Publikasi & Promosi)
   ├── Pratinjau tampilan kartu mata kuliah
   ├── Klik "Terbitkan Kelas" ──► Muncul di katalog kampus & nasional
   └── Bagikan Kartu Mata Kuliah (Deep Link) ke grup angkatan / WhatsApp
          │
          ▼
4. MANAGE (Kelola Sesi & Presensi)
   ├── Pantau daftar mahasiswa yang mendaftar
   ├── Kirim pengumuman kelas via Broadcast Notification (misal: info ruang kelas)
   └── Lakukan presensi mahasiswa menggunakan In-App QR Scanner
          │
          ▼
5. PAYOUT (Pencairan Dana)
   └── Setelah sesi selesai ──► Saldo otomatis masuk Dompet Tutor ──► Tarik ke Bank / E-Wallet
```

---

## 4. Rincian Fitur Utama Aplikasi

### A. Modul Learner (Mahasiswa Peserta)
1. **Pencarian & Radar Kampus:**
   * Pencarian berbasis **Nama Mata Kuliah** dengan *auto-complete* daftar mata kuliah standar perguruan tinggi di Indonesia.
   * **Campus Scope Toggle:** Beralih cepat antara kelas tatap muka di kampus sendiri (*Local Campus*) atau kelas daring bersama mahasiswa dari universitas lain (*Cross-Campus*).
2. **Side-by-Side Mata Kuliah Comparison:**
   * Membandingkan hingga 3 penawaran kelas untuk **mata kuliah yang sama** secara sejajar dalam satu layar (menampilkan silabus bab, durasi sesi, riwayat nilai tutor pada matkul tersebut, rating ulasan, dan biaya).
3. **Escrow Payment Protection:**
   * Pembayaran melalui QRIS dan dompet digital mahasiswa. Dana baru diteruskan ke tutor setelah sesi pembelajaran benar-benar terlaksana.
4. **Class Hub & Offline Ticket:**
   * Tiket QR Code presensi dan jadwal tersimpan secara *offline-first* di perangkat Android menggunakan Room Database sehingga tetap bisa diakses meski sinyal di gedung kampus lemah.

### B. Modul Peer Tutor (Mahasiswa Pengajar)
1. **Clean Mata Kuliah Builder:**
   * Formulir pembuatan kelas terstruktur yang secara otomatis memformat judul hanya sebagai **Nama Mata Kuliah** (memisahkan kolom *Nama Mata Kuliah*, *Universitas Asal*, dan *Pokok Bahasan* agar katalog tetap bersih dan seragam).
2. **Academic Credibility Badge:**
   * Lencana khusus pada profil tutor seperti *Terverifikasi Mahasiswa Aktif*, *Asisten Praktikum/Dosen*, atau *Nilai Mata Kuliah: A* untuk meningkatkan kepercayaan peserta.
3. **In-App QR Attendance Scanner:**
   * Pemindai QR Code bawaan kamera Android untuk mencatat kehadiran peserta secara instan di ruang kelas maupun laboratorium kampus.
4. **Auto-Generated Deep Link Card:**
   * Menghasilkan kartu informasi mata kuliah siap bagikan (*Android Share Sheet*) dengan *Android App Link* yang langsung membuka detail kelas di aplikasi StudMate.

---

## 5. Sistem Desain & Palet Warna Konsisten (*UI/UX Design System*)

Untuk menghadirkan antarmuka yang bersih, modern, akademik, dan nyaman dipandang dalam waktu lama, StudMate menerapkan sistem desain **Material Design 3** dengan palet warna konsisten berikut:

### A. Palet Warna Utama (*Color Tokens*)

| Peran Warna (*Token*) | Nama Warna | Kode HEX (Light Mode) | Kode HEX (Dark Mode) | Penggunaan pada Antarmuka |
| :--- | :--- | :--- | :--- | :--- |
| **Primary** | *Academic Indigo* | `#1E3A8A` | `#93C5FD` | Tombol aksi utama (*Daftar Kelas*), tab aktif, header identitas merek. |
| **On Primary** | *Pure White / Deep Navy* | `#FFFFFF` | `#0F172A` | Teks dan ikon di atas elemen *Primary*. |
| **Primary Container** | *Soft Periwinkle* | `#DBEAFE` | `#1E3A8A` | Latar *chip* filter aktif, *badge* fakultas/jurusan, sorotan pilihan. |
| **Secondary** | *Scholar Teal* | `#0D9488` | `#5EEAD4` | Lencana *Verified Student*, indikator kuota tersedia, status selesai. |
| **Secondary Container** | *Mint Mist* | `#CCFBF1` | `#115E59` | Latar belakang *badge* verifikasi kampus dan status *Online/Tatap Muka*. |
| **Tertiary** | *Warm Amber* | `#D97706` | `#FBBF24` | Ikon rating bintang tutor, pengingat waktu mendekati kelas. |
| **Background** | *Alabaster Slate* | `#F8FAFC` | `#0B1120` | Latar belakang utama kanvas aplikasi (mengurangi silau layar). |
| **Surface Card** | *Crisp Surface* | `#FFFFFF` | `#1E293B` | Kartu daftar mata kuliah, panel perbandingan, dan *bottom sheet*. |
| **Outline Variant** | *Subtle Border* | `#E2E8F0` | `#334155` | Garis tepi tipis (`1dp`) pemisah kartu dan tabel perbandingan. |
| **Text Primary** | *Ink Slate* | `#0F172A` | `#F8FAFC` | **Nama Mata Kuliah**, judul bagian, dan nominal harga. |
| **Text Secondary** | *Muted Slate* | `#475569` | `#94A3B8` | Nama tutor, asal universitas, jadwal hari/jam, dan deskripsi bab. |

### B. Tipografi & Hierarki Visual
* **Font Family:** `Plus Jakarta Sans` (untuk judul mata kuliah dan header) dipadukan dengan `Inter` (untuk jadwal, silabus, dan metadata agar mudah dibaca di layar ponsel).
* **Hierarki Kartu Mata Kuliah:**
  1. **Headline (`18sp`, SemiBold, `#0F172A`):** **Nama Mata Kuliah** murni (maksimal 2 baris, tanpa kode atau embel-embel).
  2. **Subtitle (`13sp`, Medium, `#475569`):** Nama Peer Tutor + Lencana Verifikasi (`#0D9488`) + Nama Universitas.
  3. **Metadata Chip (`12sp`, Medium, `#1E3A8A` di atas `#DBEAFE`):** Format kelas (`Tatap Muka` / `Online`) & Jadwal sesi.
  4. **Footer Action:** Biaya per sesi (`15sp`, Bold, `#0F172A`) sejajar dengan tombol aksi (`#1E3A8A`).

### C. Blueprint Antarmuka Layar Utama (*Clean UI Wireframe*)

#### 1. Layar Beranda & Katalog Mata Kuliah
```text
+-------------------------------------------------------+
|  StudMate                                  [Notif]    |
|  Universitas Indonesia • Teknik Informatika           |
|                                                       |
|  [ 🔍 Cari nama mata kuliah...                    ]   |
|                                                       |
|  Cakupan: ( ● Kampus Saya )  ( ○ Seluruh Indonesia )  |
|  Filter:  [Semua] [Tatap Muka] [Online] [Hari Ini]    |
|                                                       |
|  Mata Kuliah Tersedia                     Bandingkan  |
|  +-------------------------------------------------+  |
|  | [Tatap Muka]                       ★ 4.9 (24)   |  |
|  | Struktur Data                                   |  |
|  | Raka Pratama ✓ • Universitas Indonesia          |  |
|  | Topik: Tree, Graph, & Analisis Kompleksitas     |  |
|  | ----------------------------------------------- |  |
|  | Rab, 18 Nov • 16.00 WIB          Rp 25.000/sesi |  |
|  | Sisa 3 kursi                    [ Lihat Detail ] |  |
|  +-------------------------------------------------+  |
|                                                       |
|  +-------------------------------------------------+  |
|  | [Online]                           ★ 4.8 (19)   |  |
|  | Kalkulus Dasar                                  |  |
|  | Nadya Putri ✓ • Institut Teknologi Bandung      |  |
|  | Topik: Limit, Turunan, & Aplikasi Integral      |  |
|  | ----------------------------------------------- |  |
|  | Kam, 19 Nov • 19.30 WIB          Rp 20.000/sesi |  |
|  | Sisa 5 kursi                    [ Lihat Detail ] |  |
|  +-------------------------------------------------+  |
|                                                       |
|  [ Beranda ]    [ Bandingkan ]    [ Kelasku ]  [Akun] |
+-------------------------------------------------------+
```

#### 2. Layar Perbandingan Mata Kuliah (*Side-by-Side Compare*)
```text
+-------------------------------------------------------+
|  ←  Bandingkan Kelas: Struktur Data                   |
|                                                       |
|  Kriteria       | Tutor A            | Tutor B        |
|  ---------------+--------------------+---------------- |
|  Tutor          | Raka Pratama ✓     | Dimas Arya ✓   |
|  Universitas    | Univ. Indonesia    | Univ. Brawijaya|
|  Nilai Matkul   | A                  | A              |
|  Rating         | ★ 4.9 (24 ulasan)  | ★ 4.7 (12)     |
|  Metode         | Tatap Muka         | Online         |
|  Jadwal         | Rab, 16.00 WIB     | Jum, 19.00 WIB |
|  Biaya          | Rp 25.000          | Rp 20.000      |
|  ---------------+--------------------+---------------- |
|                 | [ Pilih Tutor A ]  | [ Pilih B ]    |
+-------------------------------------------------------+
```

---

## 6. Arsitektur Teknis Android (Kotlin)

* **Bahasa & UI:** 100% Kotlin dengan **Jetpack Compose** & **Material Design 3** (menerapkan *Custom ColorScheme* StudMate di atas).
* **Arsitektur:** MVVM (*Model-View-ViewModel*) + *Clean Architecture* (`Presentation`, `Domain`, `Data`).
* **State & Async:** Kotlin Coroutines & `StateFlow` untuk pembaruan kuota kursi dan filter secara *real-time*.
* **Local Storage:** Room Database (menyimpan jadwal kelas, silabus mata kuliah, dan Tiket QR presensi untuk akses *offline*) + Jetpack DataStore (menyimpan preferensi kampus dan *role* aktif).
* **Integrasi Native Android:**
  * **CameraX + ML Kit Barcode Scanning:** Pemindaian QR presensi oleh Peer Tutor.
  * **Firebase Cloud Messaging (FCM):** Pengingat jadwal mata kuliah dan pengumuman perubahan ruang/tautan.
  * **Android App Links:** Membuka langsung halaman mata kuliah dari tautan yang dibagikan di grup WhatsApp/LINE.