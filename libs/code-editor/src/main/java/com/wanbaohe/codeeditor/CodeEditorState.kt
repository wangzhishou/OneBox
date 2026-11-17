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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.CoroutineScope

/** 查找命中后的滚动请求：requestId 递增，编辑器据此把该位置滚进视口。 */
data class CodeEditorReveal(
    val offset: Int,
    val requestId: Long,
)

/**
 * 纯 Kotlin 代码编辑器的状态内核。
 *
 * 分工：
 * - 这里只存「正文 + 光标 + 语言 + 查找状态 + 撤销栈」，不认识文件、剪贴板和主题；
 * - 缩进/注释/整行移动等编辑命令见 `CodeEditorCommands.kt`；
 * - 行号、平滑光标跟随、语法着色见 `ui/`。
 */
@Stable
@Suppress("TooManyFunctions") // 编辑器状态机：输入/撤销/查找/命令支撑集中在一个状态持有者里
class CodeEditorState internal constructor(
    initialText: String,
    initialLanguage: CodeLanguage,
    scope: CoroutineScope,
) {
    private val history = CodeEditorHistory(scope)

    var value by mutableStateOf(
        TextFieldValue(
            text = initialText.normalizeLineEndings(),
            selection = TextRange(0),
        )
    )
        private set

    var language by mutableStateOf(initialLanguage)
        private set

    /** 软换行开关；关闭时编辑器横向滚动，与 CodeMirror 的 word wrap 行为一致。 */
    var wrapLines by mutableStateOf(false)
        private set

    var tabWidth by mutableIntStateOf(DEFAULT_TAB_WIDTH)
        private set

    var isDirty by mutableStateOf(false)
        private set

    /** 正文版本号：每次正文变化自增，供语法高亮/行号等派生逻辑作为 key。 */
    var contentVersion by mutableLongStateOf(0L)
        private set

    /**
     * 「用户改动」版本号：只在输入/命令/撤销时自增，外部载入文件不算。
     * 页面据此标记未保存，避免刚打开文件就弹退出确认。
     */
    var userContentVersion by mutableLongStateOf(0L)
        private set

    var isFindVisible by mutableStateOf(false)
        private set

    var findQuery by mutableStateOf("")
        private set

    var findMatches by mutableStateOf<List<IntRange>>(emptyList())
        private set

    var activeMatchIndex by mutableIntStateOf(-1)
        private set

    var reveal by mutableStateOf<CodeEditorReveal?>(null)
        private set

    val text: String
        get() = value.text

    val selection: TextRange
        get() = value.selection

    val lineCount: Int
        get() = CodeText.lineCount(value.text)

    val activeMatch: IntRange?
        get() = findMatches.getOrNull(activeMatchIndex)

    val canUndo: Boolean
        get() = history.canUndo

    val canRedo: Boolean
        get() = history.canRedo

    init {
        history.reset(snapshotOf(value))
    }

    // ── 输入 ─────────────────────────────────────────────

    fun onValueChange(next: TextFieldValue) {
        val previous = value
        if (next == previous) return
        val adjusted = withAutoIndent(previous, next)
        value = adjusted
        if (adjusted.text == previous.text) return
        isDirty = true
        contentVersion++
        userContentVersion++
        history.record(snapshotOf(adjusted))
        refreshFindMatches()
    }

    fun load(text: String, language: CodeLanguage = this.language): Boolean {
        val normalized = text.normalizeLineEndings()
        this.language = language
        if (normalized == value.text) {
            // 同一个文件重进编辑页时不要清掉撤销栈和光标
            return false
        }
        value = TextFieldValue(text = normalized, selection = TextRange(0))
        isDirty = false
        contentVersion++
        history.reset(snapshotOf(value))
        refreshFindMatches()
        return true
    }

    fun markSaved() {
        isDirty = false
    }

    fun updateLanguage(language: CodeLanguage) {
        if (language == this.language) return
        this.language = language
        contentVersion++
    }

    fun toggleWrapLines() {
        wrapLines = !wrapLines
    }

    fun updateTabWidth(width: Int) {
        tabWidth = width.coerceIn(MIN_TAB_WIDTH, MAX_TAB_WIDTH)
    }

    // ── 撤销 / 重做 ──────────────────────────────────────

    fun undo() {
        val snapshot = history.undo() ?: return
        applySnapshot(snapshot)
    }

    fun redo() {
        val snapshot = history.redo() ?: return
        applySnapshot(snapshot)
    }

    fun setSelection(selection: TextRange, revealTarget: Boolean = false) {
        val safe = selection.coerceInText(value.text.length)
        value = value.copy(selection = safe)
        if (revealTarget) requestReveal(safe.min)
    }

    fun requestReveal(offset: Int) {
        reveal = CodeEditorReveal(
            offset = offset.coerceIn(0, value.text.length),
            requestId = (reveal?.requestId ?: 0L) + 1L,
        )
    }

    // ── 查找 ─────────────────────────────────────────────

    fun openFind(prefillSelection: Boolean = true) {
        val currentSelection = value.selection
        if (prefillSelection && !currentSelection.collapsed) {
            val selected = value.text.substring(currentSelection.min, currentSelection.max)
            if (selected.isNotEmpty() && !selected.contains('\n')) {
                findQuery = selected
            }
        }
        isFindVisible = true
        refreshFindMatches()
        activeMatch?.let { requestReveal(it.first) }
    }

    fun closeFind() {
        if (!isFindVisible && findMatches.isEmpty()) return
        isFindVisible = false
        findMatches = emptyList()
        activeMatchIndex = -1
    }

    fun updateFindQuery(query: String) {
        findQuery = query
        activeMatchIndex = -1
        refreshFindMatches()
        activeMatch?.let { requestReveal(it.first) }
    }

    fun findNext() = stepMatch(forward = true)

    fun findPrevious() = stepMatch(forward = false)

    private fun stepMatch(forward: Boolean) {
        val matches = findMatches
        if (matches.isEmpty()) return
        val current = activeMatchIndex
        activeMatchIndex = if (forward) {
            (current + 1).mod(matches.size)
        } else {
            (current - 1).mod(matches.size)
        }
        requestReveal(matches[activeMatchIndex].first)
    }

    private fun refreshFindMatches() {
        val query = findQuery
        // 查找栏关掉后不能继续算命中：否则撤销/编辑会重新触发命中着色，
        // 正文上留下"没有查找栏却在高亮"的怪状态。
        if (!isFindVisible || query.isEmpty()) {
            findMatches = emptyList()
            activeMatchIndex = -1
            return
        }
        val source = value.text
        val matches = ArrayList<IntRange>()
        var index = source.indexOf(query, 0, ignoreCase = true)
        while (index >= 0 && matches.size < MAX_FIND_MATCHES) {
            matches += index until index + query.length
            val nextStart = index + query.length
            if (nextStart >= source.length) break
            index = source.indexOf(query, nextStart, ignoreCase = true)
        }
        findMatches = matches
        activeMatchIndex = when {
            matches.isEmpty() -> -1
            activeMatchIndex in matches.indices -> activeMatchIndex
            else -> 0
        }
    }

    // ── 命令支撑 ─────────────────────────────────────────

    internal fun snapshotOf(value: TextFieldValue): CodeEditorSnapshot =
        CodeEditorSnapshot(text = value.text, selection = value.selection)

    /**
     * 命令式编辑：先把正在合并的输入组提交，再落成独立撤销点。
     * [replacement] 为替换 [range]（闭区间，越界会被裁剪）后的文本。
     */
    internal fun applyEdit(
        range: IntRange,
        replacement: String,
        selectionAfter: TextRange,
    ): Boolean {
        val currentText = value.text
        val start = range.first.coerceIn(0, currentText.length)
        val endInclusive = range.last.coerceIn(start - 1, currentText.length - 1)
        val endExclusive = (endInclusive + 1).coerceIn(start, currentText.length)
        val nextText = currentText.replaceRange(start, endExclusive, replacement)
        if (nextText == currentText) return false
        history.commit()
        val next = TextFieldValue(
            text = nextText,
            selection = selectionAfter.coerceInText(nextText.length),
        )
        value = next
        isDirty = true
        contentVersion++
        userContentVersion++
        history.record(snapshotOf(next))
        history.commit()
        refreshFindMatches()
        return true
    }

    private fun applySnapshot(snapshot: CodeEditorSnapshot) {
        value = TextFieldValue(
            text = snapshot.text,
            selection = snapshot.selection.coerceInText(snapshot.text.length),
        )
        isDirty = true
        contentVersion++
        userContentVersion++
        refreshFindMatches()
    }

    /**
     * 只处理「按回车」这一种输入：继承上一行缩进，开括号/冒号结尾再进一级。
     * 其余输入原样交给文本框；不吞字符，也不做括号自动补全，避免和输入法组合态打架。
     */
    private fun withAutoIndent(previous: TextFieldValue, next: TextFieldValue): TextFieldValue {
        val caret = newlineInsertionCaret(previous, next)
        val indentWidth = if (caret == null) 0 else autoIndentWidth(previous.text, caret)
        return if (caret == null || indentWidth == 0) {
            next
        } else {
            val indent = " ".repeat(indentWidth)
            val merged = next.text.substring(0, caret) + indent + next.text.substring(caret)
            TextFieldValue(text = merged, selection = TextRange(caret + indent.length))
        }
    }

    /** 回车后新行应继承的缩进宽度：上一行缩进 + 开括号/冒号时再进一级。 */
    private fun autoIndentWidth(text: String, caret: Int): Int {
        val previousLineStart = CodeText.lineStartOffset(text, caret - 1)
        val previousLine = text.substring(previousLineStart, caret - 1)
        val opensBlock = previousLine.trimEnd().let { line ->
            line.endsWith("{") || line.endsWith("(") || line.endsWith("[") || line.endsWith(":")
        }
        return CodeText.leadingIndentWidth(previousLine) + if (opensBlock) tabWidth else 0
    }

    /** 这次变化是「在光标处只插入了一个换行」时返回插入后的光标位置，否则 null。 */
    private fun newlineInsertionCaret(previous: TextFieldValue, next: TextFieldValue): Int? {
        val caret = next.selection.max
        val isSingleNewline = next.text.length == previous.text.length + 1 &&
            next.selection.collapsed &&
            caret in 1..next.text.length &&
            next.text[caret - 1] == '\n' &&
            // 确认这次变化是纯插入，没有同时删除/替换别的内容
            next.text.removeRange(caret - 1, caret) == previous.text
        return caret.takeIf { isSingleNewline }
    }
}

@Composable
fun rememberCodeEditorState(
    initialText: String,
    initialLanguage: CodeLanguage,
): CodeEditorState {
    val scope = rememberCoroutineScope()
    return remember(scope) {
        CodeEditorState(
            initialText = initialText,
            initialLanguage = initialLanguage,
            scope = scope,
        )
    }
}

private fun String.normalizeLineEndings(): String =
    if (indexOf('\r') < 0) this else replace("\r\n", "\n").replace('\r', '\n')

private fun TextRange.coerceInText(length: Int): TextRange =
    TextRange(min.coerceIn(0, length), max.coerceIn(0, length))

internal const val DEFAULT_TAB_WIDTH = 4
private const val MIN_TAB_WIDTH = 2
private const val MAX_TAB_WIDTH = 8
private const val MAX_FIND_MATCHES = 500
