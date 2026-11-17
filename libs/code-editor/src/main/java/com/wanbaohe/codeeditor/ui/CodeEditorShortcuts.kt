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

package com.wanbaohe.codeeditor.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import com.wanbaohe.codeeditor.CodeEditorState
import com.wanbaohe.codeeditor.indentSelection
import com.wanbaohe.codeeditor.outdentSelection
import com.wanbaohe.codeeditor.selectAll

/**
 * 外接键盘快捷键（手机外接键盘 / 平板）：撤销重做、查找、缩进。
 *
 * 用 onPreviewKeyEvent 抢在文本框默认处理之前，Tab 才不会被拿去切焦点。
 * 软键盘的 Enter/退格仍由文本框自己处理。
 */
fun Modifier.codeEditorKeyboardShortcuts(state: CodeEditorState): Modifier = onPreviewKeyEvent { event ->
    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
    if (event.isCtrlPressed) {
        return@onPreviewKeyEvent when (event.key) {
            Key.Z -> {
                if (event.isShiftPressed) state.redo() else state.undo()
                true
            }

            Key.Y -> {
                state.redo()
                true
            }

            Key.F -> {
                state.openFind()
                true
            }

            Key.A -> {
                state.selectAll()
                true
            }

            else -> false
        }
    }
    when (event.key) {
        Key.Tab -> {
            if (event.isShiftPressed) state.outdentSelection() else state.indentSelection()
            true
        }

        Key.Escape -> {
            if (state.isFindVisible) {
                state.closeFind()
                true
            } else {
                false
            }
        }

        else -> false
    }
}
