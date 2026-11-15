package com.hrm.markdown.renderer

import androidx.compose.runtime.Immutable
import com.hrm.markdown.renderer.internal.core.model.*
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityMix
import com.hrm.markdown.renderer.inline.InteractiveInlineMarkKinds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 行内元素装饰请求。与 [MarkdownInlineElementClickEvent] 同源分发：
 * 命中语义 mark（`==highlight==`、`<mark>` 等）的末端在排版前解析 painter；
 * 文本 run 在绘制前解析装饰线，
 * 返回 null 表示保持默认渲染。
 */
@Immutable
data class MarkdownInlineDecorationRequest(
    val kind: String,
    val text: String,
    val payload: Map<String, String> = emptyMap(),
    /** 元素原始属性，库不解释；使用方按自己的约定读取。 */
    val attributes: Map<String, String> = emptyMap(),
)

/**
 * 绘制在文本下方的装饰线。跨行文本按可见行逐行绘制，
 * 每行只覆盖该行内实际存在的字符区间。
 *
 * 三种线型按以下优先级生效：圆点（[dotDiameter]）→ 虚线（[dashWidth] + [gapWidth]）→ 实线。
 */
@Immutable
data class MarkdownInlineUnderline(
    val color: Color,
    val strokeWidth: Dp = 1.dp,
    /** 虚线段长；与 [gapWidth] 同时提供时绘制虚线，否则为实线。 */
    val dashWidth: Dp? = null,
    /** 虚线间隔。 */
    val gapWidth: Dp? = null,
    /** 圆点直径。提供时绘制一系列圆点（点线），忽略 [dashWidth]/[gapWidth]。 */
    val dotDiameter: Dp? = null,
    /** 圆点之间的间隔，仅在 [dotDiameter] 提供时生效。 */
    val dotGap: Dp = 3.dp,
    /** 相对文本行底边向上抬升的距离。 */
    val offset: Dp = 2.dp,
    /** 仅在每个语义范围的最后一个文字端点绘制；painter 真实占据行内宽度，不增加源字符或复制偏移。 */
    val endMarker: MarkdownInlineEndMarker? = null,
) {
    /** 圆点连成实线（[dotGap] 小于直径时视觉连续）；供只想区分线型的调用方快速构造。 */
    companion object {
        fun solid(color: Color, strokeWidth: Dp = 1.dp, offset: Dp = 2.dp) =
            MarkdownInlineUnderline(color = color, strokeWidth = strokeWidth, offset = offset)
    }
}

/** 装饰线末端的行内标记；未提供 [painter] 时保持原有圆点样式。 */
@Immutable
data class MarkdownInlineEndMarker(
    val color: Color,
    val diameter: Dp = 4.dp,
    /** 标记与文字末端的间距。 */
    val gap: Dp = 3.dp,
    /** 绘制在 [diameter] 大小的方框内，由 [color] 着色，真实占据 gap + diameter 的宽度，不插入源字符或可复制字符。 */
    val painter: Painter? = null,
)

internal fun resolveInlineEndMarkers(document: InternalRenderDocumentModel, decorate: ((MarkdownInlineDecorationRequest) -> MarkdownInlineUnderline?)?): InternalRenderDocumentModel {
    if (decorate == null) return document
    fun id(v: RenderIdentity, c: Any) = v.copy(layoutRevision = renderIdentityMix(v.layoutRevision, c.hashCode().toLong()), paintRevision = renderIdentityMix(v.paintRevision, c.hashCode().toLong()))
    fun inline(m: InlineModel): InlineModel {
        val atoms = buildList<InlineAtom> {
            for (atom in m.atoms) {
                add(atom)
                if (atom !is TextAtom || atom.text.isEmpty()) continue
                val lastInteractiveIndex = atom.marks.indexOfLast { it.kind in InteractiveInlineMarkKinds }
                for (markIndex in lastInteractiveIndex downTo 0) {
                    val mark = atom.marks[markIndex]
                    if (mark.kind !in InteractiveInlineMarkKinds || !mark.isRangeEnd) continue
                    val marker = decorate(MarkdownInlineDecorationRequest(mark.kind, atom.text, mark.payload, mark.attributes))?.endMarker?.takeIf { it.painter != null } ?: continue
                    require(marker.diameter.value.isFinite() && marker.diameter.value > 0f)
                    require(marker.gap.value.isFinite() && marker.gap.value >= 0f)
                    // 用原 mark 下标区分共终点范围，不依赖回调结果；最内层保留旧 ID。
                    val seed = 0x454e444d41524bL + markIndex - lastInteractiveIndex
                    val identity = id(atom.identity, marker).copy(stableId = renderIdentityMix(atom.identity.stableId, seed))
                    add(WidgetAtom(identity, InlineEndMarkerWidgetModel(identity, marker)))
                }
            }
        }
        val identity = id(m.identity, atoms)
        return if (atoms == m.atoms) m else m.copy(identity = identity.copy(contentRevision = identity.layoutRevision), atoms = atoms)
    }
    fun block(b: InternalRenderBlockModel): InternalRenderBlockModel = when (b) {
        is ParagraphBlockModel -> inline(b.inline).let { if (it == b.inline) b else b.copy(identity = id(b.identity, it), inline = it) }
        is HtmlParagraphBlockModel -> inline(b.inline).let { if (it == b.inline) b else b.copy(identity = id(b.identity, it), inline = it) }
        is HeadingBlockModel -> inline(b.inline).let { if (it == b.inline) b else b.copy(identity = id(b.identity, it), inline = it) }
        is BlockQuoteBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is AdmonitionBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is HtmlContainerBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is CustomContainerBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is FootnoteDefinitionBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is DirectiveBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is FallbackContainerBlockModel -> b.children.map(::block).let { if (it == b.children) b else b.copy(identity = id(b.identity, it), children = it) }
        is ListBlockModel -> b.items.map { it.copy(children = it.children.map(::block)) }.let { if (it == b.items) b else b.copy(identity = id(b.identity, it), items = it) }
        is ColumnsLayoutBlockModel -> b.columns.map { it.copy(children = it.children.map(::block)) }.let { if (it == b.columns) b else b.copy(identity = id(b.identity, it), columns = it) }
        is TabBlockModel -> b.items.map { it.copy(children = it.children.map(::block)) }.let { if (it == b.items) b else b.copy(identity = id(b.identity, it), items = it) }
        is DefinitionListBlockModel -> b.items.map {
            when (it) {
                is DefinitionTermBlockModel -> inline(it.inline).let { content ->
                    if (content == it.inline) it else it.copy(identity = id(it.identity, content), inline = content)
                }
                is DefinitionDescriptionBlockModel -> it.children.map(::block).let { children ->
                    if (children == it.children) it else it.copy(identity = id(it.identity, children), children = children)
                }
            }
        }.let { if (it == b.items) b else b.copy(identity = id(b.identity, it), items = it) }
        is TableBlockModel -> b.rows.map { row -> row.copy(cells = row.cells.map { it.copy(inline = inline(it.inline)) }) }.let { if (it == b.rows) b else b.copy(identity = id(b.identity, it), rows = it) }
        else -> b
    }
    val blocks = document.blocks.map(::block)
    return if (blocks == document.blocks) document else document.copy(identity = id(document.identity, blocks), blocks = blocks)
}
