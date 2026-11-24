package com.clghks86.dailybriefing.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.data.local.UserPreferencesRepository
import com.clghks86.dailybriefing.domain.model.GoalDirection
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.PriceGoal
import com.clghks86.dailybriefing.domain.model.ThemeMode
import com.clghks86.dailybriefing.work.WorkScheduler
import com.clghks86.dailybriefing.widget.BriefingWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SettingsUiState(
    val pinned: List<IndicatorId> = emptyList(),
    val goals: List<PriceGoal> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.DARK,
    val notifyEnabled: Boolean = true,
    val lastNotifiedDate: String? = null,
    val message: String? = null,
)

class SettingsViewModel(
    private val preferences: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.pinnedIndicators.collect { pins -> _state.update { it.copy(pinned = pins) } }
        }
        viewModelScope.launch {
            preferences.goals.collect { goals -> _state.update { it.copy(goals = goals) } }
        }
        viewModelScope.launch {
            preferences.themeMode.collect { mode -> _state.update { it.copy(themeMode = mode) } }
        }
        viewModelScope.launch {
            preferences.briefingNotificationEnabled.collect { enabled ->
                _state.update { it.copy(notifyEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferences.lastNotifiedDate.collect { date -> _state.update { it.copy(lastNotifiedDate = date) } }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setBriefingNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBriefingNotificationEnabled(enabled)
            val context = ServiceLocator.appContext
            if (enabled) {
                WorkScheduler.scheduleDailyBriefingCheck(context)
                showMessage("매일 09:35경 새 브리핑을 알려 드립니다.")
            } else {
                WorkScheduler.cancelDailyBriefingCheck(context)
                showMessage("브리핑 알림을 껐습니다.")
            }
        }
    }

    fun addGoal(indicator: IndicatorId, direction: GoalDirection, target: Double) {
        if (target <= 0.0) return
        viewModelScope.launch {
            preferences.upsertGoal(
                PriceGoal(
                    id = UUID.randomUUID().toString(),
                    indicator = indicator.name,
                    direction = direction,
                    target = target,
                ),
            )
            WorkScheduler.schedulePriceGoalCheck(ServiceLocator.appContext)
            showMessage("목표가를 등록했습니다. 15분 간격으로 확인합니다.")
        }
    }

    fun setGoalEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch { preferences.setGoalEnabled(id, enabled) }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch { preferences.deleteGoal(id) }
    }

    fun togglePin(id: IndicatorId) {
        viewModelScope.launch {
            val current = _state.value.pinned
            when {
                current.contains(id) -> preferences.setPinnedIndicators(current - id)
                current.size >= UserPreferencesRepository.MAX_PINS ->
                    showMessage("관심 지표는 ${UserPreferencesRepository.MAX_PINS}개까지 고정할 수 있습니다.")
                else -> preferences.setPinnedIndicators(current + id)
            }
        }
    }

    fun sendTestNotification() {
        val posted = ServiceLocator.notifications.notifyTest("데일리 브리핑 테스트 알림")
        showMessage(
            if (posted) "테스트 알림을 보냈습니다." else "알림 권한이 없어 보내지 못했습니다. 시스템 설정에서 알림을 허용해 주세요.",
        )
    }

    fun runBriefingCheckNow() {
        viewModelScope.launch {
            val briefing = ServiceLocator.briefingRepository.latest(forceRefresh = true).getOrNull()
            if (briefing == null) {
                showMessage("최신 브리핑을 가져오지 못했습니다. 네트워크를 확인해 주세요.")
                return@launch
            }
            val lastNotified = _state.value.lastNotifiedDate
            if (lastNotified == briefing.date) {
                showMessage("오늘(${briefing.date}) 브리핑은 이미 알림을 보냈습니다.")
            } else {
                val posted = ServiceLocator.notifications.notifyBriefing(briefing)
                if (posted) {
                    preferences.setLastNotifiedDate(briefing.date)
                    showMessage("새 브리핑 알림을 보냈습니다.")
                } else {
                    showMessage("알림 권한이 없어 보내지 못했습니다.")
                }
            }
        }
    }

    fun refreshWidget() {
        viewModelScope.launch {
            val context = ServiceLocator.appContext
            ServiceLocator.briefingRepository.latest(forceRefresh = true)
                .getOrNull()
                ?.let { ServiceLocator.widgetSnapshotStore.save(it) }
            runCatching { BriefingWidget().updateAll(context) }
            showMessage("홈 화면 위젯을 새로 고쳤습니다.")
        }
    }

    fun showMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(ServiceLocator.userPreferences) }
        }
    }
}
