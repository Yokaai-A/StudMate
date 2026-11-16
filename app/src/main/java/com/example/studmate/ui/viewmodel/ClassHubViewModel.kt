package com.example.studmate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.AnnouncementEntity
import com.example.studmate.data.local.AttendanceEntity
import com.example.studmate.data.local.EnrollmentEntity
import com.example.studmate.data.local.MaterialEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.data.repo.LearningRepository
import com.example.studmate.data.repo.OrganizerRepository
import com.example.studmate.data.repo.PostItem
import com.example.studmate.domain.ClassSummary
import com.example.studmate.util.NativeIntents
import com.example.studmate.util.durationLabel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class HubUiState(
    val error: String? = null,
    val successMessage: String? = null,
    val isClaimingCert: Boolean = false,
    val certPdfFile: File? = null,
)

class ClassHubViewModel(
    val classId: String,
    private val learningRepo: LearningRepository,
    private val catalogRepo: CatalogRepository,
    private val organizerRepo: OrganizerRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val summary: StateFlow<ClassSummary?> = catalogRepo.summary(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessions: StateFlow<List<SessionEntity>> = catalogRepo.sessions(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val enrollment: StateFlow<EnrollmentEntity?> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(null) else learningRepo.enrollmentFor(classId, u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val attendance: StateFlow<List<AttendanceEntity>> = enrollment.flatMapLatest { e ->
        if (e == null) flowOf(emptyList()) else learningRepo.attendance(e.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val materials: StateFlow<List<MaterialEntity>> = learningRepo.materials(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = learningRepo.announcements(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val posts: StateFlow<List<PostItem>> = learningRepo.posts(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val certEligibility = combine(
        summary,
        sessions,
        attendance,
    ) { sum, sess, att ->
        if (sum == null) null
        else learningRepo.eligibility(
            cls = sum.cls,
            totalSessions = sess.size,
            doneSessions = sess.count { it.isDone },
            attended = att.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(HubUiState())
    val uiState: StateFlow<HubUiState> = _uiState.asStateFlow()

    fun addPost(content: String, parentId: String? = null) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        learningRepo.addPost(classId, user.id, content, parentId).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Komentar terkirim") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) },
        )
    }

    fun submitReview(rating: Int, comment: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        learningRepo.submitReview(classId, user.id, rating, comment).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Ulasan berhasil dikirim! Terima kasih atas feedback-mu.") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) },
        )
    }

    fun claimCertificate(context: Context) = viewModelScope.launch {
        val e = enrollment.value ?: return@launch
        _uiState.value = _uiState.value.copy(isClaimingCert = true, error = null)
        learningRepo.claimCertificate(e.id).fold(
            onSuccess = { updated ->
                val s = summary.value
                val user = currentUser.value
                if (s != null && user != null) {
                    val certInfo = NativeIntents.CertificateInfo(
                        certificateId = updated.certificateId ?: "SMC-STUDMATE",
                        participant = user.name,
                        classTitle = s.cls.title,
                        organizer = s.organizer?.displayOrganizerName ?: "Tutor StudMate",
                        university = s.cls.university,
                        totalHours = durationLabel(s.totalMinutes),
                        issuedAt = updated.certificateIssuedAt ?: System.currentTimeMillis(),
                    )
                    val pdf = NativeIntents.buildCertificatePdf(context, certInfo)
                    _uiState.value = _uiState.value.copy(
                        isClaimingCert = false,
                        certPdfFile = pdf,
                        successMessage = "Sertifikat berhasil diterbitkan! Kamu bisa mengunduh PDF atau membagikannya ke LinkedIn.",
                    )
                }
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isClaimingCert = false, error = it.localizedMessage)
            },
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
