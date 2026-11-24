package com.clghks86.dailybriefing.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

/** 원문 링크는 CustomTabs로 연다. 없으면 기본 브라우저로 넘긴다. */
object LinkOpener {

    fun open(context: Context, url: String) {
        if (url.isBlank()) return
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        val customTabs = runCatching {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .build()
                .apply { launchUrl(context, uri) }
        }
        if (customTabs.isSuccess) return

        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: ActivityNotFoundException) {
            // 열 수 있는 브라우저가 없는 경우 조용히 넘어간다.
        }
    }
}
