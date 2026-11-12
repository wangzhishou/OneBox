/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * This file was newly written for OneBox and is not derived
 * from ImageToolbox source. The com.t8rin.* package name is retained
 * only for compatibility with the existing upstream module layout.
 *
 * Origin: OneBox original work (upstream package name retained)
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

package com.t8rin.imagetoolbox.core.domain.history

class DocumentHistory<T>(
    private val maxSize: Int = 50,
    private val freeze: (T) -> T,
) {

    init {
        require(maxSize in 1..50) { "History capacity must be between 1 and 50" }
    }

    private val undoStack = ArrayDeque<T>()
    private val redoStack = ArrayDeque<T>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun snapshot(document: T) {
        pushUndo(document)
        redoStack.clear()
    }

    fun replaceLastSnapshot(document: T): Boolean {
        if (undoStack.isEmpty()) return false
        val frozen = freeze(document)
        undoStack.removeLast()
        undoStack.addLast(frozen)
        redoStack.clear()
        return true
    }

    fun undo(current: T): T? {
        if (undoStack.isEmpty()) return null
        redoStack.addLast(freeze(current))
        return undoStack.removeLast()
    }

    fun redo(current: T): T? {
        if (redoStack.isEmpty()) return null
        pushUndo(current)
        return redoStack.removeLast()
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }

    private fun pushUndo(document: T) {
        undoStack.addLast(freeze(document))
        if (undoStack.size > maxSize) undoStack.removeFirst()
    }
}
