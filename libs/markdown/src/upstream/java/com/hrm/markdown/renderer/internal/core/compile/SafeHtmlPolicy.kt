package com.hrm.markdown.renderer.internal.core.compile

import com.hrm.markdown.parser.html.HtmlTagCategories
import com.hrm.markdown.parser.html.HtmlTagKind
import com.hrm.markdown.parser.html.HtmlTagToken
import com.hrm.markdown.renderer.inline.areFullySupportedStyleClasses
import com.hrm.markdown.renderer.inline.isFullySupportedCssStyle
import com.hrm.markdown.renderer.internal.core.model.BlockTextAlignment
import com.hrm.markdown.renderer.internal.core.model.SpanMark

internal sealed interface SafeInlineHtmlAction {
    data class Span(val marks: List<SpanMark>) : SafeInlineHtmlAction

    data object LineBreak : SafeInlineHtmlAction

    data class Image(
        val source: String,
        val altText: String,
        val title: String?,
        val width: Int?,
        val height: Int?,
        val attributes: Map<String, String>,
    ) : SafeInlineHtmlAction

    data object Hidden : SafeInlineHtmlAction
}

internal enum class SafeBlockHtmlRole {
    CONTAINER,
    PARAGRAPH,
    HEADING,
    BLOCK_QUOTE,
    CODE_BLOCK,
    THEMATIC_BREAK,
}

internal data class SafeBlockHtmlAction(
    val role: SafeBlockHtmlRole,
    val alignment: BlockTextAlignment,
    val headingLevel: Int? = null,
)

/**
 * Compose 安全 HTML 的唯一策略入口。
 *
 * 标签、属性或属性值只要不能被完整映射，就返回 `null`，调用方必须保留原始源码。
 */
internal object SafeHtmlPolicy {
    fun inlineAction(token: HtmlTagToken): SafeInlineHtmlAction? {
        if (token.kind == HtmlTagKind.COMMENT) return SafeInlineHtmlAction.Hidden
        if (token.kind != HtmlTagKind.OPENING && token.kind != HtmlTagKind.SELF_CLOSING) return null

        val name = token.name.orEmpty()
        return when (name) {
            "strong", "b" -> plainSpan(token, SpanMark("strong"))
            "em", "i" -> plainSpan(token, SpanMark("emphasis"))
            "del", "s", "strike" -> plainSpan(token, SpanMark("strikethrough"))
            "mark" -> plainSpan(token, SpanMark("highlight"))
            "sup" -> plainSpan(token, SpanMark("superscript"))
            "sub" -> plainSpan(token, SpanMark("subscript"))
            "ins" -> plainSpan(token, SpanMark("inserted"))
            "u" -> plainSpan(token, SpanMark("underline"))
            "code" -> plainSpan(token, SpanMark("html_code"))
            "kbd" -> plainSpan(token, SpanMark("kbd"))
            "span" -> styledSpan(token)
            "a" -> anchor(token)
            "br" -> if (token.attributes.isEmpty()) SafeInlineHtmlAction.LineBreak else null
            "img" -> image(token)
            else -> null
        }
    }

    fun isSupportedInlineContainerName(name: String?): Boolean =
        name?.lowercase() in INLINE_CONTAINER_TAGS

    fun isSupportedInlineTagName(name: String?): Boolean =
        name?.lowercase() in INLINE_TAGS

    fun blockAction(
        token: HtmlTagToken,
        inheritedAlignment: BlockTextAlignment,
    ): SafeBlockHtmlAction? {
        if (token.kind != HtmlTagKind.OPENING && token.kind != HtmlTagKind.SELF_CLOSING) return null
        val name = token.name.orEmpty()
        if (!HtmlTagCategories.isHtmlBlockElement(name)) return null

        val role = when (name) {
            "article", "center", "div", "footer", "header", "main", "section" ->
                SafeBlockHtmlRole.CONTAINER
            "p" -> SafeBlockHtmlRole.PARAGRAPH
            "h1", "h2", "h3", "h4", "h5", "h6" -> SafeBlockHtmlRole.HEADING
            "blockquote" -> SafeBlockHtmlRole.BLOCK_QUOTE
            "pre" -> SafeBlockHtmlRole.CODE_BLOCK
            "hr" -> SafeBlockHtmlRole.THEMATIC_BREAK
            else -> return null
        }
        if (role == SafeBlockHtmlRole.THEMATIC_BREAK) {
            return if (token.attributes.isEmpty()) {
                SafeBlockHtmlAction(role, inheritedAlignment)
            } else {
                null
            }
        }
        if (role == SafeBlockHtmlRole.HEADING ||
            role == SafeBlockHtmlRole.BLOCK_QUOTE ||
            role == SafeBlockHtmlRole.CODE_BLOCK
        ) {
            return if (token.kind == HtmlTagKind.OPENING && token.attributes.isEmpty()) {
                SafeBlockHtmlAction(
                    role = role,
                    alignment = inheritedAlignment,
                    headingLevel = name.removePrefix("h").toIntOrNull(),
                )
            } else {
                null
            }
        }
        if (token.attributes.keys.any { it !in BLOCK_ATTRIBUTES }) return null

        val alignAttribute = token.attributes["align"]?.let { parseAlignment(it) ?: return null }
        val styleAlignment = token.attributes["style"]?.let { parseTextAlignStyle(it) ?: return null }
        val defaultAlignment = if (name == "center") {
            BlockTextAlignment.CENTER
        } else {
            inheritedAlignment
        }
        return SafeBlockHtmlAction(
            role = role,
            alignment = styleAlignment ?: alignAttribute ?: defaultAlignment,
        )
    }

    /**
     * 纯样式行内标签（mark/sup/sub/ins/u/code/kbd）：渲染器不解释任何属性，
     * 因此属性原样挂到 mark 上交给事件使用方。唯一例外是事件处理器属性（onclick 等）：
     * 它们是行为载体而非数据，出现即视为无法安全映射，整个元素退回源码。
     */
    private fun plainSpan(token: HtmlTagToken, mark: SpanMark): SafeInlineHtmlAction? =
        if (token.kind != HtmlTagKind.OPENING) {
            null
        } else if (token.attributes.isEmpty()) {
            SafeInlineHtmlAction.Span(listOf(mark))
        } else if (token.attributes.keys.any { it.lowercase().startsWith("on") }) {
            null
        } else {
            SafeInlineHtmlAction.Span(listOf(mark.copy(attributes = token.attributes)))
        }

    private fun styledSpan(token: HtmlTagToken): SafeInlineHtmlAction? {
        if (token.kind != HtmlTagKind.OPENING) return null
        val style = token.attributes["style"]
        val classes = token.attributes["class"]
        // 渲染器解释的属性（style/class）必须能完整映射，否则退回源码；
        // 其余属性是不透明数据，原样透传给事件使用方。事件处理器属性（on*）
        // 是行为载体而非数据，出现即拒绝（与 plainSpan 同一安全边界）。
        if (style != null && !isFullySupportedCssStyle(style)) return null
        if (classes != null && !areFullySupportedStyleClasses(classes)) return null
        if (token.attributes.keys.any { it.lowercase().startsWith("on") }) return null
        val opaque = token.attributes.filterKeys { it != "style" && it != "class" }
        return SafeInlineHtmlAction.Span(
            listOf(
                SpanMark(
                    kind = "styled",
                    payload = buildMap {
                        style?.let { put("style", it) }
                        classes?.let { put("class", it) }
                    },
                    attributes = opaque,
                )
            )
        )
    }

    private fun anchor(token: HtmlTagToken): SafeInlineHtmlAction? {
        if (token.kind != HtmlTagKind.OPENING) return null
        if (token.attributes.keys.any { it !in ANCHOR_ATTRIBUTES }) return null
        val target = token.attributes["href"]?.trim()
        if (target != null && !isSafeUrl(target, LINK_SCHEMES)) return null
        return SafeInlineHtmlAction.Span(
            if (target == null) {
                emptyList()
            } else {
                listOf(
                    SpanMark(
                        kind = "link",
                        payload = mapOf("target" to target, "tag" to "link"),
                    )
                )
            }
        )
    }

    private fun image(token: HtmlTagToken): SafeInlineHtmlAction? {
        if (token.attributes.keys.any { it !in IMAGE_ATTRIBUTES }) return null
        val source = token.attributes["src"]?.trim().orEmpty()
        if (!isSafeUrl(source, IMAGE_SCHEMES)) return null
        val width = token.attributes["width"]?.let { parseDimension(it) ?: return null }
        val height = token.attributes["height"]?.let { parseDimension(it) ?: return null }
        return SafeInlineHtmlAction.Image(
            source = source,
            altText = token.attributes["alt"].orEmpty(),
            title = token.attributes["title"],
            width = width,
            height = height,
            attributes = token.attributes,
        )
    }

    private fun parseDimension(value: String): Int? {
        val normalized = value.trim()
        val numeric = if (normalized.endsWith("px", ignoreCase = true)) {
            normalized.dropLast(2)
        } else {
            normalized
        }
        return numeric.toIntOrNull()?.takeIf { it > 0 }
    }

    private fun parseTextAlignStyle(style: String): BlockTextAlignment? {
        val declarations = style.split(';').map { it.trim() }.filter { it.isNotEmpty() }
        if (declarations.isEmpty()) return null
        var alignment: BlockTextAlignment? = null
        for (declaration in declarations) {
            val separator = declaration.indexOf(':')
            if (separator <= 0 || separator == declaration.lastIndex) return null
            val name = declaration.substring(0, separator).trim().lowercase()
            if (name != "text-align") return null
            alignment = parseAlignment(declaration.substring(separator + 1)) ?: return null
        }
        return alignment
    }

    private fun parseAlignment(value: String): BlockTextAlignment? = when (value.trim().lowercase()) {
        "left" -> BlockTextAlignment.LEFT
        "start" -> BlockTextAlignment.START
        "center" -> BlockTextAlignment.CENTER
        "right" -> BlockTextAlignment.RIGHT
        "end" -> BlockTextAlignment.END
        else -> null
    }

    private fun isSafeUrl(value: String, allowedSchemes: Set<String>): Boolean {
        val target = value.trim()
        if (target.isEmpty() || target.any { it.code <= 0x20 }) return false

        val colon = target.indexOf(':')
        val firstPathDelimiter = listOf(target.indexOf('/'), target.indexOf('?'), target.indexOf('#'))
            .filter { it >= 0 }
            .minOrNull()
        if (colon < 0 || (firstPathDelimiter != null && colon > firstPathDelimiter)) return true

        val scheme = target.substring(0, colon)
        if (!scheme.matches(SCHEME_REGEX)) return false
        return scheme.lowercase() in allowedSchemes
    }

    private val INLINE_CONTAINER_TAGS = setOf(
        "strong", "b", "em", "i", "del", "s", "strike", "mark", "sup", "sub",
        "ins", "u", "code", "kbd", "span", "a",
    )
    private val INLINE_TAGS = INLINE_CONTAINER_TAGS + setOf("br", "img")
    // SPAN_ATTRIBUTES 不再作为拒绝依据：未知属性一律作为不透明数据透传。
    private val ANCHOR_ATTRIBUTES = setOf("href")
    private val IMAGE_ATTRIBUTES = setOf("src", "alt", "title", "width", "height")
    private val BLOCK_ATTRIBUTES = setOf("align", "style")
    private val SCHEME_REGEX = Regex("[a-zA-Z][a-zA-Z0-9+.-]*")
    private val LINK_SCHEMES = setOf("http", "https", "mailto", "tel")
    private val IMAGE_SCHEMES = setOf("http", "https")
}
