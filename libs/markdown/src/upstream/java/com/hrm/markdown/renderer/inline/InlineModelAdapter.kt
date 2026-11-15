package com.hrm.markdown.renderer.inline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import com.hrm.markdown.renderer.internal.core.model.InlineEndMarkerWidgetModel
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.codehigh.renderer.InlineCode as CodeHighInlineCode
import com.hrm.codehigh.renderer.measureInlineCodeSize
import com.hrm.latex.renderer.measure.LatexMeasurerState
import com.hrm.markdown.parser.core.CharacterUtils
import com.hrm.markdown.renderer.LocalMarkdownComponents
import com.hrm.markdown.renderer.MarkdownImageData
import com.hrm.markdown.renderer.MarkdownInlineCodeInfo
import com.hrm.markdown.renderer.ResolvedMarkdownStyle
import com.hrm.markdown.renderer.internal.adapter.createDirectiveInlineRenderScope
import com.hrm.markdown.renderer.internal.compose.CitationAnnotationTag
import com.hrm.markdown.renderer.internal.compose.InlineElementAnnotationTag
import com.hrm.markdown.renderer.internal.compose.InlineElementRangeEndAnnotationTag
import com.hrm.markdown.renderer.internal.compose.RenderInteractiveMarkdownImage
import com.hrm.markdown.renderer.internal.core.model.DirectiveInlineWidgetModel
import com.hrm.markdown.renderer.internal.core.model.ImageWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineAtom
import com.hrm.markdown.renderer.internal.core.model.InlineCodeWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineMathWidgetModel
import com.hrm.markdown.renderer.internal.core.model.InlineModel
import com.hrm.markdown.renderer.internal.core.model.RubyTextWidgetModel
import com.hrm.markdown.renderer.internal.core.model.SpanMark
import com.hrm.markdown.renderer.internal.core.model.SpoilerWidgetModel
import com.hrm.markdown.renderer.internal.core.model.TextAtom
import com.hrm.markdown.renderer.internal.core.model.WidgetAtom
import com.hrm.markdown.renderer.internal.layout.LayoutTokens
import com.hrm.markdown.renderer.internal.layout.inline.InlineFlowInput
import com.hrm.markdown.renderer.internal.layout.inline.InlineFlowSegment
import com.hrm.markdown.runtime.MarkdownDirectiveRegistry
import kotlin.math.ceil

internal fun buildInlineRenderResultFromModel(
    model: InlineModel,
    theme: ResolvedMarkdownStyle,
    hostTextStyle: TextStyle,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)? = null,
    onFootnoteClick: ((String) -> Unit)? = null,
    latexMeasurer: LatexMeasurerState,
    density: Density,
    textMeasurer: TextMeasurer,
): InlineRenderResult {
    val flowSegments = mutableListOf<InlineFlowSegment>()
    val context = InlineRenderBuildContext(
        paintPayloads = linkedMapOf(),
        flowSegments = flowSegments,
    )
    val annotated = buildAnnotatedString {
        renderInlineModel(
            model = model,
            theme = theme,
            hostTextStyle = hostTextStyle,
            context = context,
            directiveRegistry = directiveRegistry,
            onLinkClick = onLinkClick,
            onFootnoteClick = onFootnoteClick,
            latexMeasurer = latexMeasurer,
            density = density,
            textMeasurer = textMeasurer,
        )
    }
    return InlineRenderResult(
        annotated = annotated,
        paintPayloads = context.paintPayloads,
        flowInput = InlineFlowInput(flowSegments),
        inlineMathBuildRequests = context.inlineMathBuildRequests,
    )
}

internal fun AnnotatedString.Builder.renderInlineModel(
    model: InlineModel,
    theme: ResolvedMarkdownStyle,
    hostTextStyle: TextStyle,
    context: InlineRenderBuildContext,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    latexMeasurer: LatexMeasurerState,
    density: Density,
    textMeasurer: TextMeasurer,
) {
    model.atoms.forEachIndexed { index, atom ->
        when (atom) {
            is TextAtom -> {
                val normalized = normalizeTextAtomForCjkInlineMathSpacing(
                    atoms = model.atoms,
                    index = index,
                    atom = atom,
                )
                renderTextAtom(
                    atom = normalized,
                    theme = theme,
                    context = context,
                    onLinkClick = onLinkClick,
                    onFootnoteClick = onFootnoteClick,
                    sourceOffset = atom.text.indexOf(normalized.text).coerceAtLeast(0),
                    sourceLength = atom.text.length,
                )
            }

            is WidgetAtom -> renderWidgetAtom(
                atom = atom,
                theme = theme,
                hostTextStyle = hostTextStyle,
                context = context,
                directiveRegistry = directiveRegistry,
                onLinkClick = onLinkClick,
                onFootnoteClick = onFootnoteClick,
                latexMeasurer = latexMeasurer,
                density = density,
                textMeasurer = textMeasurer,
            )
        }
    }
}

/** 参与行内元素点击分发的 mark kind（与 [InlineElementKinds] 一一对应）。 */
internal val InteractiveInlineMarkKinds = setOf(
    "highlight", "citation", "footnote", "abbreviation", "inline_html", "kbd", "spoiler",
)

private fun normalizeTextAtomForCjkInlineMathSpacing(
    atoms: List<InlineAtom>,
    index: Int,
    atom: TextAtom,
): TextAtom {
    var text = atom.text
    if (text.isEmpty()) return atom

    val previous = atoms.getOrNull(index - 1)
    val next = atoms.getOrNull(index + 1)

    if (previous.isInlineMathWidgetAtom() && text.firstOrNull()?.isWhitespace() == true) {
        val nextVisible = text.firstOrNull { !it.isWhitespace() }
        if (nextVisible != null && CharacterUtils.isCJKOrFullWidthPunctuation(nextVisible)) {
            text = text.trimStart()
        }
    }

    if (next.isInlineMathWidgetAtom() && text.lastOrNull()?.isWhitespace() == true) {
        val previousVisible = text.lastOrNull { !it.isWhitespace() }
        if (previousVisible != null && CharacterUtils.isCJKOrFullWidthPunctuation(previousVisible)) {
            text = text.trimEnd()
        }
    }

    return if (text == atom.text) atom else atom.copy(text = text)
}

private fun InlineAtom?.isInlineMathWidgetAtom(): Boolean {
    return (this as? WidgetAtom)?.widget is InlineMathWidgetModel
}

private fun AnnotatedString.Builder.renderTextAtom(
    atom: TextAtom,
    theme: ResolvedMarkdownStyle,
    context: InlineRenderBuildContext,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    sourceOffset: Int,
    sourceLength: Int,
) {
    val clickMark =
        atom.marks.lastOrNull { it.kind == "link" || it.kind == "footnote" || it.kind == "citation" }
    val abbreviation = atom.marks.lastOrNull { it.kind == "abbreviation" }?.payload?.get("fullText")
    val interactiveMark = atom.marks.lastOrNull { it.kind in InteractiveInlineMarkKinds }
    val spanStyle = atom.marks.fold(SpanStyle()) { acc, mark ->
        acc.merge(spanStyleForMark(mark, theme))
    }
    val appendText: AnnotatedString.Builder.() -> Unit = {
        if (!abbreviation.isNullOrEmpty()) {
            pushStringAnnotation(tag = "abbreviation", annotation = abbreviation)
        }
        if (spanStyle != SpanStyle()) {
            withStyle(spanStyle) { append(atom.text) }
        } else {
            append(atom.text)
        }
        if (!abbreviation.isNullOrEmpty()) {
            pop()
        }
    }

        val segment = buildAnnotatedString {
            // 文本只发射一次；注解与链接包裹都套在这唯一一次 append 外面。
            // （此前 interactive 注解分支与 when 分支各 append 一次，划线等
            //  interactive-only 元素的文本会渲染两遍。）
            when {
                clickMark?.kind == "link" -> {
                    val target = clickMark.payload["target"].orEmpty()
                    val link = LinkAnnotation.Clickable(
                        tag = clickMark.payload["tag"] ?: "link",
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = theme.colors.link,
                                textDecoration = TextDecoration.Underline,
                            )
                        ),
                        linkInteractionListener = { onLinkClick?.invoke(target) },
                    )
                    if (interactiveMark != null) {
                        pushStringAnnotation(
                            InlineElementAnnotationTag,
                            encodeInlineElementAnnotation(atom.identity.stableId, interactiveMark),
                        )
                        withLink(link) { appendText() }
                        pop()
                    } else {
                        withLink(link) { appendText() }
                    }
                }

                clickMark?.kind == "footnote" -> {
                    val label = clickMark.payload["label"].orEmpty()
                    val link = LinkAnnotation.Clickable(
                        tag = "footnote",
                        styles = TextLinkStyles(
                            style = SpanStyle(baselineShift = BaselineShift.Superscript).merge(theme.footnoteStyle)
                        ),
                        linkInteractionListener = { onFootnoteClick?.invoke(label) },
                    )
                    if (interactiveMark != null) {
                        pushStringAnnotation(
                            InlineElementAnnotationTag,
                            encodeInlineElementAnnotation(atom.identity.stableId, interactiveMark),
                        )
                        withLink(link) { appendText() }
                        pop()
                    } else {
                        withLink(link) { appendText() }
                    }
                }

                clickMark?.kind == "citation" -> {
                    pushStringAnnotation(
                        CitationAnnotationTag,
                        "${atom.identity.stableId}:${clickMark.payload["key"].orEmpty()}",
                    )
                    withStyle(SpanStyle(baselineShift = BaselineShift.Superscript).merge(theme.footnoteStyle)) { appendText() }
                    pop()
                }

                interactiveMark != null -> {
                    pushStringAnnotation(
                        InlineElementAnnotationTag,
                        encodeInlineElementAnnotation(atom.identity.stableId, interactiveMark),
                    )
                    appendText()
                    pop()
                }

                else -> appendText()
            }
            if (interactiveMark?.isRangeEnd == true) {
                val end = atom.text.indexOfLast { it != '\n' } + 1
                if (end > 0) {
                    addStringAnnotation(
                        InlineElementRangeEndAnnotationTag,
                        encodeInlineElementAnnotation(atom.identity.stableId, interactiveMark),
                        end - 1,
                        end,
                    )
                }
            }
        }
    context.emitTextAtom(
        builder = this,
        segment = segment,
        sourceOffset = sourceOffset,
        sourceLength = sourceLength,
    )
}

internal fun spanStyleForMark(mark: SpanMark, theme: ResolvedMarkdownStyle): SpanStyle = when (mark.kind) {
    "emphasis" -> SpanStyle(fontStyle = FontStyle.Italic)
    "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
    "strikethrough" -> theme.strikethroughStyle
    "highlight" -> SpanStyle(background = theme.colors.highlight)
    "superscript" -> theme.superscriptStyle.merge(SpanStyle(baselineShift = BaselineShift.Superscript))
    "subscript" -> theme.subscriptStyle.merge(SpanStyle(baselineShift = BaselineShift.Subscript))
    "inserted" -> theme.insertedTextStyle
    "styled" -> {
        val style = mark.payload["style"]?.let(::parseCssStyleToSpanStyle)
        val classes = mark.payload["class"]?.let(::styleClassNames).orEmpty()
        val classStyle = inferStyleFromClasses(classes, theme)
        when {
            classStyle != null && style != null -> classStyle.merge(style)
            style != null -> style
            classStyle != null -> classStyle
            else -> SpanStyle()
        }
    }

    "abbreviation" -> theme.abbreviationStyle
    "kbd" -> theme.kbdStyle
    "underline" -> SpanStyle(textDecoration = TextDecoration.Underline)
    "html_code" -> theme.inlineCodeStyle.copy(background = theme.inlineCodeBackground)
    "inline_html" -> SpanStyle(
        color = Color.Gray,
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
    )

    else -> SpanStyle()
}

private fun AnnotatedString.Builder.renderWidgetAtom(
    atom: WidgetAtom,
    theme: ResolvedMarkdownStyle,
    hostTextStyle: TextStyle,
    context: InlineRenderBuildContext,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    latexMeasurer: LatexMeasurerState,
    density: Density,
    textMeasurer: TextMeasurer,
) {
    when (val widget = atom.widget) {
        is InlineCodeWidgetModel -> renderInlineCodeWidget(
            widget,
            theme,
            context,
            density,
            textMeasurer,
        )

        is ImageWidgetModel -> renderImageWidget(widget, context, density)
        is InlineMathWidgetModel -> renderInlineMathWidget(
            widget,
            hostTextStyle,
            context,
            latexMeasurer,
            density
        )

        is SpoilerWidgetModel -> renderSpoilerWidget(
            widget = widget,
            theme = theme,
            hostTextStyle = hostTextStyle,
            context = context,
            directiveRegistry = directiveRegistry,
            onLinkClick = onLinkClick,
            onFootnoteClick = onFootnoteClick,
            latexMeasurer = latexMeasurer,
            density = density,
            textMeasurer = textMeasurer,
        )

        is DirectiveInlineWidgetModel -> renderDirectiveInlineWidget(
            widget,
            theme,
            context,
            directiveRegistry,
            density
        )

        is InlineEndMarkerWidgetModel -> {
            val marker = widget.marker
            val diameter = with(density) { marker.diameter.toPx() }
            val gap = with(density) { marker.gap.toPx() }
            context.emitEndMarkerWidget(InlinePlaceholderId.from(widget), gap + diameter, diameter) {
                val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
                Canvas(Modifier.fillMaxSize()) {
                    translate(left = if (rtl) 0f else gap) {
                        clipRect(right = diameter, bottom = diameter) {
                            with(requireNotNull(marker.painter)) { draw(Size(diameter, diameter), colorFilter = ColorFilter.tint(marker.color)) }
                        }
                    }
                }
            }
        }
        is RubyTextWidgetModel -> renderRubyTextWidget(widget, theme, context, density)
    }
}

private fun AnnotatedString.Builder.renderInlineCodeWidget(
    widget: InlineCodeWidgetModel,
    theme: ResolvedMarkdownStyle,
    context: InlineRenderBuildContext,
    density: Density,
    textMeasurer: TextMeasurer,
) {
    val size = measureInlineCodeSize(
        text = widget.code,
        typography = theme.codeTypography,
        layout = theme.codeLayout,
        density = density,
        textMeasurer = textMeasurer,
    )
    context.emitInlineCodeWidget(
        builder = this,
        widget = widget,
        widthPx = ceil(size.width),
        heightPx = size.height,
    ) {
        val info = MarkdownInlineCodeInfo(
            text = widget.code,
            colors = theme.codeColors,
            typography = theme.codeTypography,
            layout = theme.codeLayout,
        )
        LocalMarkdownComponents.current.inlineCode?.invoke(info)
            ?: CodeHighInlineCode(
                text = info.text,
                colors = info.colors,
                typography = info.typography,
                layout = info.layout,
            )
    }
}

private fun AnnotatedString.Builder.renderImageWidget(
    widget: ImageWidgetModel,
    context: InlineRenderBuildContext,
    density: Density,
) {
    val widthPx = with(density) {
        widget.width?.dp?.toPx() ?: LayoutTokens.InlineImageFallbackWidth.toPx()
    }
    val heightPx = with(density) {
        widget.height?.dp?.toPx() ?: LayoutTokens.InlineImageFallbackHeight.toPx()
    }
    context.emitImageWidget(
        builder = this,
        widget = widget,
        widthPx = widthPx,
        heightPx = heightPx,
    ) {
        val imageData = MarkdownImageData(
            url = widget.url,
            altText = widget.altText,
            title = widget.title,
            width = widget.width,
            height = widget.height,
            attributes = widget.attributes,
        )
        RenderInteractiveMarkdownImage(
            imageData, widget.identity.stableId,
        )
    }
}

private fun AnnotatedString.Builder.renderInlineMathWidget(
    widget: InlineMathWidgetModel,
    hostTextStyle: TextStyle,
    context: InlineRenderBuildContext,
    latexMeasurer: LatexMeasurerState,
    density: Density,
) {
    context.recordInlineMathBuildRequest()
    val trimmedLatex = widget.latex.trim()
    val inlineContent = latexMeasurer.inlineContent(trimmedLatex)
    if (inlineContent == null) {
        context.emitStyledTextAtom(
            builder = this,
            text = widget.latex,
            style = hostTextStyle.toSpanStyle(),
        )
        return
    }
    context.emitInlineMathWidget(
        builder = this,
        widget = widget,
        inlineContent = inlineContent,
        density = density,
    )
}

private fun AnnotatedString.Builder.renderSpoilerWidget(
    widget: SpoilerWidgetModel,
    theme: ResolvedMarkdownStyle,
    hostTextStyle: TextStyle,
    context: InlineRenderBuildContext,
    directiveRegistry: MarkdownDirectiveRegistry,
    onLinkClick: ((String) -> Unit)?,
    onFootnoteClick: ((String) -> Unit)?,
    latexMeasurer: LatexMeasurerState,
    density: Density,
    textMeasurer: TextMeasurer,
) {
    val fontSize = theme.bodyStyle.fontSize.value
    val avgCharWidth = widget.alternateText.sumOf { ch -> if (ch.code > 0x7F) 12 else 7 }
        .toFloat() / 10f * (fontSize / 16f)
    context.emitSpoilerWidget(
        builder = this,
        widget = widget,
        widthPx = density.scaledSpValueToPx(avgCharWidth + 8f),
        heightPx = density.scaledSpValueToPx(fontSize * 1.5f),
    ) {
        SpoilerContent(
            model = widget.content,
            theme = theme,
            hostTextStyle = hostTextStyle,
            directiveRegistry = directiveRegistry,
            onLinkClick = onLinkClick,
            onFootnoteClick = onFootnoteClick,
            latexMeasurer = latexMeasurer,
            density = density,
            textMeasurer = textMeasurer,
        )
    }
}

private fun AnnotatedString.Builder.renderDirectiveInlineWidget(
    widget: DirectiveInlineWidgetModel,
    theme: ResolvedMarkdownStyle,
    context: InlineRenderBuildContext,
    directiveRegistry: MarkdownDirectiveRegistry,
    density: Density?,
) {
    val renderer = directiveRegistry.findInlineDirectiveRenderer(widget.tagName)
    if (renderer != null) {
        val fontSize = theme.bodyStyle.fontSize.value
        val estimatedWidth = widget.alternateText.sumOf { ch -> if (ch.code > 0x7F) 12 else 7 }
            .toFloat() / 10f * (fontSize / 16f)
        context.emitDirectiveInlineWidget(
            builder = this,
            widget = widget,
            widthPx = density.scaledSpValueToPx(estimatedWidth + 8f),
            heightPx = density.scaledSpValueToPx(fontSize * 1.5f),
        ) {
            renderer(
                createDirectiveInlineRenderScope(
                    tagName = widget.tagName,
                    args = widget.args,
                    alternateText = widget.alternateText,
                )
            )
        }
    } else {
        context.emitStyledTextAtom(
            builder = this,
            text = widget.alternateText,
            style = SpanStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = theme.bodyStyle.fontSize * 0.875f,
                color = theme.colors.link,
            ),
        )
    }
}

private fun AnnotatedString.Builder.renderRubyTextWidget(
    widget: RubyTextWidgetModel,
    theme: ResolvedMarkdownStyle,
    context: InlineRenderBuildContext,
    density: Density?,
) {
    val fontSize = theme.bodyStyle.fontSize.value
    val baseWidth =
        widget.base.sumOf { ch -> if (ch.code > 0x7F) 12 else 7 }.toFloat() / 10f * (fontSize / 16f)
    context.emitRubyTextWidget(
        builder = this,
        widget = widget,
        widthPx = density.scaledSpValueToPx(baseWidth + 2f),
        heightPx = density.scaledSpValueToPx(fontSize * 2.0f),
    ) {
        RubyTextContent(
            base = widget.base,
            annotation = widget.annotation,
            theme = theme,
        )
    }
}

private fun Density?.scaledSpValueToPx(value: Float): Float {
    return this?.run { value.sp.toPx() } ?: value
}

/** 行内元素注解编码：`stableId|kind|payload|attributes`；后两段内部用 \u001f 分隔键值对。 */
internal fun encodeInlineElementAnnotation(stableId: Long, mark: SpanMark): String =
    listOf(
        stableId.toString(),
        mark.kind,
        encodePairs(mark.payload),
        encodePairs(mark.attributes),
    ).joinToString("|")

private fun encodePairs(map: Map<String, String>): String =
    map.entries.joinToString("") { "${it.key}=${it.value}" }
