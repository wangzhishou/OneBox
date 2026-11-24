@file:Suppress("FunctionNaming")

package com.shifenmiao.common.editor

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.glass.GlassStyle
import com.wanbaohe.core.ui.widget.glass.GlassSurface
import com.wanbaohe.richtext.editor.RichTextSnapshot
import com.wanbaohe.richtext.editor.command.toRichTextSnapshot
import com.wanbaohe.richtext.editor.document.MarkdownBlockEditor
import com.wanbaohe.richtext.editor.document.MarkdownEditorBlock

/**
 * 表格块编辑器。
 *
 * 编辑区把 GFM 表格拆成单元格网格（表头 + 数据行），每个单元格就是一个小输入框；
 * 任何改动都会立即回写成 Markdown 存进 raw block，所以源码模式、预览、保存导出的内容始终一致。
 * 表格结构操作（加行/加列/删行/删列）放在右上角 ⋮ 菜单里，避免表格本身被按钮挤满。
 */
internal object NativeTableBlockEditor : MarkdownBlockEditor {
    @Composable
    override fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (RichTextSnapshot) -> Unit,
    ) {
        var sourceMode by remember(block.id) { mutableStateOf(false) }
        var table by remember(block.id) {
            mutableStateOf(MarkdownTableCodec.parse(block.state.text.toString()))
        }
        var showMenu by remember(block.id) { mutableStateOf(false) }

        fun commit(updated: MarkdownTableData) {
            table = updated
            val markdown = MarkdownTableCodec.encode(updated)
            block.state.setMarkdown(markdown)
            onContentChanged(RichTextSnapshot(markdown))
        }

        GlassSurface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            style = GlassStyle.Thin,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.native_editor_kind_table),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(
                        onClick = {
                            // 从源码切回网格时重新解析，避免显示源码改过之前的旧表格。
                            if (sourceMode) {
                                table = MarkdownTableCodec.parse(block.state.text.toString())
                            }
                            sourceMode = !sourceMode
                        },
                    ) {
                        Icon(
                            imageVector = MaterialIcons.Outlined.Code,
                            contentDescription = stringResource(R.string.native_editor_table_edit_source),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        EnhancedDropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.native_editor_table_add_row)) },
                                onClick = {
                                    showMenu = false
                                    commit(table.appendRow())
                                },
                                leadingIcon = { Icon(MaterialIcons.Outlined.Add, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.native_editor_table_add_column)) },
                                onClick = {
                                    showMenu = false
                                    commit(table.appendColumn())
                                },
                                leadingIcon = { Icon(MaterialIcons.Outlined.Add, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.native_editor_table_delete_row)) },
                                onClick = {
                                    showMenu = false
                                    commit(table.removeLastRow())
                                },
                                leadingIcon = { Icon(MaterialIcons.Outlined.Delete, contentDescription = null) },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.native_editor_table_delete_column)) },
                                onClick = {
                                    showMenu = false
                                    commit(table.removeLastColumn())
                                },
                                leadingIcon = { Icon(MaterialIcons.Outlined.Delete, contentDescription = null) },
                            )
                        }
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = MaterialIcons.Outlined.Delete,
                            contentDescription = stringResource(R.string.native_editor_delete_block),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                if (sourceMode) {
                    BasicTextField(
                        state = block.state,
                        inputTransformation = InputTransformation {
                            onContentChanged(toRichTextSnapshot())
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 80.dp)
                            .padding(12.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontFamily = FontFamily.Monospace,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
                    )
                } else {
                    TableGrid(
                        table = table,
                        onCellChange = { row, column, value ->
                            commit(table.withCell(row, column, value))
                        },
                        onDeleteRow = { row -> commit(table.removeRow(row)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TableGrid(
    table: MarkdownTableData,
    onCellChange: (row: Int, column: Int, value: String) -> Unit,
    onDeleteRow: (row: Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
    ) {
        table.rows.forEachIndexed { rowIndex, cells ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                cells.forEachIndexed { columnIndex, cell ->
                    key(rowIndex, columnIndex) {
                        TableCell(
                            value = cell,
                            isHeader = rowIndex == 0,
                            onValueChange = { onCellChange(rowIndex, columnIndex, it) },
                        )
                    }
                }
                if (rowIndex > 0) {
                    IconButton(
                        onClick = { onDeleteRow(rowIndex) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = MaterialIcons.Outlined.Delete,
                            contentDescription = stringResource(R.string.native_editor_table_delete_row),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(32.dp))
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    value: String,
    isHeader: Boolean,
    onValueChange: (String) -> Unit,
) {
    val textStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = if (isHeader) FontWeight.SemiBold else FontWeight.Normal,
    )
    Box(
        modifier = Modifier
            .width(132.dp)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = textStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.native_editor_table_cell_hint),
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            },
        )
    }
}

/**
 * 复选框列表块编辑器。
 *
 * Markdown 的 `- [ ] / - [x]` 在编辑区里就是一组真实复选框 + 文本输入框；
 * 勾选状态直接回写成 Markdown，源码/预览/导出保持一致。
 */
internal object NativeTaskListBlockEditor : MarkdownBlockEditor {
    @Composable
    override fun Editor(
        block: MarkdownEditorBlock.Raw,
        onDelete: () -> Unit,
        onContentChanged: (RichTextSnapshot) -> Unit,
    ) {
        var items by remember(block.id) {
            mutableStateOf(MarkdownTaskListCodec.parse(block.state.text.toString()))
        }

        fun commit(updated: List<MarkdownTaskItem>) {
            items = updated
            val markdown = MarkdownTaskListCodec.encode(updated)
            block.state.setMarkdown(markdown)
            onContentChanged(RichTextSnapshot(markdown))
        }

        GlassSurface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            style = GlassStyle.Thin,
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.native_editor_kind_task_list),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { commit(items + MarkdownTaskItem(checked = false, text = "")) }) {
                        Icon(
                            imageVector = MaterialIcons.Outlined.Add,
                            contentDescription = stringResource(R.string.native_editor_task_add_item),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = MaterialIcons.Outlined.Delete,
                            contentDescription = stringResource(R.string.native_editor_delete_block),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items.forEachIndexed { index, item ->
                        key(index) {
                            TaskRow(
                                item = item,
                                onCheckedChange = { checked ->
                                    commit(items.toMutableList().also { it[index] = item.copy(checked = checked) })
                                },
                                onTextChange = { text ->
                                    commit(items.toMutableList().also { it[index] = item.copy(text = text) })
                                },
                                onDelete = { commit(items.filterIndexed { itemIndex, _ -> itemIndex != index }) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    item: MarkdownTaskItem,
    onCheckedChange: (Boolean) -> Unit,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = item.checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primaryContainer,
                checkmarkColor = MaterialTheme.colorScheme.onPrimaryContainer,
                uncheckedColor = MaterialTheme.colorScheme.outline,
            ),
        )
        BasicTextField(
            value = item.text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (item.checked) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
            decorationBox = { innerTextField ->
                if (item.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.native_editor_task_item_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            },
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = MaterialIcons.Outlined.Delete,
                contentDescription = stringResource(R.string.native_editor_task_delete_item),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

private fun TextFieldState.setMarkdown(markdown: String) {
    if (text.toString() == markdown) return
    edit {
        replace(0, length, markdown)
        selection = TextRange(markdown.length)
    }
}

// ===================== GFM table codec =====================

internal data class MarkdownTableData(
    val alignments: List<String>,
    val rows: List<List<String>>,
) {
    val columnCount: Int get() = rows.firstOrNull()?.size ?: alignments.size

    fun withCell(row: Int, column: Int, value: String): MarkdownTableData {
        if (row !in rows.indices) return this
        val updated = rows.toMutableList()
        val cells = updated[row].toMutableList()
        if (column !in cells.indices) return this
        cells[column] = value.replace("\n", " ")
        updated[row] = cells
        return copy(rows = updated)
    }

    fun appendRow(): MarkdownTableData =
        copy(rows = rows + listOf(List(columnCount) { "" }))

    fun removeRow(row: Int): MarkdownTableData {
        if (row <= 0 || row !in rows.indices) return this
        return copy(rows = rows.filterIndexed { index, _ -> index != row })
    }

    fun removeLastRow(): MarkdownTableData = removeRow(rows.lastIndex)

    fun appendColumn(): MarkdownTableData = copy(
        alignments = alignments + DEFAULT_ALIGNMENT,
        rows = rows.map { cells -> cells + "" },
    )

    fun removeLastColumn(): MarkdownTableData {
        if (columnCount <= 1) return this
        return copy(
            alignments = alignments.dropLast(1).ifEmpty { listOf(DEFAULT_ALIGNMENT) },
            rows = rows.map { it.dropLast(1) },
        )
    }

    companion object {
        const val DEFAULT_ALIGNMENT = "---"
    }
}

internal object MarkdownTableCodec {
    fun parse(markdown: String): MarkdownTableData {
        val sourceRows = markdown
            .lines()
            .map(String::trim)
            .filter { it.startsWith("|") }
            .map(::parseRow)
            .filter { it.isNotEmpty() }
        if (sourceRows.isEmpty()) {
            return MarkdownTableData(
                alignments = List(2) { MarkdownTableData.DEFAULT_ALIGNMENT },
                rows = listOf(List(2) { "" }, List(2) { "" }),
            )
        }
        val separatorIndex = sourceRows.indexOfFirst(::isSeparatorRow)
        val alignments = if (separatorIndex >= 0) {
            sourceRows[separatorIndex].map(::normalizeAlignment)
        } else {
            emptyList()
        }
        val header = if (separatorIndex == 0) {
            emptyList()
        } else {
            sourceRows.first()
        }
        val body = if (separatorIndex >= 0) {
            sourceRows.drop(separatorIndex + 1)
        } else {
            sourceRows.drop(1)
        }
        val columnCount = (listOf(header.size, alignments.size) + body.map { it.size })
            .maxOrNull()
            ?.coerceAtLeast(1)
            ?: 1
        fun normalize(cells: List<String>): List<String> =
            List(columnCount) { index -> cells.getOrElse(index) { "" } }
        return MarkdownTableData(
            alignments = List(columnCount) { index ->
                alignments.getOrElse(index) { MarkdownTableData.DEFAULT_ALIGNMENT }
            },
            rows = listOf(normalize(header)) + body.map(::normalize),
        )
    }

    fun encode(table: MarkdownTableData): String {
        val columnCount = table.columnCount.coerceAtLeast(1)
        val rows = table.rows.map { cells -> List(columnCount) { cells.getOrElse(it) { "" } } }
        val header = rows.firstOrNull() ?: List(columnCount) { "" }
        val body = rows.drop(1)
        val widths = List(columnCount) { column ->
            (listOf(header) + body)
                .maxOfOrNull { cells -> cells[column].length }
                ?.coerceAtLeast(MIN_COLUMN_WIDTH)
                ?: MIN_COLUMN_WIDTH
        }
        val alignmentRow = List(columnCount) { column ->
            separatorCell(
                alignment = table.alignments.getOrElse(column) { MarkdownTableData.DEFAULT_ALIGNMENT },
                width = widths[column],
            )
        }
        return buildList {
            add(formatRow(header, widths))
            add(formatRow(alignmentRow, widths))
            body.forEach { add(formatRow(it, widths)) }
        }.joinToString("\n")
    }

    private fun parseRow(line: String): List<String> {
        val body = line
            .removePrefix("|")
            .removeSuffix("|")
        val cells = mutableListOf<String>()
        val cell = StringBuilder()
        var index = 0
        while (index < body.length) {
            val char = body[index]
            if (char == '\\' && index + 1 < body.length && body[index + 1] == '|') {
                cell.append('|')
                index += 2
                continue
            }
            if (char == '|') {
                cells += cell.toString().trim()
                cell.clear()
            } else {
                cell.append(char)
            }
            index++
        }
        cells += cell.toString().trim()
        return cells
    }

    private fun isSeparatorRow(cells: List<String>): Boolean =
        cells.isNotEmpty() && cells.all { cell -> SeparatorRegex.matches(cell) }

    private fun normalizeAlignment(cell: String): String {
        val trimmed = cell.trim()
        val left = trimmed.startsWith(":")
        val right = trimmed.endsWith(":")
        return when {
            left && right -> ":---:"
            left -> ":---"
            right -> "---:"
            else -> MarkdownTableData.DEFAULT_ALIGNMENT
        }
    }

    private fun separatorCell(alignment: String, width: Int): String {
        val safeWidth = width.coerceAtLeast(3)
        return when (alignment) {
            ":---:" -> ":" + "-".repeat(safeWidth - 2) + ":"
            ":---" -> ":" + "-".repeat(safeWidth - 1)
            "---:" -> "-".repeat(safeWidth - 1) + ":"
            else -> "-".repeat(safeWidth)
        }
    }

    private fun formatRow(cells: List<String>, widths: List<Int>): String =
        buildString {
            append("|")
            cells.forEachIndexed { index, cell ->
                append(' ')
                append(
                    cell.replace("|", "\\|")
                        .replace("\n", " ")
                        .padEnd(widths.getOrElse(index) { cell.length })
                )
                append(" |")
            }
        }

    private const val MIN_COLUMN_WIDTH = 3
    private val SeparatorRegex = Regex("^:?-{1,}:?$")
}

// ===================== Task list codec =====================

internal data class MarkdownTaskItem(
    val checked: Boolean,
    val text: String,
)

internal object MarkdownTaskListCodec {
    fun parse(markdown: String): List<MarkdownTaskItem> =
        markdown.lines().mapNotNull { line ->
            val match = TaskItemRegex.find(line.trim()) ?: return@mapNotNull null
            MarkdownTaskItem(
                checked = match.groupValues[1].isNotBlank(),
                text = match.groupValues[2],
            )
        }

    fun encode(items: List<MarkdownTaskItem>): String =
        items.joinToString("\n") { item ->
            "- [${if (item.checked) "x" else " "}] ${item.text.replace("\n", " ")}"
        }

    private val TaskItemRegex = Regex("^[-*+]\\s+\\[([ xX]?)]\\s?(.*)$")
}
