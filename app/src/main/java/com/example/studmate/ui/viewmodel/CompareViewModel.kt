package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.SessionEntity
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.domain.ClassSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class CompareViewModel(
    private val catalogRepo: CatalogRepository,
) : ViewModel() {

    private val _selectedIds = MutableStateFlow<List<String>>(emptyList())
    val selectedIds: StateFlow<List<String>> = _selectedIds.asStateFlow()

    fun setComparedIds(ids: List<String>) {
        _selectedIds.value = ids
    }

    fun remove(classId: String) {
        _selectedIds.value = _selectedIds.value - classId
    }

    val comparedClasses: StateFlow<List<ClassSummary>> = combine(
        catalogRepo.allSummaries,
        _selectedIds,
    ) { summaries, ids ->
        val map = summaries.associateBy { it.cls.id }
        ids.mapNotNull { map[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun sessionsFor(classId: String) = catalogRepo.sessions(classId)
}
