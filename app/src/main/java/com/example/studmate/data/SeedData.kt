package com.example.studmate.data

import com.example.studmate.data.local.AnnouncementEntity
import com.example.studmate.data.local.AppDatabase
import com.example.studmate.data.local.AttendanceEntity
import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.EnrollmentEntity
import com.example.studmate.data.local.ForumPostEntity
import com.example.studmate.data.local.MaterialEntity
import com.example.studmate.data.local.OrgAdminEntity
import com.example.studmate.data.local.PayoutEntity
import com.example.studmate.data.local.ReviewEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.MaterialType
import com.example.studmate.domain.OrganizerType
import com.example.studmate.domain.PaymentMethod
import com.example.studmate.domain.PaymentStatus
import com.example.studmate.domain.ProgramType
import com.example.studmate.domain.VerificationStatus
import com.example.studmate.util.DAY
import com.example.studmate.util.HOUR

object SeedData {

    const val DEMO_LEARNER_ID = "user-learner-demo"
    const val DEMO_TUTOR_RAKA_ID = "user-tutor-raka"
    const val DEMO_TUTOR_NADYA_ID = "user-tutor-nadya"
    const val DEMO_TUTOR_DIMAS_ID = "user-tutor-dimas"
    const val DEMO_TUTOR_SARAH_ID = "user-tutor-sarah"
    const val DEMO_TUTOR_AHMAD_ID = "user-tutor-ahmad"
    const val DEMO_ORG_UI_ID = "user-org-ui"

    suspend fun populateIfEmpty(db: AppDatabase) {
        if (db.userDao().count() > 0) return

        val now = System.currentTimeMillis()

        val learner = UserEntity(
            id = DEMO_LEARNER_ID,
            name = "Rian Pratama",
            email = "rian.pratama@ui.ac.id",
            university = "Universitas Indonesia",
            faculty = "Fakultas Ilmu Komputer",
            major = "Teknik Informatika",
            semester = 4,
            academicRole = "Mahasiswa Aktif",
            interests = listOf("Struktur Data", "Kalkulus Dasar", "Basis Data", "Algoritma dan Pemrograman"),
            bio = "Mahasiswa semester 4 yang aktif mencari pendampingan mata kuliah komputasi.",
            portfolioUrl = "https://github.com/rian-pratama",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "CAMPUS_EMAIL",
            campusEmail = "rian.pratama@ui.ac.id",
            organizerType = OrganizerType.INDIVIDUAL,
            onboarded = true,
            joinedAt = now - 90 * DAY,
        )

        val tutorRaka = UserEntity(
            id = DEMO_TUTOR_RAKA_ID,
            name = "Raka Pratama",
            email = "raka.pratama@ui.ac.id",
            university = "Universitas Indonesia",
            faculty = "Fakultas Ilmu Komputer",
            major = "Teknik Informatika",
            semester = 6,
            academicRole = "Asisten Dosen",
            interests = listOf("Struktur Data", "Algoritma dan Pemrograman", "Basis Data"),
            bio = "Asisten Dosen mata kuliah Struktur Data Fasilkom UI. Peraih Nilai A pada Struktur Data dan Algoritma.",
            portfolioUrl = "https://linkedin.com/in/rakapratama-ui",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "CAMPUS_EMAIL",
            campusEmail = "raka.pratama@ui.ac.id",
            organizerType = OrganizerType.INDIVIDUAL,
            onboarded = true,
            joinedAt = now - 180 * DAY,
        )

        val tutorNadya = UserEntity(
            id = DEMO_TUTOR_NADYA_ID,
            name = "Nadya Putri",
            email = "nadya.putri@itb.ac.id",
            university = "Institut Teknologi Bandung",
            faculty = "Sekolah Teknik Elektro dan Informatika",
            major = "Teknik Informatika",
            semester = 6,
            academicRole = "Asisten Praktikum",
            interests = listOf("Kalkulus Dasar", "Matematika Diskrit", "Aljabar Linear Elementer"),
            bio = "Asisten Praktikum Matematika Dasar ITB. Menguasai konsep visual kalkulus dan penyelesaian soal analitik.",
            portfolioUrl = "https://linkedin.com/in/nadyaputri-itb",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "CAMPUS_EMAIL",
            campusEmail = "nadya.putri@itb.ac.id",
            organizerType = OrganizerType.INDIVIDUAL,
            onboarded = true,
            joinedAt = now - 140 * DAY,
        )

        val tutorDimas = UserEntity(
            id = DEMO_TUTOR_DIMAS_ID,
            name = "Dimas Arya",
            email = "dimas.arya@student.ub.ac.id",
            university = "Universitas Brawijaya",
            faculty = "Fakultas Ilmu Komputer",
            major = "Teknik Informatika",
            semester = 6,
            academicRole = "Peer Tutor",
            interests = listOf("Struktur Data", "Akuntansi Keuangan Menengah", "Basis Data"),
            bio = "Tutor sebaya aktif FILKOM UB. Mengajarkan Struktur Data dan Akuntansi Dasar dengan studi kasus visual.",
            portfolioUrl = "https://linkedin.com/in/dimas-arya-ub",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "CAMPUS_EMAIL",
            campusEmail = "dimas.arya@student.ub.ac.id",
            organizerType = OrganizerType.INDIVIDUAL,
            onboarded = true,
            joinedAt = now - 45 * DAY,
        )

        val tutorSarah = UserEntity(
            id = DEMO_TUTOR_SARAH_ID,
            name = "Sarah Nadya",
            email = "sarah.nadya@mail.ugm.ac.id",
            university = "Universitas Gadjah Mada",
            faculty = "Fakultas Matematika dan IPA",
            major = "Statistika",
            semester = 6,
            academicRole = "Asisten Praktikum",
            interests = listOf("Statistika dan Probabilitas", "Kalkulus Dasar", "Metode Numerik"),
            bio = "Asisten Praktikum Statistika UGM. Peraih medali perak olimpiade sains mahasiswa.",
            portfolioUrl = "https://sarahnadya.notion.site",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "KTM",
            campusEmail = "sarah.nadya@mail.ugm.ac.id",
            organizerType = OrganizerType.INDIVIDUAL,
            onboarded = true,
            joinedAt = now - 120 * DAY,
        )

        val tutorAhmad = UserEntity(
            id = DEMO_TUTOR_AHMAD_ID,
            name = "Ahmad Fauzi",
            email = "ahmad.fauzi@itb.ac.id",
            university = "Institut Teknologi Bandung",
            faculty = "Sekolah Teknik Elektro dan Informatika",
            major = "Teknik Informatika",
            semester = 8,
            academicRole = "Asisten Dosen",
            interests = listOf("Basis Data", "Sistem Operasi", "Jaringan Komputer"),
            bio = "Asisten Dosen Basis Data ITB. Menguasai SQL kompleks, normalisasi schema, dan relasional aljabar.",
            portfolioUrl = "https://linkedin.com/in/ahmadfauzi-itb",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "CAMPUS_EMAIL",
            campusEmail = "ahmad.fauzi@itb.ac.id",
            organizerType = OrganizerType.INDIVIDUAL,
            onboarded = true,
            joinedAt = now - 200 * DAY,
        )

        val orgUI = UserEntity(
            id = DEMO_ORG_UI_ID,
            name = "BEM Fasilkom UI",
            email = "akademik@fasilkom.ui.ac.id",
            university = "Universitas Indonesia",
            faculty = "Fakultas Ilmu Komputer",
            major = "Ilmu Komputer",
            semester = 6,
            academicRole = "Himpunan Mahasiswa",
            interests = listOf("Struktur Data", "Algoritma dan Pemrograman", "Basis Data"),
            bio = "Departemen Akademik BEM Fasilkom UI. Menyelenggarakan pendampingan resmi mata kuliah wajib fakultas.",
            portfolioUrl = "https://bem.cs.ui.ac.id",
            verificationStatus = VerificationStatus.VERIFIED,
            verificationMethod = "CAMPUS_EMAIL",
            campusEmail = "akademik@fasilkom.ui.ac.id",
            organizerType = OrganizerType.ORGANIZATION,
            organizationName = "BEM Fasilkom UI",
            onboarded = true,
            joinedAt = now - 220 * DAY,
        )

        val users = listOf(learner, tutorRaka, tutorNadya, tutorDimas, tutorSarah, tutorAhmad, orgUI)
        db.userDao().upsertAll(users)

        db.walletDao().upsertAdmin(OrgAdminEntity("admin-1", DEMO_ORG_UI_ID, "panitia.bem@ui.ac.id", now - 50 * DAY))

        val classStrukdatRaka = ClassEntity(
            id = "cls-strukdat-raka",
            organizerId = DEMO_TUTOR_RAKA_ID,
            title = "Struktur Data",
            description = "Pendampingan intensif mata kuliah Struktur Data. Membahas representasi memori, Tree traversal, Graph algorithms, dan analisis kompleksitas waktu/ruang (Big-O).",
            topicSummary = "Tree, Graph, & Analisis Kompleksitas",
            courseGrade = "A",
            category = Category.TECH,
            programType = ProgramType.STUDY_GROUP,
            format = ClassFormat.OFFLINE,
            tags = listOf("Struktur Data", "Tree", "Graph", "Algoritma"),
            coverStyle = 0,
            university = "Universitas Indonesia",
            location = "Lab Komputer Gedung B, Fasilkom UI",
            meetingLink = "",
            price = 25_000,
            quota = 20,
            hasCertificate = true,
            minAttendancePercent = 75,
            benefits = listOf("Rangkuman Materi PDF", "Latihan Soal & Pembahasan Coding", "Sesi Diskusi Tatap Muka"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 10 * DAY,
            startAt = now + 3 * DAY + 2 * HOUR,
            endAt = now + 14 * DAY,
        )

        val classKalkulusNadya = ClassEntity(
            id = "cls-kalkulus-nadya",
            organizerId = DEMO_TUTOR_NADYA_ID,
            title = "Kalkulus Dasar",
            description = "Pendampingan konsep kalkulus untuk mahasiswa tingkat pertama. Bedah tuntas konsep intuitif limit, turunan implisit, integral tentu, dan aplikasinya.",
            topicSummary = "Limit, Turunan, & Aplikasi Integral",
            courseGrade = "A",
            category = Category.ACADEMIC,
            programType = ProgramType.TUTORING,
            format = ClassFormat.ONLINE,
            tags = listOf("Kalkulus", "Limit", "Turunan", "Integral"),
            coverStyle = 1,
            university = "Institut Teknologi Bandung",
            location = "Google Meet Online",
            meetingLink = "https://meet.google.com/studmate-kalkulus",
            price = 20_000,
            earlyBirdPrice = 15_000,
            earlyBirdUntil = now + 2 * DAY,
            quota = 25,
            hasCertificate = true,
            minAttendancePercent = 75,
            benefits = listOf("Ringkasan Rumus Sakti PDF", "Rekaman Sesi Google Meet", "Review Tugas Mandiri"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 12 * DAY,
            startAt = now + 4 * DAY + 5 * HOUR + 30 * 60 * 1000L,
            endAt = now + 12 * DAY,
        )

        val classStrukdatDimas = ClassEntity(
            id = "cls-strukdat-dimas",
            organizerId = DEMO_TUTOR_DIMAS_ID,
            title = "Struktur Data",
            description = "Kelas pendampingan Struktur Data dengan fokus implementasi kode program, binary search tree, heap, dan graph traversal secara interaktif.",
            topicSummary = "Binary Search Tree & Graph Traversal",
            courseGrade = "A",
            category = Category.TECH,
            programType = ProgramType.STUDY_GROUP,
            format = ClassFormat.ONLINE,
            tags = listOf("Struktur Data", "BST", "Graph Traversal"),
            coverStyle = 2,
            university = "Universitas Brawijaya",
            location = "Zoom Meeting",
            meetingLink = "https://zoom.us/j/dimas-strukdat",
            price = 20_000,
            quota = 30,
            hasCertificate = true,
            minAttendancePercent = 75,
            benefits = listOf("Source Code Lengkap", "Sesi Q&A Live", "E-Sertifikat"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 6 * DAY,
            startAt = now + 5 * DAY + 5 * HOUR,
            endAt = now + 15 * DAY,
        )

        val classBasisDataAhmad = ClassEntity(
            id = "cls-basisdata-ahmad",
            organizerId = DEMO_TUTOR_AHMAD_ID,
            title = "Basis Data",
            description = "Memahami perancangan ERD, normalisasi 1NF hingga BCNF, serta penulisan SQL query kompleks dengan JOIN, subquery, dan indexing.",
            topicSummary = "Normalisasi, Relational Algebra, SQL Query Lanjut",
            courseGrade = "A",
            category = Category.TECH,
            programType = ProgramType.WORKSHOP,
            format = ClassFormat.ONLINE,
            tags = listOf("Basis Data", "SQL", "Normalisasi", "ERD"),
            coverStyle = 3,
            university = "Institut Teknologi Bandung",
            location = "Google Meet",
            meetingLink = "https://meet.google.com/studmate-db",
            price = 30_000,
            quota = 30,
            hasCertificate = true,
            minAttendancePercent = 75,
            benefits = listOf("Cheat Sheet SQL Sakti", "File Schema Praktikum", "Sertifikat"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 15 * DAY,
            startAt = now + 6 * DAY,
            endAt = now + 20 * DAY,
        )

        val classStatistikaSarah = ClassEntity(
            id = "cls-statistika-sarah",
            organizerId = DEMO_TUTOR_SARAH_ID,
            title = "Statistika dan Probabilitas",
            description = "Bimbingan gratis mata kuliah Statistika & Probabilitas. Membahas distribusi probabilitas diskrit/kontinu, uji hipotesis, dan regresi linear sederhana.",
            topicSummary = "Distribusi Probabilitas, Hipotesis, Regresi Linear",
            courseGrade = "A",
            category = Category.ACADEMIC,
            programType = ProgramType.STUDY_GROUP,
            format = ClassFormat.OFFLINE,
            tags = listOf("Statistika", "Probabilitas", "Hipotesis", "Regresi"),
            coverStyle = 4,
            university = "Universitas Gadjah Mada",
            location = "Gd. Kuliah Bersama R.302, Kampus UGM Yogyakarta",
            meetingLink = "",
            price = 0,
            quota = 35,
            hasCertificate = false,
            minAttendancePercent = 50,
            benefits = listOf("Modul Pembahasan Soal PDF", "Bimbingan Tatap Muka Bebas Tanya"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 14 * DAY,
            startAt = now + 1 * DAY + 4 * HOUR,
            endAt = now + 8 * DAY,
        )

        val classAkuntansiDimas = ClassEntity(
            id = "cls-akuntansi-dimas",
            organizerId = DEMO_TUTOR_DIMAS_ID,
            title = "Akuntansi Keuangan Menengah",
            description = "Pendampingan mata kuliah akuntansi keuangan menengah bagi mahasiswa FEB. Membedah siklus pencatatan aset tetap, liabilitas lancar, dan penyusunan laporan arus kas.",
            topicSummary = "Laporan Arus Kas, Aset Tetap, Liabilitas Lancar",
            courseGrade = "A",
            category = Category.BUSINESS,
            programType = ProgramType.STUDY_GROUP,
            format = ClassFormat.ONLINE,
            tags = listOf("Akuntansi", "Arus Kas", "Laporan Keuangan"),
            coverStyle = 5,
            university = "Universitas Brawijaya",
            location = "Google Meet Online",
            meetingLink = "https://meet.google.com/studmate-akuntansi",
            price = 20_000,
            quota = 25,
            hasCertificate = true,
            minAttendancePercent = 75,
            benefits = listOf("Template Excel Laporan Keuangan", "Sesi Pembahasan Kuis"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 8 * DAY,
            startAt = now + 7 * DAY,
            endAt = now + 15 * DAY,
        )

        val classAlgoritmaRaka = ClassEntity(
            id = "cls-algoritma-raka",
            organizerId = DEMO_TUTOR_RAKA_ID,
            title = "Algoritma dan Pemrograman",
            description = "Pendampingan persiapan ujian praktikum pemrograman: rekursi, divide and conquer, sorting algorithms, serta array multidimensi.",
            topicSummary = "Rekursi, Sorting, Array 2D, Analisis Kasus",
            courseGrade = "A",
            category = Category.TECH,
            programType = ProgramType.WORKSHOP,
            format = ClassFormat.OFFLINE,
            tags = listOf("Algoritma", "Pemrograman", "Sorting", "Rekursi"),
            coverStyle = 0,
            university = "Universitas Indonesia",
            location = "Gd. B Lab Komputer Fasilkom UI",
            meetingLink = "",
            price = 25_000,
            quota = 20,
            hasCertificate = true,
            minAttendancePercent = 100,
            benefits = listOf("Bank Soal Pemrograman", "Sertifikat", "Live Coding Review"),
            status = ClassStatus.PUBLISHED,
            createdAt = now - 20 * DAY,
            startAt = now - 10 * DAY,
            endAt = now - 2 * DAY,
        )

        val classes = listOf(
            classStrukdatRaka,
            classKalkulusNadya,
            classStrukdatDimas,
            classBasisDataAhmad,
            classStatistikaSarah,
            classAkuntansiDimas,
            classAlgoritmaRaka,
        )
        db.classDao().upsertAll(classes)

        val sessionsStrukdatRaka = listOf(
            SessionEntity("sr-1", classStrukdatRaka.id, 1, "Pengantar Pointer & Linked List", "Review implementasi memori dinamis dan linked list.", now + 3 * DAY + 2 * HOUR, 90, false),
            SessionEntity("sr-2", classStrukdatRaka.id, 2, "Binary Search Tree & Balancing", "Konsep AVL Tree dan Red-Black Tree.", now + 7 * DAY + 2 * HOUR, 90, false),
            SessionEntity("sr-3", classStrukdatRaka.id, 3, "Graph Traversal: BFS vs DFS", "Aplikasi pencarian jalur terpendek dan deteksi siklus.", now + 11 * DAY + 2 * HOUR, 90, false),
            SessionEntity("sr-4", classStrukdatRaka.id, 4, "Bedah Soal Ujian & Analisis Kompleksitas", "Membahas 10 tipe soal ujian tersulit.", now + 14 * DAY + 2 * HOUR, 120, false),
        )

        val sessionsKalkulusNadya = listOf(
            SessionEntity("sk-1", classKalkulusNadya.id, 1, "Limit Fungsi & Kekontinuan", "Teorema limit, limit tak hingga, dan kontinuitas.", now + 4 * DAY + 5 * HOUR + 30 * 60 * 1000L, 90, false),
            SessionEntity("sk-2", classKalkulusNadya.id, 2, "Turunan & Aturan Rantai", "Laju yang berkaitan dan aproksimasi linear.", now + 8 * DAY + 5 * HOUR + 30 * 60 * 1000L, 90, false),
            SessionEntity("sk-3", classKalkulusNadya.id, 3, "Integral Tentu & Aplikasi Luas Daerah", "Teknik substitusi dan integral parsial.", now + 12 * DAY + 5 * HOUR + 30 * 60 * 1000L, 120, false),
        )

        val sessionsStrukdatDimas = listOf(
            SessionEntity("sd-1", classStrukdatDimas.id, 1, "Binary Search Tree & Operasi Dasar", "Insert, delete, dan searching pada BST.", now + 5 * DAY + 5 * HOUR, 90, false),
            SessionEntity("sd-2", classStrukdatDimas.id, 2, "Graph Representation & Traversal", "Adjacency matrix vs list, BFS dan DFS.", now + 9 * DAY + 5 * HOUR, 90, false),
        )

        val sessionsStatistikaSarah = listOf(
            SessionEntity("ss-1", classStatistikaSarah.id, 1, "Peluang & Variabel Acak", "Distribusi binomial, poisson, dan normal.", now + 1 * DAY + 4 * HOUR, 90, false),
            SessionEntity("ss-2", classStatistikaSarah.id, 2, "Pengujian Hipotesis Satu & Dua Sampel", "Uji t-student dan Z-test.", now + 4 * DAY, 90, false),
        )

        val sessionsAlgoritmaRaka = listOf(
            SessionEntity("sa-1", classAlgoritmaRaka.id, 1, "Rekursi & Divide and Conquer", "Memahami base case dan stack frame.", now - 9 * DAY, 90, true),
            SessionEntity("sa-2", classAlgoritmaRaka.id, 2, "Sorting & Array Multidimensi", "Quicksort, Mergesort, dan manipulasi matriks.", now - 3 * DAY, 90, true),
        )

        db.classDao().upsertSessions(sessionsStrukdatRaka + sessionsKalkulusNadya + sessionsStrukdatDimas + sessionsStatistikaSarah + sessionsAlgoritmaRaka)

        val enrollStrukdat = EnrollmentEntity(
            id = "enr-strukdat-rian",
            classId = classStrukdatRaka.id,
            userId = DEMO_LEARNER_ID,
            status = EnrollmentStatus.ACTIVE,
            amount = 25_000,
            paymentMethod = PaymentMethod.QRIS.name,
            paymentStatus = PaymentStatus.HELD,
            paymentRef = "PAY-QRIS-99281A",
            ticketCode = "SM-STRUK01",
            createdAt = now - 3 * DAY,
            paidAt = now - 3 * DAY,
        )

        val enrollKalkulus = EnrollmentEntity(
            id = "enr-kalkulus-rian",
            classId = classKalkulusNadya.id,
            userId = DEMO_LEARNER_ID,
            status = EnrollmentStatus.ACTIVE,
            amount = 15_000,
            paymentMethod = PaymentMethod.GOPAY.name,
            paymentStatus = PaymentStatus.HELD,
            paymentRef = "PAY-GOPAY-55123",
            ticketCode = "SM-KLK088",
            createdAt = now - 2 * DAY,
            paidAt = now - 2 * DAY,
        )

        val enrollAlgoritma = EnrollmentEntity(
            id = "enr-algoritma-rian",
            classId = classAlgoritmaRaka.id,
            userId = DEMO_LEARNER_ID,
            status = EnrollmentStatus.COMPLETED,
            amount = 25_000,
            paymentMethod = PaymentMethod.QRIS.name,
            paymentStatus = PaymentStatus.RELEASED,
            paymentRef = "PAY-QRIS-11204",
            ticketCode = "SM-ALGO77",
            createdAt = now - 15 * DAY,
            paidAt = now - 15 * DAY,
            certificateId = "SMC-UI2026ALGO",
            certificateIssuedAt = now - 2 * DAY,
        )

        val dummyEnrollments = mutableListOf(enrollStrukdat, enrollKalkulus, enrollAlgoritma)
        for (i in 1..16) {
            dummyEnrollments.add(
                EnrollmentEntity(
                    id = "enr-dummy-sd-$i",
                    classId = classStrukdatRaka.id,
                    userId = "dummy-user-$i",
                    status = EnrollmentStatus.ACTIVE,
                    amount = 25_000,
                    paymentStatus = PaymentStatus.HELD,
                    ticketCode = "SM-DUMMY-$i",
                )
            )
        }
        for (i in 1..19) {
            dummyEnrollments.add(
                EnrollmentEntity(
                    id = "enr-dummy-klk-$i",
                    classId = classKalkulusNadya.id,
                    userId = "dummy-user-k-$i",
                    status = EnrollmentStatus.ACTIVE,
                    amount = 20_000,
                    paymentStatus = PaymentStatus.HELD,
                    ticketCode = "SM-DKLK-$i",
                )
            )
        }
        for (i in 1..22) {
            dummyEnrollments.add(
                EnrollmentEntity(
                    id = "enr-dummy-dim-$i",
                    classId = classStrukdatDimas.id,
                    userId = "dummy-user-d-$i",
                    status = EnrollmentStatus.ACTIVE,
                    amount = 20_000,
                    paymentStatus = PaymentStatus.HELD,
                    ticketCode = "SM-DDIM-$i",
                )
            )
        }
        db.enrollmentDao().upsertAll(dummyEnrollments)

        db.enrollmentDao().insertAttendanceAll(
            listOf(
                AttendanceEntity(enrollAlgoritma.id, "sa-1", now - 9 * DAY, "QR"),
                AttendanceEntity(enrollAlgoritma.id, "sa-2", now - 3 * DAY, "QR"),
            )
        )

        val reviews = mutableListOf(
            ReviewEntity("rev-1", classAlgoritmaRaka.id, DEMO_LEARNER_ID, 5, "Penjelasan Kak Raka sangat sistematis, rekursi yang awalnya membingungkan jadi sangat masuk akal!", now - 2 * DAY),
            ReviewEntity("rev-2", classStrukdatRaka.id, "dummy-user-1", 5, "Asisten dosen terbaik! Sangat sabar membedah struktur tree dan simulasi trace code.", now - 4 * DAY),
            ReviewEntity("rev-3", classKalkulusNadya.id, "dummy-user-k-1", 5, "Kak Nadya jago banget visualisasi grafik fungsi dan trik cepat integral substitusi.", now - 3 * DAY),
            ReviewEntity("rev-4", classStrukdatDimas.id, "dummy-user-d-1", 5, "Penjelasan BST dan graph traversal Kak Dimas sangat detail dan runtut.", now - 5 * DAY),
        )
        for (i in 2..24) {
            reviews.add(ReviewEntity("rev-sd-$i", classStrukdatRaka.id, "dummy-user-$i", if (i % 5 == 0) 4 else 5, "Materinya sangat relevan dengan soal UTS/UAS kampus.", now - (i * DAY / 2)))
        }
        for (i in 2..19) {
            reviews.add(ReviewEntity("rev-klk-$i", classKalkulusNadya.id, "dummy-user-k-$i", if (i % 4 == 0) 4 else 5, "Bagus sekali penyampaiannya, sangat terbantu untuk kuis mingguan.", now - (i * DAY / 2)))
        }
        for (i in 2..12) {
            reviews.add(ReviewEntity("rev-dim-$i", classStrukdatDimas.id, "dummy-user-d-$i", if (i % 3 == 0) 4 else 5, "Bagus dan mudah dipahami kodingannya.", now - (i * DAY / 2)))
        }
        db.communityDao().upsertReviews(reviews)

        val materials = listOf(
            MaterialEntity("mat-1", classStrukdatRaka.id, "Slide Modul 1: Pointer & Memory Allocation", MaterialType.SLIDE, "https://docs.google.com/presentation/studmate-strukdat-1", now - 2 * DAY),
            MaterialEntity("mat-2", classStrukdatRaka.id, "Rangkuman Pohon Biner & AVL Tree PDF", MaterialType.PDF, "https://studmate.app/docs/avl-tree-cheatsheet.pdf", now - 1 * DAY),
            MaterialEntity("mat-3", classKalkulusNadya.id, "Rumus Sakti Limit & Turunan Fungsi", MaterialType.PDF, "https://studmate.app/docs/kalkulus-rumus.pdf", now - 3 * DAY),
            MaterialEntity("mat-4", classAlgoritmaRaka.id, "Bank Soal Ujian Algoritma & Pembahasan", MaterialType.PDF, "https://studmate.app/docs/algo-bank-soal.pdf", now - 8 * DAY),
        )
        materials.forEach { db.communityDao().upsertMaterial(it) }

        val posts = listOf(
            ForumPostEntity("post-1", classStrukdatRaka.id, DEMO_LEARNER_ID, null, "Kak Raka, apakah di sesi 2 nanti akan dibahas perbedaan Red-Black Tree dengan AVL Tree?", now - 2 * DAY),
            ForumPostEntity("post-2", classStrukdatRaka.id, DEMO_TUTOR_RAKA_ID, "post-1", "Halo Rian! Betul, kita akan bahas trade-off rotasi AVL vs Red-Black Tree ya.", now - 1 * DAY),
        )
        db.communityDao().upsertPosts(posts)

        val announcements = listOf(
            AnnouncementEntity("ann-1", classStrukdatRaka.id, "Ruangan Praktikum Sesi 1", "Sesi pertama hari Rabu akan diadakan di Lab Komputer Gedung B Ruang 204. Harap bawa laptop masing-masing.", now - 1 * DAY),
            AnnouncementEntity("ann-2", classAlgoritmaRaka.id, "Sertifikat Kelulusan Siap Diunduh", "Selamat untuk seluruh peserta yang menyelesaikan sesi! E-Sertifikat resmi sudah terbit di tab Kelasku.", now - 2 * DAY),
        )
        announcements.forEach { db.communityDao().upsertAnnouncement(it) }

        val notifs = listOf(
            com.example.studmate.data.local.NotificationEntity("notif-1", DEMO_LEARNER_ID, "Pendaftaran Berhasil 🎉", "Kamu terdaftar di mata kuliah Struktur Data bersama Raka Pratama. Pembayaran Rp 25.000 diamankan escrow StudMate.", classStrukdatRaka.id, "ENROLL", now - 3 * DAY, isRead = false),
            com.example.studmate.data.local.NotificationEntity("notif-2", DEMO_LEARNER_ID, "Pengingat Jadwal Kelas ⏰", "Kelas Struktur Data dimulai dalam 2 hari (Rab, 18 Nov • 16.00 WIB) di Lab Komputer Fasilkom UI.", classStrukdatRaka.id, "REMINDER", now - 1 * DAY, isRead = false),
            com.example.studmate.data.local.NotificationEntity("notif-3", DEMO_LEARNER_ID, "Sertifikat Diterbitkan 🎓", "Selamat! Kehadiranmu 100% di Algoritma dan Pemrograman. E-Sertifikat digitalmu telah diterbitkan.", classAlgoritmaRaka.id, "CERTIFICATE", now - 2 * DAY, isRead = true),
        )
        db.communityDao().upsertNotifications(notifs)

        db.walletDao().upsertPayout(
            PayoutEntity("pay-1", DEMO_TUTOR_RAKA_ID, 250_000, "BCA", "0128391823", "SUCCESS", now - 10 * DAY)
        )
    }
}
