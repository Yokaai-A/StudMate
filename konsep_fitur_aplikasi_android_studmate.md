# Konsep Produk & Spesifikasi Fitur: StudMate (Android Kotlin)

## 1. Ringkasan Eksekutif & Visi Produk

* **Nama Aplikasi:** StudMate
* **Platform:** Native Android (Kotlin)
* **Kategori:** Centralized Campus Peer-Learning Marketplace
* **Target Pengguna:** Mahasiswa aktif, tutor sebaya (*peer tutor*), komunitas belajar, himpunan mahasiswa (HIMA/BEM), dan penyelenggara bootcamp mahasiswa di seluruh Indonesia.
* **Keterkaitan SDGs:** **SDG 4 — Quality Education** (Memastikan pendidikan berkualitas yang inklusif dan merata serta mempromosikan kesempatan belajar sepanjang hayat bagi semua).

### Visi Utama
Menjadi ekosistem terpusat berbasis mobile yang menjembatani mahasiswa yang ingin mengembangkan *hard skill* maupun *soft skill* dengan sesama mahasiswa atau organisasi kampus yang ingin berbagi ilmu—mulai dari skala satu kampus hingga lintas kampus di seluruh Indonesia.

---

## 2. Pemetaan Masalah ke Solusi Fitur (*Problem-Solution Fit*)

| Core Problem | Akar Masalah | Solusi Fitur di StudMate (Android) |
| :--- | :--- | :--- |
| **1. Fragmented Information** | Info kelas tersebar di WA, LINE, IG, email kampus, dan grup tidak resmi sehingga sulit dicari. | **Unified Discovery Feed & Smart Multi-Filter** (filter berdasarkan kampus, jurusan, kategori skill, jadwal, format kelas, dan harga). |
| **2. Limited Exposure for Small Organizers** | Tutor individu atau komunitas baru kalah bersaing dari lembaga besar dan sulit promosi. | **Fair Discovery Spotlight** (*"New & Rising Mentors"*), *Verified Campus Badge*, serta **Auto-Promo Card & Deep Link** untuk dibagikan ke WA/IG. |
| **3. No Centralized Peer-Learning Ecosystem** | Belum ada satu alur terpadu lintas kampus untuk mencari hingga memberi ulasan kelas. | **End-to-End Lifecycle**: `Discover → Compare → Register → Join → Review` (peserta) dan `Create → Publish → Manage → Reach Students` (penyelenggara). |

---

## 3. Arsitektur Peran Pengguna (*Dual-Role System*)

StudMate menerapkan konsep **One Account, Dual Role** agar mahasiswa tidak perlu membuat dua akun terpisah:

1. **Learner (Peserta Belajar):** Mahasiswa yang mencari bimbingan mata kuliah (*tutoring*), *workshop*, atau *bootcamp* pengembangan skill.
2. **Organizer / Peer Tutor (Pengajar & Penyelenggara):** Mahasiswa individu, kelompok belajar, UKM, atau organisasi kampus yang membuka kelas.
   * *Android UX Note:* Pengguna dapat berpindah mode antara **Mode Belajar** dan **Mode Mengajar** secara instan melalui *Role Switcher* di tab **Profil**.

---

## 4. Rincian Fitur Utama Aplikasi

### A. Onboarding & Sistem Kepercayaan (*Trust & Campus Verification*)
1. **Quick Auth & Campus Identity:**
   * Login cepat menggunakan Google Sign-In atau email.
   * Pemilihan **Asal Universitas** dan **Program Studi** saat *onboarding* untuk personalisasi konten.
2. **Campus Verification Badge:**
   * Verifikasi opsional bagi peserta dan **wajib bagi Tutor/Organizer** menggunakan email kampus (`.ac.id`) atau unggah foto Kartu Tanda Mahasiswa (KTM) guna mencegah penipuan kelas fiktif.
3. **Interest Personalization:**
   * Pemilihan topik minat saat pertama kali membuka aplikasi (misal: *Pemrograman Kotlin, UI/UX Design, Statistika, Kalkulus, Public Speaking, Persiapan TOEFL/IELTS*).

---

### B. Modul Peserta / Learner (`Discover → Compare → Register → Join → Review`)

#### 1. Discover (Eksplorasi Kelas Terpusat)
* **Personalized Home Feed:** Rekomendasi kelas berdasarkan jurusan, minat skill, dan aktivitas kampus asal.
* **National & Local Campus Radar:** Toggle untuk melihat kelas yang diadakan **khusus di kampus sendiri** (cocok untuk kelas tatap muka/tutoring matkul) atau **lintas kampus se-Indonesia** (cocok untuk bootcamp online).
* **Advanced Search & BottomSheet Filter:**
  * Kategori (Akademik/Matkul, Tech & Digital, Bisnis, Bahasa, Desain, Karir).
  * Tipe Program (*1-on-1 Tutoring, Study Group, Workshop, Intensive Bootcamp*).
  * Format (*Online Live, Offline Kampus, Hybrid*).
  * Rentang Harga (*Gratis* hingga *Berbayar*) & Rentang Tanggal.

#### 2. Compare (Perbandingan & Detail Kelas)
* **Side-by-Side Class Comparison:** Fitur khusus yang memungkinkan mahasiswa menandai 2 hingga 3 kelas serupa lalu membandingkannya secara berdampingan dalam satu layar (membandingkan harga, total jam pertemuan, silabus, rating tutor, sisa kuota, dan benefit sertifikat).
* **Comprehensive Class Detail:**
  * Rincian silabus per pertemuan (*session breakdown*).
  * Profil lengkap mentor (asal kampus, jurusan, portofolio, dan riwayat mengajar).
  * Sisa kuota secara *real-time* dan ulasan dari peserta sebelumnya.

#### 3. Register (Pendaftaran & Pembayaran Aman)
* **1-Tap Enrollment:** Pendaftaran instan tanpa perlu mengisi Google Form berulang kali (data profil otomatis terisi).
* **Integrated Payment & Escrow System:**
  * Dukungan pembayaran digital populer di kalangan mahasiswa (QRIS, GoPay, DANA, ShopeePay, Virtual Account).
  * **Sistem Escrow (Rekening Bersama):** Dana pendaftaran ditahan oleh StudMate dan baru diteruskan ke Organizer setelah sesi kelas berjalan sesuai jadwal untuk melindungi mahasiswa dari pembatalan sepihak.

#### 4. Join (Pusat Belajar & Pelaksanaan Kelas)
* **My Learning Dashboard:** Daftar kelas aktif, kelas mendatang, dan riwayat kelas selesai.
* **Smart Reminder & Calendar Sync:** Integrasi langsung ke kalender Android serta notifikasi pengingat otomatis (`H-1` dan `15 menit sebelum kelas dimulai`).
* **Class Hub:**
  * Akses 1-klik ke tautan *video conference* (Zoom/Google Meet) untuk kelas online, atau integrasi **Google Maps** menuju gedung/ruang kampus untuk kelas offline.
  * Repository materi (unduh slide/PDF/rekaman kelas).
  * Forum diskusi/tanya-jawab kelas terintegrasi.
  * **QR Code Ticket:** Kode QR unik untuk presensi masuk kelas.

#### 5. Review & Portofolio Pasca-Kelas
* **Verified Review System:** Hanya peserta yang terverifikasi hadir yang dapat memberikan rating (1–5 bintang) dan ulasan.
* **Automated E-Certificate:** Sertifikat digital otomatis diterbitkan setelah tingkat kehadiran terpenuhi dan dapat langsung dibagikan ke LinkedIn.

---

### C. Modul Organizer & Peer Tutor (`Create → Publish → Manage → Reach Students`)

#### 1. Create & Publish (Tutor Studio)
* **Mobile Class Builder Wizard:** Antarmuka pembuatan kelas bertahap yang ringkas dan nyaman diisi lewat smartphone:
  1. *Informasi Dasar* (Judul, deskripsi, kategori, cover banner).
  2. *Kurikulum & Jadwal* (Jumlah pertemuan, tanggal, jam, lokasi/link).
  3. *Tiket & Kuota* (Gratis/Berbayar, harga *early bird*, batas maksimal peserta).
  4. *Preview & Publish*.
* **Organizer Profile Type:** Mendukung identitas **Tutor Individu** maupun **Organisasi/Komunitas** (bisa dikelola lebih dari 1 admin panitia).

#### 2. Manage (Manajemen Kelas & Peserta)
* **Participant & Attendance Manager:** Daftar peserta terdaftar beserta fitur **In-App QR Scanner** menggunakan kamera Android untuk mencatat kehadiran peserta secara cepat.
* **Broadcast Announcement:** Kirim pengumuman penting (misal: perubahan ruangan kampus atau pengingat tugas) yang langsung masuk sebagai *Push Notification* ke HP seluruh peserta.
* **Earnings & Payout Wallet:** Dasbor transparansi pemasukan tiket dan fitur pencairan dana ke rekening bank atau e-wallet mahasiswa.

#### 3. Reach Students (Peningkatan Eksposur Organik)
* **Fair Exposure Algorithm:** Slot khusus di halaman beranda seperti **"Tutor Baru di Kampusmu"** dan **"Rising Organizers"** agar mahasiswa yang baru pertama kali membuka kelas tetap mendapatkan visibilitas tinggi tanpa tertutup oleh penyelenggara besar.
* **1-Click Shareable Promo Card:** Aplikasi otomatis membuatkan kartu poster ringkas berisi info kelas dan **Android App Link (Deep Link)**. Saat dibagikan ke grup WhatsApp atau Instagram Story, mahasiswa lain yang mengklik tautan akan langsung diarahkan ke halaman kelas tersebut di dalam aplikasi StudMate.

---

## 5. Pemanfaatan Fitur Native Android (Kotlin Advantages)

Untuk memaksimalkan pengalaman pengguna di perangkat Android, StudMate dirancang dengan kapabilitas native berikut:

1. **Firebase Cloud Messaging (FCM):** Pengiriman *push notification* real-time untuk pengingat kelas, status transaksi, pengumuman mendadak dari tutor, dan balasan diskusi.
2. **Offline-First Caching (Room Database):** Jadwal kelas, tiket QR presensi, dan silabus tersimpan secara lokal sehingga tetap dapat dibuka mahasiswa meskipun koneksi internet di area kampus sedang tidak stabil.
3. **Android App Links (Deep Linking):** Tautan pendaftaran yang dibagikan di media sosial langsung membuka halaman detail kelas di dalam aplikasi StudMate.
4. **Native System Intents:** Integrasi mulus dengan kamera (scan QR absensi), Google Calendar (simpan jadwal), Google Maps (navigasi lokasi kelas offline), dan Android Share Sheet.

---

## 6. Rekomendasi Arsitektur & Tech Stack (Kotlin)

* **Bahasa Pemrograman:** 100% Kotlin
* **UI Toolkit:** Jetpack Compose + Material Design 3 (mendukung *Dynamic Color* & *Dark Mode*)
* **Arsitektur Aplikasi:** MVVM (*Model-View-ViewModel*) dengan pendekatan *Clean Architecture*
* **Concurrency:** Kotlin Coroutines & Flow (`StateFlow` / `SharedFlow`)
* **Dependency Injection:** Dagger Hilt atau Koin
* **Networking:** Retrofit / Ktor Client + Kotlinx Serialization
* **Local Persistence:** Room Database & Jetpack DataStore
* **Image Loading:** Coil (optimal untuk Jetpack Compose)
* **Backend & Layanan Pihak Ketiga:**
  * *Backend/Database:* Supabase / Firebase / REST API (Node.js atau Spring Boot)
  * *Payment Gateway:* Midtrans atau Xendit Mobile SDK (mendukung QRIS otomatis)

---

## 7. Roadmap Pengembangan (MVP ke Full Scale)

* **Fase 1 — MVP (Core Marketplace):**
  * Autentikasi & Verifikasi Kampus, Home Discovery + Filter, Create & Publish Kelas, Pendaftaran Kelas Gratis & Pembayaran QRIS, My Classes, dan Push Notification dasar.
* **Fase 2 — Ecosystem & Trust:**
  * Fitur *Side-by-Side Compare*, Sistem Rating & Review Terverifikasi, QR Code Presensi, Auto-Promo Card + Deep Link, serta E-Certificate otomatis.
* **Fase 3 — Intelligence & Scale:**
  * Sistem Rekomendasi berbasis AI, Escrow Wallet otomatis penuh, dan Gamifikasi (*Top Campus Mentor Leaderboard*).