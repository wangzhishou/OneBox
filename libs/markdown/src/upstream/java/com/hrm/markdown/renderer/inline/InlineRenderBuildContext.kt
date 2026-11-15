package com.hrm.markdown.renderer.inline

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import com.hrm.markdown.renderer.internal.core.model.DirectiveInlineWidgetModel
import com.hrm.markdown.renderer.internal.core.model.ImageWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineCodeWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineMathWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineWidgetModel
import com.hrm.markdown.renderer.internal.core.model.RubyTextWidgetModel
import com.hrm.markdown.renderer.internal.core.model.SpoilerWidgetModel
import com.hrm.markdown.renderer.internal.layout.inline.InlineFlowSegment
import com.hrm.markdown.renderer.internal.layout.inline.InlinePlaceholderLayoutSpec

internal class InlineRenderBuildContext(
    val paintPayloads: MutableMap<InlinePlaceholderId, InlineWidgetPaintPayload> = linkedMapOf(),
    val flowSegments: MutableList<InlineFlowSegment>? = null,
) {
    private var sourceCursor: Int = 0

    var inlineMathBuildRequests: Int = 0
        private set

    fun recordInlineMathBuildRequest() {
        inlineMathBuildRequests++
    }

    fun emitTextAtom(
        builder: AnnotatedString.Builder,
        segment: AnnotatedString,
        sourceOffset: Int = 0,
        sourceLength: Int = segment.length,
    ) {
        if (segment.isNotEmpty()) {
            builder.append(segment)
            flowSegments?.appendTextAnnotatedSegment(
                segment = segment,
                sourceStart = sourceCursor + sourceOffset,
            )
        }
        sourceCursor += sourceLength
    }

    fun emitStyledTextAtom(
        builder: AnnotatedString.Builder,
        text: String,
        style: SpanStyle,
    ) {
        val segment = buildAnnotatedString {
            withStyle(style) {
                append(text)
            }
        }
        emitTextAtom(builder, segment)
    }

    fun emitInlinePlaceholder(
        builder: AnnotatedString.Builder,
        id: InlinePlaceholderId,
    ) {
        builder.appendInlinePlaceholder(id)
    }

    fun emitWidgetPayload(
        builder: AnnotatedString.Builder,
        id: InlinePlaceholderId,
        payload: InlineWidgetPaintPayload,
    ) {
        emitInlinePlaceholder(builder, id)
        registerWidgetPayload(id, payload)
    }

    // Layout advances without advancing annotated text or the source cursor.
    fun emitEndMarkerWidget(id: InlinePlaceholderId, widthPx: Float, heightPx: Float, content: @Composable () -> Unit) {
        val payload = inlineWidgetPaintPayload("", widthPx, heightPx, content)
        paintPayloads[id] = payload
        flowSegments?.add(InlineFlowSegment.InlineRun(id, payload.placeholder, sourceCursor, sourceCursor))
    }

    fun emitInlineCodeWidget(
        builder: AnnotatedString.Builder,
        widget: InlineCodeWidgetModel,
        widthPx: Float,
        heightPx: Float,
        content: @Composable () -> Unit,
    ) {
        emitInlineWidget(
            builder = builder,
            widget = widget,
            alternateText = widget.code,
            widthPx = widthPx,
            heightPx = heightPx,
            content = content,
        )
    }

    fun emitImageWidget(
        builder: AnnotatedString.Builder,
        widget: ImageWidgetModel,
        widthPx: Float,
        heightPx: Float,
        content: @Composable () -> Unit,
    ) {
        emitInlineWidget(
            builder = builder,
            widget = widget,
            alternateText = widget.altText.ifEmpty { widget.title ?: widget.url },
            widthPx = widthPx,
            heightPx = heightPx,
            content = content,
        )
    }

    fun emitInlineMathWidget(
        builder: AnnotatedString.Builder,
        widget: InlineMathWidgetModel,
        inlineContent: InlineTextContent,
        density: Density,
    ) {
        val placeholder = inlineContent.placeholder
        emitInlineWidget(
            builder = builder,
            widget = widget,
            alternateText = widget.latex,
            widthPx = with(density) { placeholder.width.toPx() },
            heightPx = with(density) { placeholder.height.toPx() },
        ) {
            inlineContent.children(widget.latex)
        }
    }

    fun emitSpoilerWidget(
        builder: AnnotatedString.Builder,
        widget: SpoilerWidgetModel,
        widthPx: Float,
        heightPx: Float,
        content: @Composable () -> Unit,
    ) {
        emitInlineWidget(
            builder = builder,
            widget = widget,
            alternateText = widget.alternateText,
            widthPx = widthPx,
            heightPx = heightPx,
            content = content,
        )
    }

    fun emitDirectiveInlineWidget(
        builder: AnnotatedString.Builder,
        widget: DirectiveInlineWidgetModel,
        widthPx: Float,
        heightPx: Float,
        content: @Composable () -> Unit,
    ) {
        emitInlineWidget(
            builder = builder,
            widget = widget,
            alternateText = widget.alternateText,
            widthPx = widthPx,
            heightPx = heightPx,
            content = content,
        )
    }

    fun emitRubyTextWidget(
        builder: AnnotatedString.Builder,
        widget: RubyTextWidgetModel,
        widthPx: Float,
        heightPx: Float,
        content: @Composable () -> Unit,
    ) {
        emitInlineWidget(
            builder = builder,
            widget = widget,
            alternateText = widget.base,
            widthPx = widthPx,
            heightPx = heightPx,
            content = content,
        )
    }

    fun registerWidgetPayload(
        id: InlinePlaceholderId,
        payload: InlineWidgetPaintPayload,
    ) {
        paintPayloads[id] = payload
        flowSegments?.appendInlineSegment(
            id = id,
            placeholder = payload.placeholder,
            sourceStart = sourceCursor,
        )
        sourceCursor += payload.alternateText.length
    }

    private fun emitInlineWidget(
        builder: AnnotatedString.Builder,
        widget: InlineWidgetModel,
        alternateText: String,
        widthPx: Float,
        heightPx: Float,
        content: @Composable () -> Unit,
    ) {
        emitWidgetPayload(
            builder = builder,
            id = InlinePlaceholderId.from(widget),
            payload = inlineWidgetPaintPayload(
                alternateText = alternateText,
                widthPx = widthPx,
                heightPx = heightPx,
                content = content,
            ),
        )
    }
}

private fun MutableList<InlineFlowSegment>.appendTextAnnotatedSegment(
    segment: AnnotatedString,
    sourceStart: Int,
) {
    if (segment.isEmpty()) return
    val text = segment.text
    var start = 0
    fun pushText(end: Int) {
        if (end > start) {
            add(
                InlineFlowSegment.TextRun(
                    annotated = segment.subSequence(start, end),
                    sourceStart = sourceStart + start,
                    sourceEnd = sourceStart + end,
                )
            )
        }
        start = end
    }
    for (i in text.indices) {
        if (text[i] == '\n') {
            pushText(i)
            add(InlineFlowSegment.Newline)
            start = i + 1
        }
    }
    pushText(text.length)
}

private fun MutableList<InlineFlowSegment>.appendInlineSegment(
    id: InlinePlaceholderId,
    placeholder: InlinePlaceholderLayoutSpec,
    sourceStart: Int,
) {
    add(
        InlineFlowSegment.InlineRun(
            id = id,
            placeholder = placeholder,
            sourceStart = sourceStart,
            sourceEnd = sourceStart + placeholder.alternateText.length,
        )
    )
}
