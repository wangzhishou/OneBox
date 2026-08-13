package com.wanbaohe.markuplayers.data.render

import android.graphics.Canvas
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * 导出渲染调度器:统一处理图层的定位/缩放/旋转/不透明度/可见性,
 * 再把"以内容中心为原点"的画布交给具体渲染器绘制内容。
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
    ) {
        val transform = layer.transform
        if (!transform.visible) return
        val renderer = renderers.firstOrNull {
            it.supportedType.isInstance(layer.type)
        } ?: return

        canvas.save()
        canvas.translate(
            transform.centerX * imageWidth,
            transform.centerY * imageHeight
        )
        if (transform.rotation != 0f) canvas.rotate(transform.rotation)
        if (transform.scale != 1f) canvas.scale(transform.scale, transform.scale)

        if (transform.alpha < 1f) {
            val alpha = (transform.alpha.coerceIn(0f, 1f) * 255).roundToInt()
            canvas.saveLayerAlpha(null, alpha)
            renderer.draw(canvas, layer, imageWidth, imageHeight)
            canvas.restore()
        } else {
            renderer.draw(canvas, layer, imageWidth, imageHeight)
        }

        canvas.restore()
    }
}
