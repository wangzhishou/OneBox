package com.hrm.diagram.render.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke as ComposeStroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import com.hrm.diagram.core.DiagramApi
import com.hrm.diagram.core.draw.ArrowHead
import com.hrm.diagram.core.draw.ArrowStyle
import com.hrm.diagram.core.draw.Color as DiagramColor
import com.hrm.diagram.core.draw.DrawCommand
import com.hrm.diagram.core.draw.PathCmd
import com.hrm.diagram.core.draw.PathOp
import com.hrm.diagram.core.draw.Point as DiagramPoint
import com.hrm.diagram.core.draw.Rect as DiagramRect
import com.hrm.diagram.core.draw.TextAnchorX
import com.hrm.diagram.core.draw.TextAnchorY
import com.hrm.diagram.render.streaming.DiagramSnapshot
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Pan/zoom state for [DiagramCanvas].
 *
 * Minimal usage:
 * ```kotlin
 * val viewportState = rememberDiagramViewportState()
 * DiagramCanvas(snapshot, viewportState = viewportState, panZoomEnabled = true)
 * ```
 */
@Stable
internal class DiagramViewportState(
    initialZoom: Float = 1f,
    initialPan: Offset = Offset.Zero,
) {
    var zoom: Float by mutableStateOf(initialZoom.coerceIn(MIN_ZOOM, MAX_ZOOM))
        private set

    var pan: Offset by mutableStateOf(initialPan)
        private set

    fun reset() {
        zoom = 1f
        pan = Offset.Zero
    }

    internal fun applyGesture(centroid: Offset, panDelta: Offset, zoomChange: Float) {
        pan += panDelta
        applyZoom(centroid, zoomChange)
    }

    internal fun applyZoom(anchor: Offset, zoomChange: Float) {
        if (zoomChange == 1f) return
        val oldZoom = zoom
        val newZoom = (oldZoom * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
        val scaleChange = if (oldZoom == 0f) 1f else newZoom / oldZoom
        pan = (pan - anchor) * scaleChange + anchor
        zoom = newZoom
    }

    private companion object {
        const val MIN_ZOOM = 0.1f
        const val MAX_ZOOM = 8f
    }
}

@Composable
internal fun rememberDiagramViewportState(): DiagramViewportState =
    remember { DiagramViewportState() }

/**
 * Renders a [DiagramSnapshot]'s `drawCommands` into a Compose [Canvas]. Pure projection: the
 * command list is the single source of truth, so the JVM/Android/iOS/JS/Wasm targets all paint
 * identically.
 *
 * Z-ordering is honoured by stable-sorting before draw; nested [DrawCommand.Group] /
 * [DrawCommand.Clip] preserve their parent's Z slot. Text uses the multiplatform
 * [TextMeasurer]; no platform shaping API is touched.
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun DiagramCanvas(
    snapshot: DiagramSnapshot,
    modifier: Modifier = Modifier,
    viewport: DiagramRect? = null,
    viewportState: DiagramViewportState = rememberDiagramViewportState(),
    panZoomEnabled: Boolean = false,
) {
    val measurer = rememberTextMeasurer()
    val sortedCommands = remember(snapshot.drawCommands) {
        snapshot.drawCommands.sortedBy { it.z }
    }
    val canvasModifier = if (panZoomEnabled) {
        val bounds = snapshot.laidOut?.bounds
        modifier
            .pointerInput(viewportState, bounds) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Scroll) {
                            val change = event.changes.firstOrNull()
                            if (change != null) {
                                val scroll = change.scrollDelta
                                if (scroll.y != 0f) {
                                    viewportState.applyZoom(
                                        anchor = viewportAnchor(change.position, bounds, size),
                                        zoomChange = scrollToZoomChange(scroll.y),
                                    )
                                    change.consume()
                                }
                            }
                        }
                    }
                }
            }
            .pointerInput(viewportState, bounds) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    viewportState.applyGesture(
                        centroid = viewportAnchor(centroid, bounds, size),
                        panDelta = pan,
                        zoomChange = zoom,
                    )
                }
            }
    } else {
        modifier
    }
    Canvas(modifier = canvasModifier) {
        val bounds = snapshot.laidOut?.bounds
        if (bounds == null || bounds.size.width <= 0f || bounds.size.height <= 0f) {
            for (cmd in sortedCommands) execute(cmd, measurer)
            return@Canvas
        }

        val fitScale = min(size.width / bounds.size.width, size.height / bounds.size.height)
            .coerceAtMost(1f)
        val zoom = viewportState.zoom
        val totalScale = max(0.0001f, fitScale * zoom)
        val offsetX = (size.width - bounds.size.width * fitScale) / 2f + viewportState.pan.x
        val offsetY = (size.height - bounds.size.height * fitScale) / 2f + viewportState.pan.y
        val queryViewport = viewport ?: visibleDiagramViewport(bounds, size, offsetX, offsetY, totalScale)
        val visibleCommands = snapshot.drawCommandIndex.query(queryViewport)

        withTransform({
            translate(left = offsetX, top = offsetY)
            scale(scaleX = totalScale, scaleY = totalScale)
            translate(left = -bounds.left, top = -bounds.top)
        }) {
            for (cmd in visibleCommands) execute(cmd, measurer)
        }
    }
}

private fun viewportAnchor(
    screenPosition: Offset,
    bounds: DiagramRect?,
    canvasSize: IntSize,
): Offset {
    if (bounds == null || bounds.size.width <= 0f || bounds.size.height <= 0f) return screenPosition
    val fitScale = min(canvasSize.width / bounds.size.width, canvasSize.height / bounds.size.height)
        .coerceAtMost(1f)
    val contentOrigin = Offset(
        x = (canvasSize.width - bounds.size.width * fitScale) / 2f,
        y = (canvasSize.height - bounds.size.height * fitScale) / 2f,
    )
    return screenPosition - contentOrigin
}

private fun scrollToZoomChange(scrollDeltaY: Float): Float {
    val exponent = (-scrollDeltaY / 400f).coerceIn(-1f, 1f)
    return 2.0.pow(exponent.toDouble()).toFloat()
}

private fun visibleDiagramViewport(
    bounds: DiagramRect,
    canvasSize: Size,
    offsetX: Float,
    offsetY: Float,
    totalScale: Float,
): DiagramRect {
    val left = bounds.left + (0f - offsetX) / totalScale
    val top = bounds.top + (0f - offsetY) / totalScale
    val right = bounds.left + (canvasSize.width - offsetX) / totalScale
    val bottom = bounds.top + (canvasSize.height - offsetY) / totalScale
    return DiagramRect.ltrb(
        left = min(left, right),
        top = min(top, bottom),
        right = max(left, right),
        bottom = max(top, bottom),
    )
}

private fun DrawScope.execute(cmd: DrawCommand, measurer: TextMeasurer) {
    when (cmd) {
        is DrawCommand.FillRect -> drawDiagramRect(cmd.rect, cmd.color, cmd.corner, fill = true, strokeWidth = 0f, dash = null)
        is DrawCommand.StrokeRect -> drawDiagramRect(cmd.rect, cmd.color, cmd.corner, fill = false, strokeWidth = cmd.stroke.width, dash = cmd.stroke.dash)
        is DrawCommand.FillPath -> drawPath(path = cmd.path.toComposePath(), color = cmd.color.toCompose())
        is DrawCommand.StrokePath -> {
            val pathEffect = cmd.stroke.dash?.takeIf { it.isNotEmpty() }
                ?.let { PathEffect.dashPathEffect(it.toFloatArray(), 0f) }
            drawPath(
                path = cmd.path.toComposePath(),
                color = cmd.color.toCompose(),
                style = ComposeStroke(width = cmd.stroke.width, pathEffect = pathEffect),
            )
        }
        is DrawCommand.DrawText -> {
            val style = TextStyle(color = cmd.color.toCompose(), fontSize = cmd.font.sizeSp.sp)
            val mw = cmd.maxWidth
            val constraints = if (mw != null && mw > 0f) {
                Constraints(maxWidth = mw.toInt().coerceAtLeast(1))
            } else Constraints()
            val layout = measurer.measure(text = cmd.text, style = style, constraints = constraints)
            val w = layout.size.width.toFloat()
            val h = layout.size.height.toFloat()
            val ascent = layout.firstBaseline
            val tlx = when (cmd.anchorX) {
                TextAnchorX.Start -> cmd.origin.x
                TextAnchorX.Center -> cmd.origin.x - w / 2f
                TextAnchorX.End -> cmd.origin.x - w
            }
            val tly = when (cmd.anchorY) {
                TextAnchorY.Top -> cmd.origin.y
                TextAnchorY.Middle -> cmd.origin.y - h / 2f
                TextAnchorY.Baseline -> cmd.origin.y - ascent
                TextAnchorY.Bottom -> cmd.origin.y - h
            }
            drawText(
                textLayoutResult = layout,
                topLeft = Offset(tlx, tly),
            )
        }
        is DrawCommand.DrawArrow -> {
            drawArrow(cmd.from, cmd.to, cmd.style)
        }
        is DrawCommand.DrawIcon -> Unit
        is DrawCommand.Hyperlink -> Unit
        is DrawCommand.Group -> {
            val t = cmd.transform
            if (t.isIdentity) {
                for (c in cmd.children) execute(c, measurer)
            } else {
                withTransform({
                    translate(left = t.translate.x, top = t.translate.y)
                    rotate(degrees = t.rotateDeg)
                    scale(scaleX = t.scale, scaleY = t.scale)
                }) {
                    for (c in cmd.children) execute(c, measurer)
                }
            }
        }
        is DrawCommand.Clip -> {
            clipRect(
                left = cmd.rect.left, top = cmd.rect.top,
                right = cmd.rect.right, bottom = cmd.rect.bottom,
            ) {
                for (c in cmd.children) execute(c, measurer)
            }
        }
    }
}

private fun DrawScope.drawArrow(from: DiagramPoint, to: DiagramPoint, style: ArrowStyle) {
    val color = style.color.toCompose()
    val pathEffect = style.stroke.dash?.takeIf { it.isNotEmpty() }?.let {
        PathEffect.dashPathEffect(it.toFloatArray(), 0f)
    }
    drawLine(
        color = color,
        start = Offset(from.x, from.y),
        end = Offset(to.x, to.y),
        strokeWidth = style.stroke.width,
        pathEffect = pathEffect,
    )
    drawArrowHead(from, to, style.head, style)
    drawArrowHead(to, from, style.tail, style)
}

private fun DrawScope.drawArrowHead(baseAnchor: DiagramPoint, tip: DiagramPoint, head: ArrowHead, style: ArrowStyle) {
    if (head == ArrowHead.None) return
    val angle = atan2((tip.y - baseAnchor.y).toDouble(), (tip.x - baseAnchor.x).toDouble())
    val size = 8f * style.stroke.width.coerceAtLeast(1f)
    val color = style.color.toCompose()
    val stroke = ComposeStroke(width = style.stroke.width.coerceAtLeast(1f))

    fun offset(x: Float, y: Float): Offset {
        val ca = cos(angle)
        val sa = sin(angle)
        return Offset(
            x = tip.x + (x * ca - y * sa).toFloat(),
            y = tip.y + (x * sa + y * ca).toFloat(),
        )
    }

    when (head) {
        ArrowHead.Triangle, ArrowHead.OpenTriangle -> {
            val p1 = offset(-size, -size / 2f)
            val p2 = offset(-size, size / 2f)
            val path = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                close()
            }
            if (head == ArrowHead.Triangle) drawPath(path, color, style = Fill)
            drawPath(path, color, style = stroke)
        }
        ArrowHead.Diamond, ArrowHead.OpenDiamond -> {
            val p1 = offset(-size / 2f, -size / 3f)
            val p2 = offset(-size, 0f)
            val p3 = offset(-size / 2f, size / 3f)
            val path = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(p3.x, p3.y)
                close()
            }
            if (head == ArrowHead.Diamond) drawPath(path, color, style = Fill)
            drawPath(path, color, style = stroke)
        }
        ArrowHead.Circle, ArrowHead.OpenCircle -> {
            val center = offset(-size / 2f, 0f)
            if (head == ArrowHead.Circle) drawCircle(color, radius = size / 2f, center = center, style = Fill)
            drawCircle(color, radius = size / 2f, center = center, style = stroke)
        }
        ArrowHead.Bar -> {
            val p1 = offset(0f, -size / 2f)
            val p2 = offset(0f, size / 2f)
            drawLine(color, p1, p2, strokeWidth = style.stroke.width.coerceAtLeast(1f))
        }
        ArrowHead.Cross -> {
            val a1 = offset(-size / 2f, -size / 2f)
            val a2 = offset(size / 2f, size / 2f)
            val b1 = offset(-size / 2f, size / 2f)
            val b2 = offset(size / 2f, -size / 2f)
            drawLine(color, a1, a2, strokeWidth = style.stroke.width.coerceAtLeast(1f))
            drawLine(color, b1, b2, strokeWidth = style.stroke.width.coerceAtLeast(1f))
        }
        ArrowHead.None -> Unit
    }
}

private fun DrawScope.drawDiagramRect(
    rect: DiagramRect,
    color: DiagramColor,
    corner: Float,
    fill: Boolean,
    strokeWidth: Float,
    dash: List<Float>? = null,
) {
    val topLeft = Offset(rect.left, rect.top)
    val size = Size(rect.size.width, rect.size.height)
    val pathEffect = dash?.takeIf { it.isNotEmpty() }?.let { PathEffect.dashPathEffect(it.toFloatArray(), 0f) }
    if (corner > 0f) {
        if (fill) drawRoundRect(color = color.toCompose(), topLeft = topLeft, size = size, cornerRadius = CornerRadius(corner, corner))
        else drawRoundRect(color = color.toCompose(), topLeft = topLeft, size = size, cornerRadius = CornerRadius(corner, corner), style = ComposeStroke(width = strokeWidth, pathEffect = pathEffect))
    } else {
        if (fill) drawRect(color = color.toCompose(), topLeft = topLeft, size = size)
        else drawRect(color = color.toCompose(), topLeft = topLeft, size = size, style = ComposeStroke(width = strokeWidth, pathEffect = pathEffect))
    }
}

private fun DiagramColor.toCompose(): Color = Color(argb)

private fun PathCmd.toComposePath(): Path {
    val path = Path()
    for (op in ops) when (op) {
        is PathOp.MoveTo -> path.moveTo(op.p.x, op.p.y)
        is PathOp.LineTo -> path.lineTo(op.p.x, op.p.y)
        is PathOp.QuadTo -> path.quadraticTo(op.ctrl.x, op.ctrl.y, op.end.x, op.end.y)
        is PathOp.CubicTo -> path.cubicTo(op.c1.x, op.c1.y, op.c2.x, op.c2.y, op.end.x, op.end.y)
        PathOp.Close -> path.close()
    }
    return path
}
