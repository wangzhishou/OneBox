package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.wanbaohe.markuplayers.domain.model.LayerBlendMode
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import com.wanbaohe.markuplayers.presentation.tools.adjust.toColorMatrixValues
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * 导出渲染调度器:统一处理图层的定位/缩放/旋转/不透明度/可见性与调色,
 * 再把"以内容中心为原点"的画布交给具体渲染器绘制内容。
 * 位图内容先由 applier 解码并处理效果/蒙版,[filteredLayers] 是已处理的局部内容,
 * 不是含定位/调色的画布截图。缺少所需处理结果会显式失败。
 * 新增图层类型时在构造参数里加一行对应渲染器即可。
 */
@Singleton
class LayerExportDispatcher @Inject constructor(
    textRenderer: TextLayerExportRenderer,
    stickerRenderer: StickerLayerExportRenderer,
    imageRenderer: ImageLayerExportRenderer,
    drawRenderer: DrawLayerExportRenderer,
    shapeRenderer: ShapeLayerExportRenderer,
) {

    private val renderers: List<LayerExportRenderer> = listOf(
        textRenderer,
        stickerRenderer,
        imageRenderer,
        drawRenderer,
        shapeRenderer,
    )

    fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        filteredLayers: Map<String, Bitmap> = emptyMap(),
    ) {
        val transform = layer.transform
        if (!transform.visible) return
        require(imageWidth > 0 && imageHeight > 0) { "Export dimensions must be positive" }
        require(
            transform.centerX.isFinite() && transform.centerY.isFinite() &&
                transform.rotation.isFinite() && transform.scale.isFinite() &&
                transform.scale > 0f && transform.alpha.isFinite()
        ) { "Layer transform must have finite coordinates, alpha and positive scale" }
        val renderer = checkNotNull(renderers.firstOrNull {
            it.supportedType.isInstance(layer.type)
        }) { "No export renderer for ${layer.type::class.simpleName}" }
        val filtered = filteredLayers[layer.id]
        check(!layer.requiresBitmapProcessing || filtered != null) {
            "Missing processed bitmap for layer ${layer.id}"
        }

        canvas.save()
        try {
            canvas.translate(
                transform.centerX * imageWidth,
                transform.centerY * imageHeight
            )
            if (transform.rotation != 0f) canvas.rotate(transform.rotation)
            canvas.scale(
                transform.scale * if (transform.flipHorizontal) -1f else 1f,
                transform.scale * if (transform.flipVertical) -1f else 1f
            )

            val layerPaint = Paint().apply {
                alpha = (transform.alpha.coerceIn(0f, 1f) * 255).roundToInt()
                setLayerBlendMode(transform.blendMode)
                if (!layer.adjustments.isNeutral) {
                    colorFilter = ColorMatrixColorFilter(
                        ColorMatrix(layer.adjustments.toColorMatrixValues())
                    )
                }
            }
            if (transform.alpha < 1f || !layer.adjustments.isNeutral ||
                transform.blendMode != LayerBlendMode.Normal
            ) {
                canvas.saveLayer(null, layerPaint)
                try {
                    renderer.draw(canvas, layer, imageWidth, imageHeight, filtered)
                } finally {
                    canvas.restore()
                }
            } else {
                renderer.draw(canvas, layer, imageWidth, imageHeight, filtered)
            }
        } finally {
            canvas.restore()
        }
    }
}
