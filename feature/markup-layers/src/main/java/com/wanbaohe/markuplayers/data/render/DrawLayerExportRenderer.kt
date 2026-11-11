package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.wanbaohe.core.ui.widget.editor.renderEditorStrokesBitmap
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import kotlin.reflect.KClass

class DrawLayerExportRenderer @Inject constructor() : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Draw::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        filtered: Bitmap?,
    ) {
        val type = layer.type as? LayerType.Draw ?: return
        if ((type.strokes.isEmpty() && filtered == null) || imageWidth <= 0 || imageHeight <= 0) return
        val layerBitmap = filtered ?: renderStrokesBitmap(type.strokes, imageWidth, imageHeight)
        canvas.drawBitmap(
            layerBitmap,
            null,
            RectF(-imageWidth / 2f, -imageHeight / 2f, imageWidth / 2f, imageHeight / 2f),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    fun renderStrokesBitmap(
        strokes: List<DrawStroke>,
        imageWidth: Int,
        imageHeight: Int,
    ): Bitmap = renderEditorStrokesBitmap(strokes, imageWidth, imageHeight)
}
