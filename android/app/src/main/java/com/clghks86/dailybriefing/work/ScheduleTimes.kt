package com.clghks86.dailybriefing.work

import java.time.Duration
import java.time.ZonedDateTime

/** 알림 시각 계산. 순수 함수라 단위 테스트로 검증한다. */
object ScheduleTimes {

    /** 다음 [hour]:[minute] 까지 남은 밀리초. 이미 지났으면 내일 같은 시각. */
    fun millisUntilNext(
        now: ZonedDateTime,
        hour: Int,
        minute: Int,
    ): Long {
        var target = now
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .withNano(0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return Duration.between(now, target).toMillis()
    }

    /** 09:35가 지난 뒤 앱을 열었는지 (즉시 따라잡기 알림이 필요한지). */
    fun isPastNotificationTime(now: ZonedDateTime, hour: Int, minute: Int): Boolean {
        val target = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        return !now.isBefore(target)
    }
}
