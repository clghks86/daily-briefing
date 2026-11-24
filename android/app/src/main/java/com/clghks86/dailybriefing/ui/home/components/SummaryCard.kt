package com.clghks86.dailybriefing.ui.home.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.ui.components.Dot
import com.clghks86.dailybriefing.ui.components.EventChips
import com.clghks86.dailybriefing.ui.components.InfoChip
import com.clghks86.dailybriefing.ui.util.DateLabels

/** 오늘의 요약 카드 — 커버 이미지(sort_order=1) + 헤드라인 + 요약 + 핵심 이벤트. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryCard(
    briefing: Briefing,
    bookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember(briefing.date) { mutableStateOf(false) }
    val cover = briefing.image(1)
    val details = briefing.details

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            if (cover != null) {
                Box {
                    AsyncImage(
                        model = cover.url,
                        contentDescription = cover.caption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xCC0B0F14)),
                                ),
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp),
                    ) {
                        Text(
                            text = DateLabels.full(briefing.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFD8E0EA),
                        )
                        cover.caption?.let { caption ->
                            Text(
                                text = caption,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                    Text(
                        text = DateLabels.full(briefing.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = briefing.headline,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (bookmarked) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (bookmarked) "북마크 해제" else "이 날짜 북마크",
                            tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (briefing.summary.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = briefing.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (briefing.summary.length > 160) {
                        TextButton(
                            onClick = { expanded = !expanded },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        ) {
                            Text(
                                text = if (expanded) "접기" else "더 보기",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    details.asOfKst?.let { asOf ->
                        InfoChip(label = "기준 시각", value = IndicatorFormatter.shortTimestamp(asOf) ?: asOf)
                    }
                    details.dxy?.let { dxy -> InfoChip(label = "DXY", value = IndicatorFormatter.group(dxy, 2)) }
                    details.wtiUsd?.let { wti -> InfoChip(label = "WTI", value = "$" + IndicatorFormatter.group(wti, 2)) }
                    details.vix?.let { vix -> InfoChip(label = "VIX", value = IndicatorFormatter.group(vix, 2)) }
                    briefing.fearGreed?.let { fg -> InfoChip(label = "공포·탐욕", value = "$fg ${briefing.fearGreedLabel ?: ""}") }
                }

                if (details.keyEvents.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    EventChips(events = details.keyEvents, max = if (expanded) 5 else 3)
                }
            }
        }
    }
}

/** 국내 시장 특이사항처럼 긴 설명 문단. */
@Composable
fun MarketNoteCard(note: String, modifier: Modifier = Modifier) {
    var expanded by remember(note) { mutableStateOf(false) }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "시장 메모",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (note.length > 120) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                ) {
                    Text(
                        text = if (expanded) "접기" else "전체 보기",
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
}
