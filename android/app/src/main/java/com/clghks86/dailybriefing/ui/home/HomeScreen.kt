package com.clghks86.dailybriefing.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clghks86.dailybriefing.domain.model.Briefing
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.ui.components.LoadingState
import com.clghks86.dailybriefing.ui.components.MessageState
import com.clghks86.dailybriefing.ui.components.SectionHeader
import com.clghks86.dailybriefing.ui.components.SelectableChip
import com.clghks86.dailybriefing.ui.home.components.FearGreedCard
import com.clghks86.dailybriefing.ui.home.components.IndicatorGrid
import com.clghks86.dailybriefing.ui.home.components.MarketNoteCard
import com.clghks86.dailybriefing.ui.home.components.PinHintCard
import com.clghks86.dailybriefing.ui.home.components.PinnedTickerBar
import com.clghks86.dailybriefing.ui.home.components.SummaryCard
import com.clghks86.dailybriefing.ui.util.DateLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenArticle: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = {
                    Column {
                        Text("데일리 브리핑", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = state.selected?.let { DateLabels.full(it.date) } ?: "경제·코인·금·뉴스",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "새로고침",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.refreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            when {
                state.loading && state.briefings.isEmpty() -> LoadingState()

                state.briefings.isEmpty() -> MessageState(
                    title = "브리핑을 찾지 못했습니다",
                    description = state.errorMessage ?: "아직 올라온 브리핑이 없습니다. 잠시 후 다시 확인해 주세요.",
                    actionLabel = "다시 시도",
                    onAction = { viewModel.refresh() },
                )

                else -> HomeContent(
                    state = state,
                    onSelectDate = viewModel::selectDate,
                    onToggleBookmark = viewModel::toggleBookmark,
                    onTogglePin = viewModel::togglePin,
                    onOpenArticle = onOpenArticle,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onSelectDate: (String) -> Unit,
    onToggleBookmark: () -> Unit,
    onTogglePin: (IndicatorId) -> Unit,
    onOpenArticle: (String) -> Unit,
) {
    val briefing = state.selected ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DateChipRow(
                dates = state.availableDates,
                selected = briefing.date,
                bookmarks = state.bookmarks,
                onSelect = onSelectDate,
            )
        }

        item {
            if (state.pinned.isEmpty()) {
                PinHintCard()
            } else {
                PinnedTickerBar(briefing = briefing, pinned = state.pinned)
            }
        }

        item {
            SummaryCard(
                briefing = briefing,
                bookmarked = state.isBookmarked,
                onToggleBookmark = onToggleBookmark,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                IndicatorGrid(
                    briefing = briefing,
                    pinned = state.pinned,
                    onTogglePin = onTogglePin,
                )

                if (briefing.fearGreed != null) {
                    SectionHeader(
                        title = "시장 심리",
                        emoji = "😱",
                        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp),
                    )
                    FearGreedCard(index = briefing.fearGreed, label = briefing.fearGreedLabel)
                }
            }
        }

        briefing.details.marketNote?.let { note ->
            item {
                MarketNoteCard(
                    note = note,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (state.bookmarks.isNotEmpty()) {
            item {
                BookmarkSection(
                    bookmarks = state.bookmarks,
                    selected = briefing.date,
                    onSelect = onSelectDate,
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = { onOpenArticle(briefing.date) }) {
                    Text("브리핑 전문 보기", style = MaterialTheme.typography.labelLarge)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Supabase 실시간 조회",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun DateChipRow(
    dates: List<String>,
    selected: String?,
    bookmarks: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(dates, key = { it }) { date ->
            SelectableChip(
                label = DateLabels.relativeChip(date),
                selected = date == selected,
                onClick = { onSelect(date) },
                leading = if (bookmarks.contains(date)) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.width(14.dp),
                        )
                    }
                } else {
                    null
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookmarkSection(
    bookmarks: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader(title = "저장한 날짜", emoji = "⭐", modifier = Modifier.padding(top = 6.dp, bottom = 8.dp))
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            bookmarks.forEach { date ->
                SelectableChip(
                    label = DateLabels.chip(date),
                    selected = date == selected,
                    onClick = { onSelect(date) },
                )
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}
