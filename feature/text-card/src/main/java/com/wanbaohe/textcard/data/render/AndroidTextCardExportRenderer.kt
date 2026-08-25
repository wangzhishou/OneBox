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
import com.wanbaohe.textcard.domain.model.DecorationSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.ElementTransform
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * 图文卡片导出:Bitmap.createBitmap(画布规格) → 背景(纹理/mesh 渐变/图片居中裁剪,
 * 整体乘 backgroundOpacity alpha,钉在最底)→ 按元素图层 z 序逐层画文字(StaticLayout)/
 * 装饰(SVG 解码),每层套用 offset + scale + rotation 变换(translate→rotate→scale,
 * 同 markup-layers LayerExportDispatcher 的顺序,绕内容中心)。
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

        if (state.backgroundVisible) {
            drawBackground(canvas, state, width, height)
        }
        state.visibleLayers.forEach { layer ->
            when (layer.kind) {
                ElementLayer.Kind.Text -> state.blockOf(layer.elementId)?.let { block ->
                    drawTextBlock(canvas, block, width, height)
                }

                ElementLayer.Kind.Decoration -> state.decorationOf(layer.elementId)?.let {
                    drawDecoration(canvas, it, width, height)
                }
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

    /**
     * 绘制单个文字块:基准位置(baseTopRatio)+ 归一化偏移,再绕内容中心
     * 套 scale/rotation(translate→rotate→scale,同 LayerExportDispatcher 顺序)。
     * StaticLayout 用法照搬 markup-layers。
     */
    private fun drawTextBlock(
        canvas: Canvas,
        block: TextBlock,
        width: Int,
        height: Int,
    ) {
        if (block.content.isBlank()) return

        val padding = width * CardLayout.CONTENT_PADDING_RATIO
        val contentWidth = (width - padding * 2).toInt().coerceAtLeast(1)
        val left = padding + block.offsetX * width
        val top = width * block.baseTopRatio + block.offsetY * height

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = block.color.toInt()
            textSize = width * block.baseSizeRatio * block.sizeScale
            typeface = resolveTypeface(block)
            letterSpacing = block.letterSpacingEm
        }
        val layout = StaticLayout.Builder
            .obtain(block.content, 0, block.content.length, paint, contentWidth)
            .setAlignment(block.alignment.toLayoutAlignment())
            .setLineSpacing(0f, block.lineSpacingMultiplier)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(left, top)
        canvas.applyElementTransform(block, contentWidth.toFloat(), layout.height.toFloat())
        layout.draw(canvas)
        canvas.restore()
    }

    /** 绕内容中心应用 scale/rotation(预览侧 graphicsLayer transformOrigin=Center 等价) */
    private fun Canvas.applyElementTransform(
        transform: ElementTransform,
        contentWidth: Float,
        contentHeight: Float,
    ) {
        if (transform.rotation == 0f && transform.scale == 1f) return
        val centerX = contentWidth / 2f
        val centerY = contentHeight / 2f
        translate(centerX, centerY)
        if (transform.rotation != 0f) rotate(transform.rotation)
        if (transform.scale != 1f) scale(transform.scale, transform.scale)
        translate(-centerX, -centerY)
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

    /** 单个装饰贴纸:SVG 解码后按 offset 定位,绕中心套 scale/rotation */
    private fun drawDecoration(
        canvas: Canvas,
        decoration: DecorationSpec,
        width: Int,
        height: Int,
    ) {
        val assetPath = EmojiAssets.pathAt(decoration.emojiIndex, context) ?: return
        val size = width * CardLayout.DECORATION_SIZE_RATIO

        // render 非挂起安全:decode 走 runBlocking,调用方(组件)已在 IO 线程
        val bitmap = runBlocking {
            imageGetter.getImage(
                data = "file:///android_asset/$assetPath",
                size = size.toInt().coerceAtLeast(1)
            )
        } ?: return

        val left = decoration.offsetX * width
        val top = decoration.offsetY * height
        canvas.save()
        canvas.translate(left, top)
        canvas.applyElementTransform(decoration, size, size)
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(0f, 0f, size, size),
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        )
        canvas.restore()
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
