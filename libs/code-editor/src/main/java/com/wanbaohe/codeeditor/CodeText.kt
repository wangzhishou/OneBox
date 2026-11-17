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
 * 行级纯函数：缩进/注释/整行移动都依赖它，独立于 Compose 状态，便于独立推理。
 */
object CodeText {

    /** 行首偏移（含 0 与末行：文本以换行结尾时会多出一条空行）。 */
    fun lineStarts(text: String): List<Int> {
        val starts = ArrayList<Int>(text.count { it == '\n' } + 1)
        starts += 0
        for (index in text.indices) {
            if (text[index] == '\n') starts += index + 1
        }
        return starts
    }

    fun lineStartOffset(text: String, offset: Int): Int {
        val safe = offset.coerceIn(0, text.length)
        val newline = text.lastIndexOf('\n', startIndex = (safe - 1).coerceAtLeast(0))
        return if (newline < 0) 0 else newline + 1
    }

    /** 行尾偏移（不含换行符）。 */
    fun lineEndOffset(text: String, offset: Int): Int {
        val safe = offset.coerceIn(0, text.length)
        val newline = text.indexOf('\n', startIndex = safe)
        return if (newline < 0) text.length else newline
    }

    fun lineIndexOf(text: String, offset: Int): Int {
        val limit = offset.coerceIn(0, text.length)
        var line = 0
        for (index in 0 until limit) {
            if (text[index] == '\n') line++
        }
        return line
    }

    fun lineCount(text: String): Int = text.count { it == '\n' } + 1

    /** 选区覆盖的整行区间（起止都对齐到行边界，便于整行缩进/注释）。 */
    fun selectedLineRange(text: String, selection: TextRange): IntRange {
        val start = lineStartOffset(text, selection.min)
        val rawEnd = lineEndOffset(text, selection.max)
        return start..rawEnd
    }

    /** 行首空白的宽度（tab 记一格，避免把 tab 误算成 0）。 */
    fun leadingIndentWidth(line: String): Int {
        var width = 0
        for (char in line) {
            when (char) {
                ' ' -> width += 1
                '\t' -> width += 1
                else -> return width
            }
        }
        return width
    }
}
