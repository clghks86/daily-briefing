package com.clghks86.dailybriefing.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 공포·탐욕 지수 게이지. 0(극단적 공포) ~ 100(극단적 탐욕).
 * 대시보드와 같은 구간 색을 쓴다.
 */
@Composable
fun FearGreedCard(
    index: Int?,
    label: String?,
    modifier: Modifier = Modifier,
) {
    val value = index?.coerceIn(0, 100)
    val accent = fearGreedColor(value)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = if (value == null) "-" else value.toString(),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = accent,
                )
                Spacer(Modifier.padding(horizontal = 4.dp))
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text(
                        text = label ?: "데이터 없음",
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                    )
                    Text(
                        text = "공포·탐욕 지수",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
            ) {
                val radius = CornerRadius(size.height / 2f, size.height / 2f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF4A8CFF),
                            Color(0xFF5FB3D4),
                            Color(0xFF9FB0C6),
                            Color(0xFFF5B73D),
                            Color(0xFFEF4444),
                        ),
                    ),
                    size = size,
                    cornerRadius = radius,
                )
                if (value != null) {
                    val x = (size.width * (value / 100f)).coerceIn(0f, size.width)
                    drawCircle(
                        color = Color.White,
                        radius = size.height * 0.85f,
                        center = Offset(x, size.height / 2f),
                    )
                    drawCircle(
                        color = accent,
                        radius = size.height * 0.55f,
                        center = Offset(x, size.height / 2f),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf("0 극단적 공포", "50 중립", "100 극단적 탐욕").forEach { tick ->
                    Text(
                        text = tick,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (value != null) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { value / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

/** 0~24 극단적 공포 … 75+ 극단적 탐욕 */
fun fearGreedColor(value: Int?): Color = when {
    value == null -> Color(0xFF8A94A6)
    value <= 24 -> Color(0xFF4A8CFF)
    value <= 44 -> Color(0xFF5FB3D4)
    value <= 55 -> Color(0xFF9FB0C6)
    value <= 75 -> Color(0xFFF5B73D)
    else -> Color(0xFFEF4444)
}
