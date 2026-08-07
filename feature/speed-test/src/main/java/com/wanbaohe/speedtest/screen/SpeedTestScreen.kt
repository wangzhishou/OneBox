package com.wanbaohe.speedtest.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import com.shifenmiao.theme.AppTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.speedtest.R
import com.wanbaohe.speedtest.component.SpeedTestComponent
import com.wanbaohe.speedtest.component.SpeedTestStatus
import com.wanbaohe.speedtest.component.SpeedTestUiState
import com.wanbaohe.speedtest.data.SpeedTestConfig
import com.wanbaohe.speedtest.screen.ui.SpeedGauge
import com.wanbaohe.speedtest.screen.ui.SpeedHistorySheet
import com.wanbaohe.speedtest.screen.ui.SpeedMetricsRow
import com.wanbaohe.speedtest.screen.ui.SpeedResultDisplay
import com.wanbaohe.speedtest.screen.ui.SpeedTestConfigDialog
import com.wanbaohe.speedtest.screen.ui.SpeedTestConfigSheet
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineWifi
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSpeedTest

@Composable
fun SpeedTestScreen(component: SpeedTestComponent) {
    val uiState by component.uiState.collectAsState()
    var showHistory by remember { mutableStateOf(false) }
    var showConfigSheet by remember { mutableStateOf(false) }
    /** null = 关闭；SpeedTestConfig(id=0) = 新增；id>0 = 编辑 */
    var editingConfig by remember { mutableStateOf<SpeedTestConfig?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMsg) {
        uiState.errorMsg?.let { snackbarHostState.showSnackbar(it) }
    }

    BaseScreen(
        title = stringResource(R.string.speed_test_screen_title),
        onGoBack = component.onGoBack,
        actions = {
            IconButton(
                onClick = { showConfigSheet = true },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                    contentDescription = stringResource(R.string.speed_test_config_icon_desc)
                )
            }
        },
        foreground = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
            ) { data -> Snackbar(snackbarData = data) }
        }
    ) {
        AnimatedContent(
            targetState = uiState.status,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "speed_screen_transition"
        ) { status ->
            when (status) {
                SpeedTestStatus.IDLE -> IdleContent(
                    uiState = uiState,
                    onStart = component::startTest,
                    onOpenWifi = component::openWifiSettings,
                    onShowHistory = { showHistory = true }
                )
                SpeedTestStatus.MEASURING -> MeasuringContent(
                    uiState = uiState,
                    onCancel = component::cancelTest,
                    onShowHistory = { showHistory = true }
                )
                SpeedTestStatus.DONE -> DoneContent(
                    uiState = uiState,
                    onRestart = component::restartTest,
                    onShowHistory = { showHistory = true }
                )
            }
        }
    }

    // ── 测速历史 ──────────────────────────────────────────────────────────
    SpeedHistorySheet(
        visible = showHistory,
        records = uiState.history,
        onClear = component::clearHistory,
        onDismiss = { showHistory = false }
    )

    // ── 配置管理弹层 ──────────────────────────────────────────────────────
    SpeedTestConfigSheet(
        visible = showConfigSheet,
        configs = uiState.configList,
        activeConfigId = uiState.config.id,
        onSelect = { id ->
            component.selectConfig(id)
            showConfigSheet = false
        },
        onEdit = { config ->
            editingConfig = config
            showConfigSheet = false
        },
        onDelete = component::deleteConfig,
        onAdd = {
            editingConfig = SpeedTestConfig()   // id=0 代表新增
            showConfigSheet = false
        },
        onDismiss = { showConfigSheet = false }
    )

    // ── 新增 / 编辑配置弹窗 ───────────────────────────────────────────────
    SpeedTestConfigDialog(
        visible = editingConfig != null,
        initial = editingConfig,
        onSave = { saved ->
            if (saved.id > 0) component.updateConfig(saved)
            else component.addConfig(
                name = saved.name,
                testUrl = saved.testUrl,
                estimatedMb = saved.estimatedDataMb,
                durationSeconds = saved.durationSeconds
            )
            editingConfig = null
        },
        onDismiss = { editingConfig = null }
    )
}

// ── 活跃配置名称 Chip（带图标 + 脉冲动画）─────────────────────────────────────

@Composable
private fun ActiveConfigChip(name: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "chip_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val containerColor = AppTheme.colors.getSecondaryContainerButtonColors().containerColor
    val contentColor = AppTheme.colors.getSecondaryContainerButtonColors().contentColor

    Row(
        modifier = Modifier
            .glassThin(
                color = containerColor,
                shape = RoundedCornerShape(50),
                borderWidth = 0.dp
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSpeedTest,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = pulseAlpha)
        )
    }
}

// ── 玻璃风格底部操作按钮 ────────────────────────────────────────────────────────

@Composable
private fun GlassActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = AppTheme.colors.getPrimaryColor()
    Row(
        modifier = modifier
            .glassThin(
                color = primaryColor.copy(alpha = 0.10f),
                shape = RoundedCornerShape(50),
                borderWidth = 0.dp
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = primaryColor
        )
    }
}

// ── 空闲页 ────────────────────────────────────────────────────────────────────

@Composable
private fun IdleContent(
    uiState: SpeedTestUiState,
    onStart: () -> Unit,
    onOpenWifi: () -> Unit,
    onShowHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 顶部指标行（全 "--"）
        SpeedMetricsRow(latencyDisplay = "--", downloadDisplay = "--")

        Spacer(Modifier.weight(1f))

        // 圆形仪表盘
        SpeedGauge(
            status = SpeedTestStatus.IDLE,
            liveMbps = 0f,
            progress = 0f,
            estimatedMb = uiState.config.estimatedDataMb,
            onClick = onStart
        )

        Spacer(Modifier.height(12.dp))

        // 当前激活配置名称
        ActiveConfigChip(name = uiState.config.name)

        Spacer(Modifier.weight(1f))

        // 当前网络信息 + 操作按钮
        Text(
            text = stringResource(R.string.speed_test_current_network, uiState.networkType),
            fontSize = 14.sp,
            color = AppTheme.colors.getOnInactiveContainerColor()
        )

        Spacer(Modifier.height(12.dp))

        // 底部操作区
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassActionButton(
                text = stringResource(R.string.speed_test_switch_wifi),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineWifi,
                onClick = onOpenWifi
            )
            GlassActionButton(
                text = stringResource(R.string.speed_test_history_link),
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                onClick = onShowHistory
            )
        }

        Spacer(Modifier.height(12.dp))

        // 测速免责声明
        Text(
            text = stringResource(R.string.speed_test_disclaimer),
            fontSize = 11.sp,
            color = AppTheme.colors.getOnInactiveContainerColor().copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ── 测速中页 ───────────────────────────────────────────────────────────────

@Composable
private fun MeasuringContent(
    uiState: SpeedTestUiState,
    onCancel: () -> Unit,
    onShowHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SpeedMetricsRow(latencyDisplay = "--", downloadDisplay = "--")

        Spacer(Modifier.weight(1f))

        SpeedGauge(
            status = SpeedTestStatus.MEASURING,
            liveMbps = uiState.liveMbps,
            progress = uiState.progress,
            estimatedMb = uiState.config.estimatedDataMb,
            onClick = onCancel
        )

        Spacer(Modifier.height(10.dp))
        ActiveConfigChip(name = uiState.config.name)

        Spacer(Modifier.height(16.dp))

        // 圆形取消按钮（FAB 风格）
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                )
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(R.string.speed_test_cancel),
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        GlassActionButton(
            text = stringResource(R.string.speed_test_history_link),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
            onClick = onShowHistory
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ── 完成页 ────────────────────────────────────────────────────────────────────

@Composable
private fun DoneContent(
    uiState: SpeedTestUiState,
    onRestart: () -> Unit,
    onShowHistory: () -> Unit
) {
    val result = uiState.result ?: return
    val primaryColor = AppTheme.colors.getPrimaryColor()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 测速结果展示
        SpeedResultDisplay(record = result)

        Spacer(Modifier.height(12.dp))
        ActiveConfigChip(name = uiState.config.name)

        Spacer(Modifier.weight(1f))

        // 渐变色"重新测速"按钮（纯主题色渐变，无双重背景）
        val secondaryColor = MaterialTheme.colorScheme.secondary
        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = AppTheme.colors.getOnPrimaryColor(),
                disabledContainerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(primaryColor, secondaryColor)
                        ),
                        shape = RoundedCornerShape(27.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.speed_test_restart),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.getOnPrimaryColor()
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        GlassActionButton(
            text = stringResource(R.string.speed_test_history_link),
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
            onClick = onShowHistory
        )

        Spacer(Modifier.height(16.dp))
    }
}
