package com.clghks86.dailybriefing.ui.news

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.clghks86.dailybriefing.domain.model.NewsItem
import com.clghks86.dailybriefing.ui.components.Dot
import com.clghks86.dailybriefing.ui.components.LoadingState
import com.clghks86.dailybriefing.ui.components.MessageState
import com.clghks86.dailybriefing.ui.components.SectionHeader
import com.clghks86.dailybriefing.ui.components.SelectableChip
import com.clghks86.dailybriefing.ui.home.components.MarketNoteCard
import com.clghks86.dailybriefing.ui.util.DateLabels
import com.clghks86.dailybriefing.ui.util.LinkOpener

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    onOpenArticle: (String) -> Unit,
    viewModel: NewsViewModel = viewModel(factory = NewsViewModel.factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = {
                    Column {
                        Text("뉴스 & 전문", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = state.selected?.let { "${DateLabels.full(it.date)} · 뉴스 ${it.details.news.size}건" }
                                ?: "주요 뉴스 원문과 브리핑 전문",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.loading && state.briefings.isEmpty() -> LoadingState(
                message = "뉴스를 불러오는 중…",
                modifier = Modifier.padding(padding),
            )

            state.briefings.isEmpty() -> MessageState(
                title = "표시할 브리핑이 없습니다",
                description = state.errorMessage ?: "잠시 후 다시 시도해 주세요.",
                modifier = Modifier.padding(padding),
                actionLabel = "다시 시도",
                onAction = { viewModel.load(forceRefresh = true) },
            )

            else -> {
                val briefing = state.selected ?: return@Scaffold
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(state.briefings.map { it.date }, key = { it }) { date ->
                                SelectableChip(
                                    label = DateLabels.relativeChip(date),
                                    selected = date == briefing.date,
                                    onClick = { viewModel.selectDate(date) },
                                )
                            }
                        }
                    }

                    briefing.image(3)?.let { image ->
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                            ) {
                                Column {
                                    AsyncImage(
                                        model = image.url,
                                        contentDescription = image.caption,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f),
                                    )
                                    image.caption?.let { caption ->
                                        Text(
                                            text = caption,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(12.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (briefing.details.news.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "오늘의 주요 뉴스",
                                emoji = "📰",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                        items(briefing.details.news.size) { index ->
                            val news = briefing.details.news[index]
                            NewsCard(
                                news = news,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onOpen = { LinkOpener.open(context, news.url) },
                            )
                        }
                    } else {
                        item {
                            Text(
                                text = "이 날짜에는 뉴스 목록이 없습니다.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    briefing.details.marketNote?.let { note ->
                        item { MarketNoteCard(note = note, modifier = Modifier.padding(horizontal = 16.dp)) }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Dot(MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "브리핑 전문에는 지표 해설과 전체 뉴스가 들어 있습니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { onOpenArticle(briefing.date) }) {
                                Text("전문 보기", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewsCard(
    news: NewsItem,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                news.category?.let { category ->
                    Box(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                RoundedCornerShape(6.dp),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = news.source.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = news.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            news.summary?.let { summary ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(14.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "원문 보기",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
