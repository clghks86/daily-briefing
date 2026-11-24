package com.clghks86.dailybriefing.ui.theme

import androidx.compose.ui.graphics.Color

// 다크 — 기본 테마
val Ink = Color(0xFF0B0F14)
val SurfaceDark = Color(0xFF131A22)
val SurfaceVariantDark = Color(0xFF1B2430)
val Gold = Color(0xFFE8B74B)
val GoldDeep = Color(0xFFC99A2E)
val Mint = Color(0xFF3EC6A0)
val TextPrimaryDark = Color(0xFFE9EEF5)
val TextMutedDark = Color(0xFF8A94A6)
val OutlineDark = Color(0xFF2A3542)

// 라이트
val Paper = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFEEF1F6)
val GoldDark = Color(0xFF8A6512)
val TextPrimaryLight = Color(0xFF12181F)
val TextMutedLight = Color(0xFF5E6B7C)
val OutlineLight = Color(0xFFD5DCE5)

// 등락 색 — 한국 관례: 상승 빨강 / 하락 파랑
val MarketUp = Color(0xFFF2545B)
val MarketDown = Color(0xFF4C8DFF)
val MarketFlat = Color(0xFF8A94A6)

val ChartBlue = Color(0xFF5B8CFF)
val ChartViolet = Color(0xFF8F7BFF)
val ChartTeal = Color(0xFF4FC3D9)
val ChartOrange = Color(0xFFFF9E64)
val ChartYellow = Color(0xFFF5C451)
val ChartBitcoin = Color(0xFFF7931A)
val ChartGreen = Color(0xFF3EC6A0)

/** 차트 시리즈 색 순환. */
val ChartPalette = listOf(
    ChartBlue, ChartOrange, ChartTeal, ChartViolet, ChartYellow, ChartBitcoin, ChartGreen,
)
