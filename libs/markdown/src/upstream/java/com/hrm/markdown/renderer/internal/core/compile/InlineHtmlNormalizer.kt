package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.parser.ast.InlineHtml
import com.hrm.markdown.parser.ast.Node
import com.hrm.markdown.parser.html.HtmlTagCategories
import com.hrm.markdown.parser.html.HtmlTagKind
import com.hrm.markdown.parser.html.HtmlTagParser
import com.hrm.markdown.parser.html.HtmlTagToken
import com.hrm.markdown.renderer.internal.core.model.SpanMark

/**
 * Renderer 内部的行内 HTML 语义树。
 *
 * Parser 只负责识别单个 HTML token；这里负责在同一行内容器中配对标签，避免把
 * Compose 语义或错误恢复策略泄漏到 Markdown AST。
 */
internal sealed interface NormalizedInlineSource {
    val anchor: Node
}

internal data class AstInlineSource(
    override val anchor: Node,
) : NormalizedInlineSource

internal data class HtmlSpanInlineSource(
    override val anchor: InlineHtml,
    val closing: InlineHtml,
    val marks: List<SpanMark>,
    val children: List<NormalizedInlineSource>,
) : NormalizedInlineSource

internal data class HtmlLineBreakInlineSource(
    override val anchor: InlineHtml,
) : NormalizedInlineSource

internal data class HtmlImageInlineSource(
    override val anchor: InlineHtml,
    val source: String,
    val altText: String,
    val title: String?,
    val width: Int?,
    val height: Int?,
    val attributes: Map<String, String>,
) : NormalizedInlineSource

internal data class HiddenHtmlInlineSource(
    override val anchor: InlineHtml,
) : NormalizedInlineSource

/**
 * 将扁平的 InlineHtml token 线性归一化为安全的渲染语义。
 *
 * 只消费白名单内且正确配对的标签；未知标签、危险属性、错配和未闭合标签均保留为
 * [AstInlineSource]，由既有源码回退路径渲染。
 */
internal object InlineHtmlNormalizer {
    private data class OpenFrame(
        val opening: InlineHtml,
        val tagName: String,
        val marks: List<SpanMark>,
        val children: MutableList<NormalizedInlineSource> = mutableListOf(),
        var valid: Boolean = true,
    )

    fun normalize(nodes: List<Node>): List<NormalizedInlineSource> {
        val root = mutableListOf<NormalizedInlineSource>()
        val frames = ArrayDeque<OpenFrame>()
        var requiresAtomicFallback = false

        fun append(source: NormalizedInlineSource) {
            if (frames.isEmpty()) root += source else frames.last().children += source
        }

        for (node in nodes) {
            if (node !is InlineHtml) {
                append(AstInlineSource(node))
                continue
            }

            val token = HtmlTagParser.parse(node.literal)
            if (token == null) {
                append(AstInlineSource(node))
                continue
            }

            when (token.kind) {
                HtmlTagKind.COMMENT -> append(HiddenHtmlInlineSource(node))
                HtmlTagKind.OPENING -> normalizeOpeningTag(node, token, frames, ::append)
                HtmlTagKind.SELF_CLOSING -> normalizeSelfClosingTag(node, token, ::append)
                HtmlTagKind.CLOSING -> {
                    val frame = frames.lastOrNull()
                    if (frame != null && frame.tagName == token.name) {
                        frames.removeLast()
                        if (frame.valid) {
                            append(
                                HtmlSpanInlineSource(
                                    anchor = frame.opening,
                                    closing = node,
                                    marks = frame.marks,
                                    children = frame.children,
                                )
                            )
                        } else {
                            frame.children += AstInlineSource(node)
                            frame.fallbackSources().forEach(::append)
                        }
                    } else {
                        if (SafeHtmlPolicy.isSupportedInlineContainerName(token.name)) {
                            frames.forEach { it.valid = false }
                        }
                        if (SafeHtmlPolicy.isSupportedInlineTagName(token.name)) {
                            requiresAtomicFallback = true
                        }
                        append(AstInlineSource(node))
                    }
                }
                HtmlTagKind.PROCESSING_INSTRUCTION,
                HtmlTagKind.DECLARATION,
                HtmlTagKind.CDATA -> append(AstInlineSource(node))
            }
        }

        // 从内向外恢复未闭合标签，确保源码与已经归一化的内容顺序都不丢失。
        while (frames.isNotEmpty()) {
            val frame = frames.removeLast()
            val fallback = frame.fallbackSources()
            if (frames.isEmpty()) root += fallback else frames.last().children += fallback
        }

        return if (requiresAtomicFallback) {
            root.flatMap { it.fallbackSources() }
        } else {
            root
        }
    }

    private fun normalizeOpeningTag(
        node: InlineHtml,
        token: HtmlTagToken,
        frames: ArrayDeque<OpenFrame>,
        append: (NormalizedInlineSource) -> Unit,
    ) {
        when (val action = SafeHtmlPolicy.inlineAction(token)) {
            is SafeInlineHtmlAction.Span -> frames += OpenFrame(
                opening = node,
                tagName = token.name.orEmpty(),
                marks = action.marks,
            )
            SafeInlineHtmlAction.LineBreak -> append(HtmlLineBreakInlineSource(node))
            is SafeInlineHtmlAction.Image -> append(action.toSource(node))
            SafeInlineHtmlAction.Hidden -> append(HiddenHtmlInlineSource(node))
            null -> {
                if (!HtmlTagCategories.isVoidTag(token.name.orEmpty())) {
                    frames += OpenFrame(
                        opening = node,
                        tagName = token.name.orEmpty(),
                        marks = emptyList(),
                        valid = false,
                    )
                } else {
                    append(AstInlineSource(node))
                }
            }
        }
    }

    private fun normalizeSelfClosingTag(
        node: InlineHtml,
        token: HtmlTagToken,
        append: (NormalizedInlineSource) -> Unit,
    ) {
        when (val action = SafeHtmlPolicy.inlineAction(token)) {
            SafeInlineHtmlAction.LineBreak -> append(HtmlLineBreakInlineSource(node))
            is SafeInlineHtmlAction.Image -> append(action.toSource(node))
            SafeInlineHtmlAction.Hidden -> append(HiddenHtmlInlineSource(node))
            is SafeInlineHtmlAction.Span,
            null -> append(AstInlineSource(node))
        }
    }

    private fun SafeInlineHtmlAction.Image.toSource(node: InlineHtml): HtmlImageInlineSource =
        HtmlImageInlineSource(
            anchor = node,
            source = source,
            altText = altText,
            title = title,
            width = width,
            height = height,
            attributes = attributes,
        )

    private fun OpenFrame.fallbackSources(): List<NormalizedInlineSource> = buildList {
        add(AstInlineSource(opening))
        children.forEach { addAll(it.fallbackSources()) }
    }

    private fun NormalizedInlineSource.fallbackSources(): List<NormalizedInlineSource> = when (this) {
        is AstInlineSource -> listOf(this)
        is HtmlSpanInlineSource -> buildList {
            add(AstInlineSource(anchor))
            children.forEach { addAll(it.fallbackSources()) }
            add(AstInlineSource(closing))
        }
        is HtmlLineBreakInlineSource -> listOf(AstInlineSource(anchor))
        is HtmlImageInlineSource -> listOf(AstInlineSource(anchor))
        is HiddenHtmlInlineSource -> listOf(AstInlineSource(anchor))
    }
}
