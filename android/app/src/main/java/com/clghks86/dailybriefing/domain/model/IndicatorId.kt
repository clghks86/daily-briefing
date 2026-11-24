package com.clghks86.dailybriefing.domain.model

/** 홈 화면 지표 그리드의 묶음 단위. 차트 탭의 그룹도 여기서 파생한다. */
enum class IndicatorSection(val title: String, val emoji: String, val chartGroup: ChartGroup) {
    US_MARKET("미국 증시", "🇺🇸", ChartGroup.INDEX),
    KR_MARKET("국내 증시", "🇰🇷", ChartGroup.INDEX),
    GOLD("금", "🪙", ChartGroup.GOLD),
    CRYPTO("코인", "₿", ChartGroup.CRYPTO),
    FX_RATE("환율·금리", "💱", ChartGroup.FX_RATE),
}

/** 추이 차트 탭. */
enum class ChartGroup(val title: String) {
    INDEX("지수"),
    GOLD("금"),
    CRYPTO("코인"),
    FX_RATE("환율·금리"),
}

/**
 * DB 컬럼과 1:1로 대응하는 지표 정의.
 * coreKey 는 매핑 코드에서만 쓰고, UI는 이 enum만 알면 된다.
 */
enum class IndicatorId(
    val label: String,
    val shortLabel: String,
    val section: IndicatorSection,
    val decimals: Int,
    val prefix: String = "",
    val suffix: String = "",
    /** 그리드에 카드로 노출할지. false면 다른 카드의 보조 값으로만 쓴다. */
    val gridVisible: Boolean = true,
    val chartable: Boolean = true,
    val pinnedByDefault: Boolean = false,
    val goalEligible: Boolean = false,
) {
    US_DOW("다우존스", "다우", IndicatorSection.US_MARKET, 2),
    US_SP500("S&P 500", "S&P500", IndicatorSection.US_MARKET, 2),
    US_NASDAQ("나스닥", "나스닥", IndicatorSection.US_MARKET, 2),

    KR_KOSPI("코스피", "코스피", IndicatorSection.KR_MARKET, 2, pinnedByDefault = true, goalEligible = true),
    KR_KOSDAQ("코스닥", "코스닥", IndicatorSection.KR_MARKET, 2),

    GOLD_INTL("국제 금 ($/oz)", "국제금", IndicatorSection.GOLD, 2, prefix = "$"),
    GOLD_KRX("KRX 금현물 (원/g)", "KRX금", IndicatorSection.GOLD, 0, suffix = "원", pinnedByDefault = true, goalEligible = true),

    BTC_USD("비트코인 (USD)", "BTC", IndicatorSection.CRYPTO, 0, prefix = "$", pinnedByDefault = true, goalEligible = true),
    BTC_KRW("비트코인 (원)", "BTC(원)", IndicatorSection.CRYPTO, 0, suffix = "원", gridVisible = false, goalEligible = true),
    ETH_USD("이더리움 (USD)", "ETH", IndicatorSection.CRYPTO, 2, prefix = "$"),
    ETH_KRW("이더리움 (원)", "ETH(원)", IndicatorSection.CRYPTO, 0, suffix = "원", gridVisible = false),
    XRP_USD("리플 (USD)", "XRP", IndicatorSection.CRYPTO, 4, prefix = "$"),
    XRP_KRW("리플 (원)", "XRP(원)", IndicatorSection.CRYPTO, 0, suffix = "원", gridVisible = false),
    SOL_USD("솔라나 (USD)", "SOL", IndicatorSection.CRYPTO, 2, prefix = "$"),
    SOL_KRW("솔라나 (원)", "SOL(원)", IndicatorSection.CRYPTO, 0, suffix = "원", gridVisible = false),
    USDT_USD("테더 (USD)", "USDT", IndicatorSection.CRYPTO, 4, prefix = "$"),
    USDT_KRW("테더 (원)", "USDT(원)", IndicatorSection.CRYPTO, 0, suffix = "원", gridVisible = false),

    USD_KRW("원/달러", "원/달러", IndicatorSection.FX_RATE, 2, suffix = "원", pinnedByDefault = true, goalEligible = true),
    US_10Y_YIELD("미 10년물 금리", "미10Y", IndicatorSection.FX_RATE, 2, suffix = "%"),
    KIMCHI_PREMIUM("김치 프리미엄", "김프", IndicatorSection.FX_RATE, 2, suffix = "%", chartable = false),
    ;

    /** 카드 아래 보조 줄로 함께 보여 줄 다른 표기(예: BTC 원화). */
    val subId: IndicatorId?
        get() = when (this) {
            BTC_USD -> BTC_KRW
            ETH_USD -> ETH_KRW
            XRP_USD -> XRP_KRW
            SOL_USD -> SOL_KRW
            USDT_USD -> USDT_KRW
            else -> null
        }

    companion object {
        fun pinDefaults(): List<String> = entries.filter { it.pinnedByDefault }.map { it.name }

        fun fromKey(raw: String?): IndicatorId? =
            raw?.let { key -> entries.firstOrNull { it.name == key } }

        fun gridOf(section: IndicatorSection): List<IndicatorId> =
            entries.filter { it.section == section && it.gridVisible }

        fun chartableOf(group: ChartGroup): List<IndicatorId> =
            entries.filter { it.section.chartGroup == group && it.chartable }
    }
}
