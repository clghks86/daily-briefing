package com.clghks86.dailybriefing.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.repository.BriefingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val loading: Boolean = true,
    val briefings: List<Briefing> = emptyList(),
    val selectedDate: String? = null,
    val errorMessage: String? = null,
) {
    val selected: Briefing?
        get() = briefings.firstOrNull { it.date == selectedDate } ?: briefings.firstOrNull()
}

class NewsViewModel(
    private val repository: BriefingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(NewsUiState())
    val state: StateFlow<NewsUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.briefings.isEmpty()) }
            repository.briefings(forceRefresh).fold(
                onSuccess = { briefings ->
                    _state.update { current ->
                        current.copy(
                            loading = false,
                            briefings = briefings,
                            selectedDate = current.selectedDate
                                ?.takeIf { date -> briefings.any { it.date == date } }
                                ?: briefings.firstOrNull()?.date,
                            errorMessage = null,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, errorMessage = error.message ?: "불러오기 실패") }
                },
            )
        }
    }

    fun selectDate(date: String) {
        _state.update { it.copy(selectedDate = date) }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer { NewsViewModel(ServiceLocator.briefingRepository) }
        }
    }
}
