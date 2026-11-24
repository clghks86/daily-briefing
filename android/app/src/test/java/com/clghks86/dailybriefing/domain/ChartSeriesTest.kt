package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.TestData
import com.clghks86.dailybriefing.domain.chart.ChartSeriesBuilder
import com.clghks86.dailybriefing.domain.model.ChartGroup
import com.clghks86.dailybriefing.domain.model.IndicatorId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChartSeriesTest {

    private val briefings = listOf(
        TestData.briefing(
            date = "2026-11-22",
            values = mapOf(
                IndicatorId.KR_KOSPI to (4800.0 to -1.0),
                IndicatorId.US_NASDAQ to (20000.0 to 1.0),
            ),
        ),
        TestData.briefing(
            date = "2026-11-20",
            values = mapOf(
                IndicatorId.KR_KOSPI to (5000.0 to 0.5),
                IndicatorId.US_NASDAQ to (21000.0 to 0.2),
            ),
        ),
        TestData.briefing(
            date = "2026-11-21",
            values = mapOf(IndicatorId.US_NASDAQ to (20500.0 to -0.5)),
        ),
    )

    @Test
    fun `dates are sorted ascending and values aligned per date`() {
        val series = ChartSeriesBuilder.build(
            briefings = briefings,
            ids = listOf(IndicatorId.KR_KOSPI, IndicatorId.US_NASDAQ),
            rebase = false,
        )

        assertEquals(listOf("2026-11-20", "2026-11-21", "2026-11-22"), series.dates)
        val kospi = series.lines.first { it.indicator == IndicatorId.KR_KOSPI }
        assertEquals(listOf(5000.0, null, 4800.0), kospi.values)
    }

    @Test
    fun `rebase puts the first value at 100`() {
        val series = ChartSeriesBuilder.build(
            briefings = briefings,
            ids = listOf(IndicatorId.KR_KOSPI),
            rebase = true,
        )
        val line = series.lines.single()
        val plotted = series.plottedValues(line)

        assertEquals(100.0, plotted[0]!!, 0.0001)
        assertNull(plotted[1])
        assertEquals(96.0, plotted[2]!!, 0.0001)
    }

    @Test
    fun `actual value mode keeps raw numbers`() {
        val series = ChartSeriesBuilder.build(
            briefings = briefings,
            ids = listOf(IndicatorId.US_NASDAQ),
            rebase = false,
        )
        assertEquals(listOf(21000.0, 20500.0, 20000.0), series.plottedValues(series.lines.single()))
    }

    @Test
    fun `series without any value is dropped`() {
        val series = ChartSeriesBuilder.build(
            briefings = briefings,
            ids = listOf(IndicatorId.KR_KOSPI, IndicatorId.BTC_USD),
            rebase = false,
        )
        assertEquals(1, series.lines.size)
        assertEquals(IndicatorId.KR_KOSPI, series.lines.single().indicator)
    }

    @Test
    fun `range covers both lines`() {
        val series = ChartSeriesBuilder.build(
            briefings = briefings,
            ids = listOf(IndicatorId.KR_KOSPI, IndicatorId.US_NASDAQ),
            rebase = false,
        )
        val range = series.rangeOf(0, series.dates.size - 1)!!

        assertEquals(4800.0, range.start, 0.0001)
        assertEquals(21000.0, range.endInclusive, 0.0001)
    }

    @Test
    fun `group builder picks chartable indicators for the tab`() {
        val kospiSeries = ChartSeriesBuilder.forGroup(briefings, ChartGroup.INDEX)
        assertTrue(kospiSeries.lines.any { it.indicator == IndicatorId.KR_KOSPI })
        assertTrue(kospiSeries.rebase)

        val goldSeries = ChartSeriesBuilder.forGroup(briefings, ChartGroup.GOLD)
        assertTrue(goldSeries.isEmpty())
        assertTrue(!goldSeries.rebase)
    }
}
