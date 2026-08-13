package com.wanbaohe.markuplayers.data.render

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.ShapeGeometry
import com.wanbaohe.markuplayers.domain.model.ShapeKind
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import kotlin.math.min
import kotlin.reflect.KClass

/**
 * 形状图层导出:调度器已把原点平移到图层中心,外接框即 (-w/2,-h/2)-(w/2,h/2),
 * w = widthRatio×原图宽,h = heightRatio×原图高。多边形顶点与预览侧共用
 * ShapeGeometry,平移到中心原点绘制,保证所见即所得。
 */
class ShapeLayerExportRenderer @Inject constructor() : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Shape::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
    ) {
        val spec = (layer.type as? LayerType.Shape)?.spec ?: return
        if (imageWidth <= 0 || imageHeight <= 0) return
        val width = spec.widthRatio * imageWidth
        val height = spec.heightRatio * imageHeight
        if (width <= 0f || height <= 0f) return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = spec.color
            style = if (spec.filled) Paint.Style.FILL else Paint.Style.STROKE
            strokeWidth = (spec.strokeWidthRatio * imageWidth).coerceAtLeast(1f)
        }
        when (spec.kind) {
            ShapeKind.Rectangle -> {
                val radius = (spec.cornerRadiusRatio * imageWidth)
                    .coerceIn(0f, min(width, height) / 2f)
                canvas.drawRoundRect(
                    RectF(-width / 2f, -height / 2f, width / 2f, height / 2f),
                    radius, radius, paint
                )
            }

            ShapeKind.Circle -> {
                // 圆形始终为正圆:直径取外接框短边
                val diameter = min(width, height)
                canvas.drawOval(
                    RectF(-diameter / 2f, -diameter / 2f, diameter / 2f, diameter / 2f),
                    paint
                )
            }

            ShapeKind.Line -> {
                // 线条无填充概念,始终按描边绘制(圆头横线,与外接框同宽)
                paint.style = Paint.Style.STROKE
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawLine(-width / 2f, 0f, width / 2f, 0f, paint)
            }

            ShapeKind.Triangle, ShapeKind.Arrow, ShapeKind.Star -> {
                val path = Path().apply {
                    ShapeGeometry.polygonPoints(spec.kind, width, height)
                        .forEachIndexed { index, (x, y) ->
                            val px = x - width / 2f
                            val py = y - height / 2f
                            if (index == 0) moveTo(px, py) else lineTo(px, py)
                        }
                    close()
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
