package com.clghks86.dailybriefing.domain

import com.clghks86.dailybriefing.TestData
import com.clghks86.dailybriefing.domain.goal.PriceGoalEvaluator
import com.clghks86.dailybriefing.domain.model.GoalDirection
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.PriceGoal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceGoalEvaluatorTest {

    private val briefing = TestData.briefing(
        date = "2026-11-24",
        values = mapOf(
            IndicatorId.KR_KOSPI to (4994.21 to 6.07),
            IndicatorId.BTC_USD to (61195.0 to -0.03),
            IndicatorId.GOLD_KRX to (157010.0 to 0.01),
        ),
    )

    private fun goal(
        indicator: IndicatorId,
        direction: GoalDirection,
        target: Double,
        enabled: Boolean = true,
        lastFiredDate: String? = null,
    ) = PriceGoal(
        id = "${indicator.name}-$target",
        indicator = indicator.name,
        direction = direction,
        target = target,
        enabled = enabled,
        lastFiredDate = lastFiredDate,
    )

    @Test
    fun `above target triggers when current is higher`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(goal(IndicatorId.KR_KOSPI, GoalDirection.ABOVE, 4900.0)),
            briefing,
        )
        assertEquals(1, hits.size)
        assertEquals(4994.21, hits.first().currentValue, 0.001)
        assertEquals("2026-11-24", hits.first().date)
    }

    @Test
    fun `above target does not trigger below the line`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(goal(IndicatorId.KR_KOSPI, GoalDirection.ABOVE, 5000.0)),
            briefing,
        )
        assertTrue(hits.isEmpty())
    }

    @Test
    fun `below target triggers when current is lower`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(goal(IndicatorId.BTC_USD, GoalDirection.BELOW, 65000.0)),
            briefing,
        )
        assertEquals(1, hits.size)
    }

    @Test
    fun `exact target counts as reached`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(
                goal(IndicatorId.GOLD_KRX, GoalDirection.ABOVE, 157010.0),
                goal(IndicatorId.BTC_USD, GoalDirection.BELOW, 61195.0),
            ),
            briefing,
        )
        assertEquals(2, hits.size)
    }

    @Test
    fun `disabled goals are skipped`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(goal(IndicatorId.KR_KOSPI, GoalDirection.ABOVE, 100.0, enabled = false)),
            briefing,
        )
        assertTrue(hits.isEmpty())
    }

    @Test
    fun `already notified goal is skipped for the same date`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(goal(IndicatorId.KR_KOSPI, GoalDirection.ABOVE, 100.0, lastFiredDate = "2026-11-24")),
            briefing,
        )
        assertTrue(hits.isEmpty())
    }

    @Test
    fun `goal for an indicator without a value is skipped`() {
        val hits = PriceGoalEvaluator.evaluate(
            listOf(goal(IndicatorId.USD_KRW, GoalDirection.ABOVE, 1000.0)),
            briefing,
        )
        assertTrue(hits.isEmpty())
    }

    @Test
    fun `mark fired stamps only the goals that were hit`() {
        val goals = listOf(
            goal(IndicatorId.KR_KOSPI, GoalDirection.ABOVE, 4900.0),
            goal(IndicatorId.BTC_USD, GoalDirection.ABOVE, 90000.0),
        )
        val hits = PriceGoalEvaluator.evaluate(goals, briefing)
        val updated = PriceGoalEvaluator.markFired(goals, hits)

        assertEquals("2026-11-24", updated[0].lastFiredDate)
        assertEquals(null, updated[1].lastFiredDate)
    }
}
