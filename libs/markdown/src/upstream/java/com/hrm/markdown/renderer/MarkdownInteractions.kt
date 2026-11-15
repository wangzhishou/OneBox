package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import com.hrm.codehigh.ast.CodeToken

/** Typed host actions. Replacing handlers does not reparse or remeasure Markdown. */
@Immutable
data class MarkdownInteractions(
    val onLinkClick: ((String) -> Unit)? = null,
    val onCitationClick: ((MarkdownCitationClickEvent) -> Unit)? = null,
    /**
     * 图片点击的专属监听：携带图集懒加载等图片特有能力，优先于
     * [onInlineElementClick]（两者同时提供时图片走本回调）。为 null（默认）时
     * 若提供了 [onInlineElementClick]，图片点击以 `kind = "image"` 进入统一分发。
     */
    val onImageClick: ((MarkdownImageClickEvent) -> Unit)? = null,
    /** Matches codehigh's callback. Token ranges are relative to the containing code block. */
    val onCodeTokenClick: ((CodeToken) -> Unit)? = null,
    /**
     * 统一的行内元素点击监听：所有带语义 span mark 的行内 run（`==highlight==`、`<mark>`、
     * 缩写、上标等）点击时触发；不改变这些 run 的默认渲染。[kind] 取值见
     * [InlineElementKinds]。为 null（默认）时行内点击不产生事件。
     */
    val onInlineElementClick: ((MarkdownInlineElementClickEvent) -> Unit)? = null,
    /**
     * 行内元素自定义装饰（如划线的实线/虚线下划线）。与 [onInlineElementClick] 同源分发，
     * 每个语义 run 绘制前询问一次；返回 null 表示使用默认渲染。为 null（默认）时无装饰。
     */
    val inlineDecoration: ((MarkdownInlineDecorationRequest) -> MarkdownInlineUnderline?)? = null,
) {
    companion object {
        val Default = MarkdownInteractions()
    }
}

/** 行内元素点击事件的 [MarkdownInlineElementClickEvent.kind] 取值。 */
object InlineElementKinds {
    const val Image = "image"
    const val Highlight = "highlight"
    const val Citation = "citation"
    const val Footnote = "footnote"
    const val Abbreviation = "abbreviation"
    const val Spoiler = "spoiler"
    const val InlineHtml = "inline_html"
    const val KeyboardInput = "kbd"
}

/**
 * 一次行内元素点击。同一语义 run 若内含多种格式（如加粗的划线），会按格式边界拆成相邻事件；
 * [text] 是该 run 的纯文本。[payload] 来自该 run 携带的 mark 数据（如 citation 的 key、
 * abbreviation 的 fullText），供宿主在无 AST 访问能力时还原语义。
 */
@Immutable
data class MarkdownInlineElementClickEvent(
    val kind: String,
    val text: String,
    val payload: Map<String, String> = emptyMap(),
    /** 元素原始属性，库不解释；使用方按自己的约定读取。 */
    val attributes: Map<String, String> = emptyMap(),
    val occurrenceId: Long,
    val documentRevision: Long,
    /** 窗口坐标（像素）；无几何信息（如无障碍路径）时为 null。 */
    val anchorBoundsInWindow: Rect? = null,
    /** `kind == "image"` 时可安全转型，用于读取图集等图片特有能力。 */
    val image: MarkdownImageClickEvent? = null,
)

/** Identifies an occurrence within the current rendered document, not a persistent database ID. */
@Immutable
data class MarkdownCitationClickEvent(
    val key: String,
    val occurrenceId: Long,
    val documentRevision: Long,
    /** Window coordinates in pixels; null if geometry is unavailable (e.g. accessibility). */
    val anchorBoundsInWindow: Rect?,
)

@Immutable
data class MarkdownImageOccurrence(val occurrenceId: Long, val image: MarkdownImageData)

data class MarkdownImageGallery(
    val images: List<MarkdownImageOccurrence>,
    val initialIndex: Int,
)

/** Image loading and preview UI remain owned by the host. The gallery is built only on demand. */
class MarkdownImageClickEvent internal constructor(
    val image: MarkdownImageData,
    val occurrenceId: Long,
    val documentRevision: Long,
    val anchorBoundsInWindow: Rect?,
    gallery: () -> MarkdownImageGallery,
) {
    /** A document-order snapshot, including offscreen images and repeated URLs. */
    val gallery: MarkdownImageGallery by lazy(gallery)
}
