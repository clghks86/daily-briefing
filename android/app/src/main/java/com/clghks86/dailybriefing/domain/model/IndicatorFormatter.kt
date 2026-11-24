package com.clghks86.dailybriefing.domain.model

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class ChangeDirection { UP, DOWN, FLAT }

/** 값·등락률 표기를 한 곳에서 관리한다. 순수 함수라 단위 테스트로 검증한다. */
object IndicatorFormatter {

    private fun decimalFormat(decimals: Int): DecimalFormat =
        DecimalFormat(
            "#,##0" + if (decimals > 0) "." + "0".repeat(decimals) else "",
            DecimalFormatSymbols(Locale.KOREA),
        ).apply {
            roundingMode = RoundingMode.HALF_UP
        }

    /** 1,234,567.89 형태의 천 단위 구분 문자열. */
    fun group(value: Double, decimals: Int): String = decimalFormat(decimals).format(value)

    fun formatValue(indicator: IndicatorId, value: Double): String =
        indicator.prefix + group(value, indicator.decimals) + indicator.suffix

    fun formatValueOrDash(id: IndicatorId, value: Double?, fallback: String = "-"): String =
        if (value == null) fallback else formatValue(id, value)

    /** +3.01% / -0.53% / 0.00% */
    fun formatChange(change: Double?): String {
        if (change == null) return "-"
        val sign = if (change > 0) "+" else ""
        return sign + group(change, 2) + "%"
    }

    fun directionOf(change: Double?): ChangeDirection = when {
        change == null -> ChangeDirection.FLAT
        change > 0.0 -> ChangeDirection.UP
        change < 0.0 -> ChangeDirection.DOWN
        else -> ChangeDirection.FLAT
    }

    /** 83,591,000원 → 8,359만원 처럼 카드 보조 줄에 맞춰 줄인 표기. */
    fun formatKrwCompact(value: Double): String {
        val abs = kotlin.math.abs(value)
        return when {
            abs >= 100_000_000 -> group(value / 100_000_000, 2) + "억원"
            abs >= 10_000 -> group(value / 10_000, 1) + "만원"
            else -> group(value, 0) + "원"
        }
    }

    /** 커버 카드 등에 쓰는 짧은 시각 표기: "2026-11-24 09:24 KST" → "11-24 09:24" */
    fun shortTimestamp(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val match = Regex("(\\d{4})-(\\d{2})-(\\d{2})[ T](\\d{2}:\\d{2})").find(raw) ?: return raw
        val (_, month, day, time) = match.destructured
        return "$month-$day $time"
    }
}
