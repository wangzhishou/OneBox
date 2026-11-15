package com.hrm.markdown.renderer.internal.layout.engine

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.markdown.renderer.internal.core.model.BibliographyDefinitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionDescriptionBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionListBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionTermBlockModel
import com.hrm.markdown.renderer.internal.core.model.FallbackLeafBlockModel
import com.hrm.markdown.renderer.internal.core.model.FigureBlockModel
import com.hrm.markdown.renderer.internal.core.model.HeadingBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.InlineModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.PageBreakBlockModel
import com.hrm.markdown.renderer.internal.core.model.ParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.TabBlockModel
import com.hrm.markdown.renderer.internal.core.model.TableBlockModel
import com.hrm.markdown.renderer.internal.core.model.ThematicBreakBlockModel
import com.hrm.markdown.renderer.internal.core.model.TocBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens

internal fun LayoutEnvironment.measureInlineBlock(
    model: InlineModel,
    style: TextStyle,
    widthPx: Float,
): Float {
    if (widthPx <= 0f) return 0f
    val inlineResult = inlineLayoutRuntime.renderResult(
        model = model,
        style = style,
        epoch = inlineLayoutEpoch,
        theme = markdownTheme,
        directiveRegistry = directiveRegistry,
        onLinkClick = onLinkClick,
        onFootnoteClick = onFootnoteClick,
        latexMeasurer = latexMeasurer,
        density = density,
        textMeasurer = textMeasurer,
    )
    return inlineLayoutRuntime.flowLayout(
        identity = model.identity,
        inlineResult = inlineResult,
        style = style,
        epoch = inlineLayoutEpoch,
        density = density,
        textMeasurer = textMeasurer,
        widthPx = widthPx,
        maxLines = Int.MAX_VALUE,
    ).heightPx
}

internal fun LayoutEnvironment.measureLeafBlockContentHeight(
    block: InternalRenderBlockModel,
    widthPx: Float,
): Float = when (block) {
    is ParagraphBlockModel -> measureInlineBlock(
        block.inline,
        markdownTheme.bodyStyle,
        widthPx,
    )

    is HtmlParagraphBlockModel -> measureInlineBlock(
        block.inline,
        markdownTheme.bodyStyle.withBlockTextAlignment(block.textAlignment),
        widthPx,
    )

    is HeadingBlockModel -> {
        val style = markdownTheme.headingStyles[(block.level - 1).coerceIn(
            0,
            markdownTheme.headingStyles.lastIndex
        )]
        measureInlineBlock(block.inline, style, widthPx) + if (block.level <= 2) {
            with(density) {
                LayoutTokens.HeadingDividerSpacing.toPx() + markdownTheme.layout.dividerThickness.toPx()
            }
        } else {
            0f
        }
    }

    is TableBlockModel -> measureTableBlockContentHeight(block, widthPx)
    is DefinitionListBlockModel -> measureDefinitionListContentHeight(block, widthPx)
    is TocBlockModel -> measureTocContentHeight(block, widthPx)
    is HtmlBlockModel -> textMeasurer.measure(
        text = block.html.trimEnd('\n'),
        style = markdownTheme.codeBlockStyle.copy(fontFamily = FontFamily.Monospace),
        constraints = Constraints(maxWidth = widthPx.toInt().coerceAtLeast(1)),
    ).size.height.toFloat()

    is BibliographyDefinitionBlockModel -> if (block.entries.isEmpty()) {
        0f
    } else {
        block.entries.size * lineHeightPx(markdownTheme.bodyStyle) + with(density) {
            LayoutTokens.BibliographyTitleSpacing.toPx() +
                LayoutTokens.BibliographyEntryVerticalPadding.toPx() * block.entries.size * 2f
        } + lineHeightPx(markdownTheme.headingStyles.getOrElse(3) { markdownTheme.bodyStyle })
    }

    is FigureBlockModel -> with(density) {
        block.imageHeight?.dp?.toPx() ?: LayoutTokens.FigureFallbackHeight.toPx()
    } + if (block.caption.isNotBlank()) lineHeightPx(markdownTheme.bodyStyle) else 0f
    is PageBreakBlockModel -> textMeasurer.measure(
        text = "— Page Break —",
        style = TextStyle(fontSize = 10.sp),
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
        maxLines = 1,
        softWrap = false,
    ).size.height.toFloat() + with(density) { LayoutTokens.PageBreakVerticalPadding.toPx() * 2f }
    is ThematicBreakBlockModel -> with(density) { markdownTheme.layout.dividerThickness.toPx() }
    is TabBlockModel -> lineHeightPx(markdownTheme.bodyStyle) + with(density) {
        LayoutTokens.TabTitleVerticalPadding.toPx() * 2f +
            LayoutTokens.TabContentPadding.toPx() * 2f
    }
    is FallbackLeafBlockModel -> 0f
    else -> lineHeightPx(markdownTheme.bodyStyle)
}

private fun LayoutEnvironment.measureTableBlockContentHeight(
    block: TableBlockModel,
    widthPx: Float,
): Float {
    if (block.rows.isEmpty()) return lineHeightPx(markdownTheme.bodyStyle)
    val columnCount = tableColumnCount(block)
    val horizontalPadding = with(density) { markdownTheme.layout.tableCellPadding.toPx() } * 2f
    val columnWidths = computeTableColumnWidths(block, widthPx)
    return block.rows.sumOf { row ->
        var rowHeight = lineHeightPx(markdownTheme.bodyStyle) + horizontalPadding
        for (colIndex in 0 until columnCount) {
            val cell = row.cells.getOrNull(colIndex) ?: continue
            val alignment = block.columnAlignments.getOrElse(colIndex) {
                com.hrm.markdown.parser.ast.Table.Alignment.NONE
            }
            val style = tableCellTextStyle(alignment, row.isHeader)
            val cellWidth = columnWidths.getOrElse(colIndex) { 0f }
            rowHeight = maxOf(
                rowHeight,
                measureInlineBlock(
                    model = cell.inline,
                    style = style,
                    widthPx = (cellWidth - horizontalPadding).coerceAtLeast(
                        with(density) { LayoutTokens.MinimumInlineMeasureWidth.toPx() }
                    ),
                ) + horizontalPadding,
            )
        }
        rowHeight.toDouble()
    }.toFloat()
}

private fun LayoutEnvironment.measureDefinitionListContentHeight(
    block: DefinitionListBlockModel,
    widthPx: Float,
): Float {
    val indent = with(density) { LayoutTokens.DefinitionIndent.toPx() }
    val spacing = with(density) { LayoutTokens.DefinitionSpacing.toPx() }
    return block.items.sumOf { item ->
        when (item) {
            is DefinitionTermBlockModel -> {
                measureInlineBlock(
                    model = item.inline,
                    style = markdownTheme.bodyStyle.copy(fontWeight = FontWeight.Bold),
                    widthPx = widthPx,
                ).toDouble()
            }

            is DefinitionDescriptionBlockModel -> {
                item.children.sumOf { child ->
                    measureLeafBlockContentHeight(
                        child,
                        (widthPx - indent).coerceAtLeast(0f)
                    ).toDouble()
                } + spacing
            }
        }
    }.toFloat().coerceAtLeast(lineHeightPx(markdownTheme.bodyStyle))
}

private fun LayoutEnvironment.measureTocContentHeight(
    block: TocBlockModel,
    widthPx: Float,
): Float {
    if (block.entries.isEmpty()) return 0f
    return block.entries.sumOf { entry ->
        val indentWidth = with(density) {
            LayoutTokens.TocIndentPerLevel.toPx() * (entry.level - 1).coerceAtLeast(0)
        }
        measureTocEntryHeight(
            entry,
            (widthPx - indentWidth).coerceAtLeast(
                with(density) { LayoutTokens.MinimumTocMeasureWidth.toPx() }
            ),
        ).toDouble()
    }.toFloat()
}

internal fun LayoutEnvironment.measureTocEntryHeight(
    entry: com.hrm.markdown.renderer.internal.core.model.TocEntryBlockModel,
    widthPx: Float,
): Float {
    val stableId =
        com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromText(entry.text)
    val pseudoModel = InlineModel(
        identity = com.hrm.markdown.renderer.internal.core.identity.RenderIdentity(
            stableId = stableId,
            contentRevision = 0L,
            layoutRevision = 0L,
            paintRevision = 0L,
        ),
        atoms = listOf(
            com.hrm.markdown.renderer.internal.core.model.TextAtom(
                identity = com.hrm.markdown.renderer.internal.core.identity.RenderIdentity(
                    stableId = stableId,
                    contentRevision = 0L,
                    layoutRevision = 0L,
                    paintRevision = 0L,
                ),
                text = "• ${entry.text}",
            )
        ),
    )
    return measureInlineBlock(
        model = pseudoModel,
        style = markdownTheme.bodyStyle,
        widthPx = widthPx,
    )
}

internal fun LayoutEnvironment.lineHeightPx(style: TextStyle): Float {
    val value = style.lineHeight.value.takeUnless { it.isNaN() }
        ?: style.fontSize.value.takeUnless { it.isNaN() }?.times(1.5f)
        ?: 0f
    return with(density) { value.sp.toPx() }.coerceAtLeast(0f)
}
