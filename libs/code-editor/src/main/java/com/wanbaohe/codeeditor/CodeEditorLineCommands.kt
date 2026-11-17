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
 * 行注释逐行加/去；没有行注释的语言（html/xml/css/markdown）退化成块注释包裹。
 * 无选区的块注释按「当前行」处理，避免用户只点到某处却注释掉整段。
 */
fun CodeEditorState.toggleComment() {
    val block = selectedBlock(value.text, value.selection)
    val lineComment = language.lineComment
    val blockCommentStart = language.blockCommentStart
    val blockCommentEnd = language.blockCommentEnd
    val updated = when {
        lineComment != null -> toggleLineComment(block.text, lineComment)
        blockCommentStart != null && blockCommentEnd != null ->
            toggleBlockComment(block.text, blockCommentStart, blockCommentEnd)

        else -> return
    }
    if (updated == block.text) return
    val updatedBlock = CodeEditorBlock(block.start, block.endExclusive, updated)
    applyEdit(
        range = block.start..(block.endExclusive - 1),
        replacement = updated,
        selectionAfter = block.shift(value.selection, lineDeltas(block, updatedBlock)),
    )
}

/** 复制当前行（多行选区复制整块），光标落到副本上，与桌面编辑器的手感一致。 */
fun CodeEditorState.duplicateLines() {
    val source = value.text
    val selection = value.selection
    val block = selectedBlock(source, selection)
    val lineText = block.text
    val delta = lineText.length + 1
    applyEdit(
        range = block.endExclusive..(block.endExclusive - 1),
        replacement = "\n$lineText",
        selectionAfter = TextRange(selection.min + delta, selection.max + delta),
    )
}

fun CodeEditorState.moveLines(up: Boolean) {
    val source = value.text
    val selection = value.selection
    val block = selectedBlock(source, selection)
    if (up) {
        if (block.start == 0) return
        val previousStart = CodeText.lineStartOffset(source, block.start - 1)
        val previousLine = source.substring(previousStart, block.start - 1)
        val delta = -(previousLine.length + 1)
        applyEdit(
            range = previousStart..(block.endExclusive - 1),
            replacement = block.text + "\n" + previousLine,
            selectionAfter = TextRange(selection.min + delta, selection.max + delta),
        )
    } else {
        if (block.endExclusive >= source.length) return
        val nextStart = block.endExclusive + 1
        val nextEnd = CodeText.lineEndOffset(source, nextStart)
        val nextLine = source.substring(nextStart, nextEnd)
        val delta = nextLine.length + 1
        applyEdit(
            range = block.start..(nextEnd - 1),
            replacement = nextLine + "\n" + block.text,
            selectionAfter = TextRange(selection.min + delta, selection.max + delta),
        )
    }
}

private fun toggleLineComment(blockText: String, lineComment: String): String {
    val lines = blockText.split('\n')
    val meaningful = lines.filter { it.isNotBlank() }
    if (meaningful.isEmpty()) return blockText
    val commented = meaningful.all { line -> line.trimStart().startsWith(lineComment) }
    return if (commented) {
        lines.joinToString("\n") { removeLineComment(it, lineComment) }
    } else {
        lines.joinToString("\n") { addLineComment(it, lineComment) }
    }
}

private fun addLineComment(line: String, lineComment: String): String {
    if (line.isBlank()) return line
    val indent = line.takeWhile { it == ' ' || it == '\t' }
    return "$indent$lineComment ${line.substring(indent.length)}"
}

private fun removeLineComment(line: String, lineComment: String): String {
    val indentIndex = line.indexOfFirst { !it.isWhitespace() }
    val commentIndex = if (indentIndex < 0) -1 else line.indexOf(lineComment, indentIndex)
    return if (commentIndex < 0) {
        line
    } else {
        val afterComment = commentIndex + lineComment.length
        val cutEnd = if (line.getOrNull(afterComment) == ' ') afterComment + 1 else afterComment
        line.removeRange(commentIndex, cutEnd)
    }
}

private fun toggleBlockComment(
    blockText: String,
    blockStart: String,
    blockEnd: String,
): String {
    val trimmed = blockText.trim()
    return if (trimmed.startsWith(blockStart) && trimmed.endsWith(blockEnd)) {
        blockText.replaceFirst(blockStart, "").let { withoutStart ->
            val lastIndex = withoutStart.lastIndexOf(blockEnd)
            if (lastIndex < 0) withoutStart else withoutStart.removeRange(lastIndex, lastIndex + blockEnd.length)
        }
    } else {
        "$blockStart $blockText $blockEnd"
    }
}
