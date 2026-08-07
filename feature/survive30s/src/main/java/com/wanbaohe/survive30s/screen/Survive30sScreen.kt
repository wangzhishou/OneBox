package com.wanbaohe.survive30s.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import java.util.Locale
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.wanbaohe.survive30s.R
import com.wanbaohe.survive30s.component.GameState
import com.wanbaohe.survive30s.component.Survive30sComponent
import com.wanbaohe.survive30s.component.SurvivalPhase
import com.wanbaohe.survive30s.component.Survive30sUiState
import com.wanbaohe.survive30s.engine.Survive30sEngine
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

/**
 * 躲避30秒游戏主页面
 *
 * 使用 Canvas 高性能渲染：
 * - 玩家：带光晕的圆形
 * - 障碍物：带渐变色的圆形
 * - 倒计时：顶部进度条
 */
@Composable
fun Survive30sScreen(
    component: Survive30sComponent,
) {
    val state by component.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var previousGameState by remember { mutableStateOf(state.gameState) }
    var previousShieldCount by remember { mutableStateOf(state.shieldCount) }

    LaunchedEffect(state.gameState, state.shieldCount) {
        if (state.shieldCount > previousShieldCount) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        if (state.gameState != previousGameState) {
            when (state.gameState) {
                GameState.GAME_OVER -> haptic.performHapticFeedback(HapticFeedbackType.Reject)
                GameState.WIN -> {
                    haptic.performHapticFeedback(HapticFeedbackType.GestureEnd)
                    AppToastHost.showConfetti()
                }
                else -> Unit
            }
        }
        previousGameState = state.gameState
        previousShieldCount = state.shieldCount
    }

    BaseScreen(
        title = stringResource(R.string.survive_30s_title),
        onGoBack = component.onGoBack,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── 倒计时条 + 时间文字 ────────────────────────────────────
            TimerSection(
                elapsedSec = state.elapsedSec,
                totalSec = Survive30sEngine.GAME_DURATION,
                bestTime = state.bestTime,
                shieldCount = state.shieldCount,
                nearMissCount = state.nearMissCount,
                nearMissCharge = state.nearMissCharge,
                dangerLevel = state.dangerLevel,
                phase = state.phase,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // ── 游戏画布 ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                GameCanvas(
                    state = state,
                    onCanvasSizeChanged = { w, h -> component.initCanvas(w, h) },
                    onDrag = { x, y -> component.movePlayerTo(x, y) },
                    onTap = {
                        if (state.gameState == GameState.IDLE) component.startGame()
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                // ── 覆盖层：开始 / 结束 / 胜利 ───────────────────────
                when (state.gameState) {
                    GameState.IDLE -> IdleOverlay(onStart = component::startGame)
                    GameState.GAME_OVER -> GameOverOverlay(
                        elapsed = state.elapsedSec,
                        bestTime = state.bestTime,
                        nearMissCount = state.nearMissCount,
                        onRestart = component::startGame,
                    )
                    GameState.WIN -> WinOverlay(
                        nearMissCount = state.nearMissCount,
                        onRestart = component::startGame,
                    )
                    GameState.PLAYING -> { /* 游戏进行中不显示覆盖层 */ }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─── 计时器区域 ────────────────────────────────────────────────────────────────

@Composable
private fun TimerSection(
    elapsedSec: Float,
    totalSec: Float,
    bestTime: Float,
    shieldCount: Int,
    nearMissCount: Int,
    nearMissCharge: Int,
    dangerLevel: Float,
    phase: SurvivalPhase,
    modifier: Modifier = Modifier,
) {
    val progress = (elapsedSec / totalSec).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "timer_progress"
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f", totalSec - elapsedSec) + "s",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (bestTime > 0f) {
                Text(
                    text = stringResource(R.string.survive_30s_best_time, bestTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(
                title = stringResource(R.string.survive_30s_phase),
                value = when (phase) {
                    SurvivalPhase.Warmup -> stringResource(R.string.survive_30s_phase_warmup)
                    SurvivalPhase.Rush -> stringResource(R.string.survive_30s_phase_rush)
                    SurvivalPhase.Storm -> stringResource(R.string.survive_30s_phase_storm)
                },
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                title = stringResource(R.string.survive_30s_shield),
                value = shieldCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                title = stringResource(R.string.survive_30s_charge),
                value = stringResource(R.string.survive_30s_charge_value, nearMissCharge, 3),
                modifier = Modifier.weight(1.15f),
            )
            StatusPill(
                title = stringResource(R.string.survive_30s_near_miss),
                value = nearMissCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        // 进度条：表示剩余时间，从满到空，颜色绿（安全）→ 橙 → 红（危险）
        val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        val progressColor = when {
            progress < 0.33f -> Color(0xFF66BB6A)
            progress < 0.66f -> Color(0xFFFFA726)
            else -> MaterialTheme.colorScheme.error
        }
        val remainingProgress = 1f - animatedProgress

        Canvas(modifier = Modifier.fillMaxWidth().height(6.dp)) {
            drawRoundRect(
                color = backgroundColor,
                cornerRadius = CornerRadius(3.dp.toPx()),
            )
            drawRoundRect(
                color = progressColor,
                cornerRadius = CornerRadius(3.dp.toPx()),
                size = Size(size.width * remainingProgress, size.height),
            )
        }

        Text(
            text = when {
                dangerLevel > 0.8f -> stringResource(R.string.survive_30s_danger_high)
                dangerLevel > 0.45f -> stringResource(R.string.survive_30s_danger_medium)
                else -> stringResource(R.string.survive_30s_danger_low)
            },
            style = MaterialTheme.typography.bodySmall,
            color = when {
                dangerLevel > 0.8f -> MaterialTheme.colorScheme.error
                dangerLevel > 0.45f -> Color(0xFFFFA726)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun StatusPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier,
        borderWidth = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
            )
        }
    }
}

// ─── 游戏画布 ──────────────────────────────────────────────────────────────────

@Composable
private fun GameCanvas(
    state: Survive30sUiState,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerPalette = rememberPlayerPalette()

    // 使用 mutableState 记录拖拽中的实时坐标，避免 state 滞后
    var dragTargetX by remember { mutableStateOf(Float.NaN) }
    var dragTargetY by remember { mutableStateOf(Float.NaN) }

    Canvas(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 拖拽开始时，直接跳到手指位置
                        dragTargetX = offset.x
                        dragTargetY = offset.y
                        onDrag(offset.x, offset.y)
                    },
                    onDragCancel = {
                        dragTargetX = Float.NaN
                        dragTargetY = Float.NaN
                    },
                    onDragEnd = {
                        dragTargetX = Float.NaN
                        dragTargetY = Float.NaN
                    }
                ) { change, dragAmount ->
                    change.consume()
                    val newX = (dragTargetX.takeIf { !it.isNaN() } ?: state.player.x) + dragAmount.x
                    val newY = (dragTargetY.takeIf { !it.isNaN() } ?: state.player.y) + dragAmount.y
                    dragTargetX = newX
                    dragTargetY = newY
                    onDrag(newX, newY)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { onTap() }
            }
    ) {
        // 首次布局时通知画布尺寸
        if (state.canvasWidth == 0f && size.width > 0f) {
            onCanvasSizeChanged(size.width, size.height)
        }

        val player = state.player

        // ── 绘制背景网格（轻微视觉参考线） ──
        drawGrid(size)
        drawDangerOverlay(size = size, dangerLevel = state.dangerLevel)

        // ── 绘制障碍物 ──
        state.obstacles.forEach { obs ->
            drawObstacle(obs)
        }

        // ── 绘制玩家 ──
        if (player.radius > 0f) {
            drawPlayer(
                player = player,
                invincibleSec = state.invincibleSec,
                elapsedSec = state.elapsedSec,
                palette = playerPalette,
            )
        }
    }
}

/** 绘制淡色参考网格 */
private fun DrawScope.drawGrid(canvasSize: Size) {
    val gridColor = Color.White.copy(alpha = 0.04f)
    val gridSpacing = canvasSize.width / 10f
    for (i in 1 until 10) {
        val x = gridSpacing * i
        drawLine(gridColor, Offset(x, 0f), Offset(x, canvasSize.height), strokeWidth = 1f)
    }
    for (i in 1 until 15) {
        val y = gridSpacing * i
        if (y < canvasSize.height) {
            drawLine(gridColor, Offset(0f, y), Offset(canvasSize.width, y), strokeWidth = 1f)
        }
    }
}

/** 障碍物颜色方案 */
private val OBSTACLE_COLORS = listOf(
    Color(0xFFEF5350), // 红
    Color(0xFFFF7043), // 橙
    Color(0xFFEC407A), // 粉
    Color(0xFFAB47BC), // 紫
    Color(0xFFFFCA28), // 黄
    Color(0xFF26C6DA), // 青
)

@Immutable
private data class PlayerPalette(
    val sweepColors: List<Color>,
    val glowColor: Color,
    val highlightColor: Color,
    val rimColor: Color,
    val shadowColor: Color,
    val invincibleColors: List<Color>,
)

@Composable
private fun rememberPlayerPalette(): PlayerPalette {
    val colorScheme = MaterialTheme.colorScheme

    return remember(
        colorScheme.primary,
        colorScheme.secondary,
        colorScheme.tertiary,
        colorScheme.primaryContainer,
        colorScheme.secondaryContainer,
        colorScheme.tertiaryContainer,
        colorScheme.surface,
        colorScheme.background,
        colorScheme.onSurface,
        colorScheme.onPrimaryContainer,
        colorScheme.inversePrimary,
    ) {
        val isDarkTheme = colorScheme.background.luminance() < 0.5f
        val anchors = listOf(
            colorScheme.primary,
            colorScheme.secondary,
            colorScheme.tertiary,
            colorScheme.primaryContainer,
            colorScheme.tertiaryContainer,
        )
        val surfaceLink = if (isDarkTheme) {
            blendColors(colorScheme.surface, colorScheme.onSurface, 0.08f)
        } else {
            blendColors(colorScheme.surface, colorScheme.background, 0.55f)
        }
        val themedSpectrum = anchors.mapIndexed { index, color ->
            val linked = blendColors(
                start = color,
                end = anchors[(index + 1) % anchors.size],
                ratio = 0.24f + index * 0.04f,
            )
            val surfaceBalanced = blendColors(
                start = linked,
                end = surfaceLink,
                ratio = if (isDarkTheme) 0.18f else 0.12f,
            )

            if (isDarkTheme) {
                blendColors(surfaceBalanced, Color.White, 0.10f)
            } else {
                blendColors(surfaceBalanced, Color.Black, 0.06f)
            }
        }
        val seamColor = blendColors(themedSpectrum.first(), themedSpectrum.last(), 0.5f)

        PlayerPalette(
            sweepColors = themedSpectrum + seamColor,
            glowColor = blendColors(themedSpectrum[0], themedSpectrum[2], 0.5f).copy(
                alpha = if (isDarkTheme) 0.42f else 0.30f,
            ),
            highlightColor = blendColors(
                colorScheme.onPrimaryContainer,
                Color.White,
                if (isDarkTheme) 0.35f else 0.58f,
            ).copy(alpha = 0.96f),
            rimColor = blendColors(
                colorScheme.onSurface,
                colorScheme.primary,
                if (isDarkTheme) 0.36f else 0.24f,
            ).copy(alpha = 0.82f),
            shadowColor = blendColors(
                colorScheme.surface,
                colorScheme.onSurface,
                if (isDarkTheme) 0.18f else 0.10f,
            ).copy(alpha = if (isDarkTheme) 0.24f else 0.16f),
            invincibleColors = listOf(
                blendColors(colorScheme.inversePrimary, colorScheme.secondary, 0.35f),
                blendColors(colorScheme.tertiary, colorScheme.primaryContainer, 0.42f),
                blendColors(colorScheme.secondaryContainer, colorScheme.primary, 0.36f),
                blendColors(colorScheme.inversePrimary, colorScheme.secondary, 0.35f),
            ),
        )
    }
}

private fun blendColors(
    start: Color,
    end: Color,
    ratio: Float,
): Color = Color(
    ColorUtils.blendARGB(start.toArgb(), end.toArgb(), ratio.coerceIn(0f, 1f))
)

/** 绘制障碍物：渐变圆 + 外发光 */
private fun DrawScope.drawObstacle(obs: com.wanbaohe.survive30s.engine.Obstacle) {
    val baseColor = OBSTACLE_COLORS[obs.colorIndex % OBSTACLE_COLORS.size]

    // 外发光
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(baseColor.copy(alpha = 0.3f), Color.Transparent),
            center = Offset(obs.x, obs.y),
            radius = obs.radius * 1.8f,
        ),
        radius = obs.radius * 1.8f,
        center = Offset(obs.x, obs.y),
    )

    // 主体
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(baseColor.copy(alpha = 0.95f), baseColor.copy(alpha = 0.6f)),
            center = Offset(obs.x - obs.radius * 0.3f, obs.y - obs.radius * 0.3f),
            radius = obs.radius,
        ),
        radius = obs.radius,
        center = Offset(obs.x, obs.y),
    )
}

/** 绘制玩家：光晕 + 实心圆 + 边框 */
private fun DrawScope.drawPlayer(
    player: com.wanbaohe.survive30s.engine.Player,
    invincibleSec: Float,
    elapsedSec: Float,
    palette: PlayerPalette,
) {
    val center = Offset(player.x, player.y)
    val rotation = (elapsedSec * 54f) % 360f

    // 光晕
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.glowColor, Color.Transparent),
            center = center,
            radius = player.radius * 2.5f,
        ),
        radius = player.radius * 2.5f,
        center = center,
    )

    rotate(degrees = rotation, pivot = center) {
        drawCircle(
            brush = Brush.sweepGradient(
                colors = palette.sweepColors,
                center = center,
            ),
            radius = player.radius,
            center = center,
        )
    }

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(palette.highlightColor, Color.Transparent),
            center = Offset(
                x = player.x - player.radius * 0.42f,
                y = player.y - player.radius * 0.48f,
            ),
            radius = player.radius * 1.05f,
        ),
        radius = player.radius * 0.96f,
        center = center,
    )

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, palette.shadowColor),
            center = Offset(
                x = player.x + player.radius * 0.58f,
                y = player.y + player.radius * 0.65f,
            ),
            radius = player.radius * 1.35f,
        ),
        radius = player.radius,
        center = center,
    )

    // 边框
    drawCircle(
        color = palette.rimColor,
        radius = player.radius,
        center = center,
        style = Stroke(width = 2.dp.toPx()),
    )

    if (invincibleSec > 0f) {
        rotate(degrees = -rotation * 0.75f, pivot = center) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = palette.invincibleColors,
                    center = center,
                ),
                radius = player.radius * 1.55f,
                center = center,
                style = Stroke(width = 3.dp.toPx()),
            )
        }
    }
}

private fun DrawScope.drawDangerOverlay(
    size: Size,
    dangerLevel: Float,
) {
    if (dangerLevel <= 0f) return

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color(0xFFEF5350).copy(alpha = 0.08f * dangerLevel),
            )
        ),
        size = size,
    )
}

// ─── 覆盖层组件 ────────────────────────────────────────────────────────────────

/** 等待开始覆盖层（点击任意处即可开始） */
@Composable
private fun IdleOverlay(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { onStart() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.survive_30s_ready),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.survive_30s_instruction),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.survive_30s_instruction_bonus),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(8.dp))
            GlassButton(
                onClick = onStart,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                borderWidth = 0.dp,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
            ) {
                Text(
                    text = stringResource(R.string.survive_30s_start),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** 游戏失败覆盖层 */
@Composable
private fun GameOverOverlay(
    elapsed: Float,
    bestTime: Float,
    nearMissCount: Int,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.survive_30s_game_over),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(R.string.survive_30s_survived_time, elapsed),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (elapsed >= bestTime) {
                Text(
                    text = stringResource(R.string.survive_30s_new_record),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA726),
                )
            }
            Text(
                text = stringResource(R.string.survive_30s_near_miss_count, nearMissCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            GlassButton(
                onClick = onRestart,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                borderWidth = 0.dp,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.survive_30s_retry),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** 胜利覆盖层 */
@Composable
private fun WinOverlay(
    nearMissCount: Int,
    onRestart: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.survive_30s_win),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF66BB6A),
            )
            Text(
                text = stringResource(R.string.survive_30s_win_desc),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.survive_30s_near_miss_count, nearMissCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            GlassButton(
                onClick = onRestart,
                color = Color(0xFF66BB6A).copy(alpha = 0.15f),
                contentColor = Color(0xFF66BB6A),
                borderWidth = 0.dp,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.survive_30s_retry),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}


