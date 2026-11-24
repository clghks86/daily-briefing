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

data class ArticleUiState(
    val loading: Boolean = true,
    val briefing: Briefing? = null,
    val errorMessage: String? = null,
)

/** 특정 날짜의 브리핑 전문(raw_markdown)을 보여 주는 화면의 상태. */
class ArticleViewModel(
    private val repository: BriefingRepository,
    private val date: String,
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleUiState())
    val state: StateFlow<ArticleUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.update { it.copy(loading = it.briefing == null) }
            repository.briefings(forceRefresh).fold(
                onSuccess = { briefings ->
                    val target = briefings.firstOrNull { it.date == date }
                    _state.update {
                        it.copy(
                            loading = false,
                            briefing = target,
                            errorMessage = if (target == null) "해당 날짜의 브리핑을 찾지 못했습니다." else null,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(loading = false, errorMessage = error.message ?: "불러오기 실패")
                    }
                },
            )
        }
    }

    companion object {
        fun factory(date: String): ViewModelProvider.Factory = viewModelFactory {
            initializer { ArticleViewModel(ServiceLocator.briefingRepository, date) }
        }
    }
}
