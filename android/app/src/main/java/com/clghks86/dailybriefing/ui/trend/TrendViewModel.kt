package com.clghks86.dailybriefing.ui.trend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.domain.chart.ChartSeries
import com.clghks86.dailybriefing.domain.chart.ChartSeriesBuilder
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.ChartGroup
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.repository.BriefingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrendUiState(
    val loading: Boolean = true,
    val briefings: List<Briefing> = emptyList(),
    val group: ChartGroup = ChartGroup.INDEX,
    val rebase: Boolean = true,
    val series: ChartSeries? = null,
    val errorMessage: String? = null,
)

class TrendViewModel(
    private val repository: BriefingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(TrendUiState())
    val state: StateFlow<TrendUiState> = _state.asStateFlow()

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
                            errorMessage = null,
                        )
                    }
                    rebuild()
                },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, errorMessage = error.message ?: "불러오기 실패") }
                },
            )
        }
    }

    fun selectGroup(group: ChartGroup) {
        _state.update { it.copy(group = group, rebase = defaultRebase(group)) }
        rebuild()
    }

    fun toggleRebase() {
        _state.update { it.copy(rebase = !it.rebase) }
        rebuild()
    }

    private fun rebuild() {
        val current = _state.value
        val ids = IndicatorId.chartableOf(current.group)
        val series = ChartSeriesBuilder.build(current.briefings, ids, current.rebase)
        _state.update { it.copy(series = series) }
    }

    private fun defaultRebase(group: ChartGroup): Boolean = when (group) {
        ChartGroup.INDEX, ChartGroup.CRYPTO -> true
        ChartGroup.GOLD, ChartGroup.FX_RATE -> false
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer { TrendViewModel(ServiceLocator.briefingRepository) }
        }
    }
}
