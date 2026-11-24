package com.clghks86.dailybriefing.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * WorkManager 예약을 한 곳에서 관리한다.
 * - 매일 브리핑 체크: 하루 1회, 09:35 기준
 * - 목표가 체크: 15분 주기(WorkManager 최소 주기)
 */
object WorkScheduler {

    private val networkConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun ensureScheduled(context: Context, now: ZonedDateTime = ZonedDateTime.now()) {
        scheduleDailyBriefingCheck(context, now)
        schedulePriceGoalCheck(context)
    }

    fun scheduleDailyBriefingCheck(context: Context, now: ZonedDateTime = ZonedDateTime.now()) {
        val delay = ScheduleTimes.millisUntilNext(now, DailyBriefingWorker.NOTIFY_HOUR, DailyBriefingWorker.NOTIFY_MINUTE)
        val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(networkConstraint)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyBriefingWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun schedulePriceGoalCheck(context: Context) {
        val request = PeriodicWorkRequestBuilder<PriceGoalWorker>(
            PriceGoalWorker.INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        )
            .setConstraints(networkConstraint)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PriceGoalWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /**
     * 앱을 열었을 때 한 번만 따라잡기 체크. 09:35가 지났는데 오늘 알림을 아직 안 보냈다면
     * 다음 정기 실행(최대 24시간 뒤)을 기다리지 않고 바로 확인한다.
     */
    fun runCatchUpCheck(context: Context, now: ZonedDateTime = ZonedDateTime.now()) {
        if (!ScheduleTimes.isPastNotificationTime(now, DailyBriefingWorker.NOTIFY_HOUR, DailyBriefingWorker.NOTIFY_MINUTE)) {
            return
        }
        val request = OneTimeWorkRequestBuilder<DailyBriefingWorker>()
            .setConstraints(networkConstraint)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            CATCH_UP_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancelDailyBriefingCheck(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(DailyBriefingWorker.WORK_NAME)
    }

    private const val CATCH_UP_WORK_NAME = "daily_briefing_catch_up"
}
