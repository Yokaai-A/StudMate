package com.example.studmate.data.repo

import androidx.room.withTransaction
import com.example.studmate.data.local.AppDatabase
import com.example.studmate.data.local.AttendanceEntity
import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.EnrollmentEntity
import com.example.studmate.data.local.ForumPostEntity
import com.example.studmate.data.local.ReviewEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.LearningItem
import com.example.studmate.domain.PaymentMethod
import com.example.studmate.domain.PaymentStatus
import com.example.studmate.notif.ReminderScheduler
import com.example.studmate.util.newCode
import com.example.studmate.util.newId
import com.example.studmate.util.priceLabel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class PostItem(val post: ForumPostEntity, val user: UserEntity?, val isOrganizer: Boolean)

class LearningRepository(
    private val db: AppDatabase,
    private val catalog: CatalogRepository,
    private val notifier: Notifier,
    private val reminders: ReminderScheduler,
) {
    private val classes = db.classDao()
    private val enrollments = db.enrollmentDao()
    private val community = db.communityDao()
    private val users = db.userDao()
    private val wallet = db.walletDao()

    fun enrollmentFor(classId: String, userId: String): Flow<EnrollmentEntity?> = enrollments.observeFor(classId, userId)
    fun enrollment(id: String): Flow<EnrollmentEntity?> = enrollments.observe(id)
    fun attendance(enrollmentId: String): Flow<List<AttendanceEntity>> = enrollments.observeAttendance(enrollmentId)

    fun myLearning(userId: String): Flow<List<LearningItem>> = combine(
        enrollments.observeByUser(userId), catalog.allSummaries, catalog.allSessions, enrollments.observeAttendanceCounts(),
    ) { ens, sums, sessions, att ->
        val sumMap = sums.associateBy { it.cls.id }
        val now = System.currentTimeMillis()
        val bySession = sessions.groupBy { it.classId }
        val attMap = att.associate { it.enrollmentId to it.cnt }
        ens.filter { it.status != EnrollmentStatus.CANCELLED }.mapNotNull { e ->
            val s = sumMap[e.classId] ?: return@mapNotNull null
            val next = bySession[e.classId].orEmpty().filter { !it.isDone && it.startAt + it.durationMinutes * 60_000L > now }
                .minOfOrNull { it.startAt }
            LearningItem(e, s, next, attMap[e.id] ?: 0)
        }
    }

    /** 1-Tap Enrollment: data peserta diambil langsung dari profil. */
    suspend fun enroll(classId: String, user: UserEntity): Result<EnrollmentEntity> = runCatching {
        db.withTransaction {
            val cls = classes.get(classId) ?: error("Kelas tidak ditemukan")
            require(cls.status == ClassStatus.PUBLISHED) { "Pendaftaran kelas ini sudah ditutup" }
            require(cls.organizerId != user.id && cls.organizerId !in wallet.managedOrgIds(user.email)) {
                "Kamu adalah penyelenggara kelas ini"
            }
            val existing = enrollments.getFor(classId, user.id)
            if (existing != null && existing.status != EnrollmentStatus.CANCELLED) return@withTransaction existing
            require(enrollments.seatsTaken(classId) < cls.quota) { "Kuota kelas sudah penuh" }
            val now = System.currentTimeMillis()
            val price = cls.priceAt(now)
            val e = EnrollmentEntity(
                id = existing?.id ?: newId(),
                classId = classId, userId = user.id,
                status = if (price <= 0) EnrollmentStatus.ACTIVE else EnrollmentStatus.PENDING_PAYMENT,
                amount = price,
                paymentStatus = if (price <= 0) PaymentStatus.FREE else PaymentStatus.UNPAID,
                ticketCode = existing?.ticketCode ?: newCode("SM-"),
                createdAt = now,
            )
            enrollments.upsert(e)
            e
        }.also { e ->
            if (e.status == EnrollmentStatus.ACTIVE && e.paymentStatus == PaymentStatus.FREE) onActivated(e)
        }
    }

    /** Konfirmasi pembayaran dari payment gateway → dana masuk escrow (HELD). */
    suspend fun confirmPayment(enrollmentId: String, method: PaymentMethod): Result<EnrollmentEntity> = runCatching {
        val e = enrollments.get(enrollmentId) ?: error("Pendaftaran tidak ditemukan")
        if (e.status != EnrollmentStatus.PENDING_PAYMENT) return@runCatching e
        val paid = e.copy(
            status = EnrollmentStatus.ACTIVE,
            paymentStatus = PaymentStatus.HELD,
            paymentMethod = method.name,
            paymentRef = newCode("PAY-", 10),
            paidAt = System.currentTimeMillis(),
        )
        enrollments.upsert(paid)
        onActivated(paid)
        paid
    }

    private suspend fun onActivated(e: EnrollmentEntity) {
        val cls = classes.get(e.classId) ?: return
        val user = users.get(e.userId)
        reminders.schedule(e.id, e.userId, cls.id, cls.title, classes.sessions(cls.id))
        val paymentNote = if (e.paymentStatus == PaymentStatus.HELD)
            " Pembayaran ${priceLabel(e.amount)} diamankan escrow StudMate hingga kelas berjalan." else ""
        notifier.notify(listOf(e.userId), "Pendaftaran berhasil 🎉", "Kamu terdaftar di ${cls.title}.$paymentNote", cls.id, "ENROLL", "studmate://hub/${cls.id}")
        notifier.notify(listOf(cls.organizerId), "Peserta baru", "${user?.name ?: "Peserta"} mendaftar di ${cls.title}.", cls.id, "ENROLL", "studmate://manage/${cls.id}")
    }

    suspend fun cancelEnrollment(enrollmentId: String): Result<Unit> = runCatching {
        val e = enrollments.get(enrollmentId) ?: error("Pendaftaran tidak ditemukan")
        val sessions = classes.sessions(e.classId)
        require(sessions.none { it.isDone }) { "Kelas sudah berjalan, pendaftaran tidak dapat dibatalkan" }
        val refunded = e.paymentStatus == PaymentStatus.HELD
        enrollments.upsert(
            e.copy(
                status = EnrollmentStatus.CANCELLED,
                paymentStatus = if (refunded) PaymentStatus.REFUNDED else if (e.paymentStatus == PaymentStatus.UNPAID) PaymentStatus.UNPAID else e.paymentStatus,
            )
        )
        reminders.cancel(e.id)
        val cls = classes.get(e.classId)
        if (refunded) notifier.notify(listOf(e.userId), "Dana dikembalikan", "Dana ${priceLabel(e.amount)} untuk ${cls?.title} dikembalikan dari escrow.", e.classId, "PAYMENT")
    }

    /** Hanya peserta yang tercatat hadir minimal 1 sesi yang boleh memberi ulasan. */
    suspend fun submitReview(classId: String, userId: String, rating: Int, comment: String): Result<Unit> = runCatching {
        val e = enrollments.getFor(classId, userId) ?: error("Kamu belum terdaftar di kelas ini")
        require(enrollments.attendance(e.id).isNotEmpty()) { "Ulasan hanya untuk peserta yang terverifikasi hadir" }
        require(rating in 1..5) { "Pilih rating 1–5 bintang" }
        community.upsertReview(ReviewEntity(id = "$classId-$userId", classId = classId, userId = userId, rating = rating, comment = comment.trim()))
        val cls = classes.get(classId)
        if (cls != null) notifier.notify(listOf(cls.organizerId), "Ulasan baru ⭐ $rating", "\"${comment.take(80)}\" — ${cls.title}", classId, "REVIEW")
    }

    data class CertificateEligibility(val eligible: Boolean, val reason: String, val attendancePercent: Int)

    fun eligibility(cls: ClassEntity, totalSessions: Int, doneSessions: Int, attended: Int): CertificateEligibility {
        val pct = if (totalSessions == 0) 0 else attended * 100 / totalSessions
        return when {
            !cls.hasCertificate -> CertificateEligibility(false, "Kelas ini tidak menyediakan sertifikat", pct)
            doneSessions < totalSessions -> CertificateEligibility(false, "Sertifikat terbit setelah semua sesi selesai ($doneSessions/$totalSessions)", pct)
            pct < cls.minAttendancePercent -> CertificateEligibility(false, "Kehadiran $pct% belum memenuhi minimal ${cls.minAttendancePercent}%", pct)
            else -> CertificateEligibility(true, "Kehadiran $pct% memenuhi syarat", pct)
        }
    }

    suspend fun claimCertificate(enrollmentId: String): Result<EnrollmentEntity> = runCatching {
        val e = enrollments.get(enrollmentId) ?: error("Pendaftaran tidak ditemukan")
        if (e.certificateId != null) return@runCatching e
        val cls = classes.get(e.classId) ?: error("Kelas tidak ditemukan")
        val sessions = classes.sessions(cls.id)
        val el = eligibility(cls, sessions.size, sessions.count { it.isDone }, enrollments.attendance(e.id).size)
        require(el.eligible) { el.reason }
        val issued = e.copy(certificateId = newCode("SMC-", 10), certificateIssuedAt = System.currentTimeMillis())
        enrollments.upsert(issued)
        issued
    }

    fun materials(classId: String) = community.observeMaterials(classId)
    fun announcements(classId: String) = community.observeAnnouncements(classId)

    fun posts(classId: String): Flow<List<PostItem>> = combine(
        community.observePosts(classId), users.observeAll(), classes.observe(classId),
    ) { posts, us, cls ->
        val m = us.associateBy { it.id }
        posts.map { PostItem(it, m[it.userId], it.userId == cls?.organizerId) }
    }

    suspend fun addPost(classId: String, userId: String, content: String, parentId: String?): Result<Unit> = runCatching {
        require(content.isNotBlank()) { "Tulis pertanyaan atau balasanmu" }
        val post = ForumPostEntity(newId(), classId, userId, parentId, content.trim())
        community.upsertPost(post)
        val cls = classes.get(classId) ?: return@runCatching
        val author = users.get(userId)?.name ?: "Seseorang"
        if (parentId != null) {
            val parentAuthor = community.postAuthor(parentId)
            if (parentAuthor != null && parentAuthor != userId) {
                notifier.notify(listOf(parentAuthor), "Balasan diskusi", "$author: ${content.take(80)}", classId, "FORUM", "studmate://hub/$classId")
            }
        } else if (userId != cls.organizerId) {
            notifier.notify(listOf(cls.organizerId), "Pertanyaan baru di ${cls.title}", "$author: ${content.take(80)}", classId, "FORUM", "studmate://manage/$classId")
        }
    }

    fun notifications(userId: String) = community.observeNotifications(userId)
    fun unreadCount(userId: String) = community.observeUnread(userId)
    suspend fun markAllRead(userId: String) = community.markAllRead(userId)
}
