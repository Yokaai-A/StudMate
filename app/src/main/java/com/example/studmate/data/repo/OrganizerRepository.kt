package com.example.studmate.data.repo

import androidx.room.withTransaction
import com.example.studmate.data.local.AnnouncementEntity
import com.example.studmate.data.local.AppDatabase
import com.example.studmate.data.local.AttendanceEntity
import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.EnrollmentEntity
import com.example.studmate.data.local.MaterialEntity
import com.example.studmate.data.local.OrgAdminEntity
import com.example.studmate.data.local.PayoutEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.domain.AppConfig
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.ClassSummary
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.MaterialType
import com.example.studmate.domain.PaymentStatus
import com.example.studmate.util.QrUtils
import com.example.studmate.util.newId
import com.example.studmate.util.priceLabel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class AttendeeItem(
    val enrollment: EnrollmentEntity,
    val user: UserEntity?,
    val attendedSessionIds: Set<String>,
)

data class WalletSummary(
    val grossTotal: Long,
    val escrowHeld: Long,
    val released: Long,
    val paidOut: Long,
    val availableToWithdraw: Long,
    val platformFee: Long,
)

class OrganizerRepository(
    private val db: AppDatabase,
    private val catalog: CatalogRepository,
    private val notifier: Notifier,
) {
    private val classes = db.classDao()
    private val enrollments = db.enrollmentDao()
    private val community = db.communityDao()
    private val users = db.userDao()
    private val wallet = db.walletDao()


    fun observeMyClasses(organizerId: String): Flow<List<ClassSummary>> =
        catalog.allSummaries.combine(wallet.observeManagedOrgIds(organizerId)) { list, managedIds ->
            val allIds = setOf(organizerId) + managedIds.toSet()
            list.filter { it.cls.organizerId in allIds }.sortedByDescending { it.cls.createdAt }
        }

    fun observeClassAttendees(classId: String): Flow<List<AttendeeItem>> = combine(
        enrollments.observeByClass(classId),
        users.observeAll(),
        enrollments.observeClassAttendance(classId),
    ) { ens, us, atts ->
        val userMap = us.associateBy { it.id }
        val attByEnrollment = atts.groupBy { it.enrollmentId }
            .mapValues { (_, v) -> v.map { it.sessionId }.toSet() }
        ens.filter { it.status != EnrollmentStatus.CANCELLED }.map { e ->
            AttendeeItem(
                enrollment = e,
                user = userMap[e.userId],
                attendedSessionIds = attByEnrollment[e.id].orEmpty(),
            )
        }
    }


    suspend fun checkInByTicket(
        classId: String,
        sessionId: String,
        rawTicket: String,
    ): Result<Pair<UserEntity?, EnrollmentEntity>> = runCatching {
        val code = QrUtils.parseTicket(rawTicket)
        val e = enrollments.byTicket(code) ?: error("Tiket '$code' tidak ditemukan")
        require(e.classId == classId) { "Tiket ini untuk kelas lain" }
        require(e.status == EnrollmentStatus.ACTIVE || e.status == EnrollmentStatus.COMPLETED) {
            "Status tiket ${e.status.name} (belum aktif)"
        }
        val currentAtt = enrollments.attendance(e.id)
        if (currentAtt.any { it.sessionId == sessionId }) {
            val user = users.get(e.userId)
            return@runCatching Pair(user, e)
        }
        enrollments.insertAttendance(
            AttendanceEntity(
                enrollmentId = e.id,
                sessionId = sessionId,
                checkedInAt = System.currentTimeMillis(),
                method = "QR",
            )
        )
        val user = users.get(e.userId)
        val cls = classes.get(classId)
        val sess = classes.session(sessionId)
        notifier.notify(
            listOf(e.userId),
            "Presensi Berhasil ✅",
            "Kehadiranmu di ${sess?.title ?: "sesi"} (${cls?.title}) telah dicatat.",
            classId = classId,
            type = "ATTENDANCE",
            deepLink = "studmate://hub/$classId",
        )
        Pair(user, e)
    }


    suspend fun toggleAttendance(enrollmentId: String, sessionId: String, attended: Boolean) {
        if (attended) {
            enrollments.insertAttendance(
                AttendanceEntity(
                    enrollmentId = enrollmentId,
                    sessionId = sessionId,
                    checkedInAt = System.currentTimeMillis(),
                    method = "MANUAL",
                )
            )
        } else {
            enrollments.deleteAttendance(enrollmentId, sessionId)
        }
    }


    suspend fun saveClass(
        cls: ClassEntity,
        sessions: List<SessionEntity>,
        publish: Boolean,
    ): Result<String> = runCatching {
        require(cls.title.isNotBlank()) { "Judul kelas tidak boleh kosong" }
        require(cls.description.isNotBlank()) { "Deskripsi kelas tidak boleh kosong" }
        require(sessions.isNotEmpty()) { "Minimal sediakan 1 pertemuan/sesi" }

        val startAt = sessions.minOf { it.startAt }
        val endAt = sessions.maxOf { it.startAt + it.durationMinutes * 60_000L }

        val toSave = cls.copy(
            status = if (publish) ClassStatus.PUBLISHED else ClassStatus.DRAFT,
            startAt = startAt,
            endAt = endAt,
        )

        db.withTransaction {
            classes.upsert(toSave)
            classes.deleteSessions(toSave.id)
            val updatedSessions = sessions.mapIndexed { idx, s ->
                s.copy(
                    id = if (s.id.isBlank()) newId() else s.id,
                    classId = toSave.id,
                    orderIndex = idx + 1,
                )
            }
            classes.upsertSessions(updatedSessions)
        }
        toSave.id
    }

    suspend fun deleteClass(classId: String) = db.withTransaction {
        val enrolled = enrollments.seatsTaken(classId)
        require(enrolled == 0) { "Tidak dapat menghapus kelas yang sudah memiliki peserta terdaftar" }
        classes.deleteSessions(classId)
        classes.delete(classId)
    }


    suspend fun setSessionDone(classId: String, sessionId: String, done: Boolean) = db.withTransaction {
        classes.setSessionDone(sessionId, done)
        val allSessions = classes.sessions(classId)
        val allDone = allSessions.isNotEmpty() && allSessions.all { it.isDone }
        if (allDone) {
            val cls = classes.get(classId)
            if (cls != null && cls.status == ClassStatus.PUBLISHED) {
                classes.upsert(cls.copy(status = ClassStatus.COMPLETED))
            }
            val ens = enrollments.byClass(classId)
            ens.forEach { e ->
                if (e.paymentStatus == PaymentStatus.HELD) {
                    enrollments.upsert(
                        e.copy(
                            paymentStatus = PaymentStatus.RELEASED,
                            status = EnrollmentStatus.COMPLETED,
                        )
                    )
                } else if (e.status == EnrollmentStatus.ACTIVE) {
                    enrollments.upsert(e.copy(status = EnrollmentStatus.COMPLETED))
                }
            }
            if (cls != null) {
                notifier.notify(
                    listOf(cls.organizerId),
                    "Kelas Selesai & Dana Dicairkan 💰",
                    "Semua sesi ${cls.title} telah selesai. Dana escrow telah diteruskan ke saldo dompetmu.",
                    classId = classId,
                    type = "PAYOUT",
                )
            }
        }
    }


    suspend fun broadcastAnnouncement(
        classId: String,
        title: String,
        message: String,
    ): Result<Unit> = runCatching {
        require(title.isNotBlank()) { "Judul pengumuman wajib diisi" }
        require(message.isNotBlank()) { "Pesan pengumuman wajib diisi" }
        val ann = AnnouncementEntity(
            id = newId(),
            classId = classId,
            title = title.trim(),
            message = message.trim(),
        )
        community.upsertAnnouncement(ann)
        val ens = enrollments.byClass(classId)
        val attendeeUserIds = ens.filter { it.status == EnrollmentStatus.ACTIVE || it.status == EnrollmentStatus.COMPLETED }
            .map { it.userId }
        val cls = classes.get(classId)
        notifier.notify(
            userIds = attendeeUserIds,
            title = "📢 ${cls?.title ?: "Kelas"}: $title",
            body = message,
            classId = classId,
            type = "ANNOUNCEMENT",
            deepLink = "studmate://hub/$classId",
        )
    }

    suspend fun addMaterial(classId: String, title: String, type: MaterialType, uri: String): Result<Unit> = runCatching {
        require(title.isNotBlank()) { "Judul materi wajib diisi" }
        require(uri.isNotBlank()) { "Tautan/file materi wajib diisi" }
        val m = MaterialEntity(
            id = newId(),
            classId = classId,
            title = title.trim(),
            type = type,
            uri = uri.trim(),
        )
        community.upsertMaterial(m)
        val ens = enrollments.byClass(classId)
        val attendeeUserIds = ens.map { it.userId }
        val cls = classes.get(classId)
        notifier.notify(
            userIds = attendeeUserIds,
            title = "Materi baru diunggah 📄",
            body = "$title telah ditambahkan pada kelas ${cls?.title ?: ""}",
            classId = classId,
            type = "MATERIAL",
            deepLink = "studmate://hub/$classId",
        )
    }

    suspend fun deleteMaterial(id: String) = community.deleteMaterial(id)


    fun observeWallet(organizerId: String): Flow<WalletSummary> = combine(
        enrollments.observeAll(),
        classes.observeAll(),
        wallet.observePayouts(organizerId),
    ) { allEns, allCls, payouts ->
        val orgClassIds = allCls.filter { it.organizerId == organizerId }.map { it.id }.toSet()
        val orgEns = allEns.filter { it.classId in orgClassIds }

        var grossTotal = 0L
        var escrowHeld = 0L
        var released = 0L

        orgEns.forEach { e ->
            when (e.paymentStatus) {
                PaymentStatus.HELD -> {
                    escrowHeld += e.amount
                    grossTotal += e.amount
                }
                PaymentStatus.RELEASED -> {
                    released += e.amount
                    grossTotal += e.amount
                }
                PaymentStatus.FREE, PaymentStatus.UNPAID, PaymentStatus.REFUNDED -> {}
            }
        }

        val platformFee = (grossTotal * AppConfig.PLATFORM_FEE_PERCENT) / 100
        val totalWithdrawn = payouts.filter { it.status == "SUCCESS" }.sumOf { it.amount }
        val netReleased = (released * (100 - AppConfig.PLATFORM_FEE_PERCENT)) / 100
        val available = (netReleased - totalWithdrawn).coerceAtLeast(0L)

        WalletSummary(
            grossTotal = grossTotal,
            escrowHeld = escrowHeld,
            released = released,
            paidOut = totalWithdrawn,
            availableToWithdraw = available,
            platformFee = platformFee,
        )
    }

    suspend fun requestPayout(
        organizerId: String,
        amount: Long,
        destination: String,
        accountNumber: String,
    ): Result<PayoutEntity> = runCatching {
        require(amount >= 20_000) { "Minimal penarikan saldo adalah Rp20.000" }
        require(destination.isNotBlank()) { "Pilih bank atau e-wallet tujuan" }
        require(accountNumber.isNotBlank()) { "Nomor rekening/e-wallet wajib diisi" }

        val walletState = observeWallet(organizerId).first()
        require(amount <= walletState.availableToWithdraw) {
            "Saldo tidak mencukupi (Tersedia: ${priceLabel(walletState.availableToWithdraw)})"
        }

        val payout = PayoutEntity(
            id = newId(),
            organizerId = organizerId,
            amount = amount,
            destination = destination.trim(),
            accountNumber = accountNumber.trim(),
            status = "SUCCESS",
            createdAt = System.currentTimeMillis(),
        )
        wallet.upsertPayout(payout)
        notifier.notify(
            listOf(organizerId),
            "Pencairan Dana Diproses 💸",
            "Penarikan dana sebesar ${priceLabel(amount)} ke $destination ($accountNumber) telah berhasil dikirim.",
            type = "PAYOUT",
        )
        payout
    }

    fun observePayouts(organizerId: String): Flow<List<PayoutEntity>> = wallet.observePayouts(organizerId)


    fun observeOrgAdmins(organizerId: String): Flow<List<OrgAdminEntity>> = wallet.observeAdmins(organizerId)

    suspend fun addOrgAdmin(organizerId: String, email: String): Result<Unit> = runCatching {
        val mail = email.trim().lowercase()
        require(AuthRepository.EMAIL_REGEX.matches(mail)) { "Format email tidak valid" }
        wallet.upsertAdmin(OrgAdminEntity(newId(), organizerId, mail))
    }

    suspend fun removeOrgAdmin(adminId: String) = wallet.deleteAdmin(adminId)
}
