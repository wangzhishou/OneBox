package com.wanbaohe.textcard.data.render

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.SpannableString
import android.text.Spanned
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.MetricAffectingSpan
import com.t8rin.imagetoolbox.core.settings.domain.model.FontType
import com.t8rin.imagetoolbox.core.utils.toTypeface
import com.wanbaohe.textcard.domain.model.CardTextAlignment
import com.wanbaohe.textcard.domain.model.TextBlock
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyBold
import com.wanbaohe.textcard.domain.model.isRangeEffectivelyItalic

internal fun TextBlock.exportText(): CharSequence {
    if (styleSpans.isEmpty() && fontSpans.isEmpty()) return content
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
            spannable.setSpan(
                AbsoluteTypefaceSpan(
                    resolveTypeface(
                        bold = span.bold ?: isBold,
                        italic = span.italic ?: isItalic
                    )
                ),
                clamped.start,
                clamped.end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    fontSpans.forEach { span ->
        val clamped = span.clamped(content.length) ?: return@forEach
        spannable.setSpan(
            AbsoluteTypefaceSpan(
                resolveTypeface(
                    bold = isRangeEffectivelyBold(clamped.start, clamped.end),
                    italic = isRangeEffectivelyItalic(clamped.start, clamped.end),
                    fontOverride = clamped.font,
                    hasFontOverride = true
                )
            ),
            clamped.start,
            clamped.end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannable
}

internal fun TextBlock.createTextPaint(canvasWidth: Int): TextPaint =
    TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = this@createTextPaint.color.toInt()
        alpha = (this@createTextPaint.alpha.coerceIn(0f, 1f) * 255).toInt().let { elementAlpha ->
            (android.graphics.Color.alpha(this@createTextPaint.color.toInt()) * elementAlpha / 255)
        }
        textSize = canvasWidth * baseSizeRatio * sizeScale
        typeface = resolveTypeface()
        letterSpacing = letterSpacingEm
        if (isUnderline) flags = flags or Paint.UNDERLINE_TEXT_FLAG
        if (isStrikethrough) flags = flags or Paint.STRIKE_THRU_TEXT_FLAG
    }

internal fun TextBlock.createTextLayout(
    canvasWidth: Int,
    layoutWidth: Int,
    paint: TextPaint = createTextPaint(canvasWidth),
): StaticLayout = StaticLayout.Builder
    .obtain(exportText(), 0, content.length, paint, layoutWidth.coerceAtLeast(1))
    .setAlignment(alignment.toLayoutAlignment())
    .setLineSpacing(0f, lineSpacingMultiplier)
    .setIncludePad(false)
    .apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            alignment == CardTextAlignment.Justify
        ) {
            setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
        }
    }
    .build()

internal fun TextBlock.resolveTypeface(
    bold: Boolean = isBold,
    italic: Boolean = isItalic,
    fontOverride: FontType? = null,
    hasFontOverride: Boolean = false,
): Typeface {
    val base = if (hasFontOverride) {
        runCatching { fontOverride.toTypeface() }.getOrNull() ?: Typeface.DEFAULT
    } else {
        runCatching { font.toTypeface() }.getOrNull() ?: Typeface.DEFAULT
    }
    val style = (if (bold) Typeface.BOLD else 0) or
        (if (italic) Typeface.ITALIC else 0)
    return if (style != 0) Typeface.create(base, style) else base
}

private class AbsoluteTypefaceSpan(
    private val font: Typeface,
) : MetricAffectingSpan() {
    override fun updateDrawState(paint: TextPaint) = paint.applyFont()

    override fun updateMeasureState(paint: TextPaint) = paint.applyFont()

    private fun TextPaint.applyFont() {
        typeface = font
        isFakeBoldText = false
        textSkewX = 0f
    }
}

private fun CardTextAlignment.toLayoutAlignment(): Layout.Alignment = when (this) {
    CardTextAlignment.Left -> Layout.Alignment.ALIGN_NORMAL
    CardTextAlignment.Center -> Layout.Alignment.ALIGN_CENTER
    CardTextAlignment.Right -> Layout.Alignment.ALIGN_OPPOSITE
    CardTextAlignment.Justify -> Layout.Alignment.ALIGN_NORMAL
}
