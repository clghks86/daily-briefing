package com.clghks86.dailybriefing.ui.trend.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clghks86.dailybriefing.domain.chart.ChartLine
import com.clghks86.dailybriefing.domain.chart.ChartSeries
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.ui.theme.ChartPalette
import com.clghks86.dailybriefing.ui.util.DateLabels
import kotlin.math.roundToInt

/**
 * Compose Canvas 인터랙티브 라인 차트.
 * - rebase 모드면 첫 값을 100으로 환산해 서로 다른 가격대를 견준다.
 * - 좌우로 드래그하면 그 지점 날짜의 값이 위 툴팁에 뜬다.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineChart(
    series: ChartSeries,
    modifier: Modifier = Modifier,
    height: Dp = 250.dp,
) {
    if (series.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "표시할 데이터가 아직 없습니다",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
    val axisStyle = TextStyle(color = labelColor, fontSize = 9.sp)

    var selected by remember(series.dates.size, series.lines.map { it.indicator }) { mutableStateOf<Int?>(null) }

    val count = series.dates.size

    Column(modifier = modifier.fillMaxWidth()) {
        // ── 툴팁 ─────────────────────────────────────────────
        Box(modifier = Modifier.fillMaxWidth().height(if (selected != null) 72.dp else 0.dp)) {
            selected?.let { index ->
                ChartTooltip(series = series, index = index)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .pointerInput(series.dates.size) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            selected = indexOf(offset.x, size.width, count)
                        },
                        onDrag = { change, _ ->
                            selected = indexOf(change.position.x, size.width, count)
                            change.consume()
                        },
                        onDragEnd = {},
                        onDragCancel = { selected = null },
                    )
                },
        ) {
            val leftPad = 8f
            val rightPad = 40f
            val topPad = 14f
            val bottomPad = 26f
            val chartWidth = (size.width - leftPad - rightPad).coerceAtLeast(1f)
            val chartHeight = (size.height - topPad - bottomPad).coerceAtLeast(1f)

            val range = series.rangeOf(0, count - 1) ?: return@Canvas
            val span = (range.endInclusive - range.start).takeIf { it > 0.0 } ?: 1.0
            val low = range.start - span * 0.10
            val high = range.endInclusive + span * 0.10

            fun yOf(value: Double): Float =
                topPad + (chartHeight * (1.0 - (value - low) / (high - low))).toFloat()

            fun xOf(index: Int): Float =
                if (count <= 1) leftPad + chartWidth / 2f
                else leftPad + chartWidth * (index.toFloat() / (count - 1).toFloat())

            // 가로 그리드 + 오른쪽 y 라벨
            for (step in 0..3) {
                val y = topPad + chartHeight * step / 3f
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(leftPad + chartWidth, y),
                    strokeWidth = 1f,
                )
                val value = high - (high - low) * step / 3.0
                val label = axisLabel(value, series.rebase)
                val layout = textMeasurer.measure(label, style = axisStyle)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(leftPad + chartWidth + 4f, y - layout.size.height / 2f),
                )
            }

            // x 축 라벨 (처음·중간·끝)
            val tickIndexes = listOf(0, count / 2, count - 1).distinct()
            tickIndexes.forEach { index ->
                val label = DateLabels.axis(series.dates[index])
                val layout = textMeasurer.measure(label, style = axisStyle)
                val x = (xOf(index) - layout.size.width / 2f)
                    .coerceIn(0f, size.width - layout.size.width)
                drawText(
                    textLayoutResult = layout,
                    topLeft = Offset(x, topPad + chartHeight + 6f),
                )
            }

            // 시리즈 선
            series.lines.forEachIndexed { lineIndex, line ->
                val color = ChartPalette[lineIndex % ChartPalette.size]
                drawSeriesLine(
                    series = series,
                    line = line,
                    color = color,
                    xOf = ::xOf,
                    yOf = ::yOf,
                )
            }

            // 선택 지점 세로선 + 마커
            selected?.let { index ->
                val x = xOf(index)
                drawLine(
                    color = gridColor.copy(alpha = 0.9f),
                    start = Offset(x, topPad),
                    end = Offset(x, topPad + chartHeight),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
                series.lines.forEachIndexed { lineIndex, line ->
                    val value = series.plottedValues(line).getOrNull(index) ?: return@forEachIndexed
                    drawCircle(
                        color = ChartPalette[lineIndex % ChartPalette.size],
                        radius = 4.5f,
                        center = Offset(x, yOf(value)),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = 1.8f,
                        center = Offset(x, yOf(value)),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            series.lines.forEachIndexed { index, line ->
                LegendItem(
                    color = ChartPalette[index % ChartPalette.size],
                    label = line.indicator.shortLabel,
                    value = series.plottedValues(line).lastOrNull { it != null },
                    rebase = series.rebase,
                )
            }
        }
    }
}

private fun indexOf(x: Float, width: Int, count: Int): Int {
    if (count <= 1 || width <= 0) return 0
    val ratio = (x / width).coerceIn(0f, 1f)
    return (ratio * (count - 1)).roundToInt().coerceIn(0, count - 1)
}

private fun axisLabel(value: Double, rebase: Boolean): String =
    if (rebase) IndicatorFormatter.group(value, 1) else {
        if (kotlin.math.abs(value) >= 1000) IndicatorFormatter.group(value, 0)
        else IndicatorFormatter.group(value, 2)
    }

private fun DrawScope.drawSeriesLine(
    series: ChartSeries,
    line: ChartLine,
    color: Color,
    xOf: (Int) -> Float,
    yOf: (Double) -> Float,
) {
    val values = series.plottedValues(line)
    val path = Path()
    var started = false
    values.forEachIndexed { index, value ->
        if (value == null) {
            started = false
            return@forEachIndexed
        }
        val x = xOf(index)
        val y = yOf(value)
        if (!started) {
            path.moveTo(x, y)
            started = true
        } else {
            path.lineTo(x, y)
        }
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

@Composable
private fun ChartTooltip(series: ChartSeries, index: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = DateLabels.full(series.dates.getOrElse(index) { "" }),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                series.lines.forEachIndexed { lineIndex, line ->
                    val raw = line.values.getOrNull(index)
                    if (raw != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(6.dp)
                                    .background(
                                        ChartPalette[lineIndex % ChartPalette.size],
                                        RoundedCornerShape(3.dp),
                                    ),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${line.indicator.shortLabel} ${IndicatorFormatter.formatValue(line.indicator, raw)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, value: Double?, rebase: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(8.dp)
                .background(color, RoundedCornerShape(4.dp)),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (value != null) {
            Spacer(Modifier.width(3.dp))
            Text(
                text = if (rebase) IndicatorFormatter.group(value, 1) else IndicatorFormatter.group(value, 0),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
