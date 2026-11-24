package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.domain.model.ChangeDirection
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.domain.model.IndicatorId
import org.junit.Assert.assertEquals
import org.junit.Test

class IndicatorFormatterTest {

    @Test
    fun `thousand separators and decimals`() {
        assertEquals("1,340.70", IndicatorFormatter.group(1340.7, 2))
        assertEquals("157,010", IndicatorFormatter.group(157010.0, 0))
        assertEquals("0.9414", IndicatorFormatter.group(0.941426, 4))
        assertEquals("49,595.31", IndicatorFormatter.group(49595.31, 2))
    }

    @Test
    fun `value format follows each indicator's prefix suffix and decimals`() {
        assertEquals("$61,195", IndicatorFormatter.formatValue(IndicatorId.BTC_USD, 61195.0))
        assertEquals("157,010원", IndicatorFormatter.formatValue(IndicatorId.GOLD_KRX, 157010.0))
        assertEquals("$0.9414", IndicatorFormatter.formatValue(IndicatorId.XRP_USD, 0.941426))
        assertEquals("4.94%", IndicatorFormatter.formatValue(IndicatorId.US_10Y_YIELD, 4.938))
        assertEquals("1,340.70원", IndicatorFormatter.formatValue(IndicatorId.USD_KRW, 1340.7))
    }

    @Test
    fun `null value shows dash`() {
        assertEquals("-", IndicatorFormatter.formatValueOrDash(IndicatorId.KR_KOSPI, null))
        assertEquals("1.00", IndicatorFormatter.formatValueOrDash(IndicatorId.KR_KOSPI, 1.0))
    }

    @Test
    fun `change percent has explicit plus for gains`() {
        assertEquals("+3.01%", IndicatorFormatter.formatChange(3.01))
        assertEquals("-0.53%", IndicatorFormatter.formatChange(-0.53))
        assertEquals("0.00%", IndicatorFormatter.formatChange(0.0))
        assertEquals("-", IndicatorFormatter.formatChange(null))
    }

    @Test
    fun `direction follows korean market convention`() {
        assertEquals(ChangeDirection.UP, IndicatorFormatter.directionOf(1.2))
        assertEquals(ChangeDirection.DOWN, IndicatorFormatter.directionOf(-1.2))
        assertEquals(ChangeDirection.FLAT, IndicatorFormatter.directionOf(0.0))
        assertEquals(ChangeDirection.FLAT, IndicatorFormatter.directionOf(null))
    }

    @Test
    fun `krw compact notation for card sub lines`() {
        assertEquals("8,359.1만원", IndicatorFormatter.formatKrwCompact(83_591_000.0))
        assertEquals("1.57억원", IndicatorFormatter.formatKrwCompact(157_010_000.0))
        assertEquals("1,287원", IndicatorFormatter.formatKrwCompact(1287.0))
    }

    @Test
    fun `timestamp is shortened but keeps date and time`() {
        assertEquals("11-24 09:24", IndicatorFormatter.shortTimestamp("2026-11-24 09:24 KST"))
        assertEquals("11-23 18:31", IndicatorFormatter.shortTimestamp("2026-11-23T18:31:00+09:00"))
        assertEquals(null, IndicatorFormatter.shortTimestamp("   "))
    }
}
