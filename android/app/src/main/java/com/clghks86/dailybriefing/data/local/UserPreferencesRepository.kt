package com.clghks86.dailybriefing.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.PriceGoal
import com.clghks86.dailybriefing.domain.model.ThemeMode
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * 앱 전용 설정 저장소 — 핀 고정, 북마크, 목표가, 테마, 알림 상태.
 * 모두 로컬(DataStore)에만 남고 서버로 나가지 않는다.
 */
class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val safeData: Flow<Preferences> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences())
        else throw error
    }

    val pinnedIndicators: Flow<List<IndicatorId>> = safeData.map { prefs ->
        val raw = prefs[KEY_PINNED]
        if (raw == null) {
            IndicatorId.pinDefaults().mapNotNull { IndicatorId.fromKey(it) }
        } else {
            raw.split(',').mapNotNull { IndicatorId.fromKey(it.trim()) }
        }
    }

    val bookmarks: Flow<List<String>> = safeData.map { prefs ->
        prefs[KEY_BOOKMARKS]?.split(',')?.filter { it.isNotBlank() }.orEmpty()
    }

    val goals: Flow<List<PriceGoal>> = safeData.map { prefs ->
        decodeGoals(prefs[KEY_GOALS])
    }

    val themeMode: Flow<ThemeMode> = safeData.map { prefs ->
        prefs[KEY_THEME]?.let { raw -> ThemeMode.entries.firstOrNull { it.name == raw } } ?: ThemeMode.DARK
    }

    val briefingNotificationEnabled: Flow<Boolean> = safeData.map { prefs ->
        prefs[KEY_NOTIFY_ENABLED] ?: true
    }

    val lastNotifiedDate: Flow<String?> = safeData.map { prefs -> prefs[KEY_LAST_NOTIFIED] }

    suspend fun setPinnedIndicators(ids: List<IndicatorId>) {
        dataStore.edit { prefs ->
            prefs[KEY_PINNED] = ids.distinct().joinToString(",") { it.name }
        }
    }

    suspend fun togglePin(id: IndicatorId) {
        val current = pinnedSnapshot()
        val next = if (current.contains(id)) current - id else current + id
        setPinnedIndicators(next.take(MAX_PINS))
    }

    suspend fun pinnedSnapshot(): List<IndicatorId> =
        decodePins(safeData.first()[KEY_PINNED])

    suspend fun toggleBookmark(date: String) {
        dataStore.edit { prefs ->
            val current = prefs[KEY_BOOKMARKS]?.split(',')?.filter { it.isNotBlank() }.orEmpty()
            val next = if (current.contains(date)) current - date else current + date
            prefs[KEY_BOOKMARKS] = next.sortedDescending().joinToString(",")
        }
    }

    suspend fun setBookmarks(dates: List<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_BOOKMARKS] = dates.distinct().sortedDescending().joinToString(",")
        }
    }

    suspend fun setGoals(goals: List<PriceGoal>) {
        dataStore.edit { prefs ->
            prefs[KEY_GOALS] = json.encodeToString(ListSerializer(PriceGoal.serializer()), goals)
        }
    }

    suspend fun upsertGoal(goal: PriceGoal) {
        val current = goalsSnapshot()
        val index = current.indexOfFirst { it.id == goal.id }
        val next = if (index >= 0) {
            current.toMutableList().also { it[index] = goal }
        } else {
            current + goal
        }
        setGoals(next)
    }

    suspend fun deleteGoal(id: String) {
        setGoals(goalsSnapshot().filterNot { it.id == id })
    }

    suspend fun setGoalEnabled(id: String, enabled: Boolean) {
        setGoals(goalsSnapshot().map { if (it.id == id) it.copy(enabled = enabled) else it })
    }

    suspend fun goalsSnapshot(): List<PriceGoal> =
        decodeGoals(safeData.first()[KEY_GOALS])

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs -> prefs[KEY_THEME] = mode.name }
    }

    suspend fun setBriefingNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_NOTIFY_ENABLED] = enabled }
    }

    suspend fun setLastNotifiedDate(date: String) {
        dataStore.edit { prefs -> prefs[KEY_LAST_NOTIFIED] = date }
    }

    private fun decodeGoals(raw: String?): List<PriceGoal> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(PriceGoal.serializer()), raw)
        }.getOrDefault(emptyList())
    }

    private fun decodePins(raw: String?): List<IndicatorId> {
        if (raw == null) return IndicatorId.pinDefaults().mapNotNull { IndicatorId.fromKey(it) }
        return raw.split(',').mapNotNull { IndicatorId.fromKey(it.trim()) }
    }

    companion object {
        const val MAX_PINS = 4

        private val KEY_PINNED = stringPreferencesKey("pinned_indicators")
        private val KEY_BOOKMARKS = stringPreferencesKey("bookmarked_dates")
        private val KEY_GOALS = stringPreferencesKey("price_goals")
        private val KEY_THEME = stringPreferencesKey("theme_mode")
        private val KEY_NOTIFY_ENABLED = booleanPreferencesKey("briefing_notify_enabled")
        private val KEY_LAST_NOTIFIED = stringPreferencesKey("last_notified_date")
    }
}
