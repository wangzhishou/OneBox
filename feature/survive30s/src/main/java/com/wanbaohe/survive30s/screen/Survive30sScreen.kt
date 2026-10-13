package com.wanbaohe.survive30s.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Locale
import com.shifenmiao.common.ui.BaseScreen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassLinearProgressIndicator
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassMedium
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin
import com.wanbaohe.survive30s.R
import com.wanbaohe.survive30s.component.GameState
import com.wanbaohe.survive30s.component.Survive30sComponent
import com.wanbaohe.survive30s.component.SurvivalPhase
import com.wanbaohe.survive30s.component.Survive30sUiState
import com.wanbaohe.survive30s.engine.Survive30sEngine
// 下面两个图标导入不能删：Icons.Rounded.ArrowBack / Icons.Outlined.Refresh
// 是这两个文件提供的扩展属性，删掉后全限定名调用会解析失败
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Refresh

/**
 * 躲避30秒游戏主页面
 *
 * 按 [BaseScreen] 的三层结构组织，整屏都是游戏场地，不再有独立的画布卡片：
 * - background → [GameStage]：画布铺满整屏（含状态栏 / 导航栏区域），并在整页接收拖拽手势
 * - 标题栏      → [GameTitle]：只显示标题，返回键由 [BaseScreen] 统一提供
 * - content    → [GameStatusPanel]：贴在标题栏下方，倒计时进度条 + 状态（阶段 / 护盾 /
 *                充能 / 擦身 / 危险度）合在一张面板里，仅游戏进行中显示
 * - foreground → 开始 / 失败 / 胜利覆盖层，从标题栏下方开始铺，不遮挡返回键
 *
 * 把状态收进顶部面板，是为了把屏幕下半部整块让给操作区——手指在下方拖动时
 * 既不会挡住数字，也不会误碰到任何控件。
 *
 * 玩家：带光晕的圆形；障碍物：带渐变色的圆形。
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

    // 拖拽中的实时坐标，避免 state 滞后导致的手感迟钝
    var dragTargetX by remember { mutableStateOf(Float.NaN) }
    var dragTargetY by remember { mutableStateOf(Float.NaN) }

    // 退后台自动暂停：游戏主循环跑在 componentScope，不感知 Activity 生命周期，
    // 不拦的话后台计时照走，回来直接判胜（还能白嫖通关积分）。
    // 监听 ON_PAUSE 而不是 ON_STOP：onPause→onStop 之间有几十毫秒窗口，
    // 这期间主循环还会跑几十帧，玩家没操作就撞死了。游戏类 App 在 onPause 就暂停是惯例。
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) component.pauseGame()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BaseScreen(
        // ── 标题栏：只放标题，倒计时和状态都下沉到 content 面板 ────────
        title = {
            GameTitle()
        },
        onGoBack = component.onGoBack,
        // 标题栏保持透明，让底下的游戏画面透出来
        colors = topAppBarColors().copy(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        supportGlassEffect = true,
        isShowDefaultActions = true,
        // ── background：游戏画布铺满整屏，整页任意位置都能拖动小球 ────
        backgroundImage = {
            GameStage(
                state = state,
                onCanvasSizeChanged = { w, h -> component.updateCanvasSize(w, h) },
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                dragTargetX = offset.x
                                dragTargetY = offset.y
                                component.movePlayerTo(offset.x, offset.y)
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
                            val newX =
                                (dragTargetX.takeIf { !it.isNaN() } ?: state.player.x) + dragAmount.x
                            val newY =
                                (dragTargetY.takeIf { !it.isNaN() } ?: state.player.y) + dragAmount.y
                            dragTargetX = newX
                            dragTargetY = newY
                            component.movePlayerTo(newX, newY)
                        }
                    },
            )
        },
        // ── content：紧贴标题栏下方的状态面板，仅游戏进行中显示 ────────
        content = {
            if (state.gameState == GameState.PLAYING) {
                GameStatusPanel(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
        },
        // ── foreground：开始 / 暂停 / 失败 / 胜利覆盖层 ───────────────
        foreground = {
            when (state.gameState) {
                GameState.IDLE -> IdleOverlay(onStart = component::startGame)
                GameState.PAUSED -> PausedOverlay(
                    elapsed = state.elapsedSec,
                    onResume = component::resumeGame,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )
                GameState.GAME_OVER -> GameOverOverlay(
                    elapsed = state.elapsedSec,
                    bestTime = state.bestTime,
                    nearMissCount = state.nearMissCount,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )

                GameState.WIN -> WinOverlay(
                    nearMissCount = state.nearMissCount,
                    onRestart = component::startGame,
                    onBack = component.onGoBack,
                )

                GameState.PLAYING -> Unit
            }
        }
    )
}

// ─── 标题栏 ──────────────────────────────────────────────────────────────────

/**
 * 标题栏内容：只显示游戏名。
 *
 * 倒计时和状态全部下沉到 [GameStatusPanel]，标题栏保持清爽，返回键由 [BaseScreen]
 * 统一提供——这样任何游戏状态下返回键都在同一位置、都能点。
 */
@Composable
private fun GameTitle() {
    Text(
        text = stringResource(R.string.survive_30s_title),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

// ─── 顶部状态面板（标题栏下方） ────────────────────────────────────────────────

/**
 * 游戏进行中的状态面板：剩余秒数 + 倒计时进度条 + 各项状态，收在一张玻璃卡片里。
 *
 * 挂在 content 层、紧贴标题栏下沿，所以：
 * - 屏幕下半部整块留给操作，手指拖动时不会挡住数字
 * - 玻璃底透出底下的游戏画面，跟随系统主题色自动适配
 */
@Composable
private fun GameStatusPanel(
    state: Survive30sUiState,
    modifier: Modifier = Modifier,
) {
    val progress = (state.elapsedSec / Survive30sEngine.GAME_DURATION).coerceIn(0f, 1f)
    val remaining = (Survive30sEngine.GAME_DURATION - state.elapsedSec).coerceAtLeast(0f)
    val timerColor = timerColorFor(progress)

    Column(
        modifier = modifier
            .glassMedium(shape = RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = String.format(Locale.getDefault(), "%.1f", remaining) + "s",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = timerColor,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = when {
                    state.dangerLevel > 0.8f -> stringResource(R.string.survive_30s_danger_high)
                    state.dangerLevel > 0.45f -> stringResource(R.string.survive_30s_danger_medium)
                    else -> stringResource(R.string.survive_30s_danger_low)
                },
                style = MaterialTheme.typography.labelSmall,
                color = dangerColorFor(state.dangerLevel),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (state.bestTime > 0f) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.survive_30s_best_time, state.bestTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }

        TimerProgressBar(
            progress = progress,
            color = timerColor,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusPill(
                title = stringResource(R.string.survive_30s_phase),
                value = when (state.phase) {
                    SurvivalPhase.Warmup -> stringResource(R.string.survive_30s_phase_warmup)
                    SurvivalPhase.Rush -> stringResource(R.string.survive_30s_phase_rush)
                    SurvivalPhase.Storm -> stringResource(R.string.survive_30s_phase_storm)
                },
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                title = stringResource(R.string.survive_30s_shield),
                value = state.shieldCount.toString(),
                modifier = Modifier.weight(1f),
            )
            StatusPill(
                title = stringResource(R.string.survive_30s_charge),
                value = stringResource(R.string.survive_30s_charge_value, state.nearMissCharge, 3),
                modifier = Modifier.weight(1.15f),
            )
            StatusPill(
                title = stringResource(R.string.survive_30s_near_miss),
                value = state.nearMissCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * 危险度文字颜色：安全/警戒/危险三档，全部取自主题色（onPrimaryContainer/onTertiaryContainer/error），
 * 主题切换自动适配，不锁死具体色值。
 */
@Composable
private fun dangerColorFor(dangerLevel: Float): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when {
        dangerLevel > 0.8f -> colorScheme.onErrorContainer
        dangerLevel > 0.45f -> colorScheme.onTertiaryContainer
        else -> colorScheme.onPrimaryContainer
    }
}

/** 倒计时进度条：玻璃质感，从满到空，颜色 onPrimaryContainer（安全）→ onTertiaryContainer（警戒）→ error（危险） */
@Composable
private fun TimerProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100),
        label = "timer_progress"
    )

    GlassLinearProgressIndicator(
        progress = { 1f - animatedProgress },
        modifier = modifier.height(8.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f),
    )
}

/** 倒计时颜色三档语义色，取自当前主题，主题切换自动适配 */
@Composable
private fun timerColorFor(progress: Float): Color {
    val colorScheme = MaterialTheme.colorScheme
    return when {
        progress < 0.33f -> colorScheme.onPrimaryContainer
        progress < 0.66f -> colorScheme.onTertiaryContainer
        else -> colorScheme.onErrorContainer
    }
}

// ─── 状态胶囊 ────────────────────────────────────────────────────────────────

@Composable
private fun StatusPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .glassThin(shape = RoundedCornerShape(12.dp))
            .padding(vertical = 6.dp),
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

// ─── 游戏舞台（background 层） ────────────────────────────────────────────────

/** 标题栏默认容器高度，覆盖层从这个高度往下铺，避免盖住标题栏和返回键 */
private val TOP_BAR_RESERVED_HEIGHT = 64.dp

/** 顶部渐变遮罩高度，只压住标题栏附近，不影响下方障碍物 */
private val TOP_SCRIM_HEIGHT = 104.dp

/**
 * 游戏舞台：铺满整屏的背景层。
 *
 * 拖拽手势挂在最外层 Box 上（由调用方传入），所以手指落在屏幕任意位置——
 * 包括标题栏两侧、状态条上方——都能拉动小球；按钮会自己消费点击，不会误触发。
 */
@Composable
private fun GameStage(
    state: Survive30sUiState,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrimColor = MaterialTheme.colorScheme.surface

    // 不铺实色底：全局的 MeshGradientBackground(自定义背景图 / mesh 渐变 / surface 色)
    // 在 BaseScreen 底下本来就有，这里再画一层不透明 background 色会把它整个盖掉，
    // 导致用户设置了背景图或渐变主题时游戏页完全不生效。画布直接透明叠在上面。
    Box(modifier = modifier) {
        GameCanvas(
            state = state,
            onCanvasSizeChanged = onCanvasSizeChanged,
            modifier = Modifier.fillMaxSize(),
        )
        // 顶部渐变遮罩：保证标题栏上的倒计时在杂乱画面上依然可读。
        // 只在游戏进行时铺——准备好了页面不压背景，暂停/结算页有自己的整屏遮罩。
        // 这里只有 background，没有 pointerInput，不拦截任何触摸。
        if (state.gameState == GameState.PLAYING) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(TOP_SCRIM_HEIGHT)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                scrimColor.copy(alpha = 0.7f),
                                scrimColor.copy(alpha = 0.45f),
                                Color.Transparent,
                            )
                        )
                    ),
            )
        }
    }
}

// ─── 游戏画布 ──────────────────────────────────────────────────────────────────

@Composable
private fun GameCanvas(
    state: Survive30sUiState,
    onCanvasSizeChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerPalette = rememberPlayerPalette()
    val dangerColor = MaterialTheme.colorScheme.onErrorContainer

    Canvas(
        modifier = modifier
            .onSizeChanged { size ->
                onCanvasSizeChanged(size.width.toFloat(), size.height.toFloat())
            }
    ) {
        val player = state.player

        // ── 绘制背景网格（轻微视觉参考线） ──
        drawGrid(size)
        drawDangerOverlay(
            size = size,
            dangerLevel = state.dangerLevel,
            color = dangerColor,
        )

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
        colorScheme.onPrimaryContainer,
        colorScheme.onSecondaryContainer,
        colorScheme.onTertiaryContainer,
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
            colorScheme.onPrimaryContainer,
            colorScheme.onSecondaryContainer,
            colorScheme.onTertiaryContainer,
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
                colorScheme.onPrimaryContainer,
                if (isDarkTheme) 0.36f else 0.24f,
            ).copy(alpha = 0.82f),
            shadowColor = blendColors(
                colorScheme.surface,
                colorScheme.onSurface,
                if (isDarkTheme) 0.18f else 0.10f,
            ).copy(alpha = if (isDarkTheme) 0.24f else 0.16f),
            invincibleColors = listOf(
                blendColors(colorScheme.inversePrimary, colorScheme.onSecondaryContainer, 0.35f),
                blendColors(colorScheme.onTertiaryContainer, colorScheme.primaryContainer, 0.42f),
                blendColors(colorScheme.secondaryContainer, colorScheme.onPrimaryContainer, 0.36f),
                blendColors(colorScheme.inversePrimary, colorScheme.onSecondaryContainer, 0.35f),
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
    color: Color,
) {
    if (dangerLevel <= 0f) return

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                color.copy(alpha = 0.08f * dangerLevel),
            )
        ),
        size = size,
    )
}

// ─── 覆盖层组件 ────────────────────────────────────────────────────────────────

/**
 * 覆盖层通用布局：只铺到标题栏下方（状态栏 + [TOP_BAR_RESERVED_HEIGHT]），
 * 这样标题栏和返回键始终露在外面、随时可点。
 *
 * 不铺任何底色，全局 MeshGradientBackground 直接透出来。
 */
@Composable
private fun Modifier.overlayScrim(): Modifier = this
    .fillMaxSize()
    .statusBarsPadding()
    .padding(top = TOP_BAR_RESERVED_HEIGHT)

/** 等待开始覆盖层（点击任意处即可开始）；不铺底色，透出全局背景 */
@Composable
private fun IdleOverlay(onStart: () -> Unit) {
    Box(
        modifier = Modifier
            .overlayScrim()
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
                color = MaterialTheme.colorScheme.onPrimaryContainer,
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

/** 暂停覆盖层（退后台自动触发）：继续 / 再来一次 / 返回 */
@Composable
private fun PausedOverlay(
    elapsed: Float,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.overlayScrim(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.survive_30s_paused),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.survive_30s_survived_time, elapsed),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.survive_30s_pause_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            GlassButton(
                onClick = onResume,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                borderWidth = 0.dp,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
            ) {
                Text(
                    text = stringResource(R.string.survive_30s_resume),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            ResultActions(
                onBack = onBack,
                onRestart = onRestart,
                restartContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                restartContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** 结算按钮行：返回 + 再来一次 */
@Composable
private fun ResultActions(
    onBack: () -> Unit,
    onRestart: () -> Unit,
    restartContainerColor: Color,
    restartContentColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(0.86f),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GlassButton(
            onClick = onBack,
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            borderWidth = 0.dp,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.survive_30s_back),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
        GlassButton(
            onClick = onRestart,
            color = restartContainerColor,
            contentColor = restartContentColor,
            borderWidth = 0.dp,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.survive_30s_retry),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
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
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.overlayScrim(),
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
                color = MaterialTheme.colorScheme.onErrorContainer,
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
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
            Text(
                text = stringResource(R.string.survive_30s_near_miss_count, nearMissCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            ResultActions(
                onBack = onBack,
                onRestart = onRestart,
                restartContainerColor = MaterialTheme.colorScheme.errorContainer,
                restartContentColor = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

/** 胜利覆盖层 */
@Composable
private fun WinOverlay(
    nearMissCount: Int,
    onRestart: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.overlayScrim(),
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
                color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            ResultActions(
                onBack = onBack,
                onRestart = onRestart,
                restartContainerColor = MaterialTheme.colorScheme.primaryContainer,
                restartContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
