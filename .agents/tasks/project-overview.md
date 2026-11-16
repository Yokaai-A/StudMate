# Ringkasan Proyek StudMate (StudiMate)

Laporan investigasi baca-saja. Tidak ada file proyek yang diubah. Path sumber relatif terhadap `app\src\main\java\com\example\studmate\` kecuali disebut lain.

## 1. Jawaban singkat

StudMate adalah aplikasi Android native (Kotlin + Jetpack Compose) berupa marketplace kelas belajar antar mahasiswa. Mahasiswa mencari dan mengikuti kelas (tutor 1-on-1, study group, workshop, bootcamp). Tutor atau organisasi kampus membuat dan menjalankan kelas dari "Tutor Studio". Satu akun bisa berpindah antara Mode Belajar dan Mode Penyelenggara.

Seluruh data disimpan lokal di perangkat (Room + DataStore). Tidak ada backend, jaringan, atau pembayaran sungguhan. Login Google, pembayaran, pencairan dana, dan verifikasi KTM adalah simulasi. Aplikasi sudah memuat data contoh (seed) sehingga langsung bisa dicoba. Proyek ini terlihat sebagai prototipe/tugas kuliah (Semester 5) yang cukup lengkap alurnya, tetapi belum siap produksi.

README.md hanya berisi "Aplikasi Belajar". Dokumentasi fitur ada di `FITUR_DAN_PANDUAN.md` (bahasa Indonesia) dan sesuai dengan kode yang dibaca.

## 2. Tujuan aplikasi dan pengguna

- **Peserta (learner)**: mahasiswa yang mencari bantuan mata kuliah (Struktur Data, Kalkulus Dasar, Basis Data, dst.). Fitur: cari dan filter kelas, bandingkan maksimal 3 kelas, daftar (gratis 1-tap atau berbayar), ikut kelas lewat Class Hub, tiket QR presensi, sertifikat, ulasan.
- **Tutor / penyelenggara**: mahasiswa senior (Peer Tutor) atau organisasi/komunitas (`OrganizerType.INDIVIDUAL` / `ORGANIZATION`, misalnya "BEM Fasilkom UI"). Fitur: buat kelas (wizard), kelola materi dan pengumuman, presensi QR atau manual, dompet escrow dan pencairan.
- Aturan penamaan kelas (`domain/Models.kt` → `Catalog.validateCourseTitle`): judul harus nama mata kuliah murni. Kode matkul (mis. "IF-201") dan kata promosi ("bootcamp", "kilat", "jago", "bimbel", dll.) ditolak. Ini diuji di `StudMateLogicTest`.

## 3. Tech stack dan versi

Sumber: `gradle\libs.versions.toml`, `app\build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`.

| Aspek | Nilai |
|---|---|
| Bahasa | Kotlin 2.3.20, JVM target Java 17 |
| Android Gradle Plugin | 9.2.1 |
| KSP | 2.3.14 (untuk Room compiler) |
| compileSdk / targetSdk / minSdk | 37 / 37 / **35** (hanya Android 15+) |
| applicationId / namespace | `com.example.studmate`, versi 1.0 (code 1) |
| UI | Jetpack Compose, BOM 2026.09.00, Material3, material-icons-extended, plus `material` 1.14.0 dan `appcompat` 1.8.0 |
| Navigasi | navigation-compose 2.10.2 |
| Lifecycle | 2.11.0 (runtime-compose, viewmodel-compose) |
| Database | Room 2.8.5 (`exportSchema = true`, schema ke `app/schemas`) |
| Preferensi | DataStore Preferences 1.3.0 |
| Background | WorkManager 2.12.0 |
| DI | Koin 4.2.2 (`koin-androidx-compose`) |
| Gambar | Coil 3.6.3 (compose) |
| QR | ZXing core 3.5.4 (buat QR) dan zxing-android-embedded 4.3.0 (scan, transitif dimatikan) |
| Auth Google | androidx.credentials 1.6.0 + credentials-play-services-auth + googleid 1.2.1 (lihat catatan di bagian 8) |
| Test | JUnit 4.13.2, androidx.test junit 1.3.0, Espresso 3.7.0 |

Konfigurasi lain:
- `BuildConfig.GOOGLE_WEB_CLIENT_ID` dibaca dari `local.properties` (kosong jika tidak ada). `local.properties` ada di `.gitignore`.
- `repositoriesMode = FAIL_ON_PROJECT_REPOS`; plugin foojay-resolver untuk toolchain JDK.
- Release: `isMinifyEnabled = false`.

### Manifest (`app\src\main\AndroidManifest.xml`)
- Izin: `INTERNET`, `POST_NOTIFICATIONS`, `CAMERA`; kamera `required=false`.
- Application: `.StudMateApp`, tema `Theme.StudMate`, `allowBackup=true`.
- Satu Activity: `.MainActivity` (launcher). Intent filter deep link: skema `studmate://` dan `https://studmate.app` (`autoVerify=false`).
- `FileProvider` (`${applicationId}.fileprovider`, paths di `res/xml/file_paths.xml`) untuk membagikan kartu promo dan PDF sertifikat.

## 4. Arsitektur dan tata letak paket

Pola: single-Activity, Compose, MVVM sederhana, repository di atas Room Flow, DI Koin. Semua stream data reaktif (`Flow` → `StateFlow` → `collectAsState`).

```
com.example.studmate
├── MainActivity.kt          Activity tunggal, tema dari DataStore, teruskan deep link ke MainScreen
├── StudMateApp.kt           Application + modul Koin (appModule) + seeding
├── data
│   ├── SeedData.kt          Data contoh (user, kelas, sesi, review, forum, enrollment, dst.)
│   ├── local                AppDatabase.kt, Daos.kt, Entities.kt
│   ├── prefs                SessionStore.kt (DataStore)
│   └── repo                 AuthRepository, CatalogRepository, LearningRepository,
│                            OrganizerRepository, Notifier
├── domain                   Enums.kt, Models.kt (model turunan, FilterState, Catalog, AppConfig)
├── notif                    NotificationHelper.kt, Reminders.kt (ReminderScheduler + ReminderWorker)
├── ui
│   ├── MainScreen.kt        NavHost + bottom navigation + deep link
│   ├── components           Badges.kt, ClassCard.kt, FilterBottomSheet.kt
│   ├── screens              14 layar (bagian 5)
│   ├── theme                Color.kt, Theme.kt (StudMateTheme: terang/gelap/sistem + dynamic color)
│   └── viewmodel            10 ViewModel
└── util                     Format.kt, NativeIntents.kt, QrUtils.kt
```

### 4.1 data\local (Room)
- `AppDatabase`: versi 1, 12 entitas, `Converters` (List<String> ↔ String dengan pemisah `\u001F`). DAO: `UserDao`, `ClassDao` (kelas + sesi), `EnrollmentDao` (pendaftaran + presensi), `CommunityDao` (review, materi, forum, pengumuman, notifikasi), `WalletDao` (payout, admin organisasi).
- `Daos.kt` berisi query reaktif (`Flow`) dan agregasi: jumlah peserta per kelas, rating rata-rata, jumlah dan durasi sesi, jumlah kehadiran.

### 4.2 data\prefs
`SessionStore` (DataStore `studmate_prefs`): `current_user_id`, `organizer_mode`, `theme_mode`, `dynamic_color`, `radar_local`. Logout menghapus user dan mematikan mode organizer. `AppSettings(themeMode, dynamicColor)` dipakai `MainActivity`.

### 4.3 data\repo
- **AuthRepository**: register (validasi nama, regex email, sandi ≥ 6, email unik), login, `signInWithGoogle` (sebenarnya hanya membuat/mengambil user lokal dari email), logout, onboarding, `update`, verifikasi kampus. OTP email kampus (`.ac.id`) dibuat acak 6 digit, disimpan di map memori `pendingOtp`, dan "dikirim" lewat notifikasi lokal. Hash sandi PBKDF2WithHmacSHA256, 120.000 iterasi, salt acak 16 byte, dibandingkan dengan `MessageDigest.isEqual`. `submitKtm` langsung berstatus VERIFIED (simulasi).
- **CatalogRepository**: `allSummaries` menggabungkan 5 flow (kelas, user, jumlah peserta, rating, agregat sesi) menjadi `ClassSummary`, dibagikan dengan `shareIn(WhileSubscribed(5s), replay=1)`. Turunan: `published`, `mentors` (`MentorStats`), `summary(id)`, `sessions`, `reviews`, `mentor(id)`, `classesBy`.
- **LearningRepository**: `myLearning`, `enroll` (transaksi: cek status PUBLISHED, bukan penyelenggara sendiri, kuota via `seatsTaken`, harga early-bird via `priceAt`), `confirmPayment` (status ACTIVE + `HELD` escrow, ref `PAY-xxxx`), `cancelEnrollment` (refund jika HELD, ditolak jika sudah ada sesi selesai), `submitReview` (wajib pernah hadir, rating 1–5), `eligibility` + `claimCertificate` (semua sesi selesai dan kehadiran ≥ `minAttendancePercent`), forum (`addPost`, balasan, notifikasi), notifikasi. `onActivated` menjadwalkan pengingat dan memberi notifikasi ke peserta dan penyelenggara.
- **OrganizerRepository**: `observeMyClasses` (termasuk kelas organisasi yang dikelola lewat tabel `org_admins`), `observeClassAttendees`, `checkInByTicket` (parse `studmate://ticket/<kode>`, cek kelas dan status), `toggleAttendance` (manual), `saveClass` (validasi, hitung `startAt`/`endAt` dari sesi, simpan draft atau publish dalam transaksi), `deleteClass` (hanya jika belum ada peserta), `setSessionDone` (jika semua sesi selesai: kelas → COMPLETED, escrow HELD → RELEASED, notifikasi payout), `broadcastAnnouncement`, `addMaterial`, `observeWallet`, `requestPayout` (min Rp20.000, ≤ saldo tersedia), `addOrgAdmin`/`removeOrgAdmin`.
- **Notifier**: simpan `NotificationEntity` untuk semua user tujuan, dan tampilkan notifikasi sistem hanya jika penerima adalah user yang sedang login.

### 4.4 domain
- `Enums.kt`: `Category` (Akademik, Tech, Bisnis, Bahasa, Desain, Karir), `ProgramType` (Tutoring, Study Group, Workshop, Bootcamp), `ClassFormat` (Online, Offline, Hybrid), `ClassStatus` (DRAFT, PUBLISHED, COMPLETED, CANCELLED), `EnrollmentStatus` (PENDING_PAYMENT, ACTIVE, COMPLETED, CANCELLED), `PaymentStatus` (FREE, UNPAID, HELD, RELEASED, REFUNDED), `PaymentMethod` (QRIS, GoPay, DANA, ShopeePay, VA BCA/BNI/Mandiri), `VerificationStatus`, `OrganizerType`, `MaterialType`, `PriceFilter`, `SortOption`, `ThemeMode`. `AppConfig`: `PLATFORM_FEE_PERCENT = 5`, `DEEP_LINK_HOST = "studmate.app"`, `MAX_COMPARE = 3`, `classLink(id)`.
- `Models.kt`: `ClassSummary` (kelas + organizer + statistik; `remaining`, `isFull`), `MentorStats` (skor = peserta×2 + rating×jumlah review×3 + kelas×5; `isNew`), `FilterState` (`activeCount`, `MAX_PRICE` = 1.000.000), `LearningItem`, objek `Catalog` (20 universitas, 10 fakultas, 25 jurusan, 25 mata kuliah standar, minat, pemetaan minat→kategori, validasi judul).

### 4.5 notif
- `NotificationHelper`: dua channel, `class_reminder` (HIGH) dan `class_updates` (DEFAULT). `show()` berhenti diam-diam jika izin `POST_NOTIFICATIONS` belum diberikan. Tap membuka `MainActivity` dengan deep link.
- `ReminderScheduler`: per sesi yang belum selesai, jadwalkan 2 `OneTimeWorkRequest` (H-1 dan 15 menit sebelum) dengan tag `enroll_<id>`. `ReminderWorker` (Koin `KoinComponent`) menyimpan notifikasi ke DB dan menampilkannya. Pembatalan lewat tag.

### 4.6 ui
- `MainScreen`: `NavHost` dengan rute `auth`, `onboarding`, `home`, `class_detail/{classId}`, `class_hub/{classId}`, `class_builder?classId=`, `attendance/{classId}`, `wallet`, `notifications`, `mentor/{mentorId}`. `home` berisi bottom bar yang berubah menurut mode: Learner (Beranda, Bandingkan, Kelasku, Akun) atau Organizer (Studio, Dompet, Akun). Deep link: `studmate://class|hub|manage/<id>` dan `https://studmate.app/class/<id>`; host `ticket` diterima tetapi tidak melakukan apa pun.
- `components`: `Badges.kt`, `ClassCard.kt`, `FilterBottomSheet.kt`.
- `theme`: `Color.kt`, `Theme.kt` (tema terang/gelap/ikut sistem dan dynamic color Android 12+).
- Util: `Format.kt` (Rupiah, tanggal locale id-ID, "WIB", ID dan kode acak), `QrUtils` (bitmap QR, payload `studmate://ticket/<kode>`), `NativeIntents` (buka URL/URI, kartu promo bitmap 1080×1350, PDF sertifikat A4 landscape 842×595 lewat `PdfDocument`, intent kalender, berbagi).

## 5. Layar dan fitur (`ui\screens`)

| Layar | Fungsi |
|---|---|
| `AuthScreen` | Tab daftar/masuk (nama, email, sandi), tombol "Masuk Cepat via Google (Simulasi)", pemilih akun demo |
| `OnboardingScreen` | Lengkapi universitas, fakultas, jurusan, semester, minat |
| `DiscoverScreen` (tab Beranda) | Pencarian, filter (kategori, jenis, format, harga, tanggal), urutan, radar lokal (filter kampus sama dengan user), mentor rising, tombol bandingkan, ikon notifikasi |
| `CompareScreen` | Tabel perbandingan berdampingan hingga 3 kelas |
| `ClassDetailScreen` | Detail kelas, sesi, pengajar, ulasan; daftar 1-tap/bayar (sheet metode pembayaran, "Simulasi"), selesaikan bayar, bagikan kartu promo |
| `MentorProfileScreen` | Profil tutor, statistik, portofolio, kelas yang diajar |
| `MyLearningScreen` (Kelasku) | Daftar kelas diikuti per tab, progres kehadiran, batalkan pendaftaran, buka hub |
| `ClassHubScreen` | Ruang kelas: materi, pengumuman, diskusi (balasan berutas), tiket QR presensi, tautan sesi/lokasi, tambah ke kalender, sertifikat (PDF) dan ulasan |
| `OrganizerStudioScreen` | Kelas Aktif/Draft/Selesai, buat/edit/hapus kelas, tambah materi, siarkan pengumuman, buka presensi QR, ke dompet, kembali ke Mode Belajar |
| `ClassBuilderScreen` | Wizard buat/edit kelas (info dasar, sesi, tiket/harga/early-bird, simpan draft atau publish) |
| `AttendanceScannerScreen` | Pilih sesi, scan QR via `ScanContract` (ZXing embedded) atau input kode manual, daftar peserta dengan toggle hadir, tandai sesi selesai |
| `WalletScreen` | Ringkasan escrow/saldo/biaya layanan, form tarik saldo ("Simulasi"), riwayat payout |
| `ProfileScreen` (Akun) | Profil, bio, portofolio, tipe organizer, tema/dynamic color, sakelar mode organizer, verifikasi kampus (OTP email `.ac.id` atau "Simulasi Unggah Foto KTM"), ganti akun demo, keluar |
| `NotificationInboxScreen` | Kotak masuk notifikasi, tandai semua dibaca, buka kelas terkait |

ViewModel: `AuthViewModel`, `DiscoverViewModel`, `CompareViewModel`, `MyLearningViewModel`, `ClassDetailViewModel(classId)`, `ClassHubViewModel(classId)`, `OrganizerViewModel`, `ClassBuilderViewModel(classId?)`, `WalletViewModel`, `ProfileViewModel`. Semua memakai `StateFlow` + `SharingStarted.WhileSubscribed(5_000)`.

## 6. Model data

12 tabel Room (`data\local\Entities.kt`):

| Entitas (tabel) | Isi pokok | Relasi / indeks |
|---|---|---|
| `UserEntity` (users) | nama, email (unik), `passwordHash`, `authProvider`, profil kampus, `interests`, `verificationStatus`, `organizerType`, `organizationName`, `onboarded` | email unik |
| `ClassEntity` (classes) | `organizerId`, judul, deskripsi, kategori, jenis program, format, harga, early-bird, kuota, sertifikat, `minAttendancePercent`, status, `startAt/endAt` | idx organizerId, status; `priceAt(now)` |
| `SessionEntity` (sessions) | `classId`, urutan, judul, `startAt`, durasi, `isDone` | idx classId |
| `EnrollmentEntity` (enrollments) | `classId`, `userId`, status, `amount`, `paymentStatus`, `paymentRef`, `ticketCode`, `certificateId` | unik (classId,userId) dan ticketCode |
| `AttendanceEntity` (attendance) | PK gabungan (enrollmentId, sessionId), `method` QR/MANUAL | |
| `ReviewEntity` (reviews) | rating 1–5, komentar; id = `classId-userId` | unik (classId,userId) |
| `MaterialEntity`, `ForumPostEntity`, `AnnouncementEntity` | konten Class Hub (forum punya `parentId` untuk balasan) | idx classId |
| `PayoutEntity` (payouts) | `organizerId`, jumlah, tujuan, nomor rekening, status | |
| `OrgAdminEntity` (org_admins) | email admin yang mengelola akun organisasi | unik (organizerId,email) |
| `NotificationEntity` (notifications) | `userId`, judul, isi, `classId`, `type`, `isRead` | |

Relasi hanya berupa kolom id biasa. Tidak ada `@ForeignKey` sehingga tidak ada cascade dan integritas tidak dijaga database.

Alur status: `PENDING_PAYMENT/UNPAID` → bayar → `ACTIVE/HELD` → semua sesi selesai → `COMPLETED/RELEASED`. Batal sebelum kelas jalan: `CANCELLED/REFUNDED`. Kelas gratis langsung `ACTIVE/FREE`.

Uang: biaya platform 5%. `availableToWithdraw = released × 95% − total payout sukses`.

### Seed data (`data\SeedData.kt`, `populateIfEmpty`)
Dijalankan di `StudMateApp.onCreate` (coroutine IO). Berisi 7 user: learner demo "Rian Pratama" (`user-learner-demo`), 5 tutor (Raka Pratama, Nadya Putri, Dimas Arya, Sarah Nadya, Ahmad Fauzi) dan organisasi "BEM Fasilkom UI" (`user-org-ui`, admin `panitia.bem@ui.ac.id`). Ada 8 kelas (Struktur Data ×2, Kalkulus Dasar, Basis Data, Statistika dan Probabilitas, Akuntansi Keuangan Menengah, Algoritma dan Pemrograman, dan satu lagi di daftar `classes`), sesi dengan waktu relatif terhadap `now`, review, post forum, enrollment, kehadiran, dan notifikasi. Jika tidak ada sesi login, user diset ke learner demo.

## 7. Alur pengguna dan peran

Tidak ada kolom "role". Peran ditentukan oleh `SessionStore.organizerMode` (sakelar di Profil), dan kepemilikan kelas (`organizerId`) atau keanggotaan `org_admins`.

1. **Mulai**: `StudMateApp` memulai Koin, membuat channel, mengisi seed, memilih learner demo. `MainScreen` memilih tujuan awal: `auth` (belum login), `onboarding` (belum onboarded) atau `home`.
2. **Peserta**: Beranda → detail kelas → daftar. Gratis langsung aktif. Berbayar jadi `PENDING_PAYMENT`, lalu pilih metode (simulasi) dan dana ditahan escrow. Pengingat dijadwalkan. Kelasku → Class Hub: materi/pengumuman/diskusi, tampilkan QR tiket. Setelah semua sesi selesai dan kehadiran memenuhi syarat: klaim sertifikat (PDF). Ulasan hanya setelah hadir minimal 1 sesi.
3. **Tutor**: aktifkan Mode Mengajar → Studio → Buat Kelas (draft/publish) → tambah materi atau pengumuman (notifikasi ke peserta) → Presensi QR: pilih sesi, scan QR tiket peserta atau input kode manual → tandai sesi selesai. Saat sesi terakhir selesai, kelas COMPLETED, escrow dilepas ke saldo → Dompet → tarik saldo (simulasi, langsung `SUCCESS`).
4. **Presensi**: QR peserta berisi `studmate://ticket/<ticketCode>` (`QrUtils`). Scanner mengambil `result.contents`, `checkInByTicket` memvalidasi tiket, lalu menyimpan `AttendanceEntity(method="QR")` dan mengirim notifikasi ke peserta. Check-in ulang untuk sesi yang sama idempoten.
5. **Notifikasi**: tiga sumber. Event di repositori lewat `Notifier` (daftar, pengumuman, materi, presensi, payout, review, balasan forum, OTP, verifikasi). Pengingat WorkManager H-1 dan 15 menit. Inbox di aplikasi dengan badge jumlah belum dibaca. Tap notifikasi memakai deep link.

## 8. Titik masuk, state, DI

- **Entry**: `StudMateApp` (Application) dan `MainActivity` (satu-satunya Activity, edge-to-edge, `onNewIntent` memperbarui deep link). Tema dibaca dari `SessionStore.settings`.
- **State**: `ViewModel` + `StateFlow`; UI memakai `collectAsState`. Tab bawah dan mode disimpan di `remember`/DataStore, bukan di rute navigasi. Sesi login ada di DataStore.
- **DI**: Koin, modul `appModule` di `StudMateApp.kt`: DB dan DAO singleton, `SessionStore`, `Notifier`, `ReminderScheduler`, `CoroutineScope(SupervisorJob + Default)`, 4 repository, dan `viewModel { }` (ada yang berparameter `classId`).

## 9. Temuan: celah, risiko, kualitas kode

Prioritas tinggi:
1. **Izin notifikasi tidak pernah diminta.** `POST_NOTIFICATIONS` ada di manifest dan dicek di `NotificationHelper.show`, tetapi tidak ada `ActivityResultContracts.RequestPermission` di kode (hasil pencarian `ActivityResultContracts` kosong). Pada minSdk 35 izin ini wajib diminta saat runtime, jadi notifikasi sistem, pengingat, dan kode OTP tidak akan tampil sampai pengguna mengaktifkannya manual. Verifikasi OTP kampus bergantung pada notifikasi ini (kode OTP juga tersimpan di inbox, tapi pengguna tidak diberi tahu).
2. **`fallbackToDestructiveMigration()` dengan `version = 1`.** Setiap perubahan skema akan menghapus seluruh data pengguna tanpa peringatan. Tidak ada migrasi.
3. **Keamanan data (sesuai prototipe, tapi berisiko jika dipakai nyata).** OTP disimpan di memori dan hilang jika proses mati. `submitKtm` langsung VERIFIED tanpa tinjauan. `switchDemoUser` memungkinkan pindah ke akun mana pun tanpa sandi. `allowBackup=true`. Akun demo tidak punya `passwordHash`, jadi hanya bisa dimasuki lewat pemilih demo.
4. **Tidak ada backend.** Semua data hanya ada di satu perangkat, sehingga tutor dan peserta pada perangkat berbeda tidak saling melihat data. Mekanisme presensi, notifikasi ke tutor, dan escrow hanya bekerja dalam satu instalasi.

Prioritas sedang:
5. **Dependensi Google Credentials tidak dipakai.** `androidx.credentials`, `credentials-play-services-auth`, `googleid`, dan `BuildConfig.GOOGLE_WEB_CLIENT_ID` dideklarasikan, tetapi tidak ada pemanggilan `CredentialManager`/`GetCredential` di kode. Login Google hanya `signInGoogleSimulated` dengan email tetap `mahasiswa.baru@ui.ac.id`.
6. **Radar "lokal" hanya mencocokkan universitas** (`DiscoverViewModel`), bukan lokasi GPS. Manifest tidak punya izin lokasi, padahal `FITUR_DAN_PANDUAN.md` menyebut izin lokasi.
7. **Deep link `studmate://ticket/...`** diterima di `MainScreen` tetapi tidak melakukan aksi (blok kosong).
8. **Integritas data.** Tidak ada foreign key. `checkInByTicket` tidak memeriksa bahwa `sessionId` milik `classId` yang sama. `setSessionDone` tidak membatalkan pengingat WorkManager. `deleteClass` menghapus sesi dan kelas tetapi tidak materi, forum, pengumuman, atau review.
9. **Wallet.** `observeWallet` hanya menghitung kelas dengan `organizerId` sama dengan user, sehingga admin organisasi (`org_admins`) yang dapat mengelola kelas organisasi tidak melihat pendapatan organisasi di dompetnya. Pencairan juga selalu langsung `SUCCESS`. `platformFee` dihitung dari `grossTotal` (termasuk escrow yang belum dilepas), sedangkan saldo dihitung dari `released` saja, jadi angka fee dan saldo bisa tidak selaras.
10. **Login** tidak menormalkan email ke huruf kecil sebelum query, meski DAO memakai `lower()` sehingga aman. Tidak ada pembatasan percobaan login.
11. **Race pendaftaran.** Pengecekan kuota ada di dalam `withTransaction`, jadi aman untuk satu perangkat.
12. **Teks berisi emoji pada beberapa string notifikasi tampil rusak (mojibake, mis. "âœ…") saat dibaca lewat konsol.** Ini mungkin hanya artefak pembacaan terminal, atau file tersimpan dengan encoding keliru. Perlu dicek di editor/emulator.

Kualitas dan pengujian:
13. Test sangat sedikit: `StudMateLogicTest.kt` (8 unit test untuk format, QR, early-bird, filter, MentorStats, validasi judul) dan `ExampleUnitTest`, `ExampleInstrumentedTest` bawaan. Tidak ada test untuk repository, ViewModel, DAO, atau UI.
14. File layar besar: `ClassHubScreen` ±35 KB, `ClassDetailScreen` ±35 KB, `ClassBuilderScreen` ±27 KB, `DiscoverScreen` ±27 KB. `SeedData.kt` ±29 KB. Layak dipecah.
15. `MainScreen` membuat tujuh ViewModel di level atas sekaligus, dan `OrganizerViewModel` dipakai bersama oleh Studio dan Scanner.
16. `release` tanpa minify/shrink. `README.md` nyaris kosong. `CompareViewModel` dibuat tanpa sumber data sendiri dan bergantung pada `LaunchedEffect` di `MainScreen` untuk sinkronisasi daftar bandingkan.
17. `MentorProfileScreen` menerima `CatalogRepository` langsung (melewati ViewModel), tidak konsisten dengan layar lain.

Catatan cakupan: file inti (build, manifest, seluruh `data`, `domain`, `notif`, `util`, `MainScreen`, `MainActivity`, `StudMateApp`, tes, serta ViewModel utama) dibaca penuh. Layar Compose besar (`ui\screens`, `components`) dibaca lewat penelusuran struktur, pemanggilan ViewModel, dan string kunci, bukan baris per baris. Ringkasan layar di bagian 5 didasarkan pada rute, ViewModel, dan `FITUR_DAN_PANDUAN.md`.

## 10. Cara build dan menjalankan

Prasyarat: Android Studio versi terbaru yang mendukung AGP 9.2.1, JDK 17 atau lebih baru, SDK Platform 37, emulator atau perangkat **Android 15 (API 35) atau lebih baru**. Kebutuhan internet hanya untuk mengunduh dependensi Gradle.

1. Buka folder `StudMate` di Android Studio dan tunggu Gradle sync (`gradlew.bat` tersedia).
2. (Opsional) Tambahkan `GOOGLE_WEB_CLIENT_ID=...` ke `local.properties`. Tidak diperlukan karena login Google disimulasikan.
3. Jalankan konfigurasi `app` di emulator/perangkat, atau dari terminal:
   - `gradlew.bat :app:assembleDebug` (build APK debug)
   - `gradlew.bat :app:installDebug` (pasang ke perangkat)
   - `gradlew.bat :app:testDebugUnitTest` (unit test)
4. Saat pertama kali jalan, seed data dimuat otomatis dan aplikasi masuk sebagai "Rian Pratama". Untuk mencoba sisi tutor: Akun → ganti akun demo ke tutor (mis. Raka Pratama) → aktifkan Mode Mengajar. Berikan izin notifikasi dan kamera secara manual lewat pengaturan jika diperlukan (lihat temuan 1).

Perintah di atas tidak dijalankan dalam investigasi ini (baca-saja).

## 11. Kesimpulan dan rekomendasi

StudMate adalah prototipe aplikasi marketplace kelas belajar mahasiswa dengan alur lengkap (cari, daftar, bayar escrow simulasi, presensi QR, sertifikat, forum, dompet tutor), arsitektur rapi (Compose + MVVM + Room + Koin), dan data seed yang memungkinkan demo tanpa server.

Rekomendasi (tidak diimplementasikan):
1. Tambahkan permintaan izin `POST_NOTIFICATIONS` saat runtime (mis. di `MainActivity` atau setelah onboarding).
2. Ganti `fallbackToDestructiveMigration()` dengan migrasi Room nyata sebelum skema berubah.
3. Putuskan nasib dependensi Google Credentials: implementasikan `CredentialManager` atau hapus dependensi dan `GOOGLE_WEB_CLIENT_ID`.
4. Sinkronkan dokumentasi dengan kode: radar lokal berbasis kampus, bukan lokasi. Isi `README.md` dengan cara build dan akun demo.
5. Perbaiki integritas data: foreign key atau cascade, validasi sesi milik kelas pada check-in, batalkan pengingat saat sesi selesai, hapus data turunan saat kelas dihapus, hitung dompet untuk organisasi yang dikelola.
6. Tambah test untuk repository (enroll, payment, escrow, payout, check-in) dengan Room in-memory, dan test ViewModel.
7. Jika akan dipakai nyata, butuh backend untuk autentikasi, sinkronisasi data, pembayaran, dan verifikasi KTM, serta menonaktifkan `switchDemoUser` pada build release.
