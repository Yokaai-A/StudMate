package com.example.studmate.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.MaterialType
import com.example.studmate.domain.OrganizerType
import com.example.studmate.domain.PaymentStatus
import com.example.studmate.domain.ProgramType
import com.example.studmate.domain.VerificationStatus

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String? = null,
    val authProvider: String = "EMAIL",
    val university: String = "",
    val faculty: String = "",
    val major: String = "",
    val semester: Int = 4,
    val academicRole: String = "Peer Tutor",
    val interests: List<String> = emptyList(),
    val bio: String = "",
    val portfolioUrl: String = "",
    val avatarUri: String? = null,
    val verificationStatus: VerificationStatus = VerificationStatus.NONE,
    val verificationMethod: String? = null,
    val campusEmail: String? = null,
    val ktmUri: String? = null,
    val organizerType: OrganizerType = OrganizerType.INDIVIDUAL,
    val organizationName: String = "",
    val onboarded: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis(),
) {
    val displayOrganizerName: String
        get() = if (organizerType == OrganizerType.ORGANIZATION && organizationName.isNotBlank()) organizationName else name
    val isVerified: Boolean get() = verificationStatus == VerificationStatus.VERIFIED
}

@Entity(tableName = "classes", indices = [Index("organizerId"), Index("status")])
data class ClassEntity(
    @PrimaryKey val id: String,
    val organizerId: String,
    val title: String,
    val description: String,
    val topicSummary: String = "",
    val courseGrade: String = "A",
    val category: Category,
    val programType: ProgramType,
    val format: ClassFormat,
    val tags: List<String> = emptyList(),
    val coverUri: String? = null,
    val coverStyle: Int = 0,
    val university: String,
    val location: String = "",
    val meetingLink: String = "",
    val price: Long = 0,
    val earlyBirdPrice: Long? = null,
    val earlyBirdUntil: Long? = null,
    val quota: Int = 20,
    val hasCertificate: Boolean = true,
    val minAttendancePercent: Int = 75,
    val benefits: List<String> = emptyList(),
    val status: ClassStatus = ClassStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis(),
    val startAt: Long = 0,
    val endAt: Long = 0,
) {
    val isFree: Boolean get() = price <= 0
    fun priceAt(now: Long): Long {
        val eb = earlyBirdPrice
        val until = earlyBirdUntil
        return if (eb != null && until != null && now <= until && eb < price) eb else price
    }
    fun isEarlyBird(now: Long): Boolean = priceAt(now) < price
}

@Entity(tableName = "sessions", indices = [Index("classId")])
data class SessionEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val orderIndex: Int,
    val title: String,
    val description: String = "",
    val startAt: Long,
    val durationMinutes: Int = 90,
    val isDone: Boolean = false,
)

@Entity(
    tableName = "enrollments",
    indices = [Index(value = ["classId", "userId"], unique = true), Index("userId"), Index(value = ["ticketCode"], unique = true)],
)
data class EnrollmentEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val userId: String,
    val status: EnrollmentStatus,
    val amount: Long,
    val paymentMethod: String? = null,
    val paymentStatus: PaymentStatus,
    val paymentRef: String? = null,
    val ticketCode: String,
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null,
    val certificateId: String? = null,
    val certificateIssuedAt: Long? = null,
)

@Entity(tableName = "attendance", primaryKeys = ["enrollmentId", "sessionId"], indices = [Index("sessionId")])
data class AttendanceEntity(
    val enrollmentId: String,
    val sessionId: String,
    val checkedInAt: Long = System.currentTimeMillis(),
    val method: String = "QR",
)

@Entity(tableName = "reviews", indices = [Index(value = ["classId", "userId"], unique = true)])
data class ReviewEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val userId: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "materials", indices = [Index("classId")])
data class MaterialEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val title: String,
    val type: MaterialType,
    val uri: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "forum_posts", indices = [Index("classId")])
data class ForumPostEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val userId: String,
    val parentId: String? = null,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "announcements", indices = [Index("classId")])
data class AnnouncementEntity(
    @PrimaryKey val id: String,
    val classId: String,
    val title: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "payouts", indices = [Index("organizerId")])
data class PayoutEntity(
    @PrimaryKey val id: String,
    val organizerId: String,
    val amount: Long,
    val destination: String,
    val accountNumber: String,
    val status: String = "SUCCESS",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "org_admins", indices = [Index(value = ["organizerId", "email"], unique = true)])
data class OrgAdminEntity(
    @PrimaryKey val id: String,
    val organizerId: String,
    val email: String,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notifications", indices = [Index("userId")])
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val classId: String? = null,
    val type: String = "INFO",
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
)

data class ClassCount(val classId: String, val count: Int)
data class ClassRating(val classId: String, val avg: Double, val cnt: Int)
data class SessionAgg(val classId: String, val cnt: Int, val minutes: Int)
data class AttendanceCount(val enrollmentId: String, val cnt: Int)
