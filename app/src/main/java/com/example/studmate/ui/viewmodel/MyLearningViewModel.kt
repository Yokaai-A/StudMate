package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.LearningRepository
import com.example.studmate.domain.EnrollmentStatus
import com.example.studmate.domain.LearningItem
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

enum class LearningTab(val label: String) {
    ALL("Semua"),
    ACTIVE("Aktif"),
    UPCOMING("Mendatang"),
    COMPLETED("Selesai"),
}

class MyLearningViewModel(
    private val learningRepo: LearningRepository,
    private val authRepo: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _selectedTab = MutableStateFlow(LearningTab.ALL)
    val selectedTab: StateFlow<LearningTab> = _selectedTab.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val allLearningItems: StateFlow<List<LearningItem>> = currentUser.flatMapLatest { u ->
        if (u == null) flowOf(emptyList()) else learningRepo.myLearning(u.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredItems: StateFlow<List<LearningItem>> = combine(
        allLearningItems,
        _selectedTab,
    ) { items, tab ->
        val now = System.currentTimeMillis()
        when (tab) {
            LearningTab.ALL -> items
            LearningTab.ACTIVE -> items.filter {
                it.enrollment.status == EnrollmentStatus.ACTIVE &&
                    (it.summary.cls.startAt <= now && it.summary.cls.endAt >= now || it.attended > 0)
            }
            LearningTab.UPCOMING -> items.filter {
                it.enrollment.status == EnrollmentStatus.ACTIVE && it.summary.cls.startAt > now
            }
            LearningTab.COMPLETED -> items.filter {
                it.enrollment.status == EnrollmentStatus.COMPLETED || it.summary.cls.endAt < now
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectTab(tab: LearningTab) {
        _selectedTab.value = tab
    }

    fun cancelEnrollment(enrollmentId: String) = viewModelScope.launch {
        learningRepo.cancelEnrollment(enrollmentId)
    }
}
