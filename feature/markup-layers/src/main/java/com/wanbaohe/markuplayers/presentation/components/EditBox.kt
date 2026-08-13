package com.wanbaohe.markuplayers.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wanbaohe.markuplayers.domain.model.LayerTransform
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 图层选中框:在 BoxWithConstraints 画布(与底图同尺寸)内渲染单个图层,
 * 支持点选与双指缩放/旋转/拖动,选中时显示蚂蚁线边框。
 *
 * 归一化换算方案:图层位置存的是相对底图宽高的归一化中心点
 * ([LayerTransform.centerX]/[LayerTransform.centerY])。内容 Box 先对齐画布中心,
 * 再经 graphicsLayer 平移 (center-0.5)×画布尺寸、并按 transform 缩放/旋转,
 * 与导出侧 Canvas.translate(center)→rotate→scale 的变换顺序完全同构,所见即所得。
 * 手势期间只更新本地状态保持流畅,全部手指抬起后经 [onTransformEnd] 一次性提交。
 *
 * @param tapSelectable 是否响应点选手势。铺满画布的图层(如画笔)应传 false,
 * 否则会拦截画布的「点空白取消选择」
 */
@Composable
fun BoxWithConstraintsScope.EditBox(
    transform: LayerTransform,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onTransformEnd: (LayerTransform) -> Unit,
    modifier: Modifier = Modifier,
    onEditRequest: (() -> Unit)? = null,
    tapSelectable: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!transform.visible) return

    val canvasWidth = constraints.maxWidth.toFloat()
    val canvasHeight = constraints.maxHeight.toFloat()

    var localTransform by remember(transform) { mutableStateOf(transform) }

    val canTransform = isSelected && !transform.locked

    Box(
        modifier = modifier
            .align(Alignment.Center)
            .graphicsLayer {
                scaleX = localTransform.scale
                scaleY = localTransform.scale
                rotationZ = localTransform.rotation
                translationX = (localTransform.centerX - 0.5f) * canvasWidth
                translationY = (localTransform.centerY - 0.5f) * canvasHeight
                alpha = localTransform.alpha
            }
            .pointerInput(transform.locked, tapSelectable) {
                // tapSelectable=false 时(如铺满画布的画笔图层)点按穿透到外层,
                // 保留「点空白取消选择」,图层改由图层面板选中
                if (transform.locked || !tapSelectable) return@pointerInput
                detectTapGestures(
                    onTap = {
                        if (isSelected) onEditRequest?.invoke() else onSelect()
                    }
                )
            }
            .pointerInput(canTransform, canvasWidth, canvasHeight) {
                if (!canTransform) return@pointerInput
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    // 手动实现 transform 手势检测(detectTransformGestures 无结束回调):
                    // 越过 touchSlop 后开始应用增量,全部手指抬起后一次性提交
                    var pastTouchSlop = false
                    var pan = Offset.Zero
                    var zoom = 1f
                    var rotation = 0f
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.changes.any { it.isConsumed }) break
                        val zoomChange = event.calculateZoom()
                        val rotationChange = event.calculateRotation()
                        val panChange = event.calculatePan()
                        if (!pastTouchSlop) {
                            zoom *= zoomChange
                            rotation += rotationChange
                            pan += panChange
                            val centroidSize = event.calculateCentroidSize(useCurrent = false)
                            val zoomMotion = abs(1 - zoom) * centroidSize
                            val rotationMotion =
                                abs(rotation * Math.PI.toFloat() * centroidSize / 180f)
                            val panMotion = pan.getDistance()
                            if (zoomMotion > viewConfiguration.touchSlop ||
                                rotationMotion > viewConfiguration.touchSlop ||
                                panMotion > viewConfiguration.touchSlop
                            ) {
                                pastTouchSlop = true
                            }
                        }
                        if (pastTouchSlop &&
                            (zoomChange != 1f || rotationChange != 0f || panChange != Offset.Zero)
                        ) {
                            localTransform = localTransform.applyGesture(
                                pan = panChange,
                                zoomChange = zoomChange,
                                rotationChange = rotationChange,
                                canvasWidth = canvasWidth,
                                canvasHeight = canvasHeight
                            )
                            event.changes.forEach {
                                if (it.position != it.previousPosition) it.consume()
                            }
                        }
                        if (event.changes.none { it.pressed }) break
                    }
                    if (pastTouchSlop) onTransformEnd(localTransform)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        content()
        AnimatedBorder(
            isVisible = isSelected,
            scale = localTransform.scale,
            modifier = Modifier.matchParentSize()
        )
    }
}

/**
 * 应用一次手势增量。detectTransformGestures 的 pan 在图层本地(已变换)坐标系,
 * 需乘缩放并按当前旋转角转回画布坐标系,再归一化到 0..1 累加进中心点。
 */
private fun LayerTransform.applyGesture(
    pan: Offset,
    zoomChange: Float,
    rotationChange: Float,
    canvasWidth: Float,
    canvasHeight: Float,
): LayerTransform {
    val newRotation = rotation + rotationChange
    val newScale = (scale * zoomChange).coerceIn(0.1f, 10f)
    val panInCanvas = (pan * newScale).rotateBy(newRotation)
    return copy(
        centerX = (centerX + panInCanvas.x / canvasWidth).coerceIn(0f, 1f),
        centerY = (centerY + panInCanvas.y / canvasHeight).coerceIn(0f, 1f),
        scale = newScale,
        rotation = newRotation
    )
}

private fun Offset.rotateBy(degrees: Float): Offset {
    val radians = Math.toRadians(degrees.toDouble())
    return Offset(
        x = (x * cos(radians) - y * sin(radians)).toFloat(),
        y = (x * sin(radians) + y * cos(radians)).toFloat()
    )
}

@Composable
private fun AnimatedBorder(
    isVisible: Boolean,
    scale: Float,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(if (isVisible) 1f else 0f)
    if (alpha <= 0f) return

    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(20f, 20f),
        phase = phase
    )

    val density = LocalDensity.current
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    Canvas(modifier = modifier) {
        val strokeWidth = 3.dp.toPx() * (1f / scale)
        drawRect(
            color = primary.copy(alpha),
            style = Stroke(width = strokeWidth)
        )
        drawRect(
            color = primaryContainer.copy(alpha),
            style = Stroke(width = strokeWidth, pathEffect = pathEffect)
        )
    }
}
