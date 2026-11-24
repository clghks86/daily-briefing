package com.clghks86.dailybriefing.ui.trend

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clghks86.dailybriefing.domain.model.ChartGroup
import com.clghks86.dailybriefing.ui.components.LoadingState
import com.clghks86.dailybriefing.ui.components.MessageState
import com.clghks86.dailybriefing.ui.trend.components.LineChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendScreen(
    viewModel: TrendViewModel = viewModel(factory = TrendViewModel.factory()),
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
                title = {
                    Column {
                        Text("추이 차트", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "${state.briefings.size}일치 누적 · 날짜가 쌓일수록 길어집니다",
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
                message = "차트 데이터를 준비하는 중…",
                modifier = Modifier.padding(padding),
            )

            state.briefings.isEmpty() -> MessageState(
                title = "차트로 그릴 데이터가 없습니다",
                description = "브리핑이 쌓이면 추이가 그려집니다.",
                modifier = Modifier.padding(padding),
                actionLabel = "다시 시도",
                onAction = { viewModel.load(forceRefresh = true) },
            )

            else -> Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                TabRow(
                    selectedTabIndex = state.group.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {},
                ) {
                    ChartGroup.entries.forEach { group ->
                        Tab(
                            selected = state.group == group,
                            onClick = { viewModel.selectGroup(group) },
                            text = {
                                Text(
                                    text = group.title,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                        )
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (state.rebase) "첫날 = 100 기준" else "실제 값",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = if (state.rebase) {
                                            "가격대가 다른 지표를 같은 출발선에서 견줍니다."
                                        } else {
                                            "각 지표의 실제 값으로 그립니다."
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Switch(
                                    checked = state.rebase,
                                    onCheckedChange = { viewModel.toggleRebase() },
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                            state.series?.let { series ->
                                LineChart(series = series, height = 250.dp)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = groupNote(state.group),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))

                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "차트를 좌우로 드래그하면 그날의 값이 위에 표시됩니다.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                }
            }
        }
    }
}

private fun groupNote(group: ChartGroup): String = when (group) {
    ChartGroup.INDEX -> "미국 3대 지수와 코스피·코스닥. 첫 표시일을 100으로 환산해 흐름을 견줍니다."
    ChartGroup.GOLD -> "국제 금(달러/온스)과 KRX 금현물(원/그램). 단위가 달라 실제 값으로 봅니다."
    ChartGroup.CRYPTO -> "BTC·ETH·XRP·SOL. 변동 폭을 비교하기 위해 첫날을 100으로 맞춥니다."
    ChartGroup.FX_RATE -> "원/달러 환율과 미 10년물 금리. 실제 값으로 표시합니다."
}
