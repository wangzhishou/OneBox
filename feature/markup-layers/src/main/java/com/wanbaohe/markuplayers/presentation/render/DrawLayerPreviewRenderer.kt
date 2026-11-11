package com.wanbaohe.markuplayers.presentation.render

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.wanbaohe.core.ui.widget.editor.drawEditorStroke
import com.wanbaohe.markuplayers.domain.model.DrawStroke
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import kotlin.math.roundToInt
import kotlin.reflect.KClass

object DrawLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Draw::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
        filteredBitmap: Bitmap?,
    ) {
        val type = layer.type as? LayerType.Draw ?: return
        if (layer.requiresBitmapProcessing && filteredBitmap == null) return
        if (type.strokes.isEmpty() && filteredBitmap == null) return
        val density = LocalDensity.current
        Canvas(
            modifier = Modifier
                .size(
                    width = with(density) { canvasWidthPx.toDp() },
                    height = with(density) { canvasHeightPx.toDp() }
                )
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            if (filteredBitmap != null) {
                drawImage(
                    image = filteredBitmap.asImageBitmap(),
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(filteredBitmap.width, filteredBitmap.height),
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    filterQuality = FilterQuality.Medium
                )
            } else {
                type.strokes.forEach { stroke -> drawStroke(stroke, size.width, size.height) }
            }
        }
    }
}

internal fun DrawScope.drawStroke(
    stroke: DrawStroke,
    canvasWidthPx: Float,
    canvasHeightPx: Float,
) = drawEditorStroke(stroke, canvasWidthPx, canvasHeightPx)
