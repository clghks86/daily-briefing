package com.clghks86.dailybriefing.ui.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 날짜 문자열(yyyy-MM-dd) → 화면용 라벨. 순수 함수라 테스트로 검증한다. */
object DateLabels {

    private val korean = Locale.KOREAN
    private val weekday = DateTimeFormatter.ofPattern("E", korean)

    fun parse(date: String): LocalDate? = runCatching { LocalDate.parse(date) }.getOrNull()

    /** 날짜 칩: "11.24 (화)" / 올해가 아니면 "25.01.03 (금)" */
    fun chip(date: String, today: LocalDate = LocalDate.now()): String {
        val parsed = parse(date) ?: return date
        val day = if (parsed.year == today.year) {
            parsed.format(DateTimeFormatter.ofPattern("MM.dd"))
        } else {
            parsed.format(DateTimeFormatter.ofPattern("yy.MM.dd"))
        }
        return "$day (${parsed.format(weekday)})"
    }

    /** 오늘/어제는 말로, 나머지는 "11.24 (화)" */
    fun relativeChip(date: String, today: LocalDate = LocalDate.now()): String {
        val parsed = parse(date) ?: return date
        return when (parsed) {
            today -> "오늘"
            today.minusDays(1) -> "어제"
            else -> chip(date, today)
        }
    }

    /** "2026년 11월 24일 (화)" */
    fun full(date: String): String {
        val parsed = parse(date) ?: return date
        return parsed.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", korean))
    }

    /** 차트 축 라벨: "11.24" */
    fun axis(date: String): String = parse(date)?.format(DateTimeFormatter.ofPattern("MM.dd")) ?: date

    fun isToday(date: String, today: LocalDate = LocalDate.now()): Boolean = parse(date) == today
}
