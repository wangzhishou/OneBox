package com.t8rin.imagetoolbox.core.ui.widget.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * 编辑器选中框共享 chrome(自 feature/text-card 抽取,供图文卡片/图片创作共用):
 * 静态虚线框 + 手柄圆点 + 8 向框尺寸手柄 + 四角缩放/顶部旋转手柄。
 * 全部 chrome 支持 inverseScale 反缩放:元素被放大时操作件视觉尺寸保持恒定。
 * 所有手势在元素本地坐标系计算(graphicsLayer 变换下的坐标映射天然一致)。
 */

/** 选中态虚线圆角边框;[inverseScale] 抵消元素缩放,线宽/虚线间隔视觉恒定 */
fun Modifier.dashedSelectionBorder(
    width: Dp,
    color: Color,
    cornerRadius: Dp,
    inverseScale: Float = 1f,
): Modifier = drawBehind {
    val strokeWidth = width.toPx() * inverseScale
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(6.dp.toPx() * inverseScale, 4.dp.toPx() * inverseScale)
            )
        )
    )
}

/** 手柄视觉:白边主色小圆点(热区 36dp,视觉 16dp;inverseScale 反缩放保持视觉恒定) */
@Composable
fun HandleDot(inverseScale: Float = 1f) {
    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = inverseScale
                scaleY = inverseScale
            }
            .size(16.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .border(2.dp, Color.White, CircleShape)
    )
}

/** 手柄热区尺寸(视觉圆点小于热区) */
val HANDLE_SIZE = 36.dp

/** 框尺寸手柄配置:阈值均 px;onResize 回报新框宽高与锚点补偿后的 left/top(均 px);
 * onGestureStart 在一次拖动手势开始时回调一次(宿主在这里记 undo 快照) */
class BoxResizeConfig(
    val minWidthPx: Float,
    val maxWidthPx: Float,
    val minHeightPx: Float,
    val maxHeightPx: Float,
    val onResize: (widthPx: Float, heightPx: Float, leftPx: Float, topPx: Float) -> Unit,
    val onGestureStart: () -> Unit = {},
)

/** 手柄规格:alignment=贴位,offsetSign=中心越边方向,edgeX/edgeY=-1 拖左/顶边、0 该轴不动、1 拖右/底边 */
data class HandleSpec(
    val alignment: Alignment,
    val offsetSign: IntOffset,
    val edgeX: Int,
    val edgeY: Int,
)

private val HANDLE_SPECS = listOf(
    HandleSpec(Alignment.TopStart, IntOffset(-1, -1), edgeX = -1, edgeY = -1),
    HandleSpec(Alignment.TopEnd, IntOffset(1, -1), edgeX = 1, edgeY = -1),
    HandleSpec(Alignment.BottomStart, IntOffset(-1, 1), edgeX = -1, edgeY = 1),
    HandleSpec(Alignment.BottomEnd, IntOffset(1, 1), edgeX = 1, edgeY = 1),
    HandleSpec(Alignment.CenterStart, IntOffset(-1, 0), edgeX = -1, edgeY = 0),
    HandleSpec(Alignment.CenterEnd, IntOffset(1, 0), edgeX = 1, edgeY = 0),
    HandleSpec(Alignment.TopCenter, IntOffset(0, -1), edgeX = 0, edgeY = -1),
    HandleSpec(Alignment.BottomCenter, IntOffset(0, 1), edgeX = 0, edgeY = 1),
)

/** 8 向框尺寸手柄(四角 + 四边中点):拖手柄改框宽/高,内容重排,宿主自定字号策略 */
@Composable
fun BoxScope.BoxResizeHandles(
    sizeProvider: () -> IntSize,
    leftProvider: () -> Float,
    topProvider: () -> Float,
    inverseScale: Float,
    config: BoxResizeConfig,
    rotationProvider: () -> Float,
    scaleProvider: () -> Float,
) {
    val density = LocalDensity.current
    val handlePx = with(density) { HANDLE_SIZE.toPx() }
    HANDLE_SPECS.forEach { spec ->
        ResizeHandle(
            spec = spec,
            handlePx = handlePx,
            sizeProvider = sizeProvider,
            leftProvider = leftProvider,
            topProvider = topProvider,
            rotationProvider = rotationProvider,
            scaleProvider = scaleProvider,
            inverseScale = inverseScale,
            config = config
        )
    }
}

/**
 * 单方向尺寸手柄:拖动改框宽/高;对侧边为视觉锚点,旋转/缩放下
 * 经锚点补偿保持锚点不动。补偿公式:dw=startW−w',dh=startH−h',
 * shift = (dw/2, dh/2) + R(θ)·s·((ax−½)dw, (ay−½)dh),ax/ay 为锚点比例(0/½/1)。
 */
@Composable
private fun BoxScope.ResizeHandle(
    spec: HandleSpec,
    handlePx: Float,
    sizeProvider: () -> IntSize,
    leftProvider: () -> Float,
    topProvider: () -> Float,
    rotationProvider: () -> Float,
    scaleProvider: () -> Float,
    inverseScale: Float,
    config: BoxResizeConfig,
) {
    // 锚点比例:拖右边锚左边(ax=0)、拖左边锚右边(ax=1)、不动轴锚中心(½)
    val anchorX = (1 - spec.edgeX) / 2f
    val anchorY = (1 - spec.edgeY) / 2f
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(spec.alignment)
            .offset {
                IntOffset(
                    x = (spec.offsetSign.x * handlePx / 2).roundToInt(),
                    y = (spec.offsetSign.y * handlePx / 2).roundToInt()
                )
            }
            .size(HANDLE_SIZE)
            .pointerInput(spec) {
                // 手势期起点(每次手势 onDragStart 重置,不跨手势共享)
                var drag = Offset.Zero
                var startW = 0f
                var startH = 0f
                var startLeft = 0f
                var startTop = 0f
                var startScale = 1f
                var startRotation = 0f
                detectDragGestures(
                    onDragStart = {
                        drag = Offset.Zero
                        val size = sizeProvider()
                        startW = size.width.toFloat()
                        startH = size.height.toFloat()
                        startLeft = leftProvider()
                        startTop = topProvider()
                        startScale = scaleProvider()
                        startRotation = rotationProvider()
                        config.onGestureStart()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        drag += dragAmount
                        val newW = (startW + spec.edgeX * drag.x)
                            .coerceIn(config.minWidthPx, config.maxWidthPx)
                        val newH = (startH + spec.edgeY * drag.y)
                            .coerceIn(config.minHeightPx, config.maxHeightPx)
                        val dw = startW - newW
                        val dh = startH - newH
                        val radians = Math.toRadians(startRotation.toDouble())
                        val scaledCos = kotlin.math.cos(radians).toFloat() * startScale
                        val scaledSin = kotlin.math.sin(radians).toFloat() * startScale
                        val anchorDw = (anchorX - 0.5f) * dw
                        val anchorDh = (anchorY - 0.5f) * dh
                        config.onResize(
                            newW,
                            newH,
                            startLeft + dw / 2 + (anchorDw * scaledCos - anchorDh * scaledSin),
                            startTop + dh / 2 + (anchorDw * scaledSin + anchorDh * scaledCos)
                        )
                    }
                )
            }
    ) {
        HandleDot(inverseScale)
    }
}

/** 拖角缩放:触点与元素中心的距离比 × 起始缩放;回报绝对值 */
@Composable
fun BoxScope.ScaleHandle(
    alignment: Alignment,
    cornerInElement: Offset,
    offsetSign: IntOffset,
    handlePx: Float,
    elementCenter: Offset,
    inverseScale: Float,
    scaleProvider: () -> Float,
    onScale: (Float) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(alignment)
            .offset {
                IntOffset(
                    x = (offsetSign.x * handlePx / 2).roundToInt(),
                    y = (offsetSign.y * handlePx / 2).roundToInt()
                )
            }
            .size(HANDLE_SIZE)
            .pointerInput(cornerInElement, elementCenter) {
                val half = handlePx / 2
                // 手势期起点(每次手势 onDragStart 重置,不跨手势共享)
                var startDist = 1f
                var startScale = 1f
                detectDragGestures(
                    onDragStart = { downOffset ->
                        val startPoint = cornerInElement - Offset(half, half) + downOffset
                        startDist = (startPoint - elementCenter).getDistance().coerceAtLeast(1f)
                        startScale = scaleProvider()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val point = cornerInElement - Offset(half, half) + change.position
                        val factor = (point - elementCenter).getDistance() / startDist
                        onScale((startScale * factor).coerceIn(0.2f, 5f))
                    }
                )
            }
    ) {
        HandleDot(inverseScale)
    }
}

/** 顶部中心旋转手柄:触点绕元素中心的角度增量;回报绝对角度 */
@Composable
fun BoxScope.RotationHandle(
    handlePx: Float,
    elementCenter: Offset,
    inverseScale: Float,
    rotationProvider: () -> Float,
    onRotation: (Float) -> Unit,
) {
    // handle 中心:元素顶边中点正上方半个手柄位
    val handleCenter = Offset(elementCenter.x, -handlePx / 2)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = -HANDLE_SIZE)
            .size(HANDLE_SIZE)
            .pointerInput(elementCenter) {
                val half = handlePx / 2
                // 手势期起点(每次手势 onDragStart 重置,不跨手势共享)
                var startAngle = 0f
                var startRotation = 0f
                detectDragGestures(
                    onDragStart = { downOffset ->
                        val startPoint = handleCenter - Offset(half, half) + downOffset
                        startAngle = atan2(
                            startPoint.y - elementCenter.y,
                            startPoint.x - elementCenter.x
                        )
                        startRotation = rotationProvider()
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val point = handleCenter - Offset(half, half) + change.position
                        val angle = atan2(
                            point.y - elementCenter.y,
                            point.x - elementCenter.x
                        )
                        onRotation(
                            startRotation + Math.toDegrees((angle - startAngle).toDouble()).toFloat()
                        )
                    }
                )
            }
    ) {
        HandleDot(inverseScale)
    }
}
