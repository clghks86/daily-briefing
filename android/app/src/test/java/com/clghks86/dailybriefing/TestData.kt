package com.clghks86.dailybriefing

import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.BriefingDetails
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.IndicatorSnapshot
import com.clghks86.dailybriefing.domain.model.NewsItem

/** 테스트용 브리핑 생성기 — 네트워크 없이 도메인 로직만 검증할 때 쓴다. */
object TestData {

    fun briefing(
        date: String,
        values: Map<IndicatorId, Pair<Double?, Double?>> = emptyMap(),
        fearGreed: Int? = 50,
        details: BriefingDetails = BriefingDetails(),
        rawMarkdown: String = "# 테스트\n\n- 항목",
        title: String = "[데일리 브리핑] $date 주요 동향",
        summary: String = "요약",
    ): Briefing {
        val indicators = values.mapValues { (_, pair) ->
            IndicatorSnapshot(value = pair.first, changePercent = pair.second)
        }
        return Briefing(
            date = date,
            title = title,
            summary = summary,
            indicators = indicators,
            fearGreed = fearGreed,
            details = details,
            rawMarkdown = rawMarkdown,
        )
    }

    fun news(title: String = "뉴스 제목") = NewsItem(
        title = title,
        url = "https://example.com/news",
        source = "테스트일보",
        summary = "요약",
        category = "증시",
    )
}
