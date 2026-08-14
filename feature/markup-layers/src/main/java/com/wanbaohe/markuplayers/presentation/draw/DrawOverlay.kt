package com.wanbaohe.markuplayers.presentation.draw

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import com.wanbaohe.markuplayers.presentation.render.drawStroke

/**
 * 绘画覆盖层:会话笔画(含进行中笔画),Offscreen 隔离使橡皮擦只清会话笔画。
 * 画布内绘制模式下叠加在主画布图层之上,单指绘制手势直接挂在 Canvas 上。
 */
@Composable
internal fun DrawOverlay(
    session: DrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) {
    val density = LocalDensity.current
    Canvas(
        modifier = Modifier
            .size(
                width = with(density) { canvasWidthPx.toDp() },
                height = with(density) { canvasHeightPx.toDp() }
            )
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .drawGesture(session, canvasWidthPx, canvasHeightPx)
    ) {
        session.strokes.forEach { drawStroke(it, size.width, size.height) }
        session.inProgressStroke(size.width)?.let {
            drawStroke(it, size.width, size.height)
        }
    }
}

/** 单指绘画手势:记录归一化采样点;第二指落下即收笔,避免与缩放手势互相污染 */
private fun Modifier.drawGesture(
    session: DrawSessionState,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
): Modifier = pointerInput(canvasWidthPx, canvasHeightPx, session.isPanMode) {
    if (session.isPanMode) return@pointerInput
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        if (down.isConsumed) return@awaitEachGesture
        session.beginStroke()
        session.appendPoint(down.position.normalized(canvasWidthPx, canvasHeightPx))
        try {
            while (true) {
                val event = awaitPointerEvent()
                if (event.changes.count { it.pressed } > 1) break
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                if (change.positionChanged()) {
                    session.appendPoint(change.position.normalized(canvasWidthPx, canvasHeightPx))
                    change.consume()
                }
            }
        } finally {
            // 手势被取消(如模式切换导致 pointerInput 重启)也要收笔结算,
            // 避免进行中的笔画残留成不落层的"幽灵"预览
            session.finishStroke(canvasWidthPx)
        }
    }
}

private fun Offset.normalized(width: Float, height: Float) = StrokePoint(x / width, y / height)
