package com.shifenmiao.common.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture

/**
 * 可缩放的图片预览组件
 *
 * ## 模块位置
 * - **模块**: `feature/common`
 * - **包路径**: `com.shifenmiao.common.ui`
 * - **文件**: `ZoomableImagePreview.kt`
 *
 * ## 功能概述
 * 提供类似手机相册的图片查看体验：
 * - 双指缩放：支持双指捏合缩放
 * - 双击缩放：双击在 1x 和 2.5x 之间切换
 * - 拖动平移：缩放后可拖动查看图片各部分
 * - 边界限制：防止图片拖出可视区域
 * - 动画效果：所有操作带有流畅的动画
 * - 沉浸式模式：支持适应屏幕宽度
 * - 内边距支持：避免被顶部/底部栏遮挡
 *
 * ## 使用示例
 * ```kotlin
 * ZoomableImagePreview(
 *     imageBitmap = previewBitmap,
 *     isLoading = isImageLoading,
 *     isImmersive = immersiveModeState.isImmersive,
 *     contentPadding = PaddingValues(top = 64.dp, bottom = 200.dp),
 *     onTap = { /* 单击回调 */ },
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param imageBitmap 要显示的图片
 * @param isLoading 是否正在加载
 * @param isImmersive 是否处于沉浸式模式（沉浸式模式下适应屏幕宽度，无内边距）
 * @param contentPadding 内边距（避免被顶部/底部栏遮挡，沉浸式模式下忽略）
 * @param onTap 单击回调（用于切换沉浸式模式等）
 * @param minScale 最小缩放倍数，默认 1f
 * @param maxScale 最大缩放倍数，默认 5f
 * @param doubleTapScale 双击时的缩放倍数，默认 2.5f
 * @param backgroundColor 背景颜色
 * @param contentScale 内容缩放模式（非沉浸式模式）
 * @param immersiveContentScale 沉浸式模式下的缩放模式
 */
@Composable
fun ZoomableImagePreview(
    imageBitmap: ImageBitmap?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    isImmersive: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(vertical = 40.dp),
    onTap: (() -> Unit)? = null,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    doubleTapScale: Float = 2.5f,
    backgroundColor: Color = Color.Transparent,
    contentScale: ContentScale = ContentScale.Fit,
    immersiveContentScale: ContentScale = ContentScale.FillWidth
) {
    // 缩放和偏移状态
    var targetScale by remember { mutableFloatStateOf(minScale) }
    var targetOffsetX by remember { mutableFloatStateOf(0f) }
    var targetOffsetY by remember { mutableFloatStateOf(0f) }

    // 容器尺寸
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // 动画
    val animationSpec = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = animationSpec,
        label = "scale"
    )

    val offsetX by animateFloatAsState(
        targetValue = targetOffsetX,
        animationSpec = animationSpec,
        label = "offsetX"
    )

    val offsetY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = animationSpec,
        label = "offsetY"
    )

    // 计算边界限制
    fun calculateBounds(): Pair<Float, Float> {
        if (containerSize == IntSize.Zero) return 0f to 0f
        val maxOffsetX = ((scale - 1) * containerSize.width / 2).coerceAtLeast(0f)
        val maxOffsetY = ((scale - 1) * containerSize.height / 2).coerceAtLeast(0f)
        return maxOffsetX to maxOffsetY
    }

    // 限制偏移在边界内
    fun constrainOffset(x: Float, y: Float): Pair<Float, Float> {
        val (maxX, maxY) = calculateBounds()
        return x.coerceIn(-maxX, maxX) to y.coerceIn(-maxY, maxY)
    }

    // 重置缩放和偏移
    fun resetZoom() {
        targetScale = minScale
        targetOffsetX = 0f
        targetOffsetY = 0f
    }

    // 双击缩放
    fun onDoubleTap(position: Offset) {
        if (targetScale > minScale) {
            // 已缩放，恢复原始大小
            resetZoom()
        } else {
            // 缩放到目标倍数
            targetScale = doubleTapScale
            // 以点击位置为中心缩放
            val centerX = containerSize.width / 2f
            val centerY = containerSize.height / 2f
            val newOffsetX = (centerX - position.x) * (doubleTapScale - 1)
            val newOffsetY = (centerY - position.y) * (doubleTapScale - 1)
            val (constrainedX, constrainedY) = constrainOffset(newOffsetX, newOffsetY)
            targetOffsetX = constrainedX
            targetOffsetY = constrainedY
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .onSizeChanged { containerSize = it }
            // 使用 detectTapGestures 处理单击和双击，避免冲突
            .pointerInput(imageBitmap) {
                if (imageBitmap == null) return@pointerInput
                detectTapGestures(
                    onTap = { onTap?.invoke() },
                    onDoubleTap = { position -> onDoubleTap(position) }
                )
            }
            // 单独处理缩放和拖动手势
            .pointerInput(imageBitmap) {
                if (imageBitmap == null) return@pointerInput

                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)

                    var zoom = 1f
                    var pan = Offset.Zero
                    var pastTouchSlop = false
                    val touchSlop = viewConfiguration.touchSlop

                    do {
                        val event = awaitPointerEvent()

                        if (event.type == PointerEventType.Move) {
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            if (!pastTouchSlop) {
                                zoom *= zoomChange
                                pan += panChange

                                val centroidSize = event.calculateCentroid(useCurrent = false)
                                val zoomMotion = kotlin.math.abs(1 - zoom) * centroidSize.getDistance()
                                val panMotion = pan.getDistance()

                                if (zoomMotion > touchSlop || panMotion > touchSlop) {
                                    pastTouchSlop = true
                                }
                            }

                            if (pastTouchSlop) {
                                if (event.changes.size > 1) {
                                    // 双指缩放
                                    val newScale = (targetScale * zoomChange).coerceIn(minScale, maxScale)
                                    targetScale = newScale

                                    // 更新偏移（跟随缩放中心）
                                    val newOffsetX = targetOffsetX + panChange.x * targetScale
                                    val newOffsetY = targetOffsetY + panChange.y * targetScale
                                    val (constrainedX, constrainedY) = constrainOffset(newOffsetX, newOffsetY)
                                    targetOffsetX = constrainedX
                                    targetOffsetY = constrainedY

                                    event.changes.forEach { it.consume() }
                                } else if (targetScale > minScale) {
                                    // 单指拖动（仅在缩放状态下）
                                    val newOffsetX = targetOffsetX + panChange.x * targetScale
                                    val newOffsetY = targetOffsetY + panChange.y * targetScale
                                    val (constrainedX, constrainedY) = constrainOffset(newOffsetX, newOffsetY)
                                    targetOffsetX = constrainedX
                                    targetOffsetY = constrainedY

                                    event.changes.forEach { it.consume() }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // 缩放小于最小值时恢复
                    if (targetScale < minScale) {
                        resetZoom()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // 根据沉浸式模式选择不同的 contentScale
        val currentContentScale = if (isImmersive) immersiveContentScale else contentScale

        // 内边距动画 - 沉浸式模式切换和面板展开/折叠时平滑过渡
        val paddingAnimationSpec = SpringSpec<Dp>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )

        val animatedTopPadding by animateDpAsState(
            targetValue = if (isImmersive) 0.dp else contentPadding.calculateTopPadding(),
            animationSpec = paddingAnimationSpec,
            label = "topPadding"
        )

        val animatedBottomPadding by animateDpAsState(
            targetValue = if (isImmersive) 0.dp else contentPadding.calculateBottomPadding(),
            animationSpec = paddingAnimationSpec,
            label = "bottomPadding"
        )

        val animatedStartPadding by animateDpAsState(
            targetValue = if (isImmersive) 0.dp else contentPadding.calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            animationSpec = paddingAnimationSpec,
            label = "startPadding"
        )

        val animatedEndPadding by animateDpAsState(
            targetValue = if (isImmersive) 0.dp else contentPadding.calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
            animationSpec = paddingAnimationSpec,
            label = "endPadding"
        )

        // 确保内边距非负（Spring 动画可能会产生负值的过冲）
        val animatedPadding = PaddingValues(
            start = animatedStartPadding.coerceAtLeast(0.dp),
            top = animatedTopPadding.coerceAtLeast(0.dp),
            end = animatedEndPadding.coerceAtLeast(0.dp),
            bottom = animatedBottomPadding.coerceAtLeast(0.dp)
        )

        when {
            imageBitmap != null -> {
                Picture(
                    model = imageBitmap,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(animatedPadding)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offsetX
                            translationY = offsetY
                        },
                    contentScale = currentContentScale,
                    shimmerEnabled = false,
                    showTransparencyChecker = false
                )
            }
            isLoading -> {
                Picture(
                    model = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(animatedPadding),
                    shimmerEnabled = true,
                    isLoadingFromDifferentPlace = true,
                    showTransparencyChecker = false
                )
            }
        }
    }
}

/**
 * 可缩放图片预览的状态
 * 可用于外部控制缩放状态
 */
class ZoomableImageState(
    initialScale: Float = 1f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
) {
    var scale by mutableFloatStateOf(initialScale)
        internal set
    var offsetX by mutableFloatStateOf(initialOffsetX)
        internal set
    var offsetY by mutableFloatStateOf(initialOffsetY)
        internal set

    /**
     * 重置缩放和偏移
     */
    fun reset() {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    /**
     * 是否处于缩放状态
     */
    val isZoomed: Boolean
        get() = scale > 1f
}

/**
 * 记住可缩放图片预览的状态
 */
@Composable
fun rememberZoomableImageState(
    initialScale: Float = 1f,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f
): ZoomableImageState {
    return remember {
        ZoomableImageState(initialScale, initialOffsetX, initialOffsetY)
    }
}

