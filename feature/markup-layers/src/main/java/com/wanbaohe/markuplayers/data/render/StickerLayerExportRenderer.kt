package com.wanbaohe.markuplayers.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.model.StickerSource
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * 贴纸图层导出:emoji 下标换算为 assets SVG 路径后经 coil 解码绘制。
 * 基础尺寸 = 原图宽 × [STICKER_EXPORT_BASE_RATIO](与预览侧 0.25 × 画布宽一致)。
 */
class StickerLayerExportRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap>,
) : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Sticker::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
    ) {
        val type = layer.type as? LayerType.Sticker ?: return
        val assetPath = when (val source = type.source) {
            is StickerSource.Emoji -> EmojiAssets.pathAt(source.emojiIndex, context)
            is StickerSource.Asset -> source.path
        } ?: return

        val baseSize = imageWidth * STICKER_EXPORT_BASE_RATIO
        // draw 非挂起函数,解码走 runBlocking;调用方(applier)已在 IO 线程
        val bitmap = runBlocking {
            imageGetter.getImage(
                data = "file:///android_asset/$assetPath",
                size = baseSize.toInt().coerceAtLeast(1)
            )
        } ?: return

        val half = baseSize / 2
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(-half, -half, half, half),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }
}

private const val STICKER_EXPORT_BASE_RATIO = 0.25f

/**
 * emoji 下标 → assets 路径。
 * 分类与排序必须与 core/resources 的 [com.t8rin.imagetoolbox.core.resources.emoji.Emoji] 完全一致,
 * 否则导出贴纸与预览不符。
 */
private object EmojiAssets {

    private val categories = listOf(
        "emotions", "food", "nature", "objects", "events", "transportation", "symbols"
    )

    private var cached: List<String>? = null

    @Synchronized
    fun pathAt(index: Int, context: Context): String? {
        val files = cached ?: categories.flatMap { category ->
            context.assets.list("svg/$category")
                ?.sortedWith(String.CASE_INSENSITIVE_ORDER)
                ?.map { "svg/$category/$it" }
                ?: emptyList()
        }.also { cached = it }
        return files.getOrNull(index)
    }
}
