package com.clghks86.dailybriefing.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class GoalDirection(val label: String, val symbol: String) {
    ABOVE("이상", "≥"),
    BELOW("이하", "≤"),
}

/**
 * 목표가 알림 설정 한 건. DataStore에 JSON으로 저장한다.
 * lastFiredDate 덕분에 같은 날 같은 목표로 알림이 반복되지 않는다.
 */
@Serializable
data class PriceGoal(
    val id: String,
    val indicator: String,
    val direction: GoalDirection,
    val target: Double,
    val enabled: Boolean = true,
    val lastFiredDate: String? = null,
) {
    val indicatorId: IndicatorId? get() = IndicatorId.fromKey(indicator)

    fun describe(): String {
        val id = indicatorId ?: return indicator
        return "${id.label} ${direction.symbol} ${IndicatorFormatter.formatValue(id, target)}"
    }
}
