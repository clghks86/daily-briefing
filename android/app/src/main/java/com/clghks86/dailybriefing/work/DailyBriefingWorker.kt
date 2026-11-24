package com.clghks86.dailybriefing.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clghks86.dailybriefing.core.ServiceLocator
import kotlinx.coroutines.flow.first

/**
 * 매일 09:35경 실행되어 새 `briefing_date`를 감지하면 헤드라인 알림을 띄운다.
 * 같은 날짜로는 두 번 알리지 않는다(last_notified_date).
 */
class DailyBriefingWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        ServiceLocator.init(applicationContext)
        val prefs = ServiceLocator.userPreferences

        if (!prefs.briefingNotificationEnabled.first()) return Result.success()

        val briefing = ServiceLocator.briefingRepository.latest(forceRefresh = true).getOrNull()
            ?: return Result.retry()

        val lastNotified = prefs.lastNotifiedDate.first()
        if (lastNotified == briefing.date) return Result.success()

        val posted = ServiceLocator.notifications.notifyBriefing(briefing)
        if (posted) prefs.setLastNotifiedDate(briefing.date)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "daily_briefing_check"
        const val NOTIFY_HOUR = 9
        const val NOTIFY_MINUTE = 35
    }
}
