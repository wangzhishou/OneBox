package com.hrm.markdown.renderer.block

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.hrm.markdown.parser.ast.Table
import com.hrm.markdown.parser.ast.TableBody
import com.hrm.markdown.parser.ast.TableCell
import com.hrm.markdown.parser.ast.TableHead
import com.hrm.markdown.parser.ast.TableRow
import com.hrm.markdown.renderer.LocalMarkdownComponents
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.MarkdownTableInfo
import com.hrm.markdown.renderer.inline.InlineLayoutBlockText
import com.hrm.markdown.renderer.inline.rememberInlineModel
import com.hrm.markdown.renderer.internal.core.model.TableBlockModel
import com.hrm.markdown.renderer.internal.core.model.TableCellBlockModel
import com.hrm.markdown.renderer.internal.core.model.TableRowBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTableBlockModel
import com.hrm.markdown.renderer.internal.layout.table.computeAutoTableColumnWidths
import com.hrm.markdown.renderer.internal.layout.table.computeTableRowHeights
import com.hrm.markdown.renderer.internal.layout.table.roundTableDimensionsPx
import com.hrm.markdown.renderer.internal.layout.table.tableTextAlign

/**
 * GFM 表格渲染器。
 *
 * 使用接近 CSS auto table layout 的列宽分配：
 * 先按单元格内容计算每列 min-content / max-content，再按视口宽度伸缩或水平滚动。
 */
@Composable
internal fun TableRenderer(
    node: Table,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current

    // 收集所有行（表头 + 表体）
    val allRows = mutableListOf<Pair<TableRow, Boolean>>() // (row, isHeader)
    val head = node.children.filterIsInstance<TableHead>().firstOrNull()
    head?.children?.filterIsInstance<TableRow>()?.forEach { allRows.add(it to true) }
    val body = node.children.filterIsInstance<TableBody>().firstOrNull()
    body?.children?.filterIsInstance<TableRow>()?.forEach { allRows.add(it to false) }
    val columnCount = node.columnAlignments.size
        .coerceAtLeast(allRows.maxOfOrNull { it.first.children.filterIsInstance<TableCell>().size } ?: 1)
        .coerceAtLeast(1)

    TableChrome(MarkdownTableInfo(allRows.size, columnCount), modifier) { viewportModifier ->
        AutoTableViewport(viewportModifier) { availableWidthPx ->
            // 自定义表格布局
            TableLayout(
                allRows = allRows,
                alignments = node.columnAlignments,
                columnCount = columnCount,
                availableWidthPx = availableWidthPx,
                modifier = Modifier.border(width = 1.dp, color = theme.colors.tableBorder),
            )
        }
    }
}

@Composable
private fun AutoTableViewport(
    modifier: Modifier,
    content: @Composable (availableWidthPx: Int?) -> Unit,
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val viewportWidthPx = if (maxWidth.value.isFinite()) {
            with(density) { maxWidth.roundToPx() }
        } else {
            null
        }
        val minWidthModifier = viewportWidthPx?.let {
            Modifier.widthIn(min = with(density) { it.toDp() })
        } ?: Modifier
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .then(minWidthModifier),
        ) {
            content(viewportWidthPx)
        }
    }
}

@Composable
private fun TableLayout(
    allRows: List<Pair<TableRow, Boolean>>,
    alignments: List<Table.Alignment>,
    columnCount: Int,
    availableWidthPx: Int?,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current

    TableGridLayout(
        rowCount = allRows.size,
        columnCount = columnCount,
        availableWidthPx = availableWidthPx,
        modifier = modifier,
    ) {
        for ((row, isHeader) in allRows) {
            val cells = row.children.filterIsInstance<TableCell>()
            for (colIndex in 0 until columnCount) {
                val cell = cells.getOrNull(colIndex)
                val alignment = alignments.getOrElse(colIndex) { Table.Alignment.NONE }
                TableCellRenderer(
                    cell = cell,
                    alignment = alignment,
                    isHeader = isHeader,
                    modifier = Modifier
                        .border(0.5.dp, theme.colors.tableBorder)
                        .let {
                            if (isHeader) it.background(theme.colors.tableHeaderBackground) else it
                        }
                        .padding(theme.layout.tableCellPadding),
                )
            }
        }
    }
}

@Composable
private fun TableCellRenderer(
    cell: TableCell?,
    alignment: Table.Alignment,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current

    val textAlign = tableTextAlign(alignment)

    val style = if (isHeader) {
        theme.bodyStyle.copy(fontWeight = FontWeight.Bold, textAlign = textAlign)
    } else {
        theme.bodyStyle.copy(textAlign = textAlign)
    }

    if (cell == null) {
        Box(modifier = modifier)
        return
    }

    val inlineModel = rememberInlineModel(cell)
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        InlineLayoutBlockText(
            model = inlineModel,
            style = style,
        )
    }
}

@Composable
internal fun RenderTableBlockModel(
    model: TableBlockModel,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    val columnCount = model.columnAlignments.size
        .coerceAtLeast(model.rows.maxOfOrNull { it.cells.size } ?: 1)
        .coerceAtLeast(1)
    TableChrome(MarkdownTableInfo(model.rows.size, columnCount), modifier) { viewportModifier ->
        AutoTableViewport(viewportModifier) { availableWidthPx ->
            TableBlockModelLayout(
                rows = model.rows,
                alignments = model.columnAlignments,
                columnCount = columnCount,
                availableWidthPx = availableWidthPx,
                modifier = Modifier.border(width = 1.dp, color = theme.colors.tableBorder),
            )
        }
    }
}

@Composable
internal fun RenderTableLayoutBlockModel(
    model: LayoutTableBlockModel,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    val columnWidthsPx = roundTableDimensionsPx(model.columnWidths)
    val rowHeightsPx = roundTableDimensionsPx(model.rows.map { it.frame.height })
    TableChrome(MarkdownTableInfo(model.rows.size, columnWidthsPx.size), modifier) { viewportModifier ->
        AutoTableViewport(viewportModifier) { _ ->
            TableGridLayout(
                rowCount = model.rows.size,
                columnCount = columnWidthsPx.size,
                availableWidthPx = null,
                fixedColumnWidthsPx = columnWidthsPx,
                fixedRowHeightsPx = rowHeightsPx,
                modifier = Modifier.border(width = 1.dp, color = theme.colors.tableBorder),
            ) {
                for (row in model.rows) {
                    for (cell in row.cells) {
                        val alignment = Table.Alignment.entries.getOrElse(cell.alignmentOrdinal) {
                            Table.Alignment.NONE
                        }
                        TableBlockModelCellRenderer(
                            cell = cell.cell,
                            alignment = alignment,
                            isHeader = cell.isHeader,
                            modifier = Modifier
                                .border(0.5.dp, theme.colors.tableBorder)
                                .let {
                                    if (cell.isHeader) it.background(theme.colors.tableHeaderBackground) else it
                                }
                                .padding(theme.layout.tableCellPadding),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableBlockModelLayout(
    rows: List<TableRowBlockModel>,
    alignments: List<Table.Alignment>,
    columnCount: Int,
    availableWidthPx: Int?,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    TableGridLayout(
        rowCount = rows.size,
        columnCount = columnCount,
        availableWidthPx = availableWidthPx,
        modifier = modifier,
    ) {
        for (row in rows) {
            for (colIndex in 0 until columnCount) {
                val cell = row.cells.getOrNull(colIndex)
                val alignment = alignments.getOrElse(colIndex) { Table.Alignment.NONE }
                TableBlockModelCellRenderer(
                    cell = cell,
                    alignment = alignment,
                    isHeader = row.isHeader,
                    modifier = Modifier
                        .border(0.5.dp, theme.colors.tableBorder)
                        .let {
                            if (row.isHeader) it.background(theme.colors.tableHeaderBackground) else it
                        }
                        .padding(theme.layout.tableCellPadding),
                )
            }
        }
    }
}

@Composable
internal fun TableGridLayout(
    rowCount: Int,
    columnCount: Int,
    availableWidthPx: Int?,
    fixedColumnWidthsPx: IntArray? = null,
    fixedRowHeightsPx: IntArray? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, _ ->
        if (rowCount == 0 || columnCount == 0) {
            return@Layout layout(0, 0) {}
        }

        check(measurables.size == rowCount * columnCount) {
            "Table grid expected ${rowCount * columnCount} cells, got ${measurables.size}"
        }
        val columnWidths = fixedColumnWidthsPx?.also {
            check(it.size == columnCount) {
                "Table grid expected $columnCount fixed column widths, got ${it.size}"
            }
        } ?: run {
            val minContentWidths = MutableList(columnCount) { 0f }
            val maxContentWidths = MutableList(columnCount) { 0f }
            for (index in measurables.indices) {
                val colIdx = index % columnCount
                val minIntrinsicWidth = measurables[index].minIntrinsicWidth(Constraints.Infinity)
                val maxIntrinsicWidth = measurables[index].maxIntrinsicWidth(Constraints.Infinity)
                minContentWidths[colIdx] = maxOf(minContentWidths[colIdx], minIntrinsicWidth.toFloat())
                maxContentWidths[colIdx] = maxOf(maxContentWidths[colIdx], maxIntrinsicWidth.toFloat())
            }
            computeColumnWidthsPx(minContentWidths, maxContentWidths, availableWidthPx)
        }

        val rowHeights = fixedRowHeightsPx?.also {
            check(it.size == rowCount) {
                "Table grid expected $rowCount fixed row heights, got ${it.size}"
            }
        } ?: run {
            val intrinsicCellHeights = IntArray(measurables.size) { index ->
                val colIdx = index % columnCount
                measurables[index].minIntrinsicHeight(columnWidths[colIdx])
            }
            computeTableRowHeights(
                cellHeights = intrinsicCellHeights,
                columnCount = columnCount,
            )
        }

        val placeables = Array(measurables.size) { index ->
            val colIdx = index % columnCount
            val rowIdx = index / columnCount
            val fixedWidth = columnWidths[colIdx]
            measurables[index].measure(
                Constraints(
                    minWidth = fixedWidth,
                    maxWidth = fixedWidth,
                    minHeight = rowHeights[rowIdx],
                    maxHeight = rowHeights[rowIdx],
                )
            )
        }

        val totalWidth = columnWidths.sum()
        val totalHeight = rowHeights.sum()
        layout(totalWidth, totalHeight) {
            var y = 0
            for (rowIdx in 0 until rowCount) {
                var x = 0
                for (colIdx in 0 until columnCount) {
                    val placeable = placeables[rowIdx * columnCount + colIdx]
                    // Source column order is physical and must not reverse in RTL.
                    placeable.place(x, y)
                    x += columnWidths[colIdx]
                }
                y += rowHeights[rowIdx]
            }
        }
    }
}

@Composable
private fun TableBlockModelCellRenderer(
    cell: TableCellBlockModel?,
    alignment: Table.Alignment,
    isHeader: Boolean,
    modifier: Modifier = Modifier,
) {
    val theme = LocalMarkdownStyle.current
    val textAlign = tableTextAlign(alignment)
    val style = if (isHeader) {
        theme.bodyStyle.copy(fontWeight = FontWeight.Bold, textAlign = textAlign)
    } else {
        theme.bodyStyle.copy(textAlign = textAlign)
    }
    if (cell == null) {
        Box(modifier = modifier)
        return
    }
    Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
        InlineLayoutBlockText(
            model = cell.inline,
            style = style,
            searchable = true,
        )
    }
}

private fun computeColumnWidthsPx(
    minContentWidths: List<Float>,
    maxContentWidths: List<Float>,
    availableWidthPx: Int?,
): IntArray {
    val widths = computeAutoTableColumnWidths(
        minContentWidths = minContentWidths,
        maxContentWidths = maxContentWidths,
        availableWidth = availableWidthPx?.toFloat(),
    )
    return roundTableDimensionsPx(widths)
}

@Composable
private fun TableChrome(info: MarkdownTableInfo, modifier: Modifier, content: @Composable (Modifier) -> Unit) {
    val slot = LocalMarkdownComponents.current.tableToolbar
    if (slot == null) {
        content(modifier)
    } else {
        Column(modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().height(slot.height).clipToBounds()) { slot.content(info) }
            content(Modifier)
        }
    }
}
