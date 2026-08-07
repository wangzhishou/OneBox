package com.shifenmiao.common.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.shifenmiao.common.R
import kotlin.math.abs
import kotlin.math.max
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError

/**
 * 可缩放的异步图片组件
 *
 * ## 模块位置
 * - **模块**: `feature/common`
 * - **包路径**: `com.shifenmiao.common.ui`
 * - **文件**: `ZoomableAsyncImage.kt`
 *
 * ## 功能概述
 * 提供类似手机相册的图片查看体验，支持网络图片和本地图片：
 * - 双指缩放：支持双指捏合缩放
 * - 双击缩放：双击放大/复位
 * - 拖动平移：缩放后可拖动查看图片各部分
 * - 边界限制：防止图片拖出可视区域
 * - 动画效果：所有操作带有流畅的动画
 * - 长图适配：自动识别长图并优化缩放
 * - 加载状态：支持加载中/错误状态显示
 *
 * ## 使用示例
 * ```kotlin
 * ZoomableAsyncImage(
 *     imageUrl = "https://example.com/image.jpg",
 *     onTap = { /* 单击回调 */ },
 *     onScaleChanged = { scale -> /* 缩放变化回调 */ },
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 *
 * @param imageUrl 图片 URL（支持网络和本地 URI）
 * @param onTap 单击回调
 * @param onScaleChanged 缩放变化回调
 * @param onLoadingChanged 加载状态变化回调
 * @param onErrorChanged 错误状态变化回调
 * @param initialScale 初始缩放倍数
 * @param minScale 最小缩放倍数
 * @param maxScale 最大缩放倍数
 * @param contentScale 图片内容缩放模式
 */
@Composable
fun ZoomableAsyncImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {},
    onScaleChanged: (Float) -> Unit = {},
    onLoadingChanged: (Boolean) -> Unit = {},
    onErrorChanged: (Boolean) -> Unit = {},
    initialScale: Float = 1f,
    minScale: Float = 0.5f,
    maxScale: Float = 5f,
    contentScale: ContentScale = ContentScale.FillWidth
) {
    var targetScale by remember { mutableFloatStateOf(initialScale) }
    var targetRotation by remember { mutableFloatStateOf(0f) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) }

    // 动画规格
    val animationSpec = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val offsetAnimationSpec = SpringSpec<Offset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // 动画状态
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = animationSpec,
        label = "scale"
    )
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = animationSpec,
        label = "rotation"
    )
    val offset by animateOffsetAsState(
        targetValue = targetOffset,
        animationSpec = offsetAnimationSpec,
        label = "offset"
    )

    // 跟踪是否处于缩放模式
    var isZoomModeActive by remember { mutableStateOf(false) }

    // 容器中心点
    var boxSize by remember { mutableStateOf(Offset.Zero) }

    // 容器和图片尺寸
    var containerSize by remember { mutableStateOf(IntSize(0, 0)) }
    var imageSize by remember { mutableStateOf(IntSize(0, 0)) }

    // 计算适应屏幕的最佳缩放比例
    val fitScreenScale = remember(containerSize, imageSize) {
        calculateFitScreenScale(containerSize.toSize(), imageSize.toSize())
    }

    // 是否为长图
    val isLongImage = remember(imageSize) {
        imageSize.height > 0 && imageSize.width > 0 &&
                imageSize.height.toFloat() / imageSize.width.toFloat() > 2.0f
    }

    // 初始化时应用保存的缩放值
    LaunchedEffect(imageUrl) {
        targetScale = initialScale
        onScaleChanged(initialScale)
        isZoomModeActive = initialScale > 1.01f
    }

    // 监听 scale 变化
    LaunchedEffect(scale) {
        if (scale.isFinite()) {
            onScaleChanged(scale)
        }
    }

    // 是否处于放大状态
    val isZoomed = scale > 1.01f

    Box(
        modifier = modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation,
                translationX = if (isZoomed) offset.x else 0f,
                translationY = if (isZoomed) offset.y else 0f
            )
            .onSizeChanged { size ->
                boxSize = Offset(size.width.toFloat() / 2f, size.height.toFloat() / 2f)
                containerSize = size
            }
            .pointerInput(isZoomModeActive) {
                if (isZoomModeActive) {
                    detectTransformGestures(
                        onGesture = { _, pan, zoom, rotationChange ->
                            val panSensitivity = 2f
                            val scaledPan = pan * panSensitivity

                            val newOffsetX = targetOffset.x + scaledPan.x
                            val newOffsetY = targetOffset.y + scaledPan.y

                            if (targetScale > 1f) {
                                val maxOffset = boxSize * (targetScale - 1f) * 1.5f
                                targetOffset = Offset(
                                    newOffsetX.coerceIn(-maxOffset.x, maxOffset.x),
                                    newOffsetY.coerceIn(-maxOffset.y, maxOffset.y)
                                )
                            } else {
                                targetOffset = Offset(newOffsetX, newOffsetY)
                            }

                            val prevScale = targetScale
                            val newScale = (targetScale * zoom).coerceIn(minScale, maxScale)
                            targetScale = newScale

                            if (zoom != 1f) {
                                val zoomChange = newScale / prevScale
                                targetOffset = targetOffset * zoomChange
                            }

                            targetRotation += rotationChange
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isZoomModeActive) {
                            targetScale = 1f
                            targetRotation = 0f
                            targetOffset = Offset.Zero
                            isZoomModeActive = false
                        } else {
                            onTap()
                        }
                    },
                    onDoubleTap = { tapOffset ->
                        if (isZoomModeActive) {
                            targetScale = 1f
                            targetRotation = 0f
                            targetOffset = Offset.Zero
                            isZoomModeActive = false
                        } else {
                            val newScale = if (isLongImage || imageSize.width < containerSize.width) {
                                max(fitScreenScale, 1.5f)
                            } else {
                                2.5f
                            }

                            targetScale = newScale

                            val centerToTap = Offset(
                                tapOffset.x - boxSize.x,
                                tapOffset.y - boxSize.y
                            )

                            targetOffset = centerToTap * (1 - 1 / newScale)
                            isZoomModeActive = true
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Image",
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    imageSize = coordinates.size
                },
            onLoading = { onLoadingChanged(true); onErrorChanged(false) },
            onSuccess = { onLoadingChanged(false); onErrorChanged(false) },
            onError = { onLoadingChanged(false); onErrorChanged(true) },
            loading = {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            },
            error = {
                ImageLoadError()
            }
        )
    }
}

/**
 * 图片加载错误占位组件
 */
@Composable
fun ImageLoadError(
    modifier: Modifier = Modifier,
    errorText: String = stringResource(R.string.common_image_load_failed)
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * 计算图片适合屏幕的最佳缩放比例
 */
private fun calculateFitScreenScale(containerSize: Size, imageSize: Size): Float {
    if (containerSize.width <= 0 || imageSize.width <= 0) return 1f
    val widthRatio = imageSize.width / containerSize.width
    return abs(widthRatio)
        .coerceAtLeast(1f)
        .coerceAtMost(5f)
}

