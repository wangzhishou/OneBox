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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 撤销点：正文 + 光标位置（撤销后光标回到当时的落点）。 */
data class CodeEditorSnapshot(
    val text: String,
    val selection: TextRange,
)

/**
 * 与 Markdown 编辑器同一套撤销模型：连续输入合并成一组（[IDLE_COMMIT_MS] 空闲后提交），
 * 工具坞里的命令先 flush 再记录，于是每条命令都是一个独立撤销点。
 */
internal class CodeEditorHistory(
    private val scope: CoroutineScope,
    private val maxEntries: Int = 100,
) {
    private val undoStack = mutableStateListOf<CodeEditorSnapshot>()
    private val redoStack = mutableStateListOf<CodeEditorSnapshot>()
    private var current: CodeEditorSnapshot? = null
    private var pending by mutableStateOf<CodeEditorSnapshot?>(null)
    private var commitJob: Job? = null

    val canUndo: Boolean
        get() {
            val recorded = pending
            return undoStack.isNotEmpty() ||
                (recorded != null && recorded.text != current?.text)
        }

    val canRedo: Boolean
        get() = pending == null && redoStack.isNotEmpty()

    fun reset(snapshot: CodeEditorSnapshot) {
        commitJob?.cancel()
        commitJob = null
        pending = null
        current = snapshot
        undoStack.clear()
        redoStack.clear()
    }

    /** 记录一次编辑后的状态；[IDLE_COMMIT_MS] 内的连续输入沿用同一个撤销组。 */
    fun record(snapshot: CodeEditorSnapshot) {
        if (pending?.text == snapshot.text) return
        pending = snapshot
        commitJob?.cancel()
        commitJob = scope.launch {
            delay(IDLE_COMMIT_MS)
            commit()
        }
    }

    fun commit() {
        commitJob?.cancel()
        commitJob = null
        val snapshot = pending ?: return
        pending = null
        if (snapshot.text == current?.text) return
        current?.let(undoStack::add)
        while (undoStack.size >= maxEntries) undoStack.removeAt(0)
        current = snapshot
        redoStack.clear()
    }

    fun undo(): CodeEditorSnapshot? {
        commit()
        if (undoStack.isEmpty()) return null
        current?.let(redoStack::add)
        return undoStack.removeAt(undoStack.lastIndex).also { current = it }
    }

    fun redo(): CodeEditorSnapshot? {
        commit()
        if (redoStack.isEmpty()) return null
        current?.let(undoStack::add)
        return redoStack.removeAt(redoStack.lastIndex).also { current = it }
    }
}

private const val IDLE_COMMIT_MS = 500L
