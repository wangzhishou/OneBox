package com.wanbaohe.deadpixeltest.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridOff
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.deadpixeltest.R
import com.wanbaohe.deadpixeltest.component.DeadPixelTestComponent
import com.wanbaohe.deadpixeltest.ui.ColorDotRow
import com.wanbaohe.deadpixeltest.ui.ColorPanel
import com.wanbaohe.deadpixeltest.ui.GridOverlay
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.PlayCircle
import com.t8rin.imagetoolbox.core.resources.icons.line.LineGridOn
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCompress

/**
 * 屏幕坏点检测主入口
 *
 * 布局策略：
 *  - 正常模式：BaseScreen 提供 TopAppBar + 内容区
 *  - 全屏模式：隐藏 TopAppBar，全屏显示测试色板，叠加悬浮控制条
 *
 * 交互设计：
 *  - 点击 / 左滑 → 下一色；右滑 → 上一色
 *  - 底部控制栏：颜色圆点选择器 + 当前进度文字
 *  - TopAppBar 操作：网格开关、自动播放开关、全屏开关
 */
@Composable
fun DeadPixelTestScreen(component: DeadPixelTestComponent) {
    val state by component.uiState.collectAsState()

    // ── 使用说明对话框（首次进入自动弹出） ──────────────────────────────────
    if (state.showGuide) {
        GuideDialog(onDismiss = component::dismissGuide)
    }

    if (state.isFullScreen) {
        // ── 全屏模式：无 AppBar，直接铺满 ──────────────────────────────────
        FullScreenTestView(
            state = state,
            onTap = component::nextColor,
            onSwipeLeft = component::nextColor,
            onSwipeRight = component::prevColor,
            onSelectColor = component::selectColor,
            onToggleGrid = component::toggleGrid,
            onToggleAutoPlay = component::toggleAutoPlay,
            onExitFullScreen = component::toggleFullScreen,
        )
    } else {
        // ── 常规模式：BaseScreen 壳 ─────────────────────────────────────────
        BaseScreen(
            title = stringResource(R.string.dead_pixel_test),
            onGoBack = component.onGoBack,
            actions = {
                // 网格开关
                IconButton(
                    onClick = component::toggleGrid,
                    colors = AppTheme.colors.iconButtonColors()
                ) {
                    Icon(
                        imageVector = if (state.showGrid) Icons.Outlined.GridOff else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGridOn,
                        contentDescription = stringResource(
                            if (state.showGrid) R.string.dead_pixel_grid_off else R.string.dead_pixel_grid_on
                        )
                    )
                }
                // 自动轮播开关
                IconButton(
                    onClick = component::toggleAutoPlay,
                    colors = AppTheme.colors.iconButtonColors()
                ) {
                    Icon(
                        imageVector = if (state.isAutoPlaying) Icons.Outlined.Stop else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                        contentDescription = stringResource(
                            if (state.isAutoPlaying) R.string.dead_pixel_auto_stop else R.string.dead_pixel_auto_play
                        )
                    )
                }
                // 全屏开关
                IconButton(
                    onClick = component::toggleFullScreen,
                    colors = AppTheme.colors.iconButtonColors()
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen,
                        contentDescription = stringResource(R.string.dead_pixel_fullscreen)
                    )
                }
            }
        ) {
            TestContent(
                state = state,
                onTap = component::nextColor,
                onSwipeLeft = component::nextColor,
                onSwipeRight = component::prevColor,
                onSelectColor = component::selectColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// 复用子组件
// ──────────────────────────────────────────────────────────────────────────────

/**
 * 测试内容区：纯色面板 + 网格叠加 + 底部控制栏
 */
@Composable
private fun TestContent(
    state: com.wanbaohe.deadpixeltest.component.DeadPixelUiState,
    onTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // 颜色面板（切换时 AnimatedContent 过渡）
        AnimatedContent(
            targetState = state.currentTestColor,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "colorTransition",
            modifier = Modifier.fillMaxSize()
        ) { testColor ->
            ColorPanel(
                color = testColor.color,
                onTap = onTap,
                onSwipeLeft = onSwipeLeft,
                onSwipeRight = onSwipeRight,
            )
        }

        // 网格叠加层
        AnimatedVisibility(
            visible = state.showGrid,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            GridOverlay(baseColor = state.currentTestColor.color)
        }

        // 底部控制栏（半透明悬浮）
        BottomControlBar(
            state = state,
            onSelectColor = onSelectColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * 底部控制栏：颜色圆点 + 进度提示
 */
@Composable
private fun BottomControlBar(
    state: com.wanbaohe.deadpixeltest.component.DeadPixelUiState,
    onSelectColor: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ColorDotRow(
            currentIndex = state.colorIndex,
            onSelect = onSelectColor,
        )
        Text(
            text = stringResource(
                R.string.dead_pixel_progress,
                state.colorIndex + 1,
                state.totalColors
            ),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.dead_pixel_hint),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 全屏测试视图（隐藏 AppBar，铺满全屏）
 * 叠加一个最小化悬浮控制栏供退出和切换操作。
 */
@Composable
private fun FullScreenTestView(
    state: com.wanbaohe.deadpixeltest.component.DeadPixelUiState,
    onTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSelectColor: (Int) -> Unit,
    onToggleGrid: () -> Unit,
    onToggleAutoPlay: () -> Unit,
    onExitFullScreen: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        TestContent(
            state = state,
            onTap = onTap,
            onSwipeLeft = onSwipeLeft,
            onSwipeRight = onSwipeRight,
            onSelectColor = onSelectColor,
            modifier = Modifier.fillMaxSize()
        )

        // 全屏模式悬浮操作行（右上角）
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(onClick = onToggleGrid, colors = AppTheme.colors.iconButtonColors()) {
                Icon(
                    imageVector = if (state.showGrid) Icons.Outlined.GridOff else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineGridOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(onClick = onToggleAutoPlay, colors = AppTheme.colors.iconButtonColors()) {
                Icon(
                    imageVector = if (state.isAutoPlaying) Icons.Outlined.Stop else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.PlayCircle,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
            IconButton(onClick = onExitFullScreen, colors = AppTheme.colors.iconButtonColors()) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCompress,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 首次使用说明对话框
 */
@Composable
private fun GuideDialog(onDismiss: () -> Unit) {
    EnhancedAlertDialog(
        visible = true,
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.dead_pixel_guide_title))
        },
        text = {
            Text(
                text = stringResource(R.string.dead_pixel_guide_content),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dead_pixel_guide_start))
            }
        }
    )
}

