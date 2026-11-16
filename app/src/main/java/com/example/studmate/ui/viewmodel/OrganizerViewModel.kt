package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.MaterialEntity
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.repo.AttendeeItem
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.data.repo.OrganizerRepository
import com.example.studmate.data.repo.WalletSummary
import com.example.studmate.domain.ClassSummary
import com.example.studmate.domain.MaterialType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OrganizerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val checkInResult: String? = null,
)

class OrganizerViewModel(
    private val organizerRepo: OrganizerRepository,
    private val catalogRepo: CatalogRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val myClasses: StateFlow<List<ClassSummary>> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else organizerRepo.observeMyClasses(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val walletSummary: StateFlow<WalletSummary?> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(null) else organizerRepo.observeWallet(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(OrganizerUiState())
    val uiState: StateFlow<OrganizerUiState> = _uiState.asStateFlow()

    fun attendeesFor(classId: String): StateFlow<List<AttendeeItem>> =
        organizerRepo.observeClassAttendees(classId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sessionsFor(classId: String): StateFlow<List<SessionEntity>> =
        catalogRepo.sessions(classId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun checkInByTicket(classId: String, sessionId: String, ticketCode: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null, checkInResult = null)
        organizerRepo.checkInByTicket(classId, sessionId, ticketCode).fold(
            onSuccess = { (user, _) ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    checkInResult = "Presensi Berhasil: ${user?.name ?: "Peserta"} ✅",
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = it.localizedMessage ?: "Presensi gagal",
                )
            },
        )
    }

    fun toggleAttendance(enrollmentId: String, sessionId: String, attended: Boolean) = viewModelScope.launch {
        organizerRepo.toggleAttendance(enrollmentId, sessionId, attended)
    }

    fun setSessionDone(classId: String, sessionId: String, done: Boolean) = viewModelScope.launch {
        organizerRepo.setSessionDone(classId, sessionId, done)
        _uiState.value = _uiState.value.copy(
            successMessage = if (done) "Sesi ditandai selesai." else "Status sesi diperbarui.",
        )
    }

    fun broadcastAnnouncement(classId: String, title: String, message: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        organizerRepo.broadcastAnnouncement(classId, title, message).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Pengumuman berhasil disiarkan ke seluruh HP peserta! 📢",
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.localizedMessage)
            },
        )
    }

    fun addMaterial(classId: String, title: String, type: MaterialType, uri: String) = viewModelScope.launch {
        organizerRepo.addMaterial(classId, title, type, uri).fold(
            onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Materi berhasil ditambahkan") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) },
        )
    }

    fun deleteMaterial(id: String) = viewModelScope.launch {
        organizerRepo.deleteMaterial(id)
    }

    fun deleteClass(classId: String) = viewModelScope.launch {
        runCatching { organizerRepo.deleteClass(classId) }.fold(
            onSuccess = { _uiState.value = _uiState.value.copy(successMessage = "Kelas berhasil dihapus") },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.localizedMessage) },
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null, checkInResult = null)
    }
}
