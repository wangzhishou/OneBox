package com.wanbaohe.teleprompter.screen

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.common.ui.GuideLine
import com.shifenmiao.common.ui.PlayerControlPanel
import com.shifenmiao.common.ui.PlayerControlState
import com.wanbaohe.teleprompter.R
import com.wanbaohe.teleprompter.component.TeleprompterComponent
import com.wanbaohe.teleprompter.component.TeleprompterPlayerUiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * 核心提词播放页 (Teleprompter Player)
 *
 * 全屏纯黑背景 + 自动滚动大号文字 + 导视线 + 底部控制面板
 */
@Composable
fun TeleprompterPlayerScreen(
    component: TeleprompterComponent,
) {
    val state by component.playerState.collectAsState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // ── 进度条拖拽状态 ───────────────────────────────────────────────────
    var isDraggingProgress by remember { mutableStateOf(false) }

    // ── 操作提示（进入播放后短暂展示） ────────────────────────────────────
    var showTapHint by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showTapHint = true
        delay(3000L.milliseconds)
        showTapHint = false
    }

    // ── 屏幕常亮 ─────────────────────────────────────────────────────────
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // ── 平滑自动滚动引擎（帧级别精度） ────────────────────────────────
    LaunchedEffect(state.isPlaying, state.scrollSpeed, isDraggingProgress) {
        if (!state.isPlaying || isDraggingProgress) return@LaunchedEffect

        // 引擎启动前先同步滚动位置（解决「从头播放」时 scrollState 还在末尾的问题）
        val initMax = scrollState.maxValue
        if (initMax > 0) {
            val syncTarget = (state.scrollProgress * initMax).toInt()
            if (kotlin.math.abs(scrollState.value - syncTarget) > 2) {
                scrollState.scrollTo(syncTarget)
            }
        }

        val pixelsPerFrame = state.scrollSpeed * 1.2f // 基准：speed=3 约 3.6px/frame
        while (isActive) {
            val maxScroll = scrollState.maxValue
            // 用户正在手动滑动文字时，让出控制，等手势结束再继续
            if (maxScroll <= 0 || scrollState.isScrollInProgress) {
                delay(16L.milliseconds)
                continue
            }
            val target = (scrollState.value + pixelsPerFrame).toInt()
                .coerceAtMost(maxScroll)
            try {
                scrollState.scrollTo(target)
            } catch (e: CancellationException) {
                // 区分：协程被取消 vs 用户手势打断滚动
                if (!isActive) throw e   // 协程取消，正常传播
                delay(50L.milliseconds)               // 手势打断，等一等继续
                continue
            }
            // 同步进度
            component.onScrollProgressChange(target.toFloat() / maxScroll)
            // 到底后自动暂停
            if (target >= maxScroll) {
                component.onSetPlaying(false)
                break
            }
            delay(16L.milliseconds) // ≈60fps
        }
    }

    // ── 外部拖拽进度条后同步 scrollState ──────────────────────────────
    LaunchedEffect(state.scrollProgress) {
        val maxScroll = scrollState.maxValue
        if (maxScroll > 0) {
            val target = (state.scrollProgress * maxScroll).toInt()
            if (kotlin.math.abs(scrollState.value - target) > 10) {
                scrollState.scrollTo(target)
            }
        }
    }

    BackHandler { component.onPlayerBack() }

    Box(
        modifier = Modifier
            .background(Color.Black)
            .systemBarsPadding()
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                component.onToggleControls()
            }
    ) {
        // ── 滚动文字 ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (state.isMirrorMode) {
                        scaleX = -1f
                    }
                }
        ) {
            Text(
                text = state.content,
                color = Color.White,
                fontSize = state.fontSize.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = (state.fontSize * 1.5f).sp,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 200.dp)
            )
        }

        // ── 导视线 ───────────────────────────────────────────────────
        GuideLine(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 180.dp),
            color = MaterialTheme.colorScheme.primary,
        )

        // ── 操作提示（进入播放后短暂展示，点击任意处呼出控制面板） ───────
        AnimatedVisibility(
            visible = showTapHint && !state.showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 96.dp)
        ) {
            Text(
                text = stringResource(R.string.teleprompter_tap_to_control),
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        // ── 控制面板（底部滑入）—— 使用共用组件 ─────────────────────
        AnimatedVisibility(
            visible = state.showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            PlayerControlPanel(
                state = PlayerControlState(
                    isPlaying = state.isPlaying,
                    progress = state.scrollProgress,
                    fontSize = state.fontSize,
                    minFontSize = TeleprompterPlayerUiState.MIN_FONT_SIZE,
                    maxFontSize = TeleprompterPlayerUiState.MAX_FONT_SIZE,
                    speed = state.scrollSpeed,
                    minSpeed = TeleprompterPlayerUiState.MIN_SPEED,
                    maxSpeed = TeleprompterPlayerUiState.MAX_SPEED,
                    isMirrorMode = state.isMirrorMode,
                ),
                onTogglePlay = { component.onTogglePlay() },
                onFontSizeChange = { component.onFontSizeChange(it) },
                onSpeedChange = { component.onSpeedChange(it) },
                onToggleMirror = { component.onToggleMirror() },
                onReset = { component.onResetPlayer() },
                onExit = { component.onExitPlayer() },
                onProgressChange = { progress ->
                    isDraggingProgress = true
                    component.onScrollProgressChange(progress)
                    // 立即同步滚动位置
                    scope.launch {
                        val maxScroll = scrollState.maxValue
                        if (maxScroll > 0) {
                            scrollState.scrollTo((progress * maxScroll).toInt())
                        }
                    }
                },
                onProgressChangeFinished = {
                    isDraggingProgress = false
                },
                modifier = Modifier
                    .systemBarsPadding()
            )
        }
    }
}
