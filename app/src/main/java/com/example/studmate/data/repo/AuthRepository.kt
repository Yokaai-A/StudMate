package com.example.studmate.data.repo

import android.util.Base64
import com.example.studmate.data.local.UserDao
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.domain.VerificationStatus
import com.example.studmate.util.newId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AuthRepository(
    private val users: UserDao,
    private val session: SessionStore,
    private val notifier: Notifier,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<UserEntity?> = session.currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null) else users.observe(id)
    }

    private val pendingOtp = mutableMapOf<String, String>()

    suspend fun register(name: String, email: String, password: String): Result<UserEntity> = runCatching {
        val mail = email.trim().lowercase()
        require(name.isNotBlank()) { "Nama wajib diisi" }
        require(EMAIL_REGEX.matches(mail)) { "Format email tidak valid" }
        require(password.length >= 6) { "Kata sandi minimal 6 karakter" }
        require(users.byEmail(mail) == null) { "Email sudah terdaftar, silakan masuk" }
        val user = UserEntity(id = newId(), name = name.trim(), email = mail, passwordHash = hash(password), authProvider = "EMAIL")
        users.upsert(user)
        session.setCurrentUser(user.id)
        user
    }

    suspend fun login(email: String, password: String): Result<UserEntity> = runCatching {
        val user = users.byEmail(email.trim()) ?: error("Akun tidak ditemukan")
        val stored = user.passwordHash ?: error("Akun ini terdaftar via Google. Gunakan tombol Google.")
        require(verify(password, stored)) { "Kata sandi salah" }
        session.setCurrentUser(user.id)
        user
    }

    /** Dipanggil setelah Google ID token diterima dari Credential Manager. */
    suspend fun signInWithGoogle(email: String, name: String, photoUrl: String?): Result<UserEntity> = runCatching {
        val mail = email.trim().lowercase()
        val existing = users.byEmail(mail)
        val user = existing ?: UserEntity(
            id = newId(), name = name.ifBlank { mail.substringBefore('@') }, email = mail,
            authProvider = "GOOGLE", avatarUri = photoUrl,
        ).also { users.upsert(it) }
        session.setCurrentUser(user.id)
        user
    }

    suspend fun logout() = session.setCurrentUser(null)

    suspend fun completeOnboarding(
        userId: String,
        university: String,
        faculty: String = "",
        major: String,
        semester: Int = 4,
        interests: List<String>,
    ) {
        val u = users.get(userId) ?: return
        users.upsert(
            u.copy(
                university = university,
                faculty = faculty,
                major = major,
                semester = semester,
                interests = interests,
                onboarded = true,
            )
        )
    }

    suspend fun update(userId: String, transform: (UserEntity) -> UserEntity) {
        val u = users.get(userId) ?: return
        users.upsert(transform(u))
    }

    /* -------- Campus Verification -------- */

    /** Mengirim kode OTP ke email kampus (.ac.id). Mode lokal: kode dikirim lewat notifikasi. */
    suspend fun requestCampusEmailCode(userId: String, campusEmail: String): Result<Unit> = runCatching {
        val mail = campusEmail.trim().lowercase()
        require(EMAIL_REGEX.matches(mail)) { "Format email tidak valid" }
        require(mail.endsWith(".ac.id")) { "Gunakan email resmi kampus berakhiran .ac.id" }
        val code = (100000..999999).random().toString()
        pendingOtp[userId] = "$mail|$code"
        notifier.notify(listOf(userId), "Kode verifikasi StudMate", "Kode verifikasi email kampus $mail: $code (berlaku 10 menit)", type = "OTP", deepLink = "studmate://verify")
    }

    suspend fun confirmCampusEmail(userId: String, code: String): Result<Unit> = runCatching {
        val pending = pendingOtp[userId] ?: error("Minta kode terlebih dahulu")
        val (mail, expected) = pending.split("|")
        require(code.trim() == expected) { "Kode verifikasi salah" }
        pendingOtp.remove(userId)
        update(userId) {
            it.copy(verificationStatus = VerificationStatus.VERIFIED, verificationMethod = "CAMPUS_EMAIL", campusEmail = mail)
        }
        notifier.notify(listOf(userId), "Akun terverifikasi ✅", "Email kampus $mail berhasil diverifikasi. Badge Verified Campus aktif.", type = "VERIFY")
    }

    /**
     * Unggah foto KTM. Pada versi produksi foto ditinjau tim moderasi; versi lokal
     * langsung menyetujui setelah dokumen tersimpan.
     */
    suspend fun submitKtm(userId: String, uri: String) {
        update(userId) { it.copy(verificationStatus = VerificationStatus.PENDING, verificationMethod = "KTM", ktmUri = uri) }
        update(userId) { it.copy(verificationStatus = VerificationStatus.VERIFIED) }
        notifier.notify(listOf(userId), "KTM disetujui ✅", "Kartu Tanda Mahasiswa kamu telah ditinjau. Badge Verified Campus aktif.", type = "VERIFY")
    }

    private fun hash(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return b64(salt) + ":" + b64(pbkdf2(password, salt))
    }

    private fun verify(password: String, stored: String): Boolean {
        val parts = stored.split(":")
        if (parts.size != 2) return false
        val salt = Base64.decode(parts[0], Base64.NO_WRAP)
        return java.security.MessageDigest.isEqual(Base64.decode(parts[1], Base64.NO_WRAP), pbkdf2(password, salt))
    }

    private fun pbkdf2(password: String, salt: ByteArray): ByteArray =
        SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(PBEKeySpec(password.toCharArray(), salt, 120_000, 256)).encoded

    private fun b64(b: ByteArray) = Base64.encodeToString(b, Base64.NO_WRAP)

    companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
