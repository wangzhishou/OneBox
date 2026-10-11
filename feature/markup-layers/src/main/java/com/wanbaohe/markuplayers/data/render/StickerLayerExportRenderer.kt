package com.wanbaohe.markuplayers.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.t8rin.imagetoolbox.core.ui.widget.editor.StickerSource
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * 贴纸图层导出:emoji 下标换算为 assets SVG 路径、素材贴纸取 assets 路径、
 * AI 生成贴纸读本地文件,统一经 coil 解码绘制。
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
        filtered: Bitmap?,
    ) {
        if (layer.type !is LayerType.Sticker) return
        val baseSize = imageWidth * STICKER_EXPORT_BASE_RATIO
        val bitmap = checkNotNull(filtered) { "Sticker content must be decoded before export" }

        // 保持贴纸宽高比(与预览侧 ContentScale.Fit 一致):长边对齐 baseSize,中心不变
        drawCentered(canvas, bitmap, baseSize)
    }

    /**
     * 解码贴纸内容位图(emoji/素材贴纸走 assets,AI 生成贴纸读本地文件),
     * 长边对齐 [baseSizePx]。导出渲染与图层滤镜内容提取(组件侧)共用,保证同源。
     */
    suspend fun decodeBitmap(
        type: LayerType.Sticker,
        baseSizePx: Float,
    ): Bitmap? {
        require(baseSizePx.isFinite() && baseSizePx > 0f) { "Sticker decode size must be positive" }
        val data: Any = when (val source = type.source) {
            is StickerSource.Emoji -> EmojiAssets.pathAt(source.emojiIndex, context)
                ?.let { "file:///android_asset/$it" }
                ?: return null

            is StickerSource.Asset -> "file:///android_asset/${source.path}"
            is StickerSource.Generated -> File(source.path)
        }
        return imageGetter.getImage(
            data = data,
            size = baseSizePx.toInt().coerceAtLeast(1)
        )
    }

    private fun drawCentered(
        canvas: Canvas,
        bitmap: Bitmap,
        baseSize: Float,
    ) {
        val half = baseSize / 2
        val aspect = bitmap.width.toFloat() / bitmap.height
        val halfW: Float
        val halfH: Float
        if (aspect >= 1f) {
            halfW = half
            halfH = half / aspect
        } else {
            halfH = half
            halfW = half * aspect
        }
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(-halfW, -halfH, halfW, halfH),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }
}

/** 贴纸导出基础尺寸:原图宽 × 该比例(与预览侧 0.25 × 画布宽一致) */
internal const val STICKER_EXPORT_BASE_RATIO = 0.25f

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
