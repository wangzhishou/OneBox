package com.hrm.markdown.renderer.internal.layout.engine

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hrm.markdown.parser.ast.Table
import com.hrm.markdown.renderer.internal.core.identity.RenderIdentity
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromText
import com.hrm.markdown.renderer.internal.core.identity.renderIdentityFromValues
import com.hrm.markdown.renderer.internal.core.model.AdmonitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.BibliographyDefinitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.BlockQuoteBlockModel
import com.hrm.markdown.renderer.internal.core.model.BlockWidgetModel
import com.hrm.markdown.renderer.internal.core.model.CodeBlockModel
import com.hrm.markdown.renderer.internal.core.model.ColumnsLayoutBlockModel
import com.hrm.markdown.renderer.internal.core.model.CustomContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionDescriptionBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionListBlockModel
import com.hrm.markdown.renderer.internal.core.model.DefinitionTermBlockModel
import com.hrm.markdown.renderer.internal.core.model.DiagramBlockModel
import com.hrm.markdown.renderer.internal.core.model.DirectiveBlockModel
import com.hrm.markdown.renderer.internal.core.model.FallbackContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.FigureBlockModel
import com.hrm.markdown.renderer.internal.core.model.FootnoteDefinitionBlockModel
import com.hrm.markdown.renderer.internal.core.model.HeadingBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlContainerBlockModel
import com.hrm.markdown.renderer.internal.core.model.HtmlParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.InlineModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderDocumentModel
import com.hrm.markdown.renderer.internal.core.model.ListBlockModel
import com.hrm.markdown.renderer.internal.core.model.MathBlockModel
import com.hrm.markdown.renderer.internal.core.model.PageBreakBlockModel
import com.hrm.markdown.renderer.internal.core.model.ParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.TabBlockModel
import com.hrm.markdown.renderer.internal.core.model.TableBlockModel
import com.hrm.markdown.renderer.internal.core.model.TextAtom
import com.hrm.markdown.renderer.internal.core.model.ThematicBreakBlockModel
import com.hrm.markdown.renderer.internal.core.model.TocBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens
import com.hrm.markdown.renderer.internal.layout.columns.resolveColumnWidths
import com.hrm.markdown.renderer.internal.layout.inline.buildInlineLayoutBlockFromModel
import com.hrm.markdown.renderer.internal.layout.list.OrderedListMarkerWidth
import com.hrm.markdown.renderer.internal.layout.list.listItemContentIndentPx
import com.hrm.markdown.renderer.internal.layout.list.orderedListMarkerDigitCount
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentMetadata
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutDocumentModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutBibliographyBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutBibliographyEntryGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutColumnGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutColumnsBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutDefinitionDescriptionGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutDefinitionListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutDefinitionTermGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutFigureBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutFootnoteBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutInlineBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutInsets
import com.hrm.markdown.renderer.internal.layout.model.LayoutListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutListItemGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutRect
import com.hrm.markdown.renderer.internal.layout.model.LayoutRenderBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutSize
import com.hrm.markdown.renderer.internal.layout.model.LayoutTabBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTabGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutTableBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTableCellGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutTableRowGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutTocBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTocEntryGroup
import com.hrm.markdown.renderer.internal.layout.model.LayoutWidgetBlockModel
import com.hrm.markdown.renderer.internal.layout.widget.measureBlockWidget
import kotlin.math.max
import kotlin.math.roundToInt

internal object DefaultMarkdownLayoutEngine : MarkdownLayoutEngine {
    override fun layout(
        document: InternalRenderDocumentModel,
        environment: LayoutEnvironment,
    ): InternalLayoutDocumentModel {
        val (blocks, endY) = layoutBlocks(
            blocks = document.blocks,
            left = 0f,
            top = 0f,
            width = environment.viewportWidth,
            environment = environment,
        )
        return InternalLayoutDocumentModel(
            identity = document.identity,
            blocks = blocks,
            totalSize = LayoutSize(
                width = environment.viewportWidth,
                height = endY.coerceAtLeast(0f),
            ),
            metadata = metadata(document),
        )
    }

    override fun layoutBlock(
        block: InternalRenderBlockModel,
        environment: LayoutEnvironment,
    ): InternalLayoutBlockModel = layoutBlockInternal(
        block = block,
        left = 0f,
        top = 0f,
        width = environment.viewportWidth,
        environment = environment,
    )

    override fun metadata(document: InternalRenderDocumentModel): InternalLayoutDocumentMetadata =
        InternalLayoutDocumentMetadata(
            footnoteDefinitionItemIndexes = buildMap {
                document.blocks.forEachIndexed { index, block ->
                    val label = (block as? FootnoteDefinitionBlockModel)?.label
                        ?: return@forEachIndexed
                    put(label, index)
                }
            },
        )
}

private fun layoutBlocks(
    blocks: List<InternalRenderBlockModel>,
    left: Float,
    top: Float,
    width: Float,
    environment: LayoutEnvironment,
): Pair<List<InternalLayoutBlockModel>, Float> {
    val result = ArrayList<InternalLayoutBlockModel>(blocks.size)
    var cursorY = top
    blocks.forEachIndexed { index, block ->
        val layoutBlock = layoutBlockInternal(
            block = block,
            left = left,
            top = cursorY,
            width = width,
            environment = environment,
        )
        result += layoutBlock
        cursorY = layoutBlock.frame.top + layoutBlock.frame.height
        if (index != blocks.lastIndex) {
            cursorY += environment.blockSpacing
        }
    }
    return result to cursorY
}

private fun layoutBlockInternal(
    block: InternalRenderBlockModel,
    left: Float,
    top: Float,
    width: Float,
    environment: LayoutEnvironment,
): InternalLayoutBlockModel {
    val insets = blockInsets(block, environment)
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    return when (block) {
        is CodeBlockModel -> layoutWidgetBlock(
            block,
            block.widget,
            left,
            top,
            width,
            insets,
            environment
        )

        is MathBlockModel -> layoutWidgetBlock(
            block,
            block.widget,
            left,
            top,
            width,
            insets,
            environment
        )

        is DiagramBlockModel -> layoutWidgetBlock(
            block,
            block.widget,
            left,
            top,
            width,
            insets,
            environment
        )

        is BlockQuoteBlockModel -> layoutContainerBlock(
            block,
            block.children,
            left,
            top,
            width,
            insets,
            environment
        )

        is AdmonitionBlockModel -> layoutContainerBlock(
            block,
            block.children,
            left,
            top,
            width,
            insets,
            environment,
            headerHeight = environment.containerHeaderHeight(block.children.isNotEmpty())
        )

        is CustomContainerBlockModel -> layoutContainerBlock(
            block,
            block.children,
            left,
            top,
            width,
            insets,
            environment,
            headerHeight = if (block.title.isNotBlank() || block.type.isNotBlank()) {
                environment.containerHeaderHeight(block.children.isNotEmpty())
            } else {
                0f
            },
        )

        is FootnoteDefinitionBlockModel -> layoutFootnoteBlock(
            block,
            left,
            top,
            width,
            insets,
            environment
        )

        is DirectiveBlockModel -> layoutContainerBlock(
            block,
            block.children,
            left,
            top,
            width,
            insets,
            environment,
            headerHeight = environment.directiveHeaderHeight(block.children.isNotEmpty())
        )

        is FallbackContainerBlockModel -> layoutContainerBlock(
            block,
            block.children,
            left,
            top,
            width,
            insets,
            environment
        )

        is HtmlContainerBlockModel -> layoutContainerBlock(
            block,
            block.children,
            left,
            top,
            width,
            insets,
            environment,
        )

        is ColumnsLayoutBlockModel -> {
            val spacing = with(environment.density) { LayoutTokens.ColumnsSpacing.toPx() }
            val columnWidths = resolveColumnWidths(
                values = block.columns.map { it.width },
                totalWidthPx = contentWidth,
                spacingPx = spacing,
            )
            var maxColumnBottom = contentTop
            var columnLeft = contentLeft
            val columnGroups = block.columns.mapIndexed { index, column ->
                val columnWidth = columnWidths.getOrElse(index) { 0f }
                val (columnChildren, columnBottom) = layoutBlocks(
                    column.children,
                    columnLeft,
                    contentTop,
                    columnWidth,
                    environment
                )
                maxColumnBottom = max(maxColumnBottom, columnBottom)
                LayoutColumnGroup(
                    identity = column.identity,
                    frame = LayoutRect(
                        left = columnLeft,
                        top = contentTop,
                        width = columnWidth,
                        height = (columnBottom - contentTop).coerceAtLeast(0f),
                    ),
                    contentFrame = LayoutRect(
                        left = columnLeft,
                        top = contentTop,
                        width = columnWidth,
                        height = (columnBottom - contentTop).coerceAtLeast(0f),
                    ),
                    width = column.width,
                    children = columnChildren,
                ).also {
                    columnLeft += columnWidth + spacing
                }
            }
            val contentHeight = (maxColumnBottom - contentTop).coerceAtLeast(0f)
            LayoutColumnsBlockModel(
                identity = block.identity,
                frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
                contentFrame = LayoutRect(contentLeft, contentTop, contentWidth, contentHeight),
                block = block,
                columns = columnGroups,
            )
        }

        is ListBlockModel -> {
            val orderedMarkerWidth = if (block.ordered) {
                with(environment.density) {
                    environment.orderedListMarkerMetrics.widthPx(
                        digitCount = orderedListMarkerDigitCount(block.startNumber, block.items.size),
                        density = environment.density,
                    ).toDp()
                }
            } else {
                OrderedListMarkerWidth
            }
            var itemCursorY = contentTop
            val itemGroups = block.items.mapIndexed { index, item ->
                val itemIndent = environment.density.listItemContentIndentPx(
                    theme = environment.markdownTheme,
                    taskListItem = item.taskListItem,
                    ordered = block.ordered,
                    orderedMarkerWidth = orderedMarkerWidth,
                )
                val itemTop = itemCursorY
                val (itemChildren, itemBottom) = layoutBlocks(
                    item.children,
                    contentLeft + itemIndent,
                    itemCursorY,
                    (contentWidth - itemIndent).coerceAtLeast(0f),
                    environment,
                )
                itemCursorY = itemBottom
                if (index != block.items.lastIndex) {
                    itemCursorY += if (block.tight) {
                        with(environment.density) { LayoutTokens.ListTightItemSpacing.toPx() }
                    } else {
                        environment.blockSpacing
                    }
                }
                LayoutListItemGroup(
                    identity = item.identity,
                    frame = LayoutRect(
                        left = contentLeft,
                        top = itemTop,
                        width = contentWidth,
                        height = (itemBottom - itemTop).coerceAtLeast(0f),
                    ),
                    contentFrame = LayoutRect(
                        left = contentLeft + itemIndent,
                        top = itemTop,
                        width = (contentWidth - itemIndent).coerceAtLeast(0f),
                        height = (itemBottom - itemTop).coerceAtLeast(0f),
                    ),
                    markerText = when {
                        item.taskListItem -> ""
                        block.ordered -> "${block.startNumber.toLong() + index}."
                        else -> "•"
                    },
                    taskListItem = item.taskListItem,
                    checked = item.checked,
                    children = itemChildren,
                    markerWidthPx = itemIndent - with(environment.density) { environment.markdownTheme.layout.listIndent.toPx() },
                )
            }
            LayoutListBlockModel(
                identity = block.identity,
                frame = LayoutRect(
                    left,
                    top,
                    width,
                    insets.top + (itemCursorY - contentTop).coerceAtLeast(0f) + insets.bottom
                ),
                contentFrame = LayoutRect(
                    contentLeft,
                    contentTop,
                    contentWidth,
                    (itemCursorY - contentTop).coerceAtLeast(0f)
                ),
                block = block,
                items = itemGroups,
            )
        }

        is TabBlockModel -> {
            val tabHeaderHeight = environment.tabHeaderHeight(block)
            val tabContentPadding = with(environment.density) { LayoutTokens.TabContentPadding.toPx() }
            val tabContentLeft = contentLeft + tabContentPadding
            val tabContentTop = contentTop + tabHeaderHeight + tabContentPadding
            val tabContentWidth = (contentWidth - tabContentPadding * 2f).coerceAtLeast(0f)
            val tabGroups = block.items.map { item ->
                val (tabChildren, bottom) = layoutBlocks(
                    item.children,
                    tabContentLeft,
                    tabContentTop,
                    tabContentWidth,
                    environment,
                )
                LayoutTabGroup(
                    identity = item.identity,
                    frame = LayoutRect(
                        left = tabContentLeft,
                        top = tabContentTop,
                        width = tabContentWidth,
                        height = (bottom - tabContentTop).coerceAtLeast(0f),
                    ),
                    contentFrame = LayoutRect(
                        left = tabContentLeft,
                        top = tabContentTop,
                        width = tabContentWidth,
                        height = (bottom - tabContentTop).coerceAtLeast(0f),
                    ),
                    title = item.title,
                    children = tabChildren,
                )
            }
            val contentHeight = (tabGroups.maxOfOrNull { it.frame.height } ?: 0f) +
                tabHeaderHeight + tabContentPadding * 2f
            LayoutTabBlockModel(
                identity = block.identity,
                frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
                contentFrame = LayoutRect(contentLeft, contentTop, contentWidth, contentHeight),
                block = block,
                tabs = tabGroups,
            )
        }

        is TableBlockModel -> layoutTableBlock(block, left, top, width, insets, environment)
        is DefinitionListBlockModel -> layoutDefinitionListBlock(
            block,
            left,
            top,
            width,
            insets,
            environment
        )

        is FigureBlockModel -> layoutFigureBlock(block, left, top, width, insets, environment)
        is TocBlockModel -> layoutTocBlock(block, left, top, width, insets, environment)
        is BibliographyDefinitionBlockModel -> layoutBibliographyBlock(
            block,
            left,
            top,
            width,
            insets,
            environment
        )

        is ParagraphBlockModel -> layoutInlineBlock(
            identity = block.identity,
            model = block.inline,
            style = environment.markdownTheme.bodyStyle,
            left = left,
            top = top,
            width = width,
            insets = insets,
            environment = environment,
        )

        is HtmlParagraphBlockModel -> layoutInlineBlock(
            identity = block.identity,
            model = block.inline,
            style = environment.markdownTheme.bodyStyle.withBlockTextAlignment(block.textAlignment),
            left = left,
            top = top,
            width = width,
            insets = insets,
            environment = environment,
        )

        is HeadingBlockModel -> {
            val style = environment.markdownTheme.headingStyles[(block.level - 1).coerceIn(
                0,
                environment.markdownTheme.headingStyles.lastIndex
            )]
            layoutInlineBlock(
                identity = block.identity,
                model = block.inline.prependHeadingNumbering(block.numbering),
                style = style,
                left = left,
                top = top,
                width = width,
                insets = insets,
                environment = environment,
                showDivider = block.level <= 2,
            ).copy(isHeading = true)
        }

        else -> {
            val contentHeight = environment.measureLeafBlockContentHeight(block, contentWidth)
            layoutRenderBlock(block, left, top, width, insets, contentHeight, emptyList())
        }
    }
}

private fun layoutContainerBlock(
    block: InternalRenderBlockModel,
    children: List<InternalRenderBlockModel>,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
    headerHeight: Float = 0f,
): LayoutRenderBlockModel {
    val contentLeft = left + insets.left
    val contentTop = top + insets.top + headerHeight
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    val (childLayouts, bottom) = layoutBlocks(
        children,
        contentLeft,
        contentTop,
        contentWidth,
        environment
    )
    val contentHeight = (bottom - contentTop).coerceAtLeast(0f) + headerHeight
    return layoutRenderBlock(block, left, top, width, insets, contentHeight, childLayouts)
}

private fun layoutFootnoteBlock(
    block: FootnoteDefinitionBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutFootnoteBlockModel {
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    val horizontalSpacing = with(environment.density) {
        LayoutTokens.FootnoteHorizontalSpacing.toPx()
    }
    val labelStyle = environment.markdownTheme.bodyStyle.copy(
        fontWeight = FontWeight.SemiBold,
        fontSize = environment.markdownTheme.footnoteStyle.fontSize,
    )
    val arrowStyle = environment.markdownTheme.bodyStyle.copy(
        fontSize = environment.markdownTheme.footnoteStyle.fontSize,
    )
    val labelWidth = environment.textMeasurer.measure(
        text = "[${block.index}]",
        style = labelStyle,
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
        maxLines = 1,
        softWrap = false,
    ).size.width.toFloat()
    val arrowWidth = environment.textMeasurer.measure(
        text = "↩",
        style = arrowStyle,
        constraints = Constraints(maxWidth = Int.MAX_VALUE),
        maxLines = 1,
        softWrap = false,
    ).size.width.toFloat()
    val leadLeft = contentLeft + labelWidth + arrowWidth + horizontalSpacing * 2f
    val leadWidth = (contentWidth - (leadLeft - contentLeft)).coerceAtLeast(0f)
    val leadChild = block.children.firstOrNull()?.let { child ->
        layoutBlockInternal(
            block = child,
            left = leadLeft,
            top = contentTop,
            width = leadWidth,
            environment = environment,
        )
    }
    val leadBottom = leadChild?.let { it.frame.top + it.frame.height } ?: contentTop
    val (trailingChildren, bottom) = layoutBlocks(
        blocks = block.children.drop(1),
        left = contentLeft,
        top = leadBottom,
        width = contentWidth,
        environment = environment,
    )
    val contentHeight = (bottom - contentTop).coerceAtLeast(0f)
    return LayoutFootnoteBlockModel(
        identity = block.identity,
        frame = LayoutRect(
            left = left,
            top = top,
            width = width,
            height = insets.top + contentHeight + insets.bottom,
        ),
        contentFrame = LayoutRect(
            left = contentLeft,
            top = contentTop,
            width = contentWidth,
            height = contentHeight,
        ),
        block = block,
        leadChild = leadChild,
        trailingChildren = trailingChildren,
    )
}

private fun layoutInlineBlock(
    identity: RenderIdentity,
    model: InlineModel,
    style: TextStyle,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
    showDivider: Boolean = false,
): LayoutInlineBlockModel {
    return buildInlineLayoutBlockFromModel(
        identity = identity,
        model = model,
        style = style,
        left = left,
        top = top,
        width = width,
        insets = insets,
        theme = environment.markdownTheme,
        directiveRegistry = environment.directiveRegistry,
        latexMeasurer = environment.latexMeasurer,
        density = environment.density,
        textMeasurer = environment.textMeasurer,
        inlineLayoutRuntime = environment.inlineLayoutRuntime,
        inlineLayoutEpoch = environment.inlineLayoutEpoch,
        onLinkClick = environment.onLinkClick,
        onFootnoteClick = environment.onFootnoteClick,
        showDivider = showDivider,
        layoutDirection = environment.layoutDirection,
    )
}

private fun InlineModel.prependHeadingNumbering(numbering: String?): InlineModel {
    if (numbering.isNullOrBlank()) return this
    val prefix = "$numbering "
    val prefixStableId = renderIdentityFromText(prefix, identity.stableId)
    val prefixIdentity = RenderIdentity(
        stableId = prefixStableId,
        contentRevision = renderIdentityFromValues(identity.contentRevision, prefixStableId),
        layoutRevision = renderIdentityFromValues(identity.layoutRevision, prefixStableId),
        paintRevision = identity.paintRevision,
    )
    return copy(
        identity = RenderIdentity(
            stableId = identity.stableId,
            contentRevision = renderIdentityFromValues(
                identity.contentRevision,
                prefixIdentity.contentRevision
            ),
            layoutRevision = renderIdentityFromValues(
                identity.layoutRevision,
                prefixIdentity.layoutRevision
            ),
            paintRevision = identity.paintRevision,
        ),
        atoms = listOf(TextAtom(identity = prefixIdentity, text = prefix)) + atoms,
    )
}

private fun layoutTableBlock(
    block: TableBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutTableBlockModel {
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    val columnCount = tableColumnCount(block)
    val cellPadding =
        with(environment.density) { environment.markdownTheme.layout.tableCellPadding.toPx() }
    val columnWidths = environment.computeTableColumnWidths(block, contentWidth)
    var cursorY = contentTop
    val rows = block.rows.map { row ->
        var cursorX = contentLeft
        val cells = (0 until columnCount).map { colIndex ->
            val cell = row.cells.getOrNull(colIndex)
            val columnWidth = columnWidths.getOrElse(colIndex) { 0f }
            val cellLeft = cursorX
            cursorX += columnWidth
            val alignment =
                block.columnAlignments.getOrElse(colIndex) { Table.Alignment.NONE }
            val style = environment.tableCellTextStyle(alignment, row.isHeader)
            val innerHeight = cell?.let {
                environment.measureInlineBlock(
                    model = it.inline,
                    style = style,
                    widthPx = (columnWidth - cellPadding * 2f).coerceAtLeast(
                        with(environment.density) { LayoutTokens.MinimumInlineMeasureWidth.toPx() }
                    ),
                )
            } ?: environment.lineHeightPx(environment.markdownTheme.bodyStyle)
            LayoutTableCellGroup(
                identity = cell?.identity ?: row.identity,
                frame = LayoutRect(
                    left = cellLeft,
                    top = cursorY,
                    width = columnWidth,
                    height = innerHeight + cellPadding * 2f,
                ),
                contentFrame = LayoutRect(
                    left = cellLeft + cellPadding,
                    top = cursorY + cellPadding,
                    width = (columnWidth - cellPadding * 2f).coerceAtLeast(0f),
                    height = innerHeight,
                ),
                cell = cell,
                alignmentOrdinal = block.columnAlignments.getOrElse(colIndex) { Table.Alignment.NONE }.ordinal,
                isHeader = row.isHeader,
            )
        }
        val rowHeight = cells.maxOfOrNull { it.frame.height } ?: 0f
        val normalizedCells = cells.map { cell ->
            cell.copy(frame = cell.frame.copy(height = rowHeight))
        }
        val tableWidth = columnWidths.sum()
        val rowGroup = LayoutTableRowGroup(
            identity = row.identity,
            frame = LayoutRect(contentLeft, cursorY, tableWidth, rowHeight),
            contentFrame = LayoutRect(contentLeft, cursorY, tableWidth, rowHeight),
            isHeader = row.isHeader,
            cells = normalizedCells,
        )
        cursorY += rowHeight
        rowGroup
    }
    val contentHeight = (cursorY - contentTop).coerceAtLeast(0f)
    return LayoutTableBlockModel(
        identity = block.identity,
        frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
        contentFrame = LayoutRect(contentLeft, contentTop, columnWidths.sum(), contentHeight),
        block = block,
        columnWidths = columnWidths,
        rows = rows,
    )
}

private fun layoutDefinitionListBlock(
    block: DefinitionListBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutDefinitionListBlockModel {
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    val indent = with(environment.density) { LayoutTokens.DefinitionIndent.toPx() }
    val spacing = with(environment.density) { LayoutTokens.DefinitionSpacing.toPx() }
    var cursorY = contentTop
    val items = block.items.map { item ->
        when (item) {
            is DefinitionTermBlockModel -> {
                val inline = layoutInlineBlock(
                    identity = item.identity,
                    model = item.inline,
                    style = environment.markdownTheme.bodyStyle.copy(fontWeight = FontWeight.Bold),
                    left = contentLeft,
                    top = cursorY,
                    width = contentWidth,
                    insets = LayoutInsets(),
                    environment = environment,
                )
                val group = LayoutDefinitionTermGroup(
                    identity = item.identity,
                    frame = inline.frame,
                    contentFrame = inline.contentFrame,
                    item = item,
                    inline = inline,
                )
                cursorY += inline.frame.height + spacing
                group
            }

            is DefinitionDescriptionBlockModel -> {
                val (children, bottom) = layoutBlocks(
                    item.children,
                    contentLeft + indent,
                    cursorY,
                    (contentWidth - indent).coerceAtLeast(0f),
                    environment
                )
                val height = (bottom - cursorY).coerceAtLeast(0f)
                val group = LayoutDefinitionDescriptionGroup(
                    identity = item.identity,
                    frame = LayoutRect(contentLeft, cursorY, contentWidth, height),
                    contentFrame = LayoutRect(
                        contentLeft + indent,
                        cursorY,
                        (contentWidth - indent).coerceAtLeast(0f),
                        height
                    ),
                    item = item,
                    children = children,
                )
                cursorY = bottom + spacing
                group
            }
        }
    }
    val contentHeight = (cursorY - contentTop - spacing).coerceAtLeast(0f)
    return LayoutDefinitionListBlockModel(
        identity = block.identity,
        frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
        contentFrame = LayoutRect(contentLeft, contentTop, contentWidth, contentHeight),
        block = block,
        items = items,
    )
}

private fun layoutFigureBlock(
    block: FigureBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutFigureBlockModel {
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    val imageHeight = with(environment.density) {
        block.imageHeight?.dp?.toPx() ?: LayoutTokens.FigureFallbackHeight.toPx()
    }
    val imageWidth = with(environment.density) {
        block.imageWidth?.dp?.toPx()?.coerceAtMost(contentWidth) ?: contentWidth
    }
    val imageFrame = LayoutRect(
        left = contentLeft + ((contentWidth - imageWidth) / 2f).coerceAtLeast(0f),
        top = contentTop,
        width = imageWidth,
        height = imageHeight,
    )
    val captionHorizontalPadding = with(environment.density) {
        LayoutTokens.FigureCaptionHorizontalPadding.toPx()
    }
    val captionWidth = (contentWidth - captionHorizontalPadding * 2f).coerceAtLeast(0f)
    val captionStyle = environment.markdownTheme.bodyStyle.copy(
        fontStyle = FontStyle.Italic,
        fontSize = environment.markdownTheme.bodyStyle.fontSize * 0.875f,
        textAlign = TextAlign.Center,
        color = environment.markdownTheme.colors.blockQuoteText,
    )
    val captionHeight = if (block.caption.isNotBlank() && captionWidth > 0f) {
        environment.textMeasurer.measure(
            text = block.caption,
            style = captionStyle,
            constraints = Constraints(maxWidth = captionWidth.roundToInt().coerceAtLeast(1)),
        ).size.height.toFloat()
    } else {
        0f
    }
    val captionFrame = if (captionHeight > 0f) {
        LayoutRect(
            left = contentLeft + captionHorizontalPadding,
            top = imageFrame.top + imageFrame.height +
                with(environment.density) { LayoutTokens.FigureCaptionSpacing.toPx() },
            width = captionWidth,
            height = captionHeight,
        )
    } else null
    val contentHeight = imageFrame.height + if (captionFrame != null) {
        with(environment.density) { LayoutTokens.FigureCaptionSpacing.toPx() } + captionFrame.height
    } else {
        0f
    }
    return LayoutFigureBlockModel(
        identity = block.identity,
        frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
        contentFrame = LayoutRect(contentLeft, contentTop, contentWidth, contentHeight),
        block = block,
        imageFrame = imageFrame,
        captionFrame = captionFrame,
    )
}

private fun layoutTocBlock(
    block: TocBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutTocBlockModel {
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    var cursorY = contentTop
    val entries = block.entries.map { entry ->
        val indent = with(environment.density) {
            LayoutTokens.TocIndentPerLevel.toPx() * (entry.level - 1).coerceAtLeast(0)
        }
        val height =
            environment.measureTocEntryHeight(
                entry,
                (contentWidth - indent).coerceAtLeast(
                    with(environment.density) { LayoutTokens.MinimumTocMeasureWidth.toPx() }
                ),
            )
        val group = LayoutTocEntryGroup(
            identity = RenderIdentity(
                stableId = renderIdentityFromText("${entry.level}:${entry.text}:${entry.id.orEmpty()}"),
                contentRevision = 0L,
                layoutRevision = 0L,
                paintRevision = 0L,
            ),
            frame = LayoutRect(contentLeft, cursorY, contentWidth, height),
            contentFrame = LayoutRect(
                contentLeft + indent,
                cursorY,
                (contentWidth - indent).coerceAtLeast(0f),
                height
            ),
            entry = entry,
        )
        cursorY += height + with(environment.density) { LayoutTokens.TocSpacing.toPx() }
        group
    }
    val contentHeight = (
        cursorY - contentTop - with(environment.density) { LayoutTokens.TocSpacing.toPx() }
        ).coerceAtLeast(0f)
    return LayoutTocBlockModel(
        identity = block.identity,
        frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
        contentFrame = LayoutRect(contentLeft, contentTop, contentWidth, contentHeight),
        block = block,
        entries = entries,
    )
}

private fun layoutBibliographyBlock(
    block: BibliographyDefinitionBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutBibliographyBlockModel {
    if (block.entries.isEmpty()) {
        return LayoutBibliographyBlockModel(
            identity = block.identity,
            frame = LayoutRect(left, top, width, 0f),
            contentFrame = LayoutRect(left, top, width, 0f),
            block = block,
            entries = emptyList(),
        )
    }
    val contentLeft = left + insets.left
    val contentTop = top + insets.top
    val contentWidth = (width - insets.left - insets.right).coerceAtLeast(0f)
    val titleHeight =
        environment.lineHeightPx(environment.markdownTheme.headingStyles.getOrElse(3) { environment.markdownTheme.bodyStyle })
    val titleSpacing = with(environment.density) { LayoutTokens.BibliographyTitleSpacing.toPx() }
    val entryVerticalPadding = with(environment.density) {
        LayoutTokens.BibliographyEntryVerticalPadding.toPx()
    }
    val prefixStyle = environment.markdownTheme.bodyStyle.copy(
        fontWeight = FontWeight.SemiBold,
        color = environment.markdownTheme.colors.link,
    )
    var cursorY = contentTop + titleHeight + titleSpacing
    val entries = block.entries.map { entry ->
        val prefix = "[${entry.key}] "
        val prefixMeasurement = environment.textMeasurer.measure(
            text = prefix,
            style = prefixStyle,
            constraints = Constraints(maxWidth = Int.MAX_VALUE),
            maxLines = 1,
            softWrap = false,
        )
        val entryContentWidth = (contentWidth - prefixMeasurement.size.width).coerceAtLeast(1f)
        val contentMeasurement = environment.textMeasurer.measure(
            text = entry.content,
            style = environment.markdownTheme.bodyStyle,
            constraints = Constraints(maxWidth = entryContentWidth.roundToInt().coerceAtLeast(1)),
        )
        val rowContentHeight = max(
            prefixMeasurement.size.height.toFloat(),
            contentMeasurement.size.height.toFloat(),
        )
        val entryHeight = rowContentHeight + entryVerticalPadding * 2f
        val group = LayoutBibliographyEntryGroup(
            identity = RenderIdentity(
                stableId = renderIdentityFromText("${entry.key}:${entry.content}"),
                contentRevision = 0L,
                layoutRevision = 0L,
                paintRevision = 0L,
            ),
            frame = LayoutRect(contentLeft, cursorY, contentWidth, entryHeight),
            contentFrame = LayoutRect(
                contentLeft,
                cursorY + entryVerticalPadding,
                contentWidth,
                rowContentHeight,
            ),
            entry = entry,
        )
        cursorY += entryHeight
        group
    }
    val contentHeight = (cursorY - contentTop).coerceAtLeast(titleHeight)
    return LayoutBibliographyBlockModel(
        identity = block.identity,
        frame = LayoutRect(left, top, width, insets.top + contentHeight + insets.bottom),
        contentFrame = LayoutRect(contentLeft, contentTop, contentWidth, contentHeight),
        block = block,
        entries = entries,
    )
}

private fun layoutWidgetBlock(
    block: InternalRenderBlockModel,
    widget: BlockWidgetModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    environment: LayoutEnvironment,
): LayoutWidgetBlockModel {
    val measurement = measureBlockWidget(
        widget = widget,
        viewportWidthPx = (width - insets.left - insets.right).coerceAtLeast(0f),
        environment = environment,
    )
    val contentFrame = LayoutRect(
        left = left + insets.left,
        top = top + insets.top,
        width = measurement.widthPx.coerceAtMost(
            (width - insets.left - insets.right).coerceAtLeast(
                0f
            )
        ),
        height = measurement.heightPx,
    )
    return LayoutWidgetBlockModel(
        identity = block.identity,
        frame = LayoutRect(
            left = left,
            top = top,
            width = width,
            height = insets.top + measurement.heightPx + insets.bottom,
        ),
        contentFrame = contentFrame,
        block = block,
        widget = widget,
        measurement = measurement,
    )
}

private fun layoutRenderBlock(
    block: InternalRenderBlockModel,
    left: Float,
    top: Float,
    width: Float,
    insets: LayoutInsets,
    contentHeight: Float,
    children: List<InternalLayoutBlockModel>,
): LayoutRenderBlockModel {
    return LayoutRenderBlockModel(
        identity = block.identity,
        frame = LayoutRect(
            left = left,
            top = top,
            width = width,
            height = insets.top + contentHeight + insets.bottom,
        ),
        contentFrame = LayoutRect(
            left = left + insets.left,
            top = top + insets.top,
            width = (width - insets.left - insets.right).coerceAtLeast(0f),
            height = contentHeight,
        ),
        block = block,
        children = children,
    )
}

private fun blockInsets(
    block: InternalRenderBlockModel,
    environment: LayoutEnvironment,
): LayoutInsets = with(environment.density) {
    when (block) {
        is BibliographyDefinitionBlockModel -> LayoutInsets(
            left = LayoutTokens.BibliographyPadding.toPx(),
            top = LayoutTokens.BibliographyPadding.toPx(),
            right = LayoutTokens.BibliographyPadding.toPx(),
            bottom = LayoutTokens.BibliographyPadding.toPx(),
        )

        is CodeBlockModel -> LayoutInsets(top = environment.codeBlockHeaderHeightPx)
        is TableBlockModel -> LayoutInsets(top = environment.tableToolbarHeightPx)

        is MathBlockModel,
        is DiagramBlockModel,
        is HtmlBlockModel -> LayoutInsets()

        is BlockQuoteBlockModel -> LayoutInsets(
            left = environment.markdownTheme.layout.blockQuotePadding.toPx() +
                environment.markdownTheme.layout.blockQuoteBorderWidth.toPx(),
        )

        is AdmonitionBlockModel,
        is CustomContainerBlockModel -> LayoutInsets(
            left = LayoutTokens.ContainerStartPadding.toPx(),
            top = LayoutTokens.ContainerVerticalPadding.toPx(),
            right = LayoutTokens.ContainerEndPadding.toPx(),
            bottom = LayoutTokens.ContainerVerticalPadding.toPx(),
        )

        is DirectiveBlockModel -> LayoutInsets(
            left = LayoutTokens.DirectivePadding.toPx(),
            top = LayoutTokens.DirectivePadding.toPx(),
            right = LayoutTokens.DirectivePadding.toPx(),
            bottom = LayoutTokens.DirectivePadding.toPx(),
        )

        is FigureBlockModel -> LayoutInsets()
        is FootnoteDefinitionBlockModel -> LayoutInsets(
            top = LayoutTokens.FootnoteTopPadding.toPx()
        )
        is PageBreakBlockModel,
        is ThematicBreakBlockModel -> LayoutInsets()

        else -> LayoutInsets()
    }
}

private fun LayoutEnvironment.containerHeaderHeight(hasContent: Boolean): Float =
    lineHeightPx(markdownTheme.bodyStyle) + if (hasContent) {
        with(density) { LayoutTokens.ContainerContentSpacing.toPx() }
    } else {
        0f
    }

private fun LayoutEnvironment.directiveHeaderHeight(hasContent: Boolean): Float =
    lineHeightPx(markdownTheme.bodyStyle.copy(fontSize = 14.sp)) + if (hasContent) {
        with(density) { LayoutTokens.ContainerContentSpacing.toPx() }
    } else {
        0f
    }

private fun LayoutEnvironment.tabHeaderHeight(block: TabBlockModel): Float {
    val titleHeight = block.items.maxOfOrNull { item ->
        textMeasurer.measure(
            text = item.title,
            style = markdownTheme.bodyStyle,
            constraints = Constraints(maxWidth = Int.MAX_VALUE),
            maxLines = 1,
            softWrap = false,
        ).size.height.toFloat()
    } ?: lineHeightPx(markdownTheme.bodyStyle)
    return titleHeight + with(density) { LayoutTokens.TabTitleVerticalPadding.toPx() * 2f }
}
