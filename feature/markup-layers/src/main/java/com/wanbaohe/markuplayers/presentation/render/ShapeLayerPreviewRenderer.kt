package com.wanbaohe.markuplayers.presentation.render

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wanbaohe.core.ui.widget.editor.drawEditorShape
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.ShapeSpec
import kotlin.reflect.KClass

object ShapeLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Shape::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
        filteredBitmap: Bitmap?,
    ) {
        val spec = (layer.type as? LayerType.Shape)?.spec ?: return
        val density = LocalDensity.current
        Canvas(
            modifier = Modifier
                .padding(12.dp)
                .size(
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

internal fun DrawScope.drawShapeContent(
    spec: ShapeSpec,
    strokeWidthPx: Float,
    cornerRadiusPx: Float,
) = drawEditorShape(spec, strokeWidthPx, cornerRadiusPx)
