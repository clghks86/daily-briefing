package com.clghks86.dailybriefing.domain.goal

import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.GoalDirection
import com.clghks86.dailybriefing.domain.model.PriceGoal

data class GoalHit(
    val goal: PriceGoal,
    val currentValue: Double,
    val date: String,
)

/**
 * 목표가 판정. 순수 함수라서 WorkManager 없이도 단위 테스트할 수 있다.
 * - 사용 중지(enabled=false) 목표는 건너뛴다.
 * - 값이 없는 날(휴장·수집 실패)은 건너뛴다.
 * - 같은 브리핑 날짜로 이미 알린 목표는 건너뛴다(하루 1회).
 */
object PriceGoalEvaluator {

    fun evaluate(goals: List<PriceGoal>, briefing: Briefing): List<GoalHit> =
        goals.mapNotNull { goal ->
            if (!goal.enabled) return@mapNotNull null
            if (goal.lastFiredDate == briefing.date) return@mapNotNull null
            val indicator = goal.indicatorId ?: return@mapNotNull null
            val current = briefing.value(indicator) ?: return@mapNotNull null

            val hit = when (goal.direction) {
                GoalDirection.ABOVE -> current >= goal.target
                GoalDirection.BELOW -> current <= goal.target
            }
            if (hit) GoalHit(goal = goal, currentValue = current, date = briefing.date) else null
        }

    /** 알림을 보낸 목표에 발송 날짜를 새긴다. */
    fun markFired(goals: List<PriceGoal>, hits: List<GoalHit>): List<PriceGoal> {
        if (hits.isEmpty()) return goals
        val firedIds = hits.map { it.goal.id }.toSet()
        val dateByGoal = hits.associate { it.goal.id to it.date }
        return goals.map { goal ->
            if (goal.id in firedIds) goal.copy(lastFiredDate = dateByGoal[goal.id]) else goal
        }
    }
}
