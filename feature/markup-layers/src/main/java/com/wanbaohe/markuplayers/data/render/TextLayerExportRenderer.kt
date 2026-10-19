package com.wanbaohe.markuplayers.data.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.content.res.ResourcesCompat
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.reflect.KClass

/**
 * 文字图层导出:字号 = fontSizeRatio × 原图宽,StaticLayout 多行绘制。
 * 与 [com.wanbaohe.markuplayers.presentation.render.TextLayerPreviewRenderer] 同构:
 * 文字块带 fontSize/6 横向、fontSize/10 纵向 padding,折行宽度 = 框宽 − 横向 padding;
 * widthManuallySet(拖过框尺寸手柄)= widthRatio 固定框宽;否则自适应收紧到最长行
 * (上限 (widthRatio ?: 1) × 图宽,超宽自动折行)。
 * 内容块(含 padding)中心对齐画布原点(定位/缩放/旋转由调度器处理)。
 */
class TextLayerExportRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
) : LayerExportRenderer {

    override val supportedType: KClass<out LayerType> = LayerType.Text::class

    override fun draw(
        canvas: Canvas,
        layer: MarkupLayer,
        imageWidth: Int,
        imageHeight: Int,
        filtered: Bitmap?,
    ) {
        val type = layer.type as? LayerType.Text ?: return
        if (type.text.isBlank()) return

        val decorations = type.decorations
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = type.color
            textSize = type.fontSizeRatio * imageWidth
            typeface = resolveTypeface(type.font, decorations)
            letterSpacing = type.letterSpacingEm
            if (LayerType.Text.Decoration.Underline in decorations) {
                flags = flags or Paint.UNDERLINE_TEXT_FLAG
            }
            if (LayerType.Text.Decoration.LineThrough in decorations) {
                flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        // 与预览 padding 一致:背景/块尺寸含 padding,文字在框内折行
        // (fontSize/6 横向、fontSize/10 纵向——过大留白会把选框/手柄顶出画布)
        val fontSizePx = type.fontSizeRatio * imageWidth
        val hPad = fontSizePx / 6f
        val vPad = fontSizePx / 10f

        // 框宽上限 = (widthRatio ?: 1) × 图宽;固定框宽态(拖过框尺寸手柄)直接占满,
        // 自适应(默认)短文收紧到最长行——选框/导出均贴合文字(对齐图文卡片)
        val fixedWidth = type.widthManuallySet && type.widthRatio != null
        val boxWidth = ((type.widthRatio ?: 1f) * imageWidth).coerceAtLeast(1f)
        val layoutWidth = (boxWidth - 2f * hPad).roundToInt().coerceAtLeast(1)

        val alignment = when (type.alignment) {
            LayerType.Text.TextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
            LayerType.Text.TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
            LayerType.Text.TextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
            LayerType.Text.TextAlignment.Justify -> Layout.Alignment.ALIGN_NORMAL
        }

        val exportText = type.exportText()
        fun buildLayout(p: TextPaint, width: Int): StaticLayout = StaticLayout.Builder
            .obtain(exportText, 0, exportText.length, p, width)
            .setAlignment(alignment)
            .setLineSpacing(0f, type.lineHeight)
            .setIncludePad(false)
            .build()

        val probe = buildLayout(paint, layoutWidth)
        // 固定框宽:直接用设定框宽;自适应:按折行后最长行收紧(与 Compose hug 一致),
        // 再重建以保证行内对齐正确。
        //
        // 关键:收紧宽度必须留 2px 余量。StaticLayout 的 getLineWidth 是浮点值,
        // 直接 round 成整数再重建时可能比实际行宽小零点几像素,导致最后一行被挤到下一行——
        // 编辑器/预览(Compose 按自然宽度一次测量)不会折行,导出却折行,正是这个原因。
        var contentWidth: Int
        val textWidth: Int
        if (fixedWidth) {
            contentWidth = boxWidth.roundToInt().coerceAtLeast(1)
            textWidth = (contentWidth - 2f * hPad).roundToInt().coerceAtLeast(1)
        } else {
            var maxLine = 0f
            for (i in 0 until probe.lineCount) {
                maxLine = max(maxLine, probe.getLineWidth(i))
            }
            // +2px:吸收浮点行宽与整数布局宽度的误差,避免"差 0.125px 就换行"
            val huggedWidth = (ceil(maxLine).toInt() + 2).coerceIn(1, layoutWidth)
            textWidth = huggedWidth
            contentWidth = (huggedWidth + 2f * hPad)
                .roundToInt()
                .coerceAtMost(boxWidth.roundToInt())
                .coerceAtLeast(1)
        }
        var layout = if (textWidth == layoutWidth) probe else buildLayout(paint, textWidth)
        // 防御:仍出现比 probe 更多的行(极端字体/字距),回退到 probe 的折行结果,
        // 宁可块宽略大,也不让导出与预览行数不一致。
        if (layout.lineCount > probe.lineCount) {
            layout = probe
            contentWidth = boxWidth.roundToInt().coerceAtLeast(1)
        }
        val contentHeight = layout.height + 2f * vPad

        canvas.save()
        canvas.translate(-contentWidth / 2f, -contentHeight / 2f)

        if (type.backgroundColor != 0) {
            canvas.drawRect(
                0f, 0f,
                contentWidth.toFloat(), contentHeight.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = type.backgroundColor }
            )
        }

        canvas.save()
        canvas.translate(hPad, vPad)
        type.outline?.let { outline ->
            val strokePaint = TextPaint(paint).apply {
                color = outline.color
                style = Paint.Style.STROKE
                strokeWidth = outline.width * imageWidth
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            buildLayout(strokePaint, textWidth).draw(canvas)
        }
        layout.draw(canvas)
        canvas.restore()
        canvas.restore()
    }

    /**
     * 局部样式区间 → SpannableString(无局部样式直接返回原文):
     * 颜色 ForegroundColorSpan;字号倍率 RelativeSizeSpan;粗斜合成一个 StyleSpan
     * (未设置的字段继承图层级 decorations,故图层粗体下 bold=false 的区间可反粗)。
     * 与图文卡片 AndroidTextCardExportRenderer.exportText 同构,保证预览/导出一致。
     */
    private fun LayerType.Text.exportText(): CharSequence {
        if (styleSpans.isEmpty()) return text
        val spannable = SpannableString(text)
        styleSpans.forEach { span ->
            val clamped = span.clamped(text.length) ?: return@forEach
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
                val effectiveBold = span.bold ?: (LayerType.Text.Decoration.Bold in decorations)
                val effectiveItalic =
                    span.italic ?: (LayerType.Text.Decoration.Italic in decorations)
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

    private fun resolveTypeface(
        font: FontType?,
        decorations: Set<LayerType.Text.Decoration>,
    ): Typeface {
        val base = when (font) {
            is FontType.Resource -> runCatching {
                ResourcesCompat.getFont(context, font.resId)
            }.getOrNull()

            is FontType.File -> runCatching {
                Typeface.createFromFile(font.path)
            }.getOrNull()

            null -> null
        } ?: Typeface.DEFAULT

        val bold = LayerType.Text.Decoration.Bold in decorations
        val italic = LayerType.Text.Decoration.Italic in decorations
        val style = when {
            bold && italic -> Typeface.BOLD_ITALIC
            bold -> Typeface.BOLD
            italic -> Typeface.ITALIC
            else -> null
        }

        return style?.let { Typeface.create(base, it) } ?: base
    }
}
