package com.clghks86.dailybriefing.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Supabase `daily_briefings` 한 행.
 * 컬럼이 늘어나도 앱이 깨지지 않도록 ignoreUnknownKeys 로 읽는다.
 */
@Serializable
data class BriefingDto(
    @SerialName("briefing_date") val briefingDate: String? = null,
    val title: String? = null,
    val summary: String? = null,

    @SerialName("us_dow") val usDow: Double? = null,
    @SerialName("us_dow_chg") val usDowChg: Double? = null,
    @SerialName("us_sp500") val usSp500: Double? = null,
    @SerialName("us_sp500_chg") val usSp500Chg: Double? = null,
    @SerialName("us_nasdaq") val usNasdaq: Double? = null,
    @SerialName("us_nasdaq_chg") val usNasdaqChg: Double? = null,

    @SerialName("kr_kospi") val krKospi: Double? = null,
    @SerialName("kr_kospi_chg") val krKospiChg: Double? = null,
    @SerialName("kr_kosdaq") val krKosdaq: Double? = null,
    @SerialName("kr_kosdaq_chg") val krKosdaqChg: Double? = null,

    @SerialName("usd_krw") val usdKrw: Double? = null,
    @SerialName("us_10y_yield") val us10yYield: Double? = null,

    @SerialName("gold_intl_usd") val goldIntlUsd: Double? = null,
    @SerialName("gold_intl_chg") val goldIntlChg: Double? = null,
    @SerialName("gold_krx_krw") val goldKrxKrw: Double? = null,
    @SerialName("gold_krx_chg") val goldKrxChg: Double? = null,

    @SerialName("btc_usd") val btcUsd: Double? = null,
    @SerialName("btc_krw") val btcKrw: Double? = null,
    @SerialName("btc_chg") val btcChg: Double? = null,
    @SerialName("eth_usd") val ethUsd: Double? = null,
    @SerialName("eth_krw") val ethKrw: Double? = null,
    @SerialName("eth_chg") val ethChg: Double? = null,
    @SerialName("xrp_usd") val xrpUsd: Double? = null,
    @SerialName("xrp_krw") val xrpKrw: Double? = null,
    @SerialName("xrp_chg") val xrpChg: Double? = null,
    @SerialName("usdt_usd") val usdtUsd: Double? = null,
    @SerialName("usdt_krw") val usdtKrw: Double? = null,
    @SerialName("sol_usd") val solUsd: Double? = null,
    @SerialName("sol_krw") val solKrw: Double? = null,
    @SerialName("sol_chg") val solChg: Double? = null,

    @SerialName("kimchi_premium") val kimchiPremium: Double? = null,
    @SerialName("fear_greed_index") val fearGreedIndex: Double? = null,

    val details: JsonElement? = null,
    @SerialName("raw_markdown") val rawMarkdown: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)
