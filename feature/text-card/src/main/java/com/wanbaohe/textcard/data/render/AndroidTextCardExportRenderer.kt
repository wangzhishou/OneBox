package com.wanbaohe.textcard.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.ui.widget.modifier.PointData
import com.t8rin.imagetoolbox.core.ui.widget.modifier.drawMeshGradient
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardLayer
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 文字卡片导出:Bitmap.createBitmap(画布规格) → 依次按图层 z 序画
 * 背景(纹理/渐变/图片居中裁剪,整体乘 backgroundOpacity alpha)→ 文字(StaticLayout)→ 装饰(SVG 解码)。
 * 几何/颜色常量与预览 Compose 侧共用 [CardLayout]。
 */
class AndroidTextCardExportRenderer @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val imageGetter: ImageGetter<Bitmap>,
) : TextCardExportRenderer {

    override suspend fun render(state: TextCardRenderState): Bitmap {
        val width = state.canvas.width
        val height = state.canvas.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(CardLayout.CARD_BASE_COLOR.toInt())

        state.visibleLayers.forEach { layer ->
            when (layer) {
                is TextCardLayer.Background -> drawBackground(canvas, state, width, height)
                is TextCardLayer.Text -> drawTextBlocks(canvas, state, width, height)
                is TextCardLayer.Decoration -> drawDecoration(canvas, state, width, height)
            }
        }
        return bitmap
    }

    // ---------------- 背景层 ----------------

    private fun drawBackground(
        canvas: Canvas,
        state: TextCardRenderState,
        width: Int,
        height: Int,
    ) {
        val alpha = (state.backgroundOpacity.coerceIn(0f, 1f) * 255).toInt()
        if (alpha == 0) return
        canvas.saveLayerAlpha(
            0f, 0f, width.toFloat(), height.toFloat(), alpha
        )
        when (val background = state.background) {
            BackgroundSpec.None -> Unit
            is BackgroundSpec.Paper -> PaperTexturePainter.draw(
                canvas = canvas,
                kind = background.kind,
                width = width.toFloat(),
                height = height.toFloat()
            )

            is BackgroundSpec.Gradient -> drawGradient(canvas, background, width, height)
            is BackgroundSpec.Image -> drawImageBackground(canvas, background, width, height)
        }
        canvas.restore()
    }

    /**
     * Mesh 渐变导出:android Canvas 不支持带色顶点,走离屏 Compose 软件渲染——
     * ImageBitmap + compose Canvas 复用 core/ui 的 [PointData]/[drawMeshGradient]
     * 同一代码路径(与预览 Modifier.meshGradient 同数据同插值),再合成进导出 Canvas。
     * 背景整体透明度由外层 saveLayerAlpha 承担,这里 alpha 固定 1。
     */
    private fun drawGradient(
        canvas: Canvas,
        gradient: BackgroundSpec.Gradient,
        width: Int,
        height: Int,
    ) {
        val imageBitmap = ImageBitmap(width, height)
        val composeCanvas = androidx.compose.ui.graphics.Canvas(imageBitmap)
        composeCanvas.drawMeshGradient(
            pointData = PointData(
                points = gradient.toPointPairs(),
                stepsX = MESH_RESOLUTION,
                stepsY = MESH_RESOLUTION
            ),
            size = Size(width.toFloat(), height.toFloat()),
            alpha = 1f
        )
        canvas.drawBitmap(
            imageBitmap.asAndroidBitmap(),
            0f, 0f,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    /** 相册图片:居中裁剪铺满画布,叠加用户拖动的归一化偏移 */
    private fun drawImageBackground(
        canvas: Canvas,
        image: BackgroundSpec.Image,
        width: Int,
        height: Int,
    ) {
        val source = runBlocking {
            imageGetter.getImage(data = image.uri, size = maxOf(width, height))
        } ?: return
        val scale = maxOf(
            width.toFloat() / source.width,
            height.toFloat() / source.height
        )
        val drawWidth = source.width * scale
        val drawHeight = source.height * scale
        val shiftX = image.offsetX * width
        val shiftY = image.offsetY * height
        val dest = RectF(
            (width - drawWidth) / 2f + shiftX,
            (height - drawHeight) / 2f + shiftY,
            (width + drawWidth) / 2f + shiftX,
            (height + drawHeight) / 2f + shiftY
        )
        canvas.drawBitmap(
            source,
            null,
            dest,
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }

    // ---------------- 文字层 ----------------

    /** 两个文字块独立定位:基准位置 + 用户拖动的归一化偏移,与预览侧一致 */
    private fun drawTextBlocks(
        canvas: Canvas,
        state: TextCardRenderState,
        width: Int,
        height: Int,
    ) {
        val padding = width * CardLayout.CONTENT_PADDING_RATIO
        val contentWidth = (width - padding * 2).toInt().coerceAtLeast(1)

        drawTextBlock(
            canvas = canvas,
            block = state.title,
            baseSize = width * CardLayout.TITLE_BASE_SIZE_RATIO,
            left = padding + state.title.offsetX * width,
            top = padding + state.title.offsetY * height,
            width = contentWidth
        )
        drawTextBlock(
            canvas = canvas,
            block = state.body,
            baseSize = width * CardLayout.BODY_BASE_SIZE_RATIO,
            left = padding + state.body.offsetX * width,
            top = width * CardLayout.BODY_BASE_TOP_RATIO + state.body.offsetY * height,
            width = contentWidth
        )
    }

    /** 绘制单个文字块,返回块高度。StaticLayout 用法照搬 markup-layers。 */
    private fun drawTextBlock(
        canvas: Canvas,
        block: TextBlock,
        baseSize: Float,
        left: Float,
        top: Float,
        width: Int,
    ): Float {
        if (block.content.isBlank()) return 0f

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = block.color.toInt()
            textSize = baseSize * block.sizeScale
            typeface = resolveTypeface(block)
            letterSpacing = block.letterSpacingEm
        }
        val layout = StaticLayout.Builder
            .obtain(block.content, 0, block.content.length, paint, width)
            .setAlignment(block.alignment.toLayoutAlignment())
            .setLineSpacing(0f, block.lineSpacingMultiplier)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
        return layout.height.toFloat()
    }

    /** 粗斜处理与 markup-layers TextLayerExportRenderer 一致:Typeface.create(base, style) */
    private fun resolveTypeface(block: TextBlock): Typeface {
        val base = runCatching { block.font.toTypeface() }.getOrNull() ?: Typeface.DEFAULT
        val style = (if (block.isBold) Typeface.BOLD else 0) or
            (if (block.isItalic) Typeface.ITALIC else 0)
        return if (style != 0) Typeface.create(base, style) else base
    }

    private fun CardTextAlignment.toLayoutAlignment(): Layout.Alignment = when (this) {
        CardTextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
        CardTextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
        CardTextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
        CardTextAlignment.Justify -> Layout.Alignment.ALIGN_NORMAL
    }

    // ---------------- 装饰层 ----------------

    private fun drawDecoration(
        canvas: Canvas,
        state: TextCardRenderState,
        width: Int,
        height: Int,
    ) {
        val emojiIndex = state.decoration.emojiIndex ?: return
        val assetPath = EmojiAssets.pathAt(emojiIndex, context) ?: return
        val size = width * CardLayout.DECORATION_SIZE_RATIO

        // render 非挂起安全:decode 走 runBlocking,调用方(组件)已在 IO 线程
        val bitmap = runBlocking {
            imageGetter.getImage(
                data = "file:///android_asset/$assetPath",
                size = size.toInt().coerceAtLeast(1)
            )
        } ?: return

        val left = state.decoration.offsetX * width
        val top = state.decoration.offsetY * height
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(left, top, left + size, top + size),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
    }
}

/**
 * emoji 下标 → assets 路径。
 * 分类与排序必须与 core/resources 的 [com.t8rin.imagetoolbox.core.resources.emoji.Emoji] 完全一致,
 * 否则导出装饰与预览不符(同 markup-layers StickerLayerExportRenderer 的约定)。
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
