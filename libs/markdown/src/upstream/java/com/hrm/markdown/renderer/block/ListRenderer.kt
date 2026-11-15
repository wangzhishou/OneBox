package com.hrm.markdown.renderer.block

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.hrm.markdown.parser.ast.BlankLine
import com.hrm.markdown.parser.ast.Heading
import com.hrm.markdown.parser.ast.Image
import com.hrm.markdown.parser.ast.ListBlock
import com.hrm.markdown.parser.ast.ListItem
import com.hrm.markdown.parser.ast.Paragraph
import com.hrm.markdown.parser.ast.SetextHeading
import com.hrm.markdown.renderer.LocalMarkdownStyle
import com.hrm.markdown.renderer.MarkdownBlockChildren
import com.hrm.markdown.renderer.internal.compose.LocalMergeInlineSemantics
import com.hrm.markdown.renderer.internal.compose.FirstTextLineCenter
import com.hrm.markdown.renderer.internal.core.model.HeadingBlockModel
import com.hrm.markdown.renderer.internal.core.model.InternalRenderBlockModel
import com.hrm.markdown.renderer.internal.core.model.ListBlockModel
import com.hrm.markdown.renderer.internal.core.model.ParagraphBlockModel
import com.hrm.markdown.renderer.internal.core.model.TextAtom
import com.hrm.markdown.renderer.internal.layout.LayoutTokens
import com.hrm.markdown.renderer.internal.layout.list.OrderedListMarkerGap
import com.hrm.markdown.renderer.internal.layout.list.OrderedListMarkerWidth
import com.hrm.markdown.renderer.internal.layout.list.TaskListCheckboxSize
import com.hrm.markdown.renderer.internal.layout.list.TaskListMarkerGap
import com.hrm.markdown.renderer.internal.layout.list.listMarkerWidth
import com.hrm.markdown.renderer.internal.layout.list.measureOrderedListMarkerMetrics
import com.hrm.markdown.renderer.internal.layout.list.orderedListMarkerDigitCount
import com.hrm.markdown.renderer.internal.layout.model.InternalLayoutBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutInlineBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutListBlockModel
import com.hrm.markdown.renderer.internal.layout.model.LayoutTextRun

/** Lists share one marker width across all items, including numbering digit transitions. */
@Composable
internal fun ListBlockRenderer(node: ListBlock, modifier: Modifier = Modifier) {
    val items = node.children.filterIsInstance<ListItem>()
    val orderedWidth = rememberOrderedMarkerWidth(node.ordered, node.startNumber, items.size)
    RenderListContainer(node.tight, items.size, modifier) {
        items.forEachIndexed { index, item ->
            key(item.stableKey) {
                val first = item.children.firstOrNull { it !is BlankLine }
                val startsWithText = when (first) {
                    is Paragraph -> first.children.firstOrNull() !is Image
                    is Heading, is SetextHeading -> true
                    else -> false
                }
                ListItemRow(
                    itemIndex = index,
                    markerText = if (node.ordered) "${node.startNumber.toLong() + index}." else "•",
                    ordered = node.ordered,
                    taskListItem = item.taskListItem,
                    checked = item.checked,
                    markerWidth = listMarkerWidth(item.taskListItem, node.ordered, orderedWidth),
                    startsWithText = startsWithText,
                ) {
                    MarkdownBlockChildren(item)
                }
            }
        }
    }
}

@Composable
internal fun RenderListBlockModel(
    model: ListBlockModel,
    renderChildren: @Composable (List<InternalRenderBlockModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val orderedWidth = rememberOrderedMarkerWidth(model.ordered, model.startNumber, model.items.size)
    RenderListContainer(model.tight, model.items.size, modifier) {
        model.items.forEachIndexed { index, item ->
            key(item.identity.stableId) {
                val firstAtom = when (val first = item.children.firstOrNull()) {
                    is ParagraphBlockModel -> first.inline.atoms.firstOrNull()
                    is HeadingBlockModel -> first.inline.atoms.firstOrNull()
                    else -> null
                }
                ListItemRow(
                    itemIndex = index,
                    markerText = if (model.ordered) "${model.startNumber.toLong() + index}." else "•",
                    ordered = model.ordered,
                    taskListItem = item.taskListItem,
                    checked = item.checked,
                    markerWidth = listMarkerWidth(item.taskListItem, model.ordered, orderedWidth),
                    startsWithText = firstAtom is TextAtom,
                ) {
                    renderChildren(item.children)
                }
            }
        }
    }
}

@Composable
internal fun RenderListLayoutBlockModel(
    model: LayoutListBlockModel,
    renderChildren: @Composable (List<InternalLayoutBlockModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    RenderListContainer(model.block.tight, model.items.size, modifier) {
        model.items.forEachIndexed { index, item ->
            key(item.identity.stableId) {
                val first = item.children.firstOrNull() as? LayoutInlineBlockModel
                ListItemRow(
                    itemIndex = index,
                    markerText = item.markerText,
                    ordered = model.block.ordered,
                    taskListItem = item.taskListItem,
                    checked = item.checked,
                    markerWidth = with(density) { item.markerWidthPx.toDp() },
                    startsWithText = first?.lines?.firstOrNull()?.runs?.firstOrNull() is LayoutTextRun,
                ) {
                    renderChildren(item.children)
                }
            }
        }
    }
}

@Composable
private fun rememberOrderedMarkerWidth(ordered: Boolean, startNumber: Int, itemCount: Int): Dp {
    if (!ordered) return OrderedListMarkerWidth
    val theme = LocalMarkdownStyle.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val digitCount = orderedListMarkerDigitCount(startNumber, itemCount)
    val metrics = remember(theme.bodyStyle, density, textMeasurer) {
        measureOrderedListMarkerMetrics(theme.bodyStyle, density, textMeasurer)
    }
    return with(density) {
        metrics.widthPx(digitCount, density).toDp()
    }
}

@Composable
private fun ListItemRow(
    itemIndex: Int,
    markerText: String,
    ordered: Boolean,
    taskListItem: Boolean,
    checked: Boolean,
    markerWidth: Dp,
    startsWithText: Boolean,
    content: @Composable () -> Unit,
) {
    val theme = LocalMarkdownStyle.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = theme.layout.listIndent)
            .semantics(mergeDescendants = true) {
                isTraversalGroup = true
                collectionItemInfo = CollectionItemInfo(itemIndex, 1, 0, 1)
                if (taskListItem) {
                    // Markdown tasks describe source state; they have no editing callback.
                    role = Role.Checkbox
                    toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
                }
            },
        horizontalArrangement = Arrangement.Start,
    ) {
        // Text owns its alignment geometry; markers choose the appropriate alignment line.
        val alignment = when {
            !startsWithText -> Modifier.align(Alignment.Top)
            taskListItem -> Modifier.alignBy(FirstTextLineCenter)
            else -> Modifier.alignByBaseline()
        }
        if (taskListItem) {
            Checkbox(
                checked = checked,
                onCheckedChange = null,
                modifier = Modifier.width(markerWidth)
                    .clearAndSetSemantics { }
                    .padding(end = TaskListMarkerGap)
                    .size(TaskListCheckboxSize)
                    .then(if (startsWithText) Modifier.alignBy { it.measuredHeight / 2 } else alignment),
                colors = CheckboxDefaults.colors(
                    checkedColor = theme.colors.taskChecked,
                    uncheckedColor = theme.colors.taskUnchecked,
                ),
            )
        } else {
            BasicText(
                text = markerText,
                modifier = Modifier.width(markerWidth)
                    .then(if (ordered) Modifier.padding(end = OrderedListMarkerGap) else Modifier)
                    .then(alignment),
                style = theme.bodyStyle.copy(
                    color = theme.colors.listBullet,
                    textAlign = if (ordered) TextAlign.End else TextAlign.Start,
                ),
                maxLines = 1,
                softWrap = false,
            )
        }
        Column(
            modifier = Modifier.weight(1f).then(alignment),
            verticalArrangement = Arrangement.spacedBy(theme.layout.blockSpacing),
        ) {
            CompositionLocalProvider(LocalMergeInlineSemantics provides false) {
                content()
            }
        }
    }
}

@Composable
private fun RenderListContainer(
    tight: Boolean,
    itemCount: Int,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    val theme = LocalMarkdownStyle.current
    Column(
        modifier = modifier.fillMaxWidth().semantics {
            collectionInfo = CollectionInfo(itemCount, 1)
            isTraversalGroup = true
        },
        verticalArrangement = Arrangement.spacedBy(if (tight) LayoutTokens.ListTightItemSpacing else theme.layout.blockSpacing),
    ) {
        content()
    }
}
