package com.clghks86.dailybriefing.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.clghks86.dailybriefing.core.ServiceLocator
import com.clghks86.dailybriefing.data.local.WidgetSnapshot
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 홈 화면 위젯: 코스피 · BTC · KRX 금 + 오늘의 헤드라인.
 * 값은 캐시(WidgetSnapshotStore)에서 그리고, 새로고침 버튼은 네트워크를 다시 당겨 온다.
 */
class BriefingWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        ServiceLocator.init(context)

        // 위젯이 갱신될 때마다 최신 값으로 캐시를 맞춘다(실패하면 캐시 그대로).
        val latest = withContext(Dispatchers.IO) {
            ServiceLocator.briefingRepository.latest().getOrNull()
        }
        latest?.let { ServiceLocator.widgetSnapshotStore.save(it) }
        val snapshot = ServiceLocator.widgetSnapshotStore.load()

        provideContent {
            GlanceTheme {
                WidgetContent(snapshot)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(snapshot: WidgetSnapshot?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .cornerRadius(20.dp)
                .background(ColorProvider(SURFACE))
                .padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "데일리 브리핑",
                    style = TextStyle(
                        color = ColorProvider(GOLD),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = snapshot?.date.orEmpty(),
                    style = TextStyle(color = ColorProvider(MUTED), fontSize = 11.sp),
                )
            }

            if (snapshot == null || !snapshot.hasData) {
                Text(
                    text = "새로고침을 눌러 오늘 시세를 불러오세요",
                    style = TextStyle(color = ColorProvider(MUTED), fontSize = 12.sp),
                    modifier = GlanceModifier.padding(top = 10.dp),
                )
            } else {
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Metric("코스피", value(snapshot.kospi, 2), snapshot.kospiChg, null)
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    Metric("BTC", value(snapshot.btcUsd, 0, prefix = "$"), snapshot.btcChg, snapshot.btcKrw?.let { IndicatorFormatter.formatKrwCompact(it) })
                    Spacer(modifier = GlanceModifier.width(10.dp))
                    Metric("KRX금", value(snapshot.goldKrx, 0, suffix = "원"), snapshot.goldChg, null)
                }

                Text(
                    text = snapshot.headline,
                    maxLines = 2,
                    style = TextStyle(color = ColorProvider(TEXT), fontSize = 11.sp),
                    modifier = GlanceModifier.padding(top = 8.dp),
                )
            }

            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "↻ 새로고침",
                    style = TextStyle(color = ColorProvider(BLUE), fontSize = 11.sp, fontWeight = FontWeight.Medium),
                    modifier = GlanceModifier
                        .clickable(
                            actionRunCallback<RefreshWidgetAction>(
                                actionParametersOf(ActionParameters.Key<String>("source") to "widget"),
                            ),
                        )
                        .padding(vertical = 2.dp),
                )
                Spacer(modifier = GlanceModifier.width(6.dp))
                Text(
                    text = snapshot?.timeLabel.orEmpty(),
                    style = TextStyle(color = ColorProvider(MUTED), fontSize = 10.sp),
                )
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun Metric(label: String, main: String, change: Double?, sub: String?) {
        Column {
            Text(text = label, style = TextStyle(color = ColorProvider(MUTED), fontSize = 10.sp))
            Text(
                text = main,
                style = TextStyle(
                    color = ColorProvider(TEXT),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = IndicatorFormatter.formatChange(change),
                style = TextStyle(
                    color = ColorProvider(changeColor(change)),
                    fontSize = 10.sp,
                ),
            )
            if (!sub.isNullOrBlank()) {
                Text(text = sub, style = TextStyle(color = ColorProvider(MUTED), fontSize = 9.sp))
            }
        }
    }

    private fun value(raw: Double?, decimals: Int, prefix: String = "", suffix: String = ""): String =
        if (raw == null) "-" else prefix + IndicatorFormatter.group(raw, decimals) + suffix

    private fun changeColor(change: Double?): Color = when {
        change == null -> MUTED
        change > 0 -> RED
        change < 0 -> BLUE
        else -> MUTED
    }

    private companion object {
        val SURFACE = Color(0xFF131A22)
        val GOLD = Color(0xFFE8B74B)
        val TEXT = Color(0xFFE9EEF5)
        val MUTED = Color(0xFF8A94A6)
        val RED = Color(0xFFF2545B)
        val BLUE = Color(0xFF4C8DFF)
    }
}
