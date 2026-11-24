package com.clghks86.dailybriefing.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clghks86.dailybriefing.BuildConfig
import com.clghks86.dailybriefing.core.SupabaseConfig
import com.clghks86.dailybriefing.data.local.UserPreferencesRepository
import com.clghks86.dailybriefing.domain.model.GoalDirection
import com.clghks86.dailybriefing.domain.model.IndicatorFormatter
import com.clghks86.dailybriefing.domain.model.IndicatorId
import com.clghks86.dailybriefing.domain.model.PriceGoal
import com.clghks86.dailybriefing.domain.model.ThemeMode
import com.clghks86.dailybriefing.ui.components.SelectableChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory()),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.setBriefingNotificationEnabled(true)
        if (!granted) viewModel.showMessage("알림 권한을 허용하지 않으면 알림이 오지 않습니다.")
    }

    fun toggleNotifications(enabled: Boolean) {
        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.setBriefingNotificationEnabled(enabled)
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
                        Text("알림 & 설정", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "앱 버전 ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsCard(title = "알림", emoji = "🔔") {
                    SwitchRow(
                        title = "매일 브리핑 알림",
                        description = "09:35경 새 브리핑이 올라오면 헤드라인을 알려 드립니다. FCM 서버 없이 기기에서 동작합니다.",
                        checked = state.notifyEnabled,
                        onCheckedChange = ::toggleNotifications,
                    )
                    state.lastNotifiedDate?.let { date ->
                        Text(
                            text = "마지막 알림: $date",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { viewModel.sendTestNotification() },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("테스트 알림") }
                        TextButton(
                            onClick = { viewModel.runBriefingCheckNow() },
                            contentPadding = PaddingValues(horizontal = 8.dp),
                        ) { Text("지금 새 브리핑 확인") }
                    }
                }
            }

            item {
                SettingsCard(title = "목표가 알림", emoji = "🎯") {
                    Text(
                        text = "등록한 지표가 목표가에 닿으면 알립니다. 15분 간격으로 확인하며, 같은 목표는 하루 한 번만 알립니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))

                    if (state.goals.isEmpty()) {
                        Text(
                            text = "등록된 목표가가 없습니다.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        state.goals.forEach { goal ->
                            GoalRow(
                                goal = goal,
                                onEnabledChange = { enabled -> viewModel.setGoalEnabled(goal.id, enabled) },
                                onDelete = { viewModel.deleteGoal(goal.id) },
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    TextButton(
                        onClick = { showGoalDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.width(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("목표가 추가")
                    }
                }
            }

            item {
                SettingsCard(title = "홈 화면 위젯", emoji = "🧩") {
                    Text(
                        text = "홈 화면을 길게 눌러 위젯 → 데일리 브리핑을 추가하면 코스피·BTC·KRX 금과 헤드라인을 바로 볼 수 있습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    TextButton(
                        onClick = { viewModel.refreshWidget() },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                    ) { Text("위젯 지금 새로고침") }
                }
            }

            item {
                SettingsCard(title = "관심 지표 (핀 고정)", emoji = "⭐") {
                    Text(
                        text = "홈 상단 티커에 고정할 지표를 고릅니다. 최대 ${UserPreferencesRepository.MAX_PINS}개.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    PinSelector(
                        pinned = state.pinned,
                        onToggle = viewModel::togglePin,
                    )
                }
            }

            item {
                SettingsCard(title = "화면", emoji = "🎨") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            SelectableChip(
                                label = mode.label,
                                selected = state.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                            )
                        }
                    }
                }
            }

            item {
                SettingsCard(title = "정보", emoji = "ℹ️") {
                    InfoRow("앱 버전", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    InfoRow("데이터", "Supabase · ${SupabaseConfig.host}")
                    InfoRow("공개 키", SupabaseConfig.maskedKey)
                    InfoRow("패키지", BuildConfig.APPLICATION_ID)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "브리핑 데이터는 매일 09:30 자동 생성되어 Supabase에 저장됩니다. " +
                            "핀 고정·북마크·목표가는 이 기기에만 저장되고 서버로 보내지 않습니다.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showGoalDialog) {
        GoalDialog(
            onDismiss = { showGoalDialog = false },
            onSave = { indicator, direction, target ->
                viewModel.addGoal(indicator, direction, target)
                showGoalDialog = false
            },
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    emoji: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun GoalRow(
    goal: PriceGoal,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = goal.describe(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (goal.lastFiredDate != null) {
                Text(
                    text = "마지막 알림 ${goal.lastFiredDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = goal.enabled, onCheckedChange = onEnabledChange)
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "목표가 삭제",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PinSelector(
    pinned: List<IndicatorId>,
    onToggle: (IndicatorId) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IndicatorId.entries
            .filter { it.gridVisible }
            .forEach { id ->
                val selected = pinned.contains(id)
                SelectableChip(
                    label = id.shortLabel,
                    selected = selected,
                    onClick = { onToggle(id) },
                    leading = {
                        Icon(
                            imageVector = if (selected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(14.dp),
                        )
                    },
                )
            }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(74.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalDialog(
    onDismiss: () -> Unit,
    onSave: (IndicatorId, GoalDirection, Double) -> Unit,
) {
    val options = remember { IndicatorId.entries.filter { it.goalEligible } }
    var indicator by remember { mutableStateOf(options.firstOrNull() ?: IndicatorId.KR_KOSPI) }
    var direction by remember { mutableStateOf(GoalDirection.ABOVE) }
    var targetText by remember { mutableStateOf("") }
    val target = targetText.replace(",", "").toDoubleOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("목표가 추가") },
        text = {
            Column {
                Text(
                    text = "지표 선택",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { option ->
                        SelectableChip(
                            label = option.label,
                            selected = indicator == option,
                            onClick = { indicator = option },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "조건",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoalDirection.entries.forEach { option ->
                        SelectableChip(
                            label = "${option.symbol} ${option.label}",
                            selected = direction == option,
                            onClick = { direction = option },
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { raw -> targetText = raw.filter { it.isDigit() || it == '.' || it == ',' } },
                    label = { Text("목표 값") },
                    placeholder = {
                        Text(
                            text = "예: " + exampleTarget(indicator),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (target != null && target > 0) {
                    Text(
                        text = "→ " + IndicatorFormatter.formatValue(indicator, target),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = target != null && target > 0,
                onClick = { target?.let { onSave(indicator, direction, it) } },
            ) { Text("등록") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("취소") }
        },
    )
}

private fun exampleTarget(id: IndicatorId): String = when (id) {
    IndicatorId.KR_KOSPI -> "5000"
    IndicatorId.GOLD_KRX -> "160000"
    IndicatorId.BTC_USD -> "70000"
    IndicatorId.BTC_KRW -> "90000000"
    IndicatorId.USD_KRW -> "1400"
    else -> "0"
}
