package com.clghks86.dailybriefing.ui.news

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.ui.components.LoadingState
import com.clghks86.dailybriefing.ui.components.MarkdownText
import com.clghks86.dailybriefing.ui.components.MessageState
import com.clghks86.dailybriefing.ui.util.DateLabels

/** 브리퍼 전문 뷰어 — raw_markdown을 그대로 읽는다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    date: String,
    onBack: () -> Unit,
    viewModel: ArticleViewModel = viewModel(factory = ArticleViewModel.factory(date)),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                        )
                    }
                },
                title = {
                    Column {
                        Text("브리핑 전문", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = DateLabels.full(date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        val briefing = state.briefing
        when {
            state.loading && briefing == null -> LoadingState(
                message = "전문을 불러오는 중…",
                modifier = Modifier.padding(padding),
            )

            briefing == null -> MessageState(
                title = "전문을 찾을 수 없습니다",
                description = state.errorMessage ?: "다른 날짜를 선택해 보세요.",
                modifier = Modifier.padding(padding),
                actionLabel = "다시 시도",
                onAction = { viewModel.load(forceRefresh = true) },
            )

            else -> LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 40.dp),
            ) {
                briefing.details.asOfKst?.let { asOf ->
                    item {
                        Text(
                            text = "기준 시각 " + (IndicatorFormatter.shortTimestamp(asOf) ?: asOf),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                briefing.image(2)?.let { image ->
                    item {
                        Spacer(Modifier.height(10.dp))
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth(),
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

                item {
                    Spacer(Modifier.height(8.dp))
                    MarkdownText(markdown = briefing.rawMarkdown)
                }
            }
        }
    }
}
