package com.wanbaohe.textcard.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import com.t8rin.imagetoolbox.core.domain.image.ImageGetter
import com.t8rin.imagetoolbox.core.ui.widget.modifier.PointData
import com.t8rin.imagetoolbox.core.ui.widget.modifier.drawMeshGradient
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.wanbaohe.textcard.domain.TextCardExportRenderer
import com.wanbaohe.textcard.domain.model.BackgroundSpec
import com.wanbaohe.textcard.domain.model.CardShapeKind
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.DecorationSpec
import com.wanbaohe.textcard.domain.model.DrawElementSpec
import com.wanbaohe.textcard.domain.model.ElementLayer
import com.wanbaohe.textcard.domain.model.ElementTransform
import com.wanbaohe.textcard.domain.model.ImageElementSpec
import com.wanbaohe.textcard.domain.model.ImageElementStatus
import com.wanbaohe.textcard.domain.model.ShapeElementSpec
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.TextCardRenderState
import com.wanbaohe.textcard.domain.model.contentBounds
import com.wanbaohe.textcard.domain.render.CardLayout
import com.wanbaohe.textcard.domain.render.CardShapeGeometry
import com.wanbaohe.textcard.domain.render.MESH_RESOLUTION
import com.wanbaohe.textcard.domain.render.toPointPairs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Inject
import kotlin.math.ceil

/**
 * 图文卡片导出:Bitmap.createBitmap(画布规格) → 背景(纹理/mesh 渐变/图片居中裁剪,
 * 整体乘 backgroundOpacity alpha,钉在最底)→ 按元素图层 z 序逐层画文字(StaticLayout)/
 * 装饰(SVG 解码)/AI 图片(fit 居中),每层套用 offset + scale + rotation 变换
 * (translate→rotate→scale,同 markup-layers LayerExportDispatcher 的顺序,绕内容中心)。
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

                ElementLayer.Kind.Image -> state.imageElementOf(layer.elementId)?.let {
                    drawImageElement(canvas, it, width, height)
                }

                ElementLayer.Kind.Shape -> state.shapeElementOf(layer.elementId)?.let {
                    drawShapeElement(canvas, it, width, height)
                }

                ElementLayer.Kind.Draw -> state.drawElementOf(layer.elementId)?.let {
                    drawDrawElement(canvas, it, width, height)
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

    /** 相册图片:居中裁剪铺满画布 + 过扫放大(与预览侧同步),叠加用户拖动的归一化偏移 */
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
        ) * CardLayout.BACKGROUND_IMAGE_OVERSCAN
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
     * 块内局部样式(styleSpans)以 SpannableString span 叠加在块级 paint 之上;
     * 块级底色 = 逐行矩形(预览 TextStyle.background 同款高亮;不走 span,
     * 否则 layout.draw 内的背景会盖住先画的描边层);
     * 描边 = STROKE 画笔的同宽 StaticLayout 画在填充层之下(同 markup-layers TextLayerExportRenderer);
     * 下划线/删除线 = paint flags。
     */
    private fun drawTextBlock(
        canvas: Canvas,
        block: TextBlock,
        width: Int,
        height: Int,
    ) {
        if (block.content.isBlank()) return

        val text = block.exportText()
        val padding = width * CardLayout.CONTENT_PADDING_RATIO
        // 框宽上限 = widthRatio·画布宽(文字在框内折行),与预览侧 CardTextElement 一致
        val maxContentWidth = (width * block.widthRatio).toInt().coerceAtLeast(1)
        val left = padding + block.offsetX * width
        val top = width * block.baseTopRatio + block.offsetY * height

        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = block.color.toInt()
            // 元素级不透明度:与颜色自身 alpha 叠乘
            alpha = (block.alpha.coerceIn(0f, 1f) * 255).toInt().let { elementAlpha ->
                (android.graphics.Color.alpha(block.color.toInt()) * elementAlpha / 255)
            }
            textSize = width * block.baseSizeRatio * block.sizeScale
            typeface = resolveTypeface(block)
            letterSpacing = block.letterSpacingEm
            if (block.isUnderline) flags = flags or Paint.UNDERLINE_TEXT_FLAG
            if (block.isStrikethrough) flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        fun buildLayout(p: TextPaint, layoutWidth: Int): StaticLayout = StaticLayout.Builder
            .obtain(text, 0, text.length, p, layoutWidth)
            .setAlignment(block.alignment.toLayoutAlignment())
            .setLineSpacing(0f, block.lineSpacingMultiplier)
            .setIncludePad(false)
            .build()

        // 未手动拖宽的块预览侧框随内容收缩(wrap):先按上限宽折行,取各行最大宽度
        // 作为实际内容宽重建布局,定位与变换中心与预览一致;手动拖宽块直接用设定框宽
        var layoutWidthUsed = maxContentWidth
        var layout = buildLayout(paint, layoutWidthUsed)
        val contentWidth = if (block.widthManuallySet) {
            maxContentWidth
        } else {
            val maxLineWidth = (0 until layout.lineCount).maxOf { layout.getLineWidth(it) }
            val wrappedWidth = ceil(maxLineWidth).toInt().coerceAtLeast(1)
            if (wrappedWidth < maxContentWidth) {
                layoutWidthUsed = wrappedWidth
                layout = buildLayout(paint, layoutWidthUsed)
            }
            wrappedWidth
        }

        // 变换中心 = 框中心:框高 = max(内容高, 设定最小高),与预览 heightIn(min) 一致
        val boxHeight = maxOf(
            layout.height.toFloat(),
            block.heightRatio * height
        )
        canvas.save()
        canvas.translate(left, top)
        canvas.applyElementTransform(block, contentWidth.toFloat(), boxHeight)
        // 逐行底色矩形(先于描边/填充;行矩形 = 该行文字实际排布范围,含行距高度)
        if (block.backgroundColor != 0L) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = block.backgroundColor.toInt()
            }
            for (line in 0 until layout.lineCount) {
                canvas.drawRect(
                    layout.getLineLeft(line),
                    layout.getLineTop(line).toFloat(),
                    layout.getLineRight(line),
                    layout.getLineBottom(line).toFloat(),
                    bgPaint
                )
            }
        }
        // 描边:同布局宽度重建 STROKE 版垫底(线宽/折行与填充层一致),再画填充层
        if (block.outlineWidthRatio > 0f) {
            val strokePaint = TextPaint(paint).apply {
                color = block.outlineColor.toInt()
                style = Paint.Style.STROKE
                strokeWidth = block.outlineWidthRatio * width
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            buildLayout(strokePaint, layoutWidthUsed).draw(canvas)
        }
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

    /**
     * 局部样式区间 → SpannableString(无局部样式直接返回原文):
     * 颜色用 ForegroundColorSpan;字号倍率用 RelativeSizeSpan;粗斜合成一个 StyleSpan
     * (未设置的字段继承块级,故块粗体下 bold=false 的区间经 StyleSpan(NORMAL) 反粗),
     * 区间钳制到内容长度。
     * 注意块级底色不走 BackgroundColorSpan:span 在 layout.draw 内绘制,会盖住先画的描边层;
     * 底色改由 drawTextBlock 按行手动画矩形(在描边层之前)。
     */
    private fun TextBlock.exportText(): CharSequence {
        if (styleSpans.isEmpty()) return content
        val spannable = SpannableString(content)
        styleSpans.forEach { span ->
            val clamped = span.clamped(content.length) ?: return@forEach
            span.color?.let { color ->
                spannable.setSpan(
                    ForegroundColorSpan(color.toInt()),
                    clamped.start,
                    clamped.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            span.sizeScale?.let { scale ->
                spannable.setSpan(
                    RelativeSizeSpan(scale),
                    clamped.start,
                    clamped.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            if (span.bold != null || span.italic != null) {
                val effectiveBold = span.bold ?: isBold
                val effectiveItalic = span.italic ?: isItalic
                val style = (if (effectiveBold) Typeface.BOLD else 0) or
                    (if (effectiveItalic) Typeface.ITALIC else 0)
                spannable.setSpan(
                    StyleSpan(style),
                    clamped.start,
                    clamped.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return spannable
    }

    private fun CardTextAlignment.toLayoutAlignment(): Layout.Alignment = when (this) {
        CardTextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
        CardTextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
        CardTextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
        CardTextAlignment.Justify -> Layout.Alignment.ALIGN_NORMAL
    }

    // ---------------- 装饰层 ----------------

    /**
     * 单个 AI 图片元素:fullCanvas = cover 铺满整张画布(与预览 ContentScale.Crop 一致,
     * 绕画布中心变换);否则 fit 居中进 IMAGE_ELEMENT_SIZE_RATIO 正方形框(绕框中心)。
     * 仅导出 Ready 图层(Loading/Error 是编辑态占位,不进成图);
     * 加载失败静默跳过,不阻断整体导出。
     */
    private fun drawImageElement(
        canvas: Canvas,
        element: ImageElementSpec,
        width: Int,
        height: Int,
    ) {
        if (element.status != ImageElementStatus.Ready || element.uri.isBlank()) return
        val size = width * CardLayout.IMAGE_ELEMENT_SIZE_RATIO
        val bitmap = runBlocking {
            imageGetter.getImage(
                data = element.uri,
                size = if (element.fullCanvas) {
                    maxOf(width, height)
                } else size.toInt().coerceAtLeast(1)
            )
        } ?: return

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (element.alpha.coerceIn(0f, 1f) * 255).toInt()
        }
        if (element.fullCanvas) {
            // cover:等比放大到铺满,居中裁掉超出部分
            val coverScale = maxOf(
                width / bitmap.width.toFloat(),
                height / bitmap.height.toFloat()
            )
            val drawWidth = bitmap.width * coverScale
            val drawHeight = bitmap.height * coverScale
            canvas.save()
            canvas.translate(element.offsetX * width, element.offsetY * height)
            canvas.applyElementTransform(element, width.toFloat(), height.toFloat())
            canvas.drawBitmap(
                bitmap,
                null,
                RectF(
                    (width - drawWidth) / 2f,
                    (height - drawHeight) / 2f,
                    (width + drawWidth) / 2f,
                    (height + drawHeight) / 2f
                ),
                paint
            )
            canvas.restore()
            return
        }

        // fit 居中:保持比例放进正方形框
        val fitScale = minOf(size / bitmap.width, size / bitmap.height)
        val drawWidth = bitmap.width * fitScale
        val drawHeight = bitmap.height * fitScale
        val imageLeft = (size - drawWidth) / 2f
        val imageTop = (size - drawHeight) / 2f

        val left = element.offsetX * width
        val top = element.offsetY * height
        canvas.save()
        canvas.translate(left, top)
        canvas.applyElementTransform(element, size, size)
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(imageLeft, imageTop, imageLeft + drawWidth, imageTop + drawHeight),
            paint
        )
        canvas.restore()
    }

    /** 单个装饰贴纸:SVG/生成图解码后按 offset 定位,绕中心套 scale/rotation */
    private fun drawDecoration(
        canvas: Canvas,
        decoration: DecorationSpec,
        width: Int,
        height: Int,
    ) {
        // AI 生成贴纸(本地文件)> 素材贴纸(assets SVG)> emoji 下标换算 assets 路径
        val data: Any = when {
            decoration.imagePath != null -> File(decoration.imagePath)
            decoration.assetPath != null -> "file:///android_asset/${decoration.assetPath}"
            else -> decoration.emojiIndex
                ?.let { EmojiAssets.pathAt(it, context) }
                ?.let { "file:///android_asset/$it" }
                ?: return
        }
        val size = width * CardLayout.DECORATION_SIZE_RATIO

        // render 非挂起安全:decode 走 runBlocking,调用方(组件)已在 IO 线程
        val bitmap = runBlocking {
            imageGetter.getImage(
                data = data,
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
            Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                alpha = (decoration.alpha.coerceIn(0f, 1f) * 255).toInt()
            }
        )
        canvas.restore()
    }

    // ---------------- 形状元素(几何与预览侧 CardShapeGeometry 共用) ----------------

    /** 单个形状:外接框 offset 定位,绕中心套 scale/rotation */
    private fun drawShapeElement(
        canvas: Canvas,
        element: ShapeElementSpec,
        width: Int,
        height: Int,
    ) {
        val boxWidth = element.widthRatio * width
        val boxHeight = element.heightRatio * height
        canvas.save()
        canvas.translate(element.offsetX * width, element.offsetY * height)
        canvas.applyElementTransform(element, boxWidth, boxHeight)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = element.colorArgb.toInt()
            alpha = (element.alpha.coerceIn(0f, 1f) * 255).toInt()
        }
        val strokeWidth = (element.strokeWidthRatio * width).coerceAtLeast(1f)
        when (element.kind) {
            CardShapeKind.Rectangle -> {
                val radius = (element.cornerRadiusRatio * width)
                    .coerceIn(0f, minOf(boxWidth, boxHeight) / 2f)
                paint.style = if (element.filled) Paint.Style.FILL else Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                canvas.drawRoundRect(RectF(0f, 0f, boxWidth, boxHeight), radius, radius, paint)
            }

            CardShapeKind.Circle -> {
                // 正圆:直径取外接框短边,居中(与预览一致)
                val diameter = minOf(boxWidth, boxHeight)
                paint.style = if (element.filled) Paint.Style.FILL else Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                canvas.drawOval(
                    RectF(
                        (boxWidth - diameter) / 2f,
                        (boxHeight - diameter) / 2f,
                        (boxWidth + diameter) / 2f,
                        (boxHeight + diameter) / 2f
                    ),
                    paint
                )
            }

            CardShapeKind.Line -> {
                // 线条无填充概念,圆头横线与外接框同宽(与预览一致)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                paint.strokeCap = Paint.Cap.ROUND
                canvas.drawLine(0f, boxHeight / 2f, boxWidth, boxHeight / 2f, paint)
            }

            CardShapeKind.Triangle, CardShapeKind.Arrow, CardShapeKind.Star -> {
                val path = android.graphics.Path()
                CardShapeGeometry.polygonPoints(element.kind, boxWidth, boxHeight)
                    .forEachIndexed { index, (x, y) ->
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                path.close()
                paint.style = if (element.filled) Paint.Style.FILL else Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                paint.strokeJoin = Paint.Join.ROUND
                canvas.drawPath(path, paint)
            }
        }
        canvas.restore()
    }

    // ---------------- 画笔元素 ----------------

    /** 画笔元素:外接框 = 笔画包围盒(与预览 chrome 一致),逐笔画圆头圆接折线 */
    private fun drawDrawElement(
        canvas: Canvas,
        element: DrawElementSpec,
        width: Int,
        height: Int,
    ) {
        val bounds = element.contentBounds() ?: return
        canvas.save()
        canvas.translate(
            (element.offsetX + bounds.left) * width,
            (element.offsetY + bounds.top) * height
        )
        canvas.applyElementTransform(element, bounds.width * width, bounds.height * height)
        element.strokes.forEach { stroke ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stroke.colorArgb.toInt()
                alpha = (element.alpha.coerceIn(0f, 1f) * 255).toInt()
                style = Paint.Style.STROKE
                strokeWidth = (stroke.widthRatio * width).coerceAtLeast(1f)
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            if (stroke.points.size == 1) {
                val p = stroke.points.first()
                canvas.drawPoint(
                    (p.x - bounds.left) * width,
                    (p.y - bounds.top) * height,
                    paint
                )
            } else {
                val path = android.graphics.Path()
                stroke.points.forEachIndexed { index, point ->
                    val x = (point.x - bounds.left) * width
                    val y = (point.y - bounds.top) * height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                canvas.drawPath(path, paint)
            }
        }
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
