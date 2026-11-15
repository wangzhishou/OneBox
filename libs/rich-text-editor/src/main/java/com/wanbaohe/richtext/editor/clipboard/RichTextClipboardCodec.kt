package com.wanbaohe.richtext.editor.clipboard

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.sp
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.style.RichTextBlockSnapshot
import com.wanbaohe.richtext.editor.style.RichTextStyleScheme
import org.json.JSONArray
import org.json.JSONObject

class RichTextClipboardCodec(
    private val scheme: RichTextStyleScheme,
) {
    fun encode(snapshot: RichTextSnapshot): String = JSONObject()
        .put("version", 1)
        .put("text", snapshot.text)
        .put("spans", encodeSpans(snapshot.spanStyles))
        .put("paragraphs", encodeParagraphs(snapshot.paragraphStyles))
        .toString()

    private fun encodeSpans(styles: List<AnnotatedString.Range<SpanStyle>>) = JSONArray().apply {
        styles.forEach { style -> encodeSpan(style)?.let(::put) }
    }

    private fun encodeSpan(range: AnnotatedString.Range<SpanStyle>): JSONObject? {
        val inlineIds = scheme.inlineStyles.filter { it.matches(range.item) }.map { it.id }
        val blockIds = scheme.blockStyles.filter { definition ->
            !definition.isDefault && definition.matches(
                RichTextBlockSnapshot("", listOf(range.item), emptyList()),
            )
        }.map { it.id }
        if (inlineIds.isEmpty() && blockIds.isEmpty()) return null
        return JSONObject()
            .put("start", range.start)
            .put("end", range.end)
            .put("inline", JSONArray(inlineIds))
            .put("block", JSONArray(blockIds))
    }

    private fun encodeParagraphs(styles: List<AnnotatedString.Range<ParagraphStyle>>) =
        JSONArray().apply {
            styles.forEach { style -> put(encodeParagraph(style)) }
        }

    private fun encodeParagraph(range: AnnotatedString.Range<ParagraphStyle>) = JSONObject()
        .put("start", range.start)
        .put("end", range.end)
        .put("lineHeight", range.item.lineHeight.toJson())
        .put("textAlign", textAlignToJson(range.item.textAlign))
        .put("textIndent", range.item.textIndent?.let(::encodeTextIndent))

    private fun encodeTextIndent(indent: TextIndent) = JSONObject()
        .put("firstLine", indent.firstLine.toJson())
        .put("restLine", indent.restLine.toJson())

    fun decode(value: String): RichTextSnapshot? = runCatching {
        val root = JSONObject(value)
        require(root.optInt("version") == 1)
        val text = root.getString("text")
        val spanStyles = mutableListOf<AnnotatedString.Range<SpanStyle>>()
        val paragraphStyles = mutableListOf<AnnotatedString.Range<ParagraphStyle>>()
        root.optJSONArray("spans").forEachObject { span ->
            val start = span.getInt("start").coerceIn(0, text.length)
            val end = span.getInt("end").coerceIn(start, text.length)
            if (start < end) {
                var spanStyle = SpanStyle()
                var paragraphStyle = ParagraphStyle()
                span.optJSONArray("inline").forEachString { id ->
                    scheme.inlineStyle(id)?.let { definition ->
                        spanStyle = definition.apply(spanStyle, true)
                    }
                }
                span.optJSONArray("block").forEachString { id ->
                    scheme.blockStyle(id)?.let { definition ->
                        spanStyle = scheme.reduceSpanStyle(spanStyle, definition.spanStyle)
                        paragraphStyle = paragraphStyle.merge(definition.paragraphStyle)
                    }
                }
                if (spanStyle != SpanStyle()) {
                    spanStyles += AnnotatedString.Range(spanStyle, start, end)
                }
                if (paragraphStyle != ParagraphStyle()) {
                    paragraphStyles += AnnotatedString.Range(paragraphStyle, start, end)
                }
            }
        }
        root.optJSONArray("paragraphs").forEachObject { paragraph ->
            val start = paragraph.getInt("start").coerceIn(0, text.length)
            val end = paragraph.getInt("end").coerceIn(start, text.length)
            if (start < end) {
                val indent = paragraph.optJSONObject("textIndent")
                paragraphStyles += AnnotatedString.Range(
                    ParagraphStyle(
                        textAlign = jsonToTextAlign(paragraph.optString("textAlign")),
                        lineHeight = paragraph.optDouble("lineHeight").takeIf { it > 0 }?.sp
                            ?: TextUnit.Unspecified,
                        textIndent = indent?.let {
                            TextIndent(
                                firstLine = it.optDouble("firstLine").sp,
                                restLine = it.optDouble("restLine").sp,
                            )
                        },
                    ),
                    start,
                    end,
                )
            }
        }
        RichTextSnapshot(text, spanStyles, paragraphStyles)
    }.getOrNull()
}

private fun TextUnit.toJson(): Double = if (type == TextUnitType.Sp) value.toDouble() else 0.0

private fun textAlignToJson(value: TextAlign): String = when (value) {
    TextAlign.Left -> "left"
    TextAlign.Right -> "right"
    TextAlign.Center -> "center"
    TextAlign.Justify -> "justify"
    TextAlign.Start -> "start"
    TextAlign.End -> "end"
    else -> "unspecified"
}

private fun jsonToTextAlign(value: String): TextAlign = when (value) {
    "left" -> TextAlign.Left
    "right" -> TextAlign.Right
    "center" -> TextAlign.Center
    "justify" -> TextAlign.Justify
    "start" -> TextAlign.Start
    "end" -> TextAlign.End
    else -> TextAlign.Unspecified
}

private inline fun JSONArray?.forEachObject(block: (JSONObject) -> Unit) {
    if (this == null) return
    for (index in 0 until length()) block(getJSONObject(index))
}

private inline fun JSONArray?.forEachString(block: (String) -> Unit) {
    if (this == null) return
    for (index in 0 until length()) block(getString(index))
}
