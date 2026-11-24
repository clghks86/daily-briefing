package com.clghks86.dailybriefing.core

import com.clghks86.dailybriefing.BuildConfig

/**
 * Supabase 접속 정보. 값은 빌드 시 BuildConfig 로 주입된다.
 * 앱에는 공개(publishable) 키만 들어가며, service_role(secret) 키는 빌드 스크립트가 거부한다.
 */
object SupabaseConfig {
    val url: String = BuildConfig.SUPABASE_URL.trim().trimEnd('/')
    val publishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY.trim()

    val isConfigured: Boolean
        get() = url.startsWith("https://") &&
            publishableKey.isNotBlank() &&
            !publishableKey.startsWith("sb_secret_")

    /** 설정 화면에 보여 줄 마스킹된 키 (앞 12자만). */
    val maskedKey: String
        get() = if (publishableKey.length <= 16) "••••" else publishableKey.take(12) + "••••"

    val host: String
        get() = url.removePrefix("https://").substringBefore('/')
}
