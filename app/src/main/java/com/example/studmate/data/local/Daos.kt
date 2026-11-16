package com.example.studmate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Upsert suspend fun upsert(user: UserEntity)
    @Upsert suspend fun upsertAll(users: List<UserEntity>)
    @Query("SELECT * FROM users WHERE id = :id") fun observe(id: String): Flow<UserEntity?>
    @Query("SELECT * FROM users WHERE id = :id") suspend fun get(id: String): UserEntity?
    @Query("SELECT * FROM users WHERE lower(email) = lower(:email) LIMIT 1") suspend fun byEmail(email: String): UserEntity?
    @Query("SELECT * FROM users") fun observeAll(): Flow<List<UserEntity>>
    @Query("SELECT COUNT(*) FROM users") suspend fun count(): Int
}

@Dao
interface ClassDao {
    @Upsert suspend fun upsert(c: ClassEntity)
    @Upsert suspend fun upsertAll(c: List<ClassEntity>)
    @Query("SELECT * FROM classes") fun observeAll(): Flow<List<ClassEntity>>
    @Query("SELECT * FROM classes WHERE id = :id") fun observe(id: String): Flow<ClassEntity?>
    @Query("SELECT * FROM classes WHERE id = :id") suspend fun get(id: String): ClassEntity?
    @Query("SELECT COUNT(*) FROM classes") suspend fun count(): Int
    @Query("DELETE FROM classes WHERE id = :id") suspend fun delete(id: String)

    @Upsert suspend fun upsertSessions(s: List<SessionEntity>)
    @Query("SELECT * FROM sessions WHERE classId = :classId ORDER BY orderIndex") fun observeSessions(classId: String): Flow<List<SessionEntity>>
    @Query("SELECT * FROM sessions WHERE classId = :classId ORDER BY orderIndex") suspend fun sessions(classId: String): List<SessionEntity>
    @Query("SELECT * FROM sessions WHERE id = :id") suspend fun session(id: String): SessionEntity?
    @Query("SELECT * FROM sessions ORDER BY startAt") fun observeAllSessions(): Flow<List<SessionEntity>>
    @Query("DELETE FROM sessions WHERE classId = :classId AND id NOT IN (:keep)") suspend fun deleteSessionsExcept(classId: String, keep: List<String>)
    @Query("DELETE FROM sessions WHERE classId = :classId") suspend fun deleteSessions(classId: String)
    @Query("UPDATE sessions SET isDone = :done WHERE id = :id") suspend fun setSessionDone(id: String, done: Boolean)
    @Query("SELECT classId, COUNT(*) AS cnt, COALESCE(SUM(durationMinutes),0) AS minutes FROM sessions GROUP BY classId")
    fun observeSessionAgg(): Flow<List<SessionAgg>>
}

@Dao
interface EnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(e: EnrollmentEntity)
    @Upsert suspend fun upsert(e: EnrollmentEntity)
    @Upsert suspend fun upsertAll(e: List<EnrollmentEntity>)
    @Query("SELECT * FROM enrollments WHERE id = :id") fun observe(id: String): Flow<EnrollmentEntity?>
    @Query("SELECT * FROM enrollments WHERE id = :id") suspend fun get(id: String): EnrollmentEntity?
    @Query("SELECT * FROM enrollments WHERE ticketCode = :code") suspend fun byTicket(code: String): EnrollmentEntity?
    @Query("SELECT * FROM enrollments WHERE classId = :classId AND userId = :userId") fun observeFor(classId: String, userId: String): Flow<EnrollmentEntity?>
    @Query("SELECT * FROM enrollments WHERE classId = :classId AND userId = :userId") suspend fun getFor(classId: String, userId: String): EnrollmentEntity?
    @Query("SELECT * FROM enrollments WHERE userId = :userId ORDER BY createdAt DESC") fun observeByUser(userId: String): Flow<List<EnrollmentEntity>>
    @Query("SELECT * FROM enrollments WHERE classId = :classId ORDER BY createdAt") fun observeByClass(classId: String): Flow<List<EnrollmentEntity>>
    @Query("SELECT * FROM enrollments WHERE classId = :classId") suspend fun byClass(classId: String): List<EnrollmentEntity>
    @Query("SELECT * FROM enrollments") fun observeAll(): Flow<List<EnrollmentEntity>>
    @Query("DELETE FROM enrollments WHERE id = :id") suspend fun delete(id: String)
    @Query("SELECT classId, COUNT(*) AS count FROM enrollments WHERE status IN ('ACTIVE','COMPLETED') GROUP BY classId")
    fun observeCounts(): Flow<List<ClassCount>>
    @Query("SELECT COUNT(*) FROM enrollments WHERE classId = :classId AND status IN ('ACTIVE','COMPLETED','PENDING_PAYMENT')")
    suspend fun seatsTaken(classId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAttendance(a: AttendanceEntity): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertAttendanceAll(a: List<AttendanceEntity>)
    @Query("DELETE FROM attendance WHERE enrollmentId = :enrollmentId AND sessionId = :sessionId") suspend fun deleteAttendance(enrollmentId: String, sessionId: String)
    @Query("SELECT * FROM attendance WHERE enrollmentId = :enrollmentId") fun observeAttendance(enrollmentId: String): Flow<List<AttendanceEntity>>
    @Query("SELECT * FROM attendance WHERE enrollmentId = :enrollmentId") suspend fun attendance(enrollmentId: String): List<AttendanceEntity>
    @Query("SELECT enrollmentId, COUNT(*) AS cnt FROM attendance GROUP BY enrollmentId")
    fun observeAttendanceCounts(): Flow<List<AttendanceCount>>
    @Query("SELECT a.* FROM attendance a JOIN enrollments e ON e.id = a.enrollmentId WHERE e.classId = :classId")
    fun observeClassAttendance(classId: String): Flow<List<AttendanceEntity>>
}

@Dao
interface CommunityDao {
    @Upsert suspend fun upsertReview(r: ReviewEntity)
    @Upsert suspend fun upsertReviews(r: List<ReviewEntity>)
    @Query("SELECT * FROM reviews WHERE classId = :classId ORDER BY createdAt DESC") fun observeReviews(classId: String): Flow<List<ReviewEntity>>
    @Query("SELECT * FROM reviews") fun observeAllReviews(): Flow<List<ReviewEntity>>
    @Query("SELECT classId, AVG(rating) AS avg, COUNT(*) AS cnt FROM reviews GROUP BY classId") fun observeRatings(): Flow<List<ClassRating>>

    @Upsert suspend fun upsertMaterial(m: MaterialEntity)
    @Query("DELETE FROM materials WHERE id = :id") suspend fun deleteMaterial(id: String)
    @Query("SELECT * FROM materials WHERE classId = :classId ORDER BY createdAt DESC") fun observeMaterials(classId: String): Flow<List<MaterialEntity>>

    @Upsert suspend fun upsertPost(p: ForumPostEntity)
    @Upsert suspend fun upsertPosts(p: List<ForumPostEntity>)
    @Query("SELECT userId FROM forum_posts WHERE id = :id") suspend fun postAuthor(id: String): String?
    @Query("SELECT * FROM forum_posts WHERE classId = :classId ORDER BY createdAt") fun observePosts(classId: String): Flow<List<ForumPostEntity>>

    @Upsert suspend fun upsertAnnouncement(a: AnnouncementEntity)
    @Query("SELECT * FROM announcements WHERE classId = :classId ORDER BY createdAt DESC") fun observeAnnouncements(classId: String): Flow<List<AnnouncementEntity>>

    @Upsert suspend fun upsertNotifications(n: List<NotificationEntity>)
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC") fun observeNotifications(userId: String): Flow<List<NotificationEntity>>
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0") fun observeUnread(userId: String): Flow<Int>
    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId") suspend fun markAllRead(userId: String)
}

@Dao
interface WalletDao {
    @Upsert suspend fun upsertPayout(p: PayoutEntity)
    @Query("SELECT * FROM payouts WHERE organizerId = :organizerId ORDER BY createdAt DESC") fun observePayouts(organizerId: String): Flow<List<PayoutEntity>>
    @Query("SELECT COALESCE(SUM(amount),0) FROM payouts WHERE organizerId = :organizerId AND status != 'FAILED'") suspend fun totalPayout(organizerId: String): Long

    @Upsert suspend fun upsertAdmin(a: OrgAdminEntity)
    @Query("DELETE FROM org_admins WHERE id = :id") suspend fun deleteAdmin(id: String)
    @Query("SELECT * FROM org_admins WHERE organizerId = :organizerId ORDER BY addedAt") fun observeAdmins(organizerId: String): Flow<List<OrgAdminEntity>>
    @Query("SELECT organizerId FROM org_admins WHERE lower(email) = lower(:email)") fun observeManagedOrgIds(email: String): Flow<List<String>>
    @Query("SELECT organizerId FROM org_admins WHERE lower(email) = lower(:email)") suspend fun managedOrgIds(email: String): List<String>
}
