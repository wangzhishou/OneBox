package com.wanbaohe.markuplayers.data.render

import android.graphics.Bitmap
import android.graphics.Canvas
import com.wanbaohe.core.ui.widget.editor.drawEditorShape
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import javax.inject.Inject
import kotlin.reflect.KClass

class ShapeLayerExportRenderer @Inject constructor() : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Shape::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        filtered: Bitmap?,
    ) {
        val type = layer.type as? LayerType.Shape ?: return
        drawEditorShape(canvas, type.spec, imageWidth.toFloat(), imageHeight.toFloat())
    }
}
