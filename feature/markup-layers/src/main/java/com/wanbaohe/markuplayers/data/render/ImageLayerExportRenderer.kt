package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * 图片图层导出:imageData 经 ImageGetter 解码,
 * 基础宽度 = 原图宽 × 0.4(与预览侧一致),高度按图片宽高比自适应。
 */
class ImageLayerExportRenderer @Inject constructor() : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Image::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        filtered: Bitmap?,
    ) {
        if (layer.type !is LayerType.Image) return
        val bitmap = checkNotNull(filtered) { "Image content must be decoded before export" }

        val baseWidth = imageWidth * 0.4f
        val scale = baseWidth / bitmap.width
        val halfW = baseWidth / 2
        val halfH = bitmap.height * scale / 2

        canvas.drawBitmap(
            bitmap,
            null,
            RectF(-halfW, -halfH, halfW, halfH),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }
}
