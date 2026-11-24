package com.clghks86.dailybriefing.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.data.local.UserPreferencesRepository
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.repository.BriefingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val briefings: List<Briefing> = emptyList(),
    val selectedDate: String? = null,
    val pinned: List<IndicatorId> = emptyList(),
    val bookmarks: List<String> = emptyList(),
    val errorMessage: String? = null,
) {
    val selected: Briefing?
        get() = briefings.firstOrNull { it.date == selectedDate } ?: briefings.firstOrNull()

    val isBookmarked: Boolean
        get() = selected?.date?.let { date -> bookmarks.contains(date) } ?: false

    val availableDates: List<String> get() = briefings.map { it.date }
}

class HomeViewModel(
    private val repository: BriefingRepository,
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        observePreferences()
        load(forceRefresh = false)
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferences.pinnedIndicators.collect { pinned ->
                _state.update { it.copy(pinned = pinned) }
            }
        }
        viewModelScope.launch {
            preferences.bookmarks.collect { bookmarks ->
                _state.update { it.copy(bookmarks = bookmarks) }
            }
        }
    }

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update {
                it.copy(loading = it.briefings.isEmpty(), refreshing = forceRefresh)
            }
            val result = repository.briefings(forceRefresh)
            result.fold(
                onSuccess = { briefings ->
                    _state.update { current ->
                        val selected = current.selectedDate
                            ?.takeIf { date -> briefings.any { it.date == date } }
                            ?: briefings.firstOrNull()?.date
                        current.copy(
                            loading = false,
                            refreshing = false,
                            briefings = briefings,
                            selectedDate = selected,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            refreshing = false,
                            errorMessage = friendlyError(error),
                        )
                    }
                },
            )
        }
    }

    fun refresh() = load(forceRefresh = true)

    fun selectDate(date: String) {
        _state.update { it.copy(selectedDate = date) }
    }

    fun toggleBookmark() {
        val date = _state.value.selected?.date ?: return
        viewModelScope.launch { preferences.toggleBookmark(date) }
    }

    fun togglePin(id: IndicatorId) {
        viewModelScope.launch {
            val current = _state.value.pinned
            if (current.contains(id)) {
                preferences.setPinnedIndicators(current - id)
            } else {
                if (current.size >= UserPreferencesRepository.MAX_PINS) {
                    // 4개까지만 — 화면에서 안내 문구를 띄운다.
                    _state.update { it.copy(errorMessage = "관심 지표는 ${UserPreferencesRepository.MAX_PINS}개까지 고정할 수 있습니다.") }
                    return@launch
                }
                preferences.setPinnedIndicators(current + id)
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun friendlyError(error: Throwable): String = when {
        error is java.net.UnknownHostException -> "네트워크에 연결할 수 없습니다. 연결 후 당겨서 새로고침해 주세요."
        error is java.net.SocketTimeoutException -> "응답이 늦어지고 있습니다. 잠시 후 다시 시도해 주세요."
        error is java.io.IOException -> "데이터를 불러오지 못했습니다. 네트워크 상태를 확인해 주세요."
        else -> "브리핑을 불러오지 못했습니다: ${error.message ?: error::class.simpleName}"
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(ServiceLocator.briefingRepository, ServiceLocator.userPreferences)
            }
        }
    }
}
