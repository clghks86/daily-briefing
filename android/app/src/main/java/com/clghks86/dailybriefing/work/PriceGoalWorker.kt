package com.clghks86.dailybriefing.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.domain.goal.PriceGoalEvaluator

/**
 * 15분 주기로 최신 시세를 확인해 목표가에 닿았는지 본다.
 * 목표가 알림은 서버 푸시가 아니라 로컬 판정이므로 앱 권한만으로 동작한다.
 */
class PriceGoalWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        ServiceLocator.init(applicationContext)
        val prefs = ServiceLocator.userPreferences

        val goals = prefs.goalsSnapshot()
        if (goals.none { it.enabled }) return Result.success()

        val briefing = ServiceLocator.briefingRepository.latest(forceRefresh = true).getOrNull()
            ?: return Result.retry()

        val hits = PriceGoalEvaluator.evaluate(goals, briefing)
        if (hits.isEmpty()) return Result.success()

        val posted = ServiceLocator.notifications.notifyGoals(hits)
        if (posted) {
            // 사용 중지된 목표가 사라지지 않도록 전체 목록에 발송 날짜만 새긴다.
            prefs.setGoals(PriceGoalEvaluator.markFired(goals, hits))
        }
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "price_goal_check"
        const val INTERVAL_MINUTES = 15L
    }
}
