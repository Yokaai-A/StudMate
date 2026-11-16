package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.ClassEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.data.repo.OrganizerRepository
import com.example.studmate.domain.Catalog
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.ClassStatus
import com.example.studmate.domain.ProgramType
import com.example.studmate.util.DAY
import com.example.studmate.util.HOUR
import com.example.studmate.util.newId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BuilderUiState(
    val currentStep: Int = 1,
    val title: String = "",
    val topicSummary: String = "",
    val courseGrade: String = "A",
    val description: String = "",
    val category: Category = Category.TECH,
    val programType: ProgramType = ProgramType.STUDY_GROUP,
    val format: ClassFormat = ClassFormat.ONLINE,
    val university: String = "",
    val location: String = "",
    val meetingLink: String = "",
    val coverStyle: Int = 0,
    val isFree: Boolean = false,
    val price: Long = 20_000,
    val hasEarlyBird: Boolean = false,
    val earlyBirdPrice: Long = 0,
    val earlyBirdDays: Int = 3,
    val quota: Int = 20,
    val hasCertificate: Boolean = true,
    val minAttendancePercent: Int = 75,
    val sessions: List<SessionEntity> = emptyList(),
    val isSaving: Boolean = false,
    val savedClassId: String? = null,
    val error: String? = null,
    val titleWarning: String? = null,
)

class ClassBuilderViewModel(
    val editingClassId: String?,
    private val organizerRepo: OrganizerRepository,
    private val catalogRepo: CatalogRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = currentUser.firstOrNull()
            if (editingClassId != null) {
                val sum = catalogRepo.summary(editingClassId).firstOrNull()
                val sess = catalogRepo.sessions(editingClassId).firstOrNull().orEmpty()
                if (sum != null) {
                    val cls = sum.cls
                    _uiState.value = BuilderUiState(
                        title = cls.title,
                        topicSummary = cls.topicSummary,
                        courseGrade = cls.courseGrade,
                        description = cls.description,
                        category = cls.category,
                        programType = cls.programType,
                        format = cls.format,
                        university = cls.university,
                        location = cls.location,
                        meetingLink = cls.meetingLink,
                        coverStyle = cls.coverStyle,
                        isFree = cls.isFree,
                        price = cls.price,
                        hasEarlyBird = cls.earlyBirdPrice != null,
                        earlyBirdPrice = cls.earlyBirdPrice ?: 0,
                        quota = cls.quota,
                        hasCertificate = cls.hasCertificate,
                        minAttendancePercent = cls.minAttendancePercent,
                        sessions = sess,
                    )
                }
            } else {
                val now = System.currentTimeMillis()
                _uiState.value = _uiState.value.copy(
                    university = user?.university ?: "Universitas Indonesia",
                    sessions = listOf(
                        SessionEntity(
                            id = newId(),
                            classId = "",
                            orderIndex = 1,
                            title = "Pertemuan 1: Pengantar & Konsep Dasar",
                            description = "Materi pembuka dan instalasi tools praktikum.",
                            startAt = now + 2 * DAY,
                            durationMinutes = 90,
                        )
                    ),
                )
            }
        }
    }

    fun setStep(step: Int) {
        _uiState.value = _uiState.value.copy(currentStep = step, error = null)
    }

    fun updateBasicInfo(
        title: String,
        topicSummary: String,
        courseGrade: String,
        description: String,
        category: Category,
        programType: ProgramType,
        format: ClassFormat,
        university: String,
        location: String,
        meetingLink: String,
        coverStyle: Int,
    ) {
        val (isValid, warnMsg) = Catalog.validateCourseTitle(title)
        _uiState.value = _uiState.value.copy(
            title = title,
            topicSummary = topicSummary,
            courseGrade = courseGrade,
            description = description,
            category = category,
            programType = programType,
            format = format,
            university = university,
            location = location,
            meetingLink = meetingLink,
            coverStyle = coverStyle,
            titleWarning = if (!isValid) warnMsg else null,
        )
    }

    fun addSession(title: String, description: String, startAt: Long, durationMinutes: Int) {
        val current = _uiState.value.sessions
        val newSession = SessionEntity(
            id = newId(),
            classId = editingClassId.orEmpty(),
            orderIndex = current.size + 1,
            title = title,
            description = description,
            startAt = startAt,
            durationMinutes = durationMinutes,
        )
        _uiState.value = _uiState.value.copy(sessions = current + newSession)
    }

    fun removeSession(index: Int) {
        val current = _uiState.value.sessions.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(sessions = current)
        }
    }

    fun updateTicketInfo(
        isFree: Boolean,
        price: Long,
        hasEarlyBird: Boolean,
        earlyBirdPrice: Long,
        earlyBirdDays: Int,
        quota: Int,
        hasCertificate: Boolean,
        minAttendancePercent: Int,
    ) {
        _uiState.value = _uiState.value.copy(
            isFree = isFree,
            price = if (isFree) 0 else price,
            hasEarlyBird = hasEarlyBird,
            earlyBirdPrice = earlyBirdPrice,
            earlyBirdDays = earlyBirdDays,
            quota = quota,
            hasCertificate = hasCertificate,
            minAttendancePercent = minAttendancePercent,
        )
    }

    fun save(publish: Boolean) = viewModelScope.launch {
        val state = _uiState.value
        val user = currentUser.value ?: return@launch
        val (isValidTitle, errorMsg) = Catalog.validateCourseTitle(state.title)
        if (!isValidTitle) {
            _uiState.value = state.copy(error = errorMsg ?: "Nama mata kuliah tidak sesuai aturan standarisasi")
            return@launch
        }
        if (state.sessions.isEmpty()) {
            _uiState.value = state.copy(error = "Minimal buat 1 sesi pertemuan")
            return@launch
        }

        _uiState.value = state.copy(isSaving = true, error = null)
        val now = System.currentTimeMillis()
        val classId = editingClassId ?: newId()
        val cls = ClassEntity(
            id = classId,
            organizerId = user.id,
            title = state.title.trim(),
            topicSummary = state.topicSummary.trim(),
            courseGrade = state.courseGrade.trim().ifBlank { "A" },
            description = state.description.trim(),
            category = state.category,
            programType = state.programType,
            format = state.format,
            coverStyle = state.coverStyle,
            university = state.university.ifBlank { user.university },
            location = state.location.trim(),
            meetingLink = state.meetingLink.trim(),
            price = if (state.isFree) 0 else state.price,
            earlyBirdPrice = if (!state.isFree && state.hasEarlyBird) state.earlyBirdPrice else null,
            earlyBirdUntil = if (!state.isFree && state.hasEarlyBird) now + state.earlyBirdDays * DAY else null,
            quota = state.quota,
            hasCertificate = state.hasCertificate,
            minAttendancePercent = state.minAttendancePercent,
            status = if (publish) ClassStatus.PUBLISHED else ClassStatus.DRAFT,
            createdAt = now,
        )

        organizerRepo.saveClass(cls, state.sessions, publish).fold(
            onSuccess = { id ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    savedClassId = id,
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = it.localizedMessage,
                )
            },
        )
    }
}
