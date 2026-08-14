package com.wanbaohe.markuplayers.presentation.tools.ai

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.wanbaohe.markuplayers.domain.model.NormalizedRect
import kotlin.math.min

/**
 * 图像修复框选层(裁剪框简化版):框外压暗 + 白色边框 + 四角 L 形手柄。
 * 框内拖动整体平移,四角手柄自由缩放;矩形以相对底图的归一化坐标记录,
 * 覆盖层铺满「底图适配盒」时直接一一对应,无需额外换算。
 */
@Composable
fun AiRectSelectOverlay(
    rect: NormalizedRect,
    onRectChange: (NormalizedRect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val touchSlopPx = with(density) { 28.dp.toPx() }
    val minSizePx = with(density) { 48.dp.toPx() }
    val scrimColor = Color.Black.copy(alpha = 0.5f)
    val frameColor = Color.White
    val borderStroke = with(density) { 1.5.dp.toPx() }
    val handleStroke = with(density) { 3.5.dp.toPx() }
    val handleLength = with(density) { 20.dp.toPx() }
    // 手势回调里读最新值,避免 pointerInput 闭包捕获旧矩形
    val currentRect by rememberUpdatedState(rect)

    Canvas(
        modifier = modifier.rectGestures(
            rect = { currentRect },
            onRectChange = onRectChange,
            touchSlopPx = touchSlopPx,
            minSizePx = minSizePx
        )
    ) {
        val left = rect.left * size.width
        val top = rect.top * size.height
        val right = rect.right * size.width
        val bottom = rect.bottom * size.height

        // 框外压暗(上/下/左/右四条带)
        drawRect(scrimColor, size = Size(size.width, top))
        drawRect(
            scrimColor,
            topLeft = Offset(0f, bottom),
            size = Size(size.width, size.height - bottom)
        )
        drawRect(scrimColor, topLeft = Offset(0f, top), size = Size(left, bottom - top))
        drawRect(
            scrimColor,
            topLeft = Offset(right, top),
            size = Size(size.width - right, bottom - top)
        )

        // 边框
        drawRect(
            frameColor,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            style = Stroke(width = borderStroke)
        )

        // 四角 L 形手柄
        drawCornerHandle(Offset(left, top), 1f, 1f, handleLength, handleStroke, frameColor)
        drawCornerHandle(Offset(right, top), -1f, 1f, handleLength, handleStroke, frameColor)
        drawCornerHandle(Offset(left, bottom), 1f, -1f, handleLength, handleStroke, frameColor)
        drawCornerHandle(Offset(right, bottom), -1f, -1f, handleLength, handleStroke, frameColor)
    }
}

private enum class RectHandle {
    TopLeft, TopRight, BottomLeft, BottomRight, Move
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerHandle(
    corner: Offset,
    dirX: Float,
    dirY: Float,
    length: Float,
    strokeWidth: Float,
    color: Color,
) {
    drawLine(
        color,
        start = corner,
        end = Offset(corner.x + dirX * length, corner.y),
        strokeWidth = strokeWidth
    )
    drawLine(
        color,
        start = corner,
        end = Offset(corner.x, corner.y + dirY * length),
        strokeWidth = strokeWidth
    )
}

/** 框选手势:角手柄自由缩放,框内拖动整体平移 */
private fun Modifier.rectGestures(
    rect: () -> NormalizedRect,
    onRectChange: (NormalizedRect) -> Unit,
    touchSlopPx: Float,
    minSizePx: Float,
): Modifier = pointerInput(touchSlopPx, minSizePx) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val handle = hitTestHandle(
            position = down.position,
            rect = rect(),
            boxSize = size,
            slop = touchSlopPx
        ) ?: return@awaitEachGesture
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (!change.pressed) break
            if (change.positionChange() != Offset.Zero) {
                onRectChange(
                    rect().draggedBy(
                        handle = handle,
                        position = change.position,
                        delta = change.positionChange(),
                        boxSize = size,
                        minSizePx = minSizePx
                    )
                )
                change.consume()
            }
        }
    }
}

private fun hitTestHandle(
    position: Offset,
    rect: NormalizedRect,
    boxSize: IntSize,
    slop: Float,
): RectHandle? {
    val left = rect.left * boxSize.width
    val top = rect.top * boxSize.height
    val right = rect.right * boxSize.width
    val bottom = rect.bottom * boxSize.height
    val corners = listOf(
        RectHandle.TopLeft to Offset(left, top),
        RectHandle.TopRight to Offset(right, top),
        RectHandle.BottomLeft to Offset(left, bottom),
        RectHandle.BottomRight to Offset(right, bottom)
    )
    corners.forEach { (handle, corner) ->
        if ((position - corner).getDistance() <= slop) return handle
    }
    if (position.x in left..right && position.y in top..bottom) return RectHandle.Move
    return null
}

/** 下限优先的收敛:上限不足下限时退化为下限(避免 coerceIn 区间倒置崩溃) */
private fun Float.clampLower(lower: Float, upper: Float): Float =
    coerceIn(lower, upper.coerceAtLeast(lower))

/** 上限优先的收敛:下限超过上限时退化为上限 */
private fun Float.clampUpper(lower: Float, upper: Float): Float =
    coerceIn(lower.coerceAtMost(upper), upper)

/** 在覆盖层像素空间完成一次拖动计算,结果换回归一化矩形(自由比例) */
private fun NormalizedRect.draggedBy(
    handle: RectHandle,
    position: Offset,
    delta: Offset,
    boxSize: IntSize,
    minSizePx: Float,
): NormalizedRect {
    val boxWidth = boxSize.width.toFloat()
    val boxHeight = boxSize.height.toFloat()
    var left = this.left * boxWidth
    var top = this.top * boxHeight
    var right = this.right * boxWidth
    var bottom = this.bottom * boxHeight
    val minWidth = min(minSizePx, boxWidth)
    val minHeight = min(minSizePx, boxHeight)

    when (handle) {
        RectHandle.Move -> {
            val dx = delta.x.coerceIn(-left, boxWidth - right)
            val dy = delta.y.coerceIn(-top, boxHeight - bottom)
            left += dx
            right += dx
            top += dy
            bottom += dy
        }

        RectHandle.TopLeft -> {
            left = position.x.clampLower(0f, right - minWidth)
            top = position.y.clampLower(0f, bottom - minHeight)
        }

        RectHandle.TopRight -> {
            right = position.x.clampUpper(left + minWidth, boxWidth)
            top = position.y.clampLower(0f, bottom - minHeight)
        }

        RectHandle.BottomLeft -> {
            left = position.x.clampLower(0f, right - minWidth)
            bottom = position.y.clampUpper(top + minHeight, boxHeight)
        }

        RectHandle.BottomRight -> {
            right = position.x.clampUpper(left + minWidth, boxWidth)
            bottom = position.y.clampUpper(top + minHeight, boxHeight)
        }
    }
    return NormalizedRect(
        left = left / boxWidth,
        top = top / boxHeight,
        right = right / boxWidth,
        bottom = bottom / boxHeight
    )
}
