package com.hrm.markdown.renderer.block

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import com.hrm.markdown.parser.ast.ColumnItem
import com.hrm.markdown.parser.ast.ColumnsLayout
import com.hrm.markdown.renderer.MarkdownBlockChildren
import com.hrm.markdown.renderer.internal.core.model.ColumnBlockModel
import com.hrm.markdown.renderer.internal.core.model.ColumnsLayoutBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutColumnsBlockModel
import com.hrm.markdown.renderer.internal.layout.LayoutTokens
import com.hrm.markdown.renderer.internal.layout.columns.resolveColumnWidths
import kotlin.math.roundToInt

/**
 * 多列布局渲染器：将 [ColumnsLayout] 渲染为水平排列的多列结构。
 *
 * 每个 [ColumnItem] 按指定宽度（或平均分配）排列。
 */
@Composable
internal fun ColumnsLayoutRenderer(
    node: ColumnsLayout,
    modifier: Modifier = Modifier,
) {
    val columns = node.children.filterIsInstance<ColumnItem>()
    if (columns.isEmpty()) return
    RenderColumnsRow(
        modifier = modifier,
        columns = columns.map { column ->
            column.width to {
                MarkdownBlockChildren(
                    parent = column,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}

@Composable
internal fun RenderColumnsLayoutBlockModel(
    model: ColumnsLayoutBlockModel,
    renderChildren: @Composable (List<InternalRenderBlockModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.columns.isEmpty()) return
    RenderColumnsRow(
        modifier = modifier,
        columns = model.columns.map { column ->
            column.width to {
                renderChildren(column.children)
            }
        },
    )
}

@Composable
internal fun RenderColumnsLayoutGroupModel(
    model: LayoutColumnsBlockModel,
    renderChildren: @Composable (List<InternalLayoutBlockModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (model.columns.isEmpty()) return
    RenderColumnsRow(
        modifier = modifier,
        columns = model.columns.map { column ->
            column.width to {
                renderChildren(column.children)
            }
        },
    )
}

@Composable
private fun RenderColumnsRow(
    columns: List<Pair<String, @Composable () -> Unit>>,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier.fillMaxWidth(),
        content = {
            columns.forEach { (_, content) -> Box { content() } }
        },
    ) { measurables, constraints ->
        val totalWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else constraints.minWidth
        val spacing = LayoutTokens.ColumnsSpacing.roundToPx()
        val resolved = resolveColumnWidths(
            values = columns.map { it.first },
            totalWidthPx = totalWidth.toFloat(),
            spacingPx = spacing.toFloat(),
        )
        val placeables = measurables.mapIndexed { index, measurable ->
            val columnWidth = resolved.getOrElse(index) { 0f }.roundToInt().coerceAtLeast(0)
            measurable.measure(
                Constraints(
                    minWidth = columnWidth,
                    maxWidth = columnWidth,
                    minHeight = 0,
                    maxHeight = constraints.maxHeight,
                )
            )
        }
        val height = placeables.maxOfOrNull { it.height }?.coerceIn(
            constraints.minHeight,
            constraints.maxHeight,
        ) ?: constraints.minHeight
        layout(totalWidth, height) {
            var x = 0
            placeables.forEach { placeable ->
                // Markdown column order is physical source order, matching tables and the pure
                // layout engine; ambient RTL affects text inside a column, not column ordering.
                placeable.place(x, 0)
                x += placeable.width + spacing
            }
        }
    }
}
