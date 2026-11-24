package com.clghks86.dailybriefing.domain.chart

import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.ChartGroup
import com.clghks86.dailybriefing.domain.model.IndicatorId

/** 날짜 축에 맞춰 정렬된 한 시리즈. values[i] 는 dates[i] 의 값(없으면 null). */
data class ChartLine(
    val indicator: IndicatorId,
    val values: List<Double?>,
)

data class ChartSeries(
    val dates: List<String>,
    val lines: List<ChartLine>,
    val rebase: Boolean,
) {
    fun isEmpty(): Boolean = dates.isEmpty() || lines.all { line -> line.values.all { it == null } }

    /** 첫 값(0이 아닌)을 100으로 환산한 기준값. */
    fun baseOf(line: ChartLine): Double? =
        line.values.firstOrNull { it != null && it != 0.0 }

    /** 차트에 그릴 값 목록. rebase면 첫 값을 100으로 맞춘다. */
    fun plottedValues(line: ChartLine): List<Double?> {
        val base = baseOf(line) ?: return line.values.map { null }
        return line.values.map { v -> v?.let { if (rebase) it / base * 100.0 else it } }
    }

    /** [from]~[to] 구간의 최소·최대(표시 값 기준). */
    fun rangeOf(from: Int, to: Int): ClosedFloatingPointRange<Double>? {
        var min = Double.MAX_VALUE
        var max = -Double.MAX_VALUE
        var found = false
        lines.forEach { line ->
            plottedValues(line).forEachIndexed { index, v ->
                if (index in from..to && v != null) {
                    found = true
                    if (v < min) min = v
                    if (v > max) max = v
                }
            }
        }
        return if (found) min..max else null
    }
}

object ChartSeriesBuilder {

    /**
     * @param briefings 날짜 오름차순으로 정렬해 넘길 필요 없음 — 내부에서 정렬한다.
     * @param ids 그릴 지표들 (순서 유지)
     * @param rebase 첫 값을 100으로 환산할지 (지수·코인 비교용)
     */
    fun build(
        briefings: List<Briefing>,
        ids: List<IndicatorId>,
        rebase: Boolean,
    ): ChartSeries {
        val sorted = briefings.sortedWith(compareBy { it.date })
        val dates = sorted.map { it.date }

        val lines = ids.map { id ->
            ChartLine(indicator = id, values = sorted.map { it.value(id) })
        }
        // 값이 전혀 없는 시리즈는 차트에서 제외한다.
        val visible = lines.filter { line -> line.values.any { it != null } }
        return ChartSeries(dates = dates, lines = visible, rebase = rebase)
    }

    fun forGroup(briefings: List<Briefing>, group: ChartGroup): ChartSeries {
        val rebase = group == ChartGroup.INDEX || group == ChartGroup.CRYPTO
        return build(briefings, IndicatorId.chartableOf(group), rebase)
    }
}
