package com.clghks86.dailybriefing

import android.app.Application
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.work.WorkScheduler

class DailyBriefingApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        // 알림 채널은 앱 시작 시 한 번 만들어 둔다.
        ServiceLocator.notifications.ensureChannels()
        // 앱을 처음 켠 날에도 예약이 빠지지 않도록 매번 확인(UPDATE 정책이라 중복 예약 없음).
        runCatching { WorkScheduler.ensureScheduled(this) }
    }
}
