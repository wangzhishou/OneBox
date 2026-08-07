package com.shifenmiao.common.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/**
 * 沉浸式模式状态管理
 * 用于控制 UI 元素的显示/隐藏，实现全屏预览效果
 *
 * 设计原则：
 * 1. 高性能：使用 State 驱动，避免不必要的重组
 * 2. 可扩展：支持自定义动画时长和方向
 * 3. 可复用：独立的状态管理，可在任何页面使用
 */
@Stable
class ImmersiveModeState(
    initialValue: Boolean = false
) {
    /**
     * 是否处于沉浸式模式（UI 隐藏状态）
     */
    var isImmersive by mutableStateOf(initialValue)
        private set

    /**
     * UI 是否可见（与 isImmersive 相反）
     */
    val isUiVisible: Boolean
        get() = !isImmersive

    /**
     * 切换沉浸式模式
     */
    fun toggle() {
        isImmersive = !isImmersive
    }

    /**
     * 进入沉浸式模式（隐藏 UI）
     */
    fun enterImmersive() {
        isImmersive = true
    }

    /**
     * 退出沉浸式模式（显示 UI）
     */
    fun exitImmersive() {
        isImmersive = false
    }

    companion object {
        /**
         * 用于 rememberSaveable 的 Saver
         */
        val Saver: Saver<ImmersiveModeState, Boolean> = Saver(
            save = { it.isImmersive },
            restore = { ImmersiveModeState(it) }
        )
    }
}

/**
 * 记住并保存沉浸式模式状态
 * @param initialValue 初始状态，默认为非沉浸式（UI 可见）
 */
@Composable
fun rememberImmersiveModeState(
    initialValue: Boolean = false
): ImmersiveModeState {
    return rememberSaveable(saver = ImmersiveModeState.Saver) {
        ImmersiveModeState(initialValue)
    }
}

/**
 * 沉浸式动画配置
 */
object ImmersiveAnimationDefaults {
    const val ANIMATION_DURATION_MS = 300
}

/**
 * 带沉浸式动画的顶部内容容器
 * 从顶部滑入/滑出
 *
 * 注意：使用 AnimatedVisibility 而不是 graphicsLayer，
 * 因为 graphicsLayer 只是视觉上隐藏，不会停止子组件的事件处理
 */
@Composable
inline fun ImmersiveTopContent(
    visible: Boolean,
    animationDurationMs: Int = ImmersiveAnimationDefaults.ANIMATION_DURATION_MS,
    crossinline content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(animationDurationMs)
        ) + fadeIn(animationSpec = tween(animationDurationMs)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(animationDurationMs)
        ) + fadeOut(animationSpec = tween(animationDurationMs))
    ) {
        content()
    }
}

/**
 * 带沉浸式动画的底部内容容器
 * 从底部滑入/滑出
 *
 * 使用 graphicsLayer 实现动画，保持组件活跃状态
 * 避免 Coil AsyncImage 在退出动画期间的 bitmap recycle 崩溃
 */
@Composable
fun ImmersiveBottomContent(
    visible: Boolean,
    animationDurationMs: Int = ImmersiveAnimationDefaults.ANIMATION_DURATION_MS,
    content: @Composable () -> Unit
) {
    // 跟踪组件是否应该被渲染
    // 当 visible 变为 true 时立即显示，变为 false 时延迟移除
    var shouldRender by rememberSaveable { mutableStateOf(visible) }

    // 当 visible 变化时更新 shouldRender
    LaunchedEffect(visible) {
        if (visible) {
            // 立即显示
            shouldRender = true
        } else {
            // 延迟移除，等待动画完成
            delay(animationDurationMs.toLong() + 50) // 额外 50ms 缓冲
            shouldRender = false
        }
    }

    val alphaValue by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(animationDurationMs),
        label = "immersiveAlpha"
    )

    val translationYFraction by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(animationDurationMs),
        label = "immersiveTranslationY"
    )

    // 只在需要渲染时渲染组件
    if (shouldRender) {
        Box(
            modifier = Modifier.graphicsLayer {
                alpha = alphaValue
                // 向下滑出：translationYFraction = 0 表示在原位，1 表示完全滑出
                translationY = size.height * translationYFraction
            }
        ) {
            content()
        }
    }
}

/**
 * 带沉浸式动画的淡入淡出内容容器
 */
@Composable
fun ImmersiveFadeContent(
    visible: Boolean,
    animationDurationMs: Int = ImmersiveAnimationDefaults.ANIMATION_DURATION_MS,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(animationDurationMs)),
        exit = fadeOut(animationSpec = tween(animationDurationMs))
    ) {
        content()
    }
}

