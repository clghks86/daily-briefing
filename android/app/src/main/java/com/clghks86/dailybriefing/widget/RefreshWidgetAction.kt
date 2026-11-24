package com.clghks86.dailybriefing.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.clghks86.dailybriefing.core.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 위젯의 "새로고침" 버튼. 네트워크에서 다시 받아 캐시를 갱신하고 위젯을 다시 그린다. */
class RefreshWidgetAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        ServiceLocator.init(context)
        val latest = withContext(Dispatchers.IO) {
            ServiceLocator.briefingRepository.latest(forceRefresh = true).getOrNull()
        }
        latest?.let { ServiceLocator.widgetSnapshotStore.save(it) }
        BriefingWidget().update(context, glanceId)
    }
}
