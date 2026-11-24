package com.clghks86.dailybriefing.work

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.clghks86.dailybriefing.MainActivity
import com.clghks86.dailybriefing.R
import com.clghks86.dailybriefing.domain.goal.GoalHit
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter

/**
 * 로컬 알림 전담. FCM 서버 없이 단독 동작한다.
 * 알림 권한이 없으면 조용히 false를 돌려주고, 호출부는 "보냈다"고 기록하지 않는다.
 */
class NotificationHelper(private val context: Context) {

    fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_BRIEFING,
                context.getString(R.string.notification_channel_briefing),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_briefing_desc)
            },
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_GOAL,
                context.getString(R.string.notification_channel_goal),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.notification_channel_goal_desc)
            },
        )
    }

    fun canPostNotifications(): Boolean {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return granted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** 새 브리핑 알림. 실제로 게시되면 true. */
    fun notifyBriefing(briefing: Briefing): Boolean {
        ensureChannels()
        if (!canPostNotifications()) return false

        val headline = briefing.headline.ifBlank { "새 브리핑이 도착했습니다" }
        val kospi = briefing.value(com.clghks86.dailybriefing.domain.model.IndicatorId.KR_KOSPI)
        val btc = briefing.value(com.clghks86.dailybriefing.domain.model.IndicatorId.BTC_USD)
        val body = buildString {
            append(briefing.summary.take(140))
            if (kospi != null || btc != null) append('\n')
            if (kospi != null) {
                append("코스피 ").append(IndicatorFormatter.group(kospi, 2))
                append(" (").append(IndicatorFormatter.formatChange(briefing.change(com.clghks86.dailybriefing.domain.model.IndicatorId.KR_KOSPI))).append(")")
            }
            if (kospi != null && btc != null) append(" · ")
            if (btc != null) {
                append("BTC $").append(IndicatorFormatter.group(btc, 0))
            }
        }

        return post(
            channel = CHANNEL_BRIEFING,
            id = NOTIFICATION_ID_BRIEFING,
            title = headline,
            text = body,
            bigText = briefing.summary,
        )
    }

    /** 목표가 알림. 여러 개가 걸리면 한 번에 묶어서 보낸다. */
    fun notifyGoals(hits: List<GoalHit>): Boolean {
        if (hits.isEmpty()) return false
        ensureChannels()
        if (!canPostNotifications()) return false

        val lines = hits.map { hit ->
            val indicator = hit.goal.indicatorId
            val current = if (indicator != null) {
                IndicatorFormatter.formatValue(indicator, hit.currentValue)
            } else {
                hit.currentValue.toString()
            }
            "${hit.goal.describe()} 도달 · 현재 $current"
        }
        return post(
            channel = CHANNEL_GOAL,
            id = NOTIFICATION_ID_GOAL,
            title = if (hits.size == 1) "목표가 도달" else "목표가 ${hits.size}건 도달",
            text = lines.first(),
            bigText = lines.joinToString("\n"),
        )
    }

    /** 설정 화면의 "테스트 알림". */
    fun notifyTest(headline: String): Boolean = post(
        channel = CHANNEL_BRIEFING,
        id = NOTIFICATION_ID_TEST,
        title = headline.ifBlank { "테스트 알림" },
        text = "알림이 정상 동작합니다. 매일 브리핑이 올라오면 이렇게 알려 드립니다.",
        bigText = null,
    )

    // canPostNotifications() 로 권한을 먼저 확인하고, 게시는 SecurityException 도 잡는다.
    @SuppressLint("MissingPermission")
    private fun post(channel: String, id: Int, title: String, text: String, bigText: String?): Boolean {
        ensureChannels()
        if (!canPostNotifications()) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText ?: text))
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        return runCatching {
            NotificationManagerCompat.from(context).notify(id, builder.build())
            true
        }.getOrElse { error ->
            if (error is SecurityException) false else throw error
        }
    }

    companion object {
        const val CHANNEL_BRIEFING = "briefing_v1"
        const val CHANNEL_GOAL = "goal_v1"

        private const val NOTIFICATION_ID_BRIEFING = 1001
        private const val NOTIFICATION_ID_GOAL = 1002
        private const val NOTIFICATION_ID_TEST = 1003
    }
}
