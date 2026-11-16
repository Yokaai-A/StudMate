package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.NotificationEntity
import com.example.studmate.data.local.OrgAdminEntity
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.prefs.AppSettings
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.LearningRepository
import com.example.studmate.data.repo.OrganizerRepository
import com.example.studmate.domain.OrganizerType
import com.example.studmate.domain.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepo: AuthRepository,
    private val learningRepo: LearningRepository,
    private val session: SessionStore,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val isOrganizerMode: StateFlow<Boolean> = session.organizerMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val settings: StateFlow<AppSettings> = session.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notifications: StateFlow<List<NotificationEntity>> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else learningRepo.notifications(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadCount: StateFlow<Int> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(0) else learningRepo.unreadCount(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun toggleOrganizerMode() = viewModelScope.launch {
        session.setOrganizerMode(!isOrganizerMode.value)
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        session.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        session.setDynamicColor(enabled)
    }

    fun markNotificationsRead() = viewModelScope.launch {
        val u = currentUser.value ?: return@launch
        learningRepo.markAllRead(u.id)
    }

    fun updateProfile(bio: String, portfolio: String, orgType: OrganizerType, orgName: String) = viewModelScope.launch {
        val u = currentUser.value ?: return@launch
        authRepo.update(u.id) {
            it.copy(
                bio = bio.trim(),
                portfolioUrl = portfolio.trim(),
                organizerType = orgType,
                organizationName = orgName.trim(),
            )
        }
    }

    fun logout() = viewModelScope.launch {
        authRepo.logout()
    }
}
