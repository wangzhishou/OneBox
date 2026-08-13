package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil3.request.ImageRequest
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import kotlin.reflect.KClass

/** 图片图层基础宽度:预览画布宽度的 40%(导出侧同比例 × 原图宽) */
internal const val IMAGE_LAYER_BASE_WIDTH_RATIO = 0.4f

/** 图片图层预览:imageData 为 Uri 等图片引用,宽按基础宽度、高按比例自适应 */
object ImageLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Image::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
    ) {
        val type = layer.type as? LayerType.Image ?: return
        val baseWidth = with(LocalDensity.current) {
            (canvasWidthPx * IMAGE_LAYER_BASE_WIDTH_RATIO).toDp()
        }
        Picture(
            model = ImageRequest.Builder(LocalContext.current)
                .data(type.imageData)
                .size(1600)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            showTransparencyChecker = false,
            modifier = Modifier.width(baseWidth)
        )
    }
}
