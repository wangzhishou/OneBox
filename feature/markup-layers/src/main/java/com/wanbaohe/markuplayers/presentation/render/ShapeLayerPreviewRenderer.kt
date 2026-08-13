package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.ShapeGeometry
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * 形状图层预览:基础尺寸 = widthRatio×画布宽 / heightRatio×画布高的外接框,
 * 框内绘制形状。圆角/描边粗细按底图宽(canvasWidthPx)换算,与导出侧一致。
 */
object ShapeLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Shape::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
    ) {
        val spec = (layer.type as? LayerType.Shape)?.spec ?: return
        val density = LocalDensity.current
        Canvas(
            modifier = Modifier.size(
                width = with(density) { (spec.widthRatio * canvasWidthPx).toDp() },
                height = with(density) { (spec.heightRatio * canvasHeightPx).toDp() }
            )
        ) {
            drawShapeContent(
                spec = spec,
                strokeWidthPx = (spec.strokeWidthRatio * canvasWidthPx).coerceAtLeast(1f),
                cornerRadiusPx = spec.cornerRadiusRatio * canvasWidthPx
            )
        }
    }
}

/** 在外接框(当前 DrawScope size)内绘制形状内容,画布预览与缩略图共用 */
internal fun DrawScope.drawShapeContent(
    spec: ShapeSpec,
    strokeWidthPx: Float,
    cornerRadiusPx: Float,
) {
    val color = Color(spec.color)
    val stroke = Stroke(width = strokeWidthPx)
    when (spec.kind) {
        ShapeKind.Rectangle -> {
            val radius = cornerRadiusPx.coerceIn(0f, min(size.width, size.height) / 2f)
            drawRoundRect(
                color = color,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(radius),
                style = if (spec.filled) Fill else stroke
            )
        }

        ShapeKind.Circle -> {
            // 圆形始终为正圆:直径取外接框短边,居中
            val diameter = min(size.width, size.height)
            drawOval(
                color = color,
                topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                ),
                size = Size(diameter, diameter),
                style = if (spec.filled) Fill else stroke
            )
        }

        ShapeKind.Line -> {
            // 线条无填充概念,始终按描边绘制(圆头横线,与外接框同宽)
            drawLine(
                color = color,
                start = Offset(0f, size.height / 2f),
                end = Offset(size.width, size.height / 2f),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )
        }

        ShapeKind.Triangle, ShapeKind.Arrow, ShapeKind.Star -> {
            val path = Path().apply {
                ShapeGeometry.polygonPoints(spec.kind, size.width, size.height)
                    .forEachIndexed { index, (x, y) ->
                        if (index == 0) moveTo(x, y) else lineTo(x, y)
                    }
                close()
            }
            drawPath(
                path = path,
                color = color,
                style = if (spec.filled) Fill else stroke
            )
        }
    }
}
