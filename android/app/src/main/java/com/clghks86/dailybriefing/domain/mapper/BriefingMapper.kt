package com.clghks86.dailybriefing.domain.mapper

import com.clghks86.dailybriefing.data.remote.dto.BriefingDto
import com.clghks86.dailybriefing.data.remote.dto.BriefingImageDto
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.BriefingDetails
import com.clghks86.dailybriefing.domain.model.BriefingImage
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.IndicatorSnapshot
import com.clghks86.dailybriefing.domain.model.NewsItem
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.math.roundToInt
import kotlinx.serialization.json.doubleOrNull

/** DTO → 도메인 변환. DB 컬럼 이름은 여기서만 등장한다. */
object BriefingMapper {

    fun map(rows: List<BriefingDto>, imageRows: List<BriefingImageDto> = emptyList()): List<Briefing> {
        val imagesByDate = imageRows
            .mapNotNull { img ->
                img.briefingDate?.let { date -> date to img }
            }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, list) ->
                list.mapNotNull { img ->
                    img.imageUrl?.takeIf { it.isNotBlank() }?.let { url ->
                        BriefingImage(
                            url = url,
                            caption = img.caption,
                            sortOrder = img.sortOrder ?: Int.MAX_VALUE,
                            section = img.section,
                        )
                    }
                }.sortedBy { it.sortOrder }
            }

        return rows
            .filter { !it.briefingDate.isNullOrBlank() }
            .map { row -> mapOne(row, imagesByDate[row.briefingDate].orEmpty()) }
            .sortedByDescending { it.date }
    }

    fun mapOne(row: BriefingDto, images: List<BriefingImage> = emptyList()): Briefing {
        val values = mapOf(
            IndicatorId.US_DOW to (row.usDow to row.usDowChg),
            IndicatorId.US_SP500 to (row.usSp500 to row.usSp500Chg),
            IndicatorId.US_NASDAQ to (row.usNasdaq to row.usNasdaqChg),
            IndicatorId.KR_KOSPI to (row.krKospi to row.krKospiChg),
            IndicatorId.KR_KOSDAQ to (row.krKosdaq to row.krKosdaqChg),
            IndicatorId.GOLD_INTL to (row.goldIntlUsd to row.goldIntlChg),
            IndicatorId.GOLD_KRX to (row.goldKrxKrw to row.goldKrxChg),
            IndicatorId.BTC_USD to (row.btcUsd to row.btcChg),
            IndicatorId.BTC_KRW to (row.btcKrw to row.btcChg),
            IndicatorId.ETH_USD to (row.ethUsd to row.ethChg),
            IndicatorId.ETH_KRW to (row.ethKrw to row.ethChg),
            IndicatorId.XRP_USD to (row.xrpUsd to row.xrpChg),
            IndicatorId.XRP_KRW to (row.xrpKrw to row.xrpChg),
            IndicatorId.SOL_USD to (row.solUsd to row.solChg),
            IndicatorId.SOL_KRW to (row.solKrw to row.solChg),
            IndicatorId.USDT_USD to (row.usdtUsd to null),
            IndicatorId.USDT_KRW to (row.usdtKrw to null),
            IndicatorId.USD_KRW to (row.usdKrw to null),
            IndicatorId.US_10Y_YIELD to (row.us10yYield to null),
            IndicatorId.KIMCHI_PREMIUM to (row.kimchiPremium to null),
        ).mapValues { (_, pair) -> IndicatorSnapshot(value = pair.first, changePercent = pair.second) }

        return Briefing(
            date = row.briefingDate.orEmpty(),
            title = row.title.orEmpty(),
            summary = row.summary.orEmpty(),
            indicators = values,
            fearGreed = row.fearGreedIndex?.roundToInt()?.coerceIn(0, 100),
            details = mapDetails(row.details),
            rawMarkdown = row.rawMarkdown.orEmpty(),
            images = images,
            updatedAt = row.updatedAt,
        )
    }

    fun mapDetails(element: JsonElement?): BriefingDetails {
        val obj = element as? JsonObject ?: return BriefingDetails()
        return BriefingDetails(
            asOfKst = obj.string("as_of_kst"),
            keyEvents = obj.stringList("key_events"),
            news = obj.newsList("news"),
            marketNote = obj.string("kr_market_note"),
            dxy = obj.number("dxy"),
            vix = obj.number("vix"),
            wtiUsd = obj.number("wti_usd"),
            goldEtf = (obj["gold_etf"] as? JsonObject)?.let { etf ->
                BriefingDetails.GoldEtf(
                    aceKrx = etf.number("ace_krx"),
                    aceChg = etf.number("ace_chg"),
                    tigerKrx = etf.number("tiger_krx"),
                    tigerChg = etf.number("tiger_chg"),
                )
            },
        )
    }

    private fun JsonObject.string(key: String): String? =
        (this[key] as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }

    private fun JsonObject.number(key: String): Double? =
        (this[key] as? JsonPrimitive)?.doubleOrNull

    private fun JsonObject.stringList(key: String): List<String> =
        (this[key] as? JsonArray)
            ?.mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString }?.content }
            ?.filter { it.isNotBlank() }
            .orEmpty()

    private fun JsonObject.newsList(key: String): List<NewsItem> =
        (this[key] as? JsonArray)?.mapNotNull { item ->
            val news = item as? JsonObject ?: return@mapNotNull null
            val title = news.string("title") ?: return@mapNotNull null
            val url = news.string("url") ?: return@mapNotNull null
            NewsItem(
                title = title,
                url = url,
                source = news.string("source"),
                summary = news.string("summary"),
                category = news.string("category"),
            )
        }.orEmpty()
}
