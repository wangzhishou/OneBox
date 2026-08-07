package com.shifenmiao.base.ui.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import com.shifenmiao.core.ui.skeleton.shimmerLoading

object Animation {
    @Composable
    fun extendedFabCollapseAnimation() =
        fadeOut(
            animationSpec = tween(durationMillis = 300)
        ) +
                shrinkHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    shrinkTowards = Alignment.Start,
                )

    @Composable
    fun extendedFabExpandAnimation() =
        fadeIn(
            animationSpec = tween(durationMillis = 300),
        ) +
                expandHorizontally(
                    animationSpec = tween(durationMillis = 300),
                    expandFrom = Alignment.Start,
                )


    @Composable
    fun extendedFabCollapseExpandVerticallyAnimation() =
        fadeOut(
            animationSpec = tween(durationMillis = 300)
        ) +
                shrinkVertically(
                    animationSpec = tween(durationMillis = 300),
                    shrinkTowards = Alignment.Top,
                )

    @Composable
    fun extendedFabExpandExpandVerticallyAnimation() =
        fadeIn(
            animationSpec = tween(durationMillis = 300),
        ) +
                expandVertically (
                    animationSpec = tween(durationMillis = 300),
                    expandFrom = Alignment.Top,
                )

    /**
     * 带交错入场动画的通用容器
     *
     * 首次组合时自动执行 fadeIn + slideInVertically，
     * 各项按 [index] 依次递增 40 ms 延迟，营造瀑布流效果。
     * 动画期间显示 shimmer 骨架占位，动画结束后自动移除。
     *
     * @param index       列表中的序号，用于计算交错延迟（上限 360 ms）
     * @param modifier    外部传入的 Modifier（如 animateItem）
     * @param placeholder 自定义骨架占位内容；传 null 则不显示骨架
     * @param content     卡片/条目内容
     */
    @Composable
    fun StaggeredAnimatedItem(
        index: Int,
        modifier: Modifier = Modifier,
        placeholder: (@Composable BoxScope.() -> Unit)? = { CardSkeletonPlaceholder() },
        content: @Composable () -> Unit
    ) {
        val delay = (index * 60).coerceAtMost(360)
        val duration = 400
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }
        Box(modifier = modifier) {
            // 骨架占位层（底层）：动画未完成时显示 shimmer 骨架
            if (placeholder != null && !visibleState.isIdle) {
                placeholder()
            }
            // 内容层（顶层）：带 fadeIn + slideInVertically 入场动画
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(
                    animationSpec = tween(durationMillis = duration, delayMillis = delay)
                ) + slideInVertically(
                    initialOffsetY = { it / 6 },
                    animationSpec = tween(durationMillis = duration, delayMillis = delay)
                ),
                label = "staggered_card_$index"
            ) {
                content()
            }
        }
    }

    /**
     * 默认卡片骨架占位：圆角 + surfaceContainerHigh 底色 + shimmer 动画，
     * 自动填满父 Box 尺寸（由 AnimatedVisibility 内容撑起）。
     */
    @Composable
    private fun BoxScope.CardSkeletonPlaceholder() {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(0.5f))
                .shimmerLoading(true)
        )
    }
}