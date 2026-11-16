package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.SeedData
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.data.repo.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val infoMessage: String? = null,
    val otpSent: Boolean = false,
)

class AuthViewModel(
    private val authRepo: AuthRepository,
    private val session: SessionStore,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        authRepo.login(email, pass).fold(
            onSuccess = { _uiState.value = AuthUiState() },
            onFailure = { _uiState.value = AuthUiState(error = it.localizedMessage ?: "Gagal masuk") },
        )
    }

    fun register(name: String, email: String, pass: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        authRepo.register(name, email, pass).fold(
            onSuccess = { _uiState.value = AuthUiState() },
            onFailure = { _uiState.value = AuthUiState(error = it.localizedMessage ?: "Gagal mendaftar") },
        )
    }


    fun signInGoogleSimulated(email: String = "mahasiswa.baru@ui.ac.id", name: String = "Mahasiswa Demo") = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        authRepo.signInWithGoogle(email, name, null).fold(
            onSuccess = { _uiState.value = AuthUiState() },
            onFailure = { _uiState.value = AuthUiState(error = it.localizedMessage) },
        )
    }


    fun switchDemoUser(userId: String) = viewModelScope.launch {
        session.setCurrentUser(userId)
    }

    fun completeOnboarding(
        university: String,
        faculty: String = "",
        major: String,
        semester: Int = 4,
        interests: List<String>,
    ) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        authRepo.completeOnboarding(user.id, university, faculty, major, semester, interests)
    }

    fun requestCampusOtp(campusEmail: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        authRepo.requestCampusEmailCode(user.id, campusEmail).fold(
            onSuccess = {
                _uiState.value = AuthUiState(
                    otpSent = true,
                    infoMessage = "Kode verifikasi 6-digit telah dikirim ke notifikasi perangkat ini (simulasi).",
                )
            },
            onFailure = { _uiState.value = AuthUiState(error = it.localizedMessage) },
        )
    }

    fun verifyCampusOtp(code: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        authRepo.confirmCampusEmail(user.id, code).fold(
            onSuccess = {
                _uiState.value = AuthUiState(
                    otpSent = false,
                    infoMessage = "Kampus berhasil diverifikasi! Verified Campus Badge aktif.",
                )
            },
            onFailure = { _uiState.value = AuthUiState(error = it.localizedMessage, otpSent = true) },
        )
    }

    fun submitKtm(uri: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        authRepo.submitKtm(user.id, uri)
        _uiState.value = AuthUiState(infoMessage = "Foto KTM berhasil diverifikasi! Badge aktif.")
    }

    fun logout() = viewModelScope.launch {
        authRepo.logout()
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, infoMessage = null)
    }
}
