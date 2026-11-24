package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.data.remote.dto.BriefingDto
import com.clghks86.dailybriefing.data.remote.dto.BriefingImageDto
import com.clghks86.dailybriefing.domain.mapper.BriefingMapper
import com.clghks86.dailybriefing.domain.model.IndicatorId
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 실제 Supabase 응답(src/test/resources/briefings.json, images.json)을 그대로 넣고
 * DTO → 도메인 변환이 흔들리지 않는지 확인한다.
 */
class BriefingMapperTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; explicitNulls = false }

    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(name)) { "$name 없음" }
            .bufferedReader().use { it.readText() }

    private fun rows(): List<BriefingDto> =
        json.decodeFromString(ListSerializer(BriefingDto.serializer()), fixture("briefings.json"))

    private fun images(): List<BriefingImageDto> =
        json.decodeFromString(ListSerializer(BriefingImageDto.serializer()), fixture("images.json"))

    @Test
    fun `rows are mapped and sorted newest first`() {
        val briefings = BriefingMapper.map(rows(), images())

        assertEquals(2, briefings.size)
        assertEquals("2026-11-24", briefings[0].date)
        assertEquals("2026-11-23", briefings[1].date)
    }

    @Test
    fun `indicator values and changes come from matching columns`() {
        val latest = BriefingMapper.map(rows(), images()).first()

        assertEquals(4994.21, latest.value(IndicatorId.KR_KOSPI)!!, 0.001)
        assertEquals(6.07, latest.change(IndicatorId.KR_KOSPI)!!, 0.001)
        assertEquals(24089.56, latest.value(IndicatorId.US_NASDAQ)!!, 0.001)
        assertEquals(157010.0, latest.value(IndicatorId.GOLD_KRX)!!, 0.001)
        assertEquals(61195.0, latest.value(IndicatorId.BTC_USD)!!, 0.001)
        assertEquals(83591000.0, latest.value(IndicatorId.BTC_KRW)!!, 0.001)
        assertEquals(1340.7, latest.value(IndicatorId.USD_KRW)!!, 0.001)
        assertEquals(11, latest.fearGreed)
        assertEquals("극단적 공포", latest.fearGreedLabel)
    }

    @Test
    fun `columns without a change keep null change but keep value`() {
        val latest = BriefingMapper.map(rows(), images()).first()

        assertNotNull(latest.value(IndicatorId.USD_KRW))
        assertNull(latest.change(IndicatorId.USD_KRW))
        assertNull(latest.change(IndicatorId.USDT_USD))
    }

    @Test
    fun `details json is parsed into news, key events, note and etf`() {
        val details = BriefingMapper.map(rows(), images()).first().details

        assertEquals("2026-11-24 09:24 KST", details.asOfKst)
        assertEquals(5, details.news.size)
        assertEquals(5, details.keyEvents.size)
        assertEquals("연합뉴스", details.news.first().source)
        assertTrue(details.news.first().url.startsWith("https://"))
        assertNotNull(details.marketNote)
        assertEquals(104.32, details.dxy!!, 0.001)
        assertEquals(26.62, details.vix!!, 0.001)
        assertEquals(80.46, details.wtiUsd!!, 0.001)
        assertNotNull(details.goldEtf?.aceKrx)
    }

    @Test
    fun `images are attached to their own date and ordered`() {
        val briefings = BriefingMapper.map(rows(), images())
        val latest = briefings.first()

        assertEquals(3, latest.images.size)
        assertEquals(1, latest.images[0].sortOrder)
        assertTrue(latest.image(1)!!.url.contains("01-cover"))
        assertTrue(latest.image(2)!!.url.contains("02-crypto"))
        assertTrue(latest.image(3)!!.url.contains("03-global"))
        // 11-23 브리핑에는 이미지가 없다.
        assertTrue(briefings[1].images.isEmpty())
        assertNull(briefings[1].image(1))
    }

    @Test
    fun `headline drops the daily briefing prefix`() {
        val latest = BriefingMapper.map(rows(), images()).first()

        assertTrue(latest.headline.startsWith("2026-11-24"))
        assertTrue(!latest.headline.contains("데일리 브리핑"))
    }

    @Test
    fun `empty details json does not crash`() {
        val empty = BriefingDto(briefingDate = "2026-01-01", title = "제목")
        val mapped = BriefingMapper.mapOne(empty)

        assertEquals("2026-01-01", mapped.date)
        assertTrue(mapped.details.news.isEmpty())
        assertTrue(mapped.details.keyEvents.isEmpty())
        assertNull(mapped.value(IndicatorId.KR_KOSPI))
        assertEquals("제목", mapped.headline)
    }
}
