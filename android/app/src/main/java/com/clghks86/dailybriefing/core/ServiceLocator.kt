package com.clghks86.dailybriefing.core

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore
import com.clghks86.dailybriefing.data.local.UserPreferencesRepository
import com.clghks86.dailybriefing.data.local.WidgetSnapshotStore
import com.clghks86.dailybriefing.data.local.settingsDataStore
import com.clghks86.dailybriefing.data.remote.NetworkModule
import com.clghks86.dailybriefing.data.repository.BriefingRepositoryImpl
import com.clghks86.dailybriefing.domain.repository.BriefingRepository
import com.clghks86.dailybriefing.work.NotificationHelper

/**
 * Hilt 없이 쓰는 최소 DI. 앱·워커·위젯이 같은 저장소 인스턴스(캐시 포함)를 공유한다.
 */
object ServiceLocator {

    @Volatile
    private var appContextRef: Context? = null

    fun init(context: Context) {
        if (appContextRef == null) {
            synchronized(this) {
                if (appContextRef == null) appContextRef = context.applicationContext
            }
        }
    }

    /** 앱 Context. WorkManager·위젯처럼 Context가 필요한 곳에서 쓴다. */
    val appContext: Context
        get() = requireNotNull(appContextRef) { "ServiceLocator.init(context) 이 먼저 호출되어야 합니다." }

    val widgetSnapshotStore: WidgetSnapshotStore by lazy { WidgetSnapshotStore(appContext) }

    val briefingRepository: BriefingRepository by lazy {
        BriefingRepositoryImpl(
            api = NetworkModule.createApi(),
            onLoaded = { briefing -> widgetSnapshotStore.save(briefing) },
        )
    }

    val dataStore: DataStore<Preferences> by lazy { appContext.settingsDataStore }

    val userPreferences: UserPreferencesRepository by lazy { UserPreferencesRepository(dataStore) }

    val notifications: NotificationHelper by lazy { NotificationHelper(appContext) }
}
