package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import com.wanbaohe.markuplayers.domain.model.BrushType
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.DrawStrokeGeometry
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import kotlin.reflect.KClass

/**
 * 画笔图层预览:内容铺满整个画布(基础尺寸 = 底图),笔画宽 = widthRatio × 画布宽。
 * 经 Offscreen 合成隔离,Eraser 笔画以 [BlendMode.Clear] 绘制只清本图层,
 * 不伤底图与其他图层。
 */
object DrawLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Draw::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
    ) {
        val type = layer.type as? LayerType.Draw ?: return
        if (type.strokes.isEmpty()) return

        val density = LocalDensity.current
        Canvas(
            modifier = Modifier
                .size(
                    width = with(density) { canvasWidthPx.toDp() },
                    height = with(density) { canvasHeightPx.toDp() }
                )
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            type.strokes.forEach { stroke ->
                drawStroke(stroke, size.width, size.height)
            }
        }
    }
}

/**
 * 单条笔画绘制:画笔图层预览与绘画会话覆盖层共用。
 * TODO: 铅笔/钢笔目前同为圆头实线,后续可加纹理/压感区分。
 */
internal fun DrawScope.drawStroke(
    stroke: DrawStroke,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) {
    val points = stroke.points
    if (points.isEmpty()) return
    val widthPx = (stroke.widthRatio * canvasWidthPx).coerceAtLeast(1f)
    val isEraser = stroke.brush == BrushType.Eraser
    val blendMode = if (isEraser) BlendMode.Clear else BlendMode.SrcOver
    // Clear 不需要颜色,强制不透明黑避免源 alpha 干扰
    val color = if (isEraser) Color.Black else Color(stroke.color).copy(alpha = stroke.alpha)

    fun StrokePoint.toOffset() = Offset(x * canvasWidthPx, y * canvasHeightPx)

    if (points.size == 1) {
        drawCircle(
            color = color,
            radius = widthPx / 2,
            center = points.first().toOffset(),
            blendMode = blendMode
        )
        return
    }

    if (stroke.brush == BrushType.Brush) {
        // 毛笔近似:平滑折线逐段绘制,宽度按 sin 曲线两端收尖
        val smoothed = DrawStrokeGeometry.smoothed(points)
        val lastIndex = smoothed.size - 1
        for (i in 0 until lastIndex) {
            drawLine(
                color = color,
                start = smoothed[i].toOffset(),
                end = smoothed[i + 1].toOffset(),
                strokeWidth = widthPx * DrawStrokeGeometry.taperFactor(i, lastIndex),
                cap = StrokeCap.Round,
                blendMode = blendMode
            )
        }
        return
    }

    val path = Path().apply {
        val first = points.first().toOffset()
        moveTo(first.x, first.y)
        for (i in 1 until points.size - 1) {
            val control = points[i].toOffset()
            val end = Offset(
                x = (points[i].x + points[i + 1].x) / 2f * canvasWidthPx,
                y = (points[i].y + points[i + 1].y) / 2f * canvasHeightPx
            )
            quadraticTo(control.x, control.y, end.x, end.y)
        }
        val last = points.last().toOffset()
        lineTo(last.x, last.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = widthPx,
            cap = if (stroke.brush == BrushType.Marker) StrokeCap.Square else StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        blendMode = blendMode
    )
}
