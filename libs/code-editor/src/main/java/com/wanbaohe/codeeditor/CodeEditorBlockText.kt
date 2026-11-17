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

/** 选区覆盖的整行块：[start, endExclusive) 不含块尾换行。 */
internal data class CodeEditorBlock(
    val start: Int,
    val endExclusive: Int,
    val text: String,
) {
    fun shift(selection: TextRange, deltas: List<LineDelta>): TextRange {
        fun shiftOffset(offset: Int): Int {
            var delta = 0
            deltas.forEach { line ->
                if (start + line.offset < offset) delta += line.delta
            }
            return (offset + delta).coerceAtLeast(0)
        }
        return TextRange(shiftOffset(selection.min), shiftOffset(selection.max))
    }
}

/** [offset] 为行首相对块首的偏移，[delta] 为该行长度增量。 */
internal data class LineDelta(val offset: Int, val delta: Int)

internal fun selectedBlock(text: String, selection: TextRange): CodeEditorBlock {
    val start = CodeText.lineStartOffset(text, selection.min)
    val endExclusive = CodeText.lineEndOffset(text, selection.max)
    return CodeEditorBlock(start, endExclusive, text.substring(start, endExclusive))
}

/** 逐行对比修改前后的长度差，供选区贴合新正文。 */
internal fun lineDeltas(before: CodeEditorBlock, after: CodeEditorBlock): List<LineDelta> {
    val beforeLines = before.text.split('\n')
    val afterLines = after.text.split('\n')
    val deltas = mutableListOf<LineDelta>()
    var offset = 0
    beforeLines.indices.forEach { index ->
        val beforeLength = beforeLines[index].length
        val afterLength = afterLines.getOrNull(index)?.length ?: beforeLength
        if (afterLength != beforeLength) deltas += LineDelta(offset, afterLength - beforeLength)
        offset += beforeLength + 1
    }
    return deltas
}
