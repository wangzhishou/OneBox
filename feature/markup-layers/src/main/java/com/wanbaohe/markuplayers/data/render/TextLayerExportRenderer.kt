package com.wanbaohe.markuplayers.data.render

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.wanbaohe.markuplayers.domain.model.LayerType
import com.wanbaohe.markuplayers.domain.model.MarkupLayer
import com.wanbaohe.markuplayers.domain.render.LayerExportRenderer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.reflect.KClass

/**
 * 文字图层导出:字号 = fontSizeRatio × 原图宽,StaticLayout 多行绘制。
 * 内容中心对齐画布原点(定位/缩放/旋转由调度器处理)。
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

        // 以最长行的实测宽度作为布局宽,保证内容块紧贴文字(居中以内容为准);
        // 有 widthRatio 时按框宽折行(拖边/角改框,文字重排,字号不变)
        val contentWidth = type.widthRatio?.let {
            (it * imageWidth).roundToInt().coerceIn(1, imageWidth)
        } ?: type.text.split('\n')
            .maxOf { paint.measureText(it) }
            .roundToInt()
            .coerceAtLeast(1)
            .coerceAtMost(imageWidth)

        val alignment = when (type.alignment) {
            LayerType.Text.TextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
            LayerType.Text.TextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
            LayerType.Text.TextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
            LayerType.Text.TextAlignment.Justify -> Layout.Alignment.ALIGN_NORMAL
        }

        fun buildLayout(p: TextPaint): StaticLayout = StaticLayout.Builder
            .obtain(type.text, 0, type.text.length, p, contentWidth)
            .setAlignment(alignment)
            .setLineSpacing(0f, type.lineHeight)
            .setIncludePad(false)
            .build()

        val layout = buildLayout(paint)
        val contentHeight = layout.height

        canvas.save()
        canvas.translate(-contentWidth / 2f, -contentHeight / 2f)

        if (type.backgroundColor != 0) {
            canvas.drawRect(
                0f, 0f,
                contentWidth.toFloat(), contentHeight.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = type.backgroundColor }
            )
        }

        type.outline?.let { outline ->
            val strokePaint = TextPaint(paint).apply {
                color = outline.color
                style = Paint.Style.STROKE
                strokeWidth = outline.width * imageWidth
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            buildLayout(strokePaint).draw(canvas)
        }

        layout.draw(canvas)
        canvas.restore()
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

        val style = (if (LayerType.Text.Decoration.Bold in decorations) Typeface.BOLD else 0) or
            (if (LayerType.Text.Decoration.Italic in decorations) Typeface.ITALIC else 0)

        return if (style != 0) Typeface.create(base, style) else base
    }
}
