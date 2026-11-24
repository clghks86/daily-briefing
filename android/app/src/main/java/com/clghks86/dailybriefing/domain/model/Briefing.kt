package com.clghks86.dailybriefing.domain.model

/** 한 날짜의 한 지표 값. 값이 없으면 null (휴장·수집 실패 등). */
data class IndicatorSnapshot(
    val value: Double? = null,
    val changePercent: Double? = null,
) {
    val hasValue: Boolean get() = value != null
}

data class NewsItem(
    val title: String,
    val url: String,
    val source: String? = null,
    val summary: String? = null,
    val category: String? = null,
)

data class BriefingImage(
    val url: String,
    val caption: String? = null,
    val sortOrder: Int = 0,
    val section: String? = null,
)

/** details JSONB 안에서 앱이 쓰는 값들. 없는 키는 null로 둔다. */
data class BriefingDetails(
    val asOfKst: String? = null,
    val keyEvents: List<String> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val marketNote: String? = null,
    val dxy: Double? = null,
    val vix: Double? = null,
    val wtiUsd: Double? = null,
    val goldEtf: GoldEtf? = null,
) {
    data class GoldEtf(
        val aceKrx: Double? = null,
        val aceChg: Double? = null,
        val tigerKrx: Double? = null,
        val tigerChg: Double? = null,
    )
}

/**
 * 하루치 브리핑. Supabase `daily_briefings` 한 행 + 자식 `briefing_images`.
 */
data class Briefing(
    val date: String,
    val title: String,
    val summary: String,
    val indicators: Map<IndicatorId, IndicatorSnapshot>,
    val fearGreed: Int?,
    val details: BriefingDetails,
    val rawMarkdown: String,
    val images: List<BriefingImage> = emptyList(),
    val updatedAt: String? = null,
) {
    fun indicator(id: IndicatorId): IndicatorSnapshot = indicators[id] ?: IndicatorSnapshot()
    fun value(id: IndicatorId): Double? = indicators[id]?.value
    fun change(id: IndicatorId): Double? = indicators[id]?.changePercent

    /** 커버(1) · 코인/금(2) · 섹션(3) 이미지 규칙은 대시보드와 동일하다. */
    fun image(sortOrder: Int): BriefingImage? = images.firstOrNull { it.sortOrder == sortOrder }

    /** 상단 브랜드와 겹치는 "[데일리 브리핑]" 꼬리표만 떼어 낸 제목. */
    val headline: String
        get() = title
            .replace(Regex("^\\s*\\[?\\s*데일리\\s*브리핑\\s*\\]?\\s*"), "")
            .ifBlank { title }

    val fearGreedLabel: String?
        get() = fearGreed?.let {
            when {
                it <= 24 -> "극단적 공포"
                it <= 44 -> "공포"
                it <= 55 -> "중립"
                it <= 75 -> "탐욕"
                else -> "극단적 탐욕"
            }
        }
}
