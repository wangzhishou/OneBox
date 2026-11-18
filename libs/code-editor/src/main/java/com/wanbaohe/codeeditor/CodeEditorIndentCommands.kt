/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.codeeditor

import androidx.compose.ui.text.TextRange

/**
 * 代码编辑器的插入与缩进命令。
 *
 * 全部走 [CodeEditorState.applyEdit]，因此每条命令都是一个独立撤销点，
 * 并且会同步刷新查找命中。纯文本计算都在 [CodeText] 上完成，方便单独推理。
 */
fun CodeEditorState.insertText(text: String) {
    val selection = value.selection
    applyEdit(
        range = selection.min..(selection.max - 1),
        replacement = text,
        selectionAfter = TextRange(selection.min + text.length),
    )
}

fun CodeEditorState.wrapSelection(prefix: String, suffix: String) {
    val selection = value.selection
    val selected = value.text.substring(selection.min, selection.max)
    val replacement = prefix + selected + suffix
    applyEdit(
        range = selection.min..(selection.max - 1),
        replacement = replacement,
        selectionAfter = if (selection.collapsed) {
            TextRange(selection.min + prefix.length)
        } else {
            TextRange(selection.min + replacement.length)
        },
    )
}

fun CodeEditorState.deleteSelection() {
    val selection = value.selection
    if (selection.collapsed) return
    applyEdit(
        range = selection.min..(selection.max - 1),
        replacement = "",
        selectionAfter = TextRange(selection.min),
    )
}

fun CodeEditorState.selectAll() {
    setSelection(TextRange(0, value.text.length))
}

fun CodeEditorState.indentSelection() {
    val source = value.text
    val selection = value.selection
    val indent = " ".repeat(tabWidth)
    if (selection.collapsed) {
        applyEdit(
            range = selection.min..(selection.min - 1),
            replacement = indent,
            selectionAfter = TextRange(selection.min + indent.length),
        )
    } else {
        val block = selectedBlock(source, selection)
        val deltas = mutableListOf<LineDelta>()
        val updated = buildIndentedBlock(block.text, indent, deltas)
        applyEdit(
            range = block.start..(block.endExclusive - 1),
            replacement = updated,
            selectionAfter = block.shift(selection, deltas),
        )
    }
}

fun CodeEditorState.outdentSelection() {
    val source = value.text
    val selection = value.selection
    if (selection.collapsed) {
        removeIndentAtCursor(source, selection.min)
    } else {
        val block = selectedBlock(source, selection)
        val deltas = mutableListOf<LineDelta>()
        val updated = buildOutdentedBlock(block.text, tabWidth, deltas)
        if (deltas.isNotEmpty()) {
            applyEdit(
                range = block.start..(block.endExclusive - 1),
                replacement = updated,
                selectionAfter = block.shift(selection, deltas),
            )
        }
    }
}

private fun CodeEditorState.removeIndentAtCursor(source: String, caret: Int) {
    val lineStart = CodeText.lineStartOffset(source, caret)
    val prefix = source.substring(lineStart, caret)
    val removable = prefix.takeLastWhile { it == ' ' || it == '\t' }
        .length
        .coerceAtMost(tabWidth)
    if (removable > 0) {
        applyEdit(
            range = (caret - removable)..(caret - 1),
            replacement = "",
            selectionAfter = TextRange(caret - removable),
        )
    }
}

private fun buildIndentedBlock(
    block: String,
    indent: String,
    deltas: MutableList<LineDelta>,
): String {
    val lines = block.split('\n')
    var offset = 0
    return lines.joinToString("\n") { line ->
        val currentOffset = offset
        offset += line.length + 1
        if (line.isBlank()) {
            line
        } else {
            deltas += LineDelta(currentOffset, indent.length)
            indent + line
        }
    }
}

private fun buildOutdentedBlock(
    block: String,
    tabWidth: Int,
    deltas: MutableList<LineDelta>,
): String {
    val lines = block.split('\n')
    var offset = 0
    return lines.joinToString("\n") { line ->
        val currentOffset = offset
        offset += line.length + 1
        val removable = line.takeWhile { it == ' ' || it == '\t' }
            .length
            .coerceAtMost(tabWidth)
        if (removable == 0) {
            line
        } else {
            deltas += LineDelta(currentOffset, -removable)
            line.substring(removable)
        }
    }
}
