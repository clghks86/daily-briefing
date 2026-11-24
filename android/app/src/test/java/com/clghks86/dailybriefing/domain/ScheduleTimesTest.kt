package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.TestData
import com.clghks86.dailybriefing.data.local.WidgetSnapshot
import com.clghks86.dailybriefing.domain.model.BriefingDetails
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.work.ScheduleTimes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class ScheduleTimesTest {

    private val seoul = ZoneId.of("Asia/Seoul")

    private fun at(hour: Int, minute: Int): ZonedDateTime =
        ZonedDateTime.of(2026, 11, 24, hour, minute, 0, 0, seoul)

    @Test
    fun `before 09 35 waits until today`() {
        val millis = ScheduleTimes.millisUntilNext(at(8, 0), 9, 35)
        assertEquals(95 * 60 * 1000L, millis)
    }

    @Test
    fun `after 09 35 waits until tomorrow`() {
        val millis = ScheduleTimes.millisUntilNext(at(10, 0), 9, 35)
        assertEquals((23 * 60 + 35) * 60 * 1000L, millis)
    }

    @Test
    fun `exactly at the time schedules the next day`() {
        val millis = ScheduleTimes.millisUntilNext(at(9, 35), 9, 35)
        assertEquals(24 * 60 * 60 * 1000L, millis)
    }

    @Test
    fun `catch up check is only needed after the notification time`() {
        assertFalse(ScheduleTimes.isPastNotificationTime(at(9, 34), 9, 35))
        assertTrue(ScheduleTimes.isPastNotificationTime(at(9, 35), 9, 35))
        assertTrue(ScheduleTimes.isPastNotificationTime(at(21, 10), 9, 35))
    }
}

class WidgetSnapshotTest {

    private val briefing = TestData.briefing(
        date = "2026-11-24",
        values = mapOf(
            IndicatorId.KR_KOSPI to (4994.21 to 6.07),
            IndicatorId.BTC_USD to (61195.0 to -0.03),
            IndicatorId.BTC_KRW to (83591000.0 to -0.03),
            IndicatorId.GOLD_KRX to (157010.0 to 0.01),
        ),
        details = BriefingDetails(asOfKst = "2026-11-24 09:24 KST"),
    )

    @Test
    fun `snapshot copies the four widget values and the headline`() {
        val snapshot = WidgetSnapshot.of(briefing, nowMillis = 1_000L)

        assertEquals("2026-11-24", snapshot.date)
        assertEquals(4994.21, snapshot.kospi!!, 0.001)
        assertEquals(6.07, snapshot.kospiChg!!, 0.001)
        assertEquals(61195.0, snapshot.btcUsd!!, 0.001)
        assertEquals(83591000.0, snapshot.btcKrw!!, 0.001)
        assertEquals(157010.0, snapshot.goldKrx!!, 0.001)
        assertEquals("11-24 09:24", snapshot.timeLabel)
        assertEquals(1_000L, snapshot.updatedAtMillis)
        assertTrue(snapshot.hasData)
        assertFalse(snapshot.headline.contains("데일리 브리핑"))
    }
}
