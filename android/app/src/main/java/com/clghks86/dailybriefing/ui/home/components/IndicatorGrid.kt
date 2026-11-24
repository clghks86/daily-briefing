package com.clghks86.dailybriefing.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.IndicatorSection
import com.clghks86.dailybriefing.ui.components.ChangeText
import com.clghks86.dailybriefing.ui.components.SectionHeader

/** 핵심 지표 그리드. 섹션마다 2열 카드, 각 카드의 별로 핀 고정. */
@Composable
fun IndicatorGrid(
    briefing: Briefing,
    pinned: List<IndicatorId>,
    onTogglePin: (IndicatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        IndicatorSection.entries.forEach { section ->
            val items = IndicatorId.gridOf(section).filter { briefing.indicator(it).hasValue }
            if (items.isEmpty()) return@forEach

            SectionHeader(
                title = section.title,
                emoji = section.emoji,
                modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
            )
            items.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    rowItems.forEach { id ->
                        IndicatorCard(
                            briefing = briefing,
                            id = id,
                            pinned = pinned.contains(id),
                            onTogglePin = { onTogglePin(id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun IndicatorCard(
    briefing: Briefing,
    id: IndicatorId,
    pinned: Boolean,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snapshot = briefing.indicator(id)

    Card(
        modifier = modifier.heightIn(min = 104.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = id.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onTogglePin) {
                    Icon(
                        imageVector = if (pinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (pinned) "${id.label} 고정 해제" else "${id.label} 고정",
                        tint = if (pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = IndicatorFormatter.formatValueOrDash(id, snapshot.value),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (snapshot.changePercent != null) {
                ChangeText(
                    change = snapshot.changePercent,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            val sub = subLine(briefing, id)
            if (sub != null) {
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

/** 카드 보조 줄: 코인 원화 표기, KRX 금 ETF, 김프 기준 등. */
private fun subLine(briefing: Briefing, id: IndicatorId): String? = when (id) {
    IndicatorId.GOLD_KRX -> briefing.details.goldEtf?.let { etf ->
        listOfNotNull(
            etf.aceKrx?.let { "ACE ${IndicatorFormatter.group(it, 0)}원(${IndicatorFormatter.formatChange(etf.aceChg)})" },
            etf.tigerKrx?.let { "TIGER ${IndicatorFormatter.group(it, 0)}원(${IndicatorFormatter.formatChange(etf.tigerChg)})" },
        ).joinToString(" · ").ifBlank { null }
    }

    IndicatorId.KIMCHI_PREMIUM -> "BTC 기준"

    else -> id.subId?.let { subId ->
        briefing.value(subId)?.let { value ->
            if (subId == IndicatorId.USDT_KRW) {
                "\u20A9" + IndicatorFormatter.group(value, 0)
            } else {
                IndicatorFormatter.formatKrwCompact(value)
            }
        }
    }
}
