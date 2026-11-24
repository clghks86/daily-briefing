package com.clghks86.dailybriefing.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clghks86.dailybriefing.domain.markdown.MarkdownBlock
import com.clghks86.dailybriefing.domain.markdown.MarkdownParser
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.ui.theme.LocalMarketColors

@Composable
fun SectionHeader(
    title: String,
    emoji: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (emoji != null) {
            Text(text = emoji, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** 등락률 텍스트 — 상승 빨강 / 하락 파랑. */
@Composable
fun ChangeText(
    change: Double?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.labelLarge,
    showArrow: Boolean = true,
) {
    val market = LocalMarketColors.current
    val color = market.of(change)
    val arrow = when {
        !showArrow -> ""
        change == null -> ""
        change > 0 -> "▲ "
        change < 0 -> "▼ "
        else -> ""
    }
    Text(
        text = arrow + IndicatorFormatter.formatChange(change),
        style = style,
        color = color,
        modifier = modifier,
    )
}

/** 값 + 등락률을 한 줄에. */
@Composable
fun ValueWithChange(
    value: String,
    change: Double?,
    valueStyle: TextStyle = MaterialTheme.typography.titleLarge,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = value,
            style = valueStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(6.dp))
        ChangeText(change = change, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun InfoChip(
    label: String,
    value: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                shape = RoundedCornerShape(50),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!value.isNullOrBlank()) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EventChips(events: List<String>, modifier: Modifier = Modifier, max: Int = 3) {
    if (events.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        events.take(max).forEach { event ->
            Row(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = event,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun LoadingState(message: String = "브리핑을 불러오는 중…", modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun MessageState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Composable
fun Dot(color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color = color, shape = CircleShape),
    )
}

@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        modifier = modifier,
        leadingIcon = leading,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
            selectedLabelColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

/** raw_markdown 전문을 가볍게 그린다(제목·목록·인용·구분선). */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    startAt: Int = 0,
) {
    val blocks = MarkdownParser.parse(markdown)
    val body = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier) {
        blocks.drop(startAt).forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    Spacer(Modifier.height(if (block.level <= 2) 18.dp else 12.dp))
                    Text(
                        text = annotatedInline(block.text, body, MaterialTheme.colorScheme.primary),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.headlineSmall
                            2 -> MaterialTheme.typography.titleLarge
                            else -> MaterialTheme.typography.titleMedium
                        },
                    )
                }

                is MarkdownBlock.Paragraph -> {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = annotatedInline(block.text, body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = body,
                    )
                }

                is MarkdownBlock.Bullet -> {
                    Row(modifier = Modifier.padding(top = 6.dp)) {
                        Text(
                            text = if (block.ordered) block.marker ?: "·" else "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(18.dp),
                        )
                        Text(
                            text = annotatedInline(block.text, body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = body,
                        )
                    }
                }

                is MarkdownBlock.Quote -> {
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(20.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = annotatedInline(block.text, muted),
                            style = MaterialTheme.typography.bodySmall,
                            color = muted,
                        )
                    }
                }

                MarkdownBlock.Divider -> {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outline),
                    )
                    Spacer(Modifier.height(4.dp))
                }

                MarkdownBlock.Spacer -> Unit
            }
        }
    }
}

@Composable
private fun annotatedInline(
    text: String,
    baseColor: androidx.compose.ui.graphics.Color,
    accentColor: androidx.compose.ui.graphics.Color = baseColor,
): AnnotatedString {
    val runs = MarkdownParser.parseInline(text)
    val codeBackground = MaterialTheme.colorScheme.surfaceVariant
    return buildAnnotatedString {
        runs.forEach { run ->
            when {
                run.bold -> withStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, color = accentColor),
                ) { append(run.text) }

                run.code -> withStyle(
                    SpanStyle(fontFamily = FontFamily.Monospace, background = codeBackground),
                ) { append(run.text) }

                else -> append(run.text)
            }
        }
    }
}
