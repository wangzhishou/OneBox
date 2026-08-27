package com.wanbaohe.markuplayers.presentation.render

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import com.t8rin.imagetoolbox.core.resources.emoji.Emoji
import com.t8rin.imagetoolbox.core.ui.widget.image.Picture
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import kotlin.reflect.KClass

/** 贴纸基础尺寸:预览画布宽度的 25%(导出侧同比例 × 原图宽) */
internal const val STICKER_BASE_WIDTH_RATIO = 0.25f

/** 贴纸图层预览:emoji 走 core emoji 表(assets SVG),Asset 读 assets 位图 */
object StickerLayerPreviewRenderer : LayerPreviewRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Sticker::class

    @Composable
    override fun Content(
        layer: MarkupLayer,
        canvasWidthPx: Float,
        canvasHeightPx: Float,
    ) {
        val type = layer.type as? LayerType.Sticker ?: return
        val baseSize = with(LocalDensity.current) {
            (canvasWidthPx * STICKER_BASE_WIDTH_RATIO).toDp()
        }
        val model: Any = when (val source = type.source) {
            is StickerSource.Emoji -> Emoji.allIcons().getOrNull(source.emojiIndex) ?: return
            is StickerSource.Asset -> "file:///android_asset/${source.path}"
        }
        Picture(
            model = model,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            showTransparencyChecker = false,
            modifier = Modifier.size(baseSize)
        )
    }
}
