package com.clghks86.dailybriefing.data.local

import android.content.Context
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.domain.model.IndicatorId
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 홈 화면 위젯이 마지막으로 그린 값. Glance는 매번 네트워크를 기다릴 수 없어 캐시를 쓴다. */
@Serializable
data class WidgetSnapshot(
    val date: String = "",
    val timeLabel: String? = null,
    val kospi: Double? = null,
    val kospiChg: Double? = null,
    val btcUsd: Double? = null,
    val btcKrw: Double? = null,
    val btcChg: Double? = null,
    val goldKrx: Double? = null,
    val goldChg: Double? = null,
    val headline: String = "",
    val updatedAtMillis: Long = 0L,
) {
    val hasData: Boolean get() = kospi != null || btcUsd != null || goldKrx != null || headline.isNotBlank()

    companion object {
        fun of(briefing: Briefing, nowMillis: Long = System.currentTimeMillis()): WidgetSnapshot =
            WidgetSnapshot(
                date = briefing.date,
                timeLabel = IndicatorFormatter.shortTimestamp(briefing.details.asOfKst),
                kospi = briefing.value(IndicatorId.KR_KOSPI),
                kospiChg = briefing.change(IndicatorId.KR_KOSPI),
                btcUsd = briefing.value(IndicatorId.BTC_USD),
                btcKrw = briefing.value(IndicatorId.BTC_KRW),
                btcChg = briefing.change(IndicatorId.BTC_USD),
                goldKrx = briefing.value(IndicatorId.GOLD_KRX),
                goldChg = briefing.change(IndicatorId.GOLD_KRX),
                headline = briefing.headline,
                updatedAtMillis = nowMillis,
            )
    }
}

/** SharedPreferences 한 곳에 위젯용 스냅숏을 남긴다(앱·워커·위젯이 공유). */
class WidgetSnapshotStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun save(briefing: Briefing) = save(WidgetSnapshot.of(briefing))

    fun save(snapshot: WidgetSnapshot) {
        prefs.edit().putString(KEY_SNAPSHOT, json.encodeToString(WidgetSnapshot.serializer(), snapshot)).apply()
    }

    fun load(): WidgetSnapshot? {
        val raw = prefs.getString(KEY_SNAPSHOT, null) ?: return null
        return runCatching { json.decodeFromString(WidgetSnapshot.serializer(), raw) }.getOrNull()
    }

    companion object {
        private const val PREFS_NAME = "daily_briefing_widget"
        private const val KEY_SNAPSHOT = "snapshot"
    }
}
