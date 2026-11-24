package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.ui.util.DateLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateLabelsTest {

    private val today = LocalDate.of(2026, 11, 24)

    @Test
    fun `chip shows month day and korean weekday`() {
        assertEquals("11.24 (화)", DateLabels.chip("2026-11-24", today))
        assertEquals("01.03 (토)", DateLabels.chip("2026-01-03", today))
    }

    @Test
    fun `past year includes the year`() {
        assertEquals("25.12.31 (수)", DateLabels.chip("2025-12-31", today))
    }

    @Test
    fun `today and yesterday are spoken`() {
        assertEquals("오늘", DateLabels.relativeChip("2026-11-24", today))
        assertEquals("어제", DateLabels.relativeChip("2026-11-23", today))
        assertEquals("11.22 (일)", DateLabels.relativeChip("2026-11-22", today))
    }

    @Test
    fun `full label includes year and weekday`() {
        assertEquals("2026년 11월 24일 (화)", DateLabels.full("2026-11-24"))
    }

    @Test
    fun `axis label is month day`() {
        assertEquals("11.24", DateLabels.axis("2026-11-24"))
    }

    @Test
    fun `malformed date falls back to the raw string`() {
        assertEquals("not-a-date", DateLabels.chip("not-a-date", today))
        assertEquals("not-a-date", DateLabels.full("not-a-date"))
    }

    @Test
    fun `isToday compares calendar date`() {
        assertTrue(DateLabels.isToday("2026-11-24", today))
        assertFalse(DateLabels.isToday("2026-11-23", today))
    }
}
