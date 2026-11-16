package com.example.studmate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.EnrollmentEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.data.repo.LearningRepository
import com.example.studmate.data.repo.ReviewItem
import com.example.studmate.domain.ClassSummary
import com.example.studmate.domain.PaymentMethod
import com.example.studmate.util.NativeIntents
import com.example.studmate.util.formatDate
import com.example.studmate.util.priceLabel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ClassDetailUiState(
    val isEnrolling: Boolean = false,
    val pendingEnrollment: EnrollmentEntity? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

class ClassDetailViewModel(
    val classId: String,
    private val catalogRepo: CatalogRepository,
    private val learningRepo: LearningRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val summary: StateFlow<ClassSummary?> = catalogRepo.summary(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sessions: StateFlow<List<SessionEntity>> = catalogRepo.sessions(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reviews: StateFlow<List<ReviewItem>> = catalogRepo.reviews(classId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val enrollment: StateFlow<EnrollmentEntity?> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(null) else learningRepo.enrollmentFor(classId, u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(ClassDetailUiState())
    val uiState: StateFlow<ClassDetailUiState> = _uiState.asStateFlow()

    /** 1-Tap Enrollment */
    fun enroll() = viewModelScope.launch {
        val user = currentUser.value ?: run {
            _uiState.value = _uiState.value.copy(error = "Silakan login terlebih dahulu")
            return@launch
        }
        _uiState.value = _uiState.value.copy(isEnrolling = true, error = null)
        learningRepo.enroll(classId, user).fold(
            onSuccess = { e ->
                _uiState.value = _uiState.value.copy(
                    isEnrolling = false,
                    pendingEnrollment = if (e.amount > 0) e else null,
                    successMessage = if (e.amount <= 0) "Pendaftaran berhasil! Kelas gratis siap diikuti." else null,
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isEnrolling = false, error = it.localizedMessage)
            },
        )
    }

    /** Bayar kelas (Simulasi Payment Gateway Escrow) */
    fun pay(method: PaymentMethod) = viewModelScope.launch {
        val e = _uiState.value.pendingEnrollment ?: enrollment.value ?: return@launch
        _uiState.value = _uiState.value.copy(isEnrolling = true, error = null)
        learningRepo.confirmPayment(e.id, method).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isEnrolling = false,
                    pendingEnrollment = null,
                    successMessage = "Pembayaran berhasil! Dana kamu aman di Escrow StudMate.",
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isEnrolling = false, error = it.localizedMessage)
            },
        )
    }

    /** Buat Promo Poster Card dan buka Android Share Sheet */
    fun sharePromoCard(context: Context) {
        val s = summary.value ?: return
        val cls = s.cls
        val now = System.currentTimeMillis()
        val info = NativeIntents.PromoInfo(
            classId = cls.id,
            title = cls.title,
            organizer = s.organizer?.displayOrganizerName ?: "Tutor StudMate",
            university = cls.university,
            schedule = if (cls.startAt > 0) formatDate(cls.startAt) else "Segera",
            format = cls.format.label,
            price = priceLabel(cls.priceAt(now)),
            type = cls.programType.label,
            coverStyle = cls.coverStyle,
        )
        val file = NativeIntents.buildPromoCard(context, info)
        NativeIntents.shareFile(
            context = context,
            file = file,
            mime = "image/png",
            text = NativeIntents.promoText(info),
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
