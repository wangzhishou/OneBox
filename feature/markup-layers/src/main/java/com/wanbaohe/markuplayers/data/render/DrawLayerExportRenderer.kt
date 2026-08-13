package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.wanbaohe.markuplayers.domain.model.BrushType
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.DrawStrokeGeometry
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.StrokePoint
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.reflect.KClass

/**
 * 画笔图层导出:笔画先绘制到该图层独立 Bitmap(橡皮擦用 PorterDuff CLEAR,
 * 只清本图层位图),再整体 drawBitmap 到主画布,不伤底图与其他图层。
 * 基础尺寸 = 整张原图,调度器已把原点平移到图层中心(默认图中心),
 * 故以 (-w/2, -h/2) 偏移铺满。笔画样式与预览侧 DrawLayerPreviewRenderer 保持一致。
 */
class DrawLayerExportRenderer @Inject constructor() : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Draw::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
    ) {
        val type = layer.type as? LayerType.Draw ?: return
        if (type.strokes.isEmpty() || imageWidth <= 0 || imageHeight <= 0) return

        val layerBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        val layerCanvas = Canvas(layerBitmap)
        type.strokes.forEach { stroke ->
            drawStroke(layerCanvas, stroke, imageWidth, imageHeight)
        }

        canvas.drawBitmap(
            layerBitmap,
            -imageWidth / 2f,
            -imageHeight / 2f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    private fun drawStroke(
        canvas: Canvas,
        stroke: DrawStroke,
        imageWidth: Int,
        imageHeight: Int,
    ) {
        val points = stroke.points
        if (points.isEmpty()) return
        val widthPx = (stroke.widthRatio * imageWidth).coerceAtLeast(1f)
        val paint = strokePaint(stroke, widthPx)

        fun StrokePoint.toX() = x * imageWidth
        fun StrokePoint.toY() = y * imageHeight

        if (points.size == 1) {
            canvas.drawCircle(points.first().toX(), points.first().toY(), widthPx / 2, paint)
            return
        }

        if (stroke.brush == BrushType.Brush) {
            // 毛笔近似:与预览侧同一 taper 曲线,逐段变宽
            val smoothed = DrawStrokeGeometry.smoothed(points)
            val lastIndex = smoothed.size - 1
            for (i in 0 until lastIndex) {
                paint.strokeWidth = widthPx * DrawStrokeGeometry.taperFactor(i, lastIndex)
                canvas.drawLine(
                    smoothed[i].toX(), smoothed[i].toY(),
                    smoothed[i + 1].toX(), smoothed[i + 1].toY(),
                    paint
                )
            }
            return
        }

        val path = Path().apply {
            moveTo(points.first().toX(), points.first().toY())
            for (i in 1 until points.size - 1) {
                val midX = (points[i].x + points[i + 1].x) / 2f * imageWidth
                val midY = (points[i].y + points[i + 1].y) / 2f * imageHeight
                quadTo(points[i].toX(), points[i].toY(), midX, midY)
            }
            lineTo(points.last().toX(), points.last().toY())
        }
        canvas.drawPath(path, paint)
    }

    private fun strokePaint(
        stroke: DrawStroke,
        widthPx: Float,
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = widthPx
        strokeJoin = Paint.Join.ROUND
        strokeCap = if (stroke.brush == BrushType.Marker) Paint.Cap.SQUARE else Paint.Cap.ROUND
        if (stroke.brush == BrushType.Eraser) {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            color = stroke.color
            alpha = (stroke.alpha.coerceIn(0f, 1f) * 255).roundToInt()
        }
    }
}
