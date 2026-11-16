package com.example.studmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studmate.data.local.UserEntity
import com.example.studmate.data.prefs.SessionStore
import com.example.studmate.data.repo.AuthRepository
import com.example.studmate.data.repo.CatalogRepository
import com.example.studmate.domain.AppConfig
import com.example.studmate.domain.Category
import com.example.studmate.domain.ClassFormat
import com.example.studmate.domain.ClassSummary
import com.example.studmate.domain.FilterState
import com.example.studmate.domain.MentorStats
import com.example.studmate.domain.PriceFilter
import com.example.studmate.domain.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val catalogRepo: CatalogRepository,
    private val authRepo: AuthRepository,
    private val session: SessionStore,
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepo.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val radarLocal: StateFlow<Boolean> = session.radarLocal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _compareList = MutableStateFlow<List<String>>(emptyList())
    val compareList: StateFlow<List<String>> = _compareList.asStateFlow()

    val risingMentors: StateFlow<List<MentorStats>> = catalogRepo.mentors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredClasses: StateFlow<List<ClassSummary>> = combine(
        catalogRepo.published,
        _filterState,
        radarLocal,
        currentUser,
    ) { classes, filter, isLocal, user ->
        val now = System.currentTimeMillis()
        classes.filter { summary ->
            val cls = summary.cls
            if (isLocal && user != null && user.university.isNotBlank()) {
                val matchesUniversity = cls.university.equals(user.university, ignoreCase = true)
                if (!matchesUniversity) return@filter false
            }

            if (filter.query.isNotBlank()) {
                val q = filter.query.trim().lowercase()
                val match = cls.title.lowercase().contains(q) ||
                    cls.description.lowercase().contains(q) ||
                    cls.university.lowercase().contains(q) ||
                    cls.tags.any { it.lowercase().contains(q) } ||
                    summary.organizer?.name?.lowercase()?.contains(q) == true
                if (!match) return@filter false
            }

            if (filter.categories.isNotEmpty() && cls.category !in filter.categories) return@filter false

            if (filter.formats.isNotEmpty() && cls.format !in filter.formats) return@filter false

            if (filter.types.isNotEmpty() && cls.programType !in filter.types) return@filter false

            val price = cls.priceAt(now)
            when (filter.price) {
                PriceFilter.ALL -> {}
                PriceFilter.FREE -> if (price > 0) return@filter false
                PriceFilter.PAID -> if (price <= 0) return@filter false
            }
            if (price > filter.maxPrice) return@filter false

            true
        }.let { list ->
            when (filter.sort) {
                SortOption.RELEVANT -> list.sortedByDescending { it.rating * 10 + it.enrolled }
                SortOption.SOONEST -> list.sortedBy { if (it.cls.startAt > 0) it.cls.startAt else Long.MAX_VALUE }
                SortOption.CHEAPEST -> list.sortedBy { it.cls.priceAt(now) }
                SortOption.RATING -> list.sortedByDescending { it.rating }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setRadarLocal(local: Boolean) = viewModelScope.launch {
        session.setRadarLocal(local)
    }

    fun setQuery(q: String) {
        _filterState.value = _filterState.value.copy(query = q)
    }

    fun updateFilter(filter: FilterState) {
        _filterState.value = filter
    }

    fun toggleCategory(cat: Category) {
        val current = _filterState.value.categories
        _filterState.value = _filterState.value.copy(
            categories = if (cat in current) current - cat else current + cat
        )
    }

    fun toggleFormat(format: ClassFormat) {
        val current = _filterState.value.formats
        _filterState.value = _filterState.value.copy(
            formats = if (format in current) current - format else current + format
        )
    }

    fun toggleCompare(classId: String) {
        val current = _compareList.value
        if (classId in current) {
            _compareList.value = current - classId
        } else if (current.size < AppConfig.MAX_COMPARE) {
            _compareList.value = current + classId
        }
    }

    fun clearCompare() {
        _compareList.value = emptyList()
    }
}
