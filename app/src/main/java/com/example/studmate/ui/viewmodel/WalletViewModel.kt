package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.PayoutEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.OrganizerRepository
import com.example.studmate.data.repo.WalletSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WalletUiState(
    val isWithdrawing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

class WalletViewModel(
    private val organizerRepo: OrganizerRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val walletSummary: StateFlow<WalletSummary?> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(null) else organizerRepo.observeWallet(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val payouts: StateFlow<List<PayoutEntity>> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else organizerRepo.observePayouts(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    fun withdraw(amount: Long, destination: String, accountNum: String) = viewModelScope.launch {
        val user = currentUser.value ?: return@launch
        _uiState.value = _uiState.value.copy(isWithdrawing = true, error = null)
        organizerRepo.requestPayout(user.id, amount, destination, accountNum).fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isWithdrawing = false,
                    successMessage = "Penarikan saldo sebesar Rp$amount berhasil diproses!",
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(
                    isWithdrawing = false,
                    error = it.localizedMessage,
                )
            },
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}
