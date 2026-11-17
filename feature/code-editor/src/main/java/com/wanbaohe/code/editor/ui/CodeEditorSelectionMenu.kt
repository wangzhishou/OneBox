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

@file:Suppress("FunctionNaming")

package com.wanbaohe.code.editor.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.res.stringResource
import com.wanbaohe.code.editor.R
import com.wanbaohe.codeeditor.CodeEditorState
import com.wanbaohe.codeeditor.deleteSelection
import com.wanbaohe.codeeditor.indentSelection
import com.wanbaohe.codeeditor.insertText
import com.wanbaohe.codeeditor.outdentSelection
import com.wanbaohe.codeeditor.selectAll
import com.wanbaohe.codeeditor.toggleComment
import com.wanbaohe.codeeditor.ui.CodeEditorCallbacks
import com.wanbaohe.richtext.editor.ui.RichTextSelectionAction
import com.wanbaohe.richtext.editor.ui.RichTextSelectionToolbar
import com.wanbaohe.richtext.editor.ui.RichTextSelectionToolbarConfig

/**
 * 选区工具条的页面级状态：选区锚点、长按锚点、长按菜单是否可见。
 * 抽成独立状态对象后，页面外壳只负责把 [callbacks] 交给编辑器。
 */
@Stable
internal class CodeEditorSelectionUi {
    var selectionAnchor by mutableStateOf(Rect.Zero)
        private set
    var menuAnchor by mutableStateOf(Rect.Zero)
        private set
    var isMenuVisible by mutableStateOf(false)
        private set

    val anchors: CodeEditorSelectionAnchors
        get() = CodeEditorSelectionAnchors(
            selection = selectionAnchor,
            menu = menuAnchor,
            menuVisible = isMenuVisible,
        )

    val callbacks: CodeEditorCallbacks = CodeEditorCallbacks(
        onSelectionAnchorChange = { anchor ->
            selectionAnchor = anchor
            // 一旦有真正的选区，长按菜单就该让位给选区工具条
            if (anchor != Rect.Zero) isMenuVisible = false
        },
        onLongPress = { anchor ->
            menuAnchor = anchor
            isMenuVisible = true
        },
    )

    fun dismissMenu() {
        isMenuVisible = false
    }
}

@Composable
internal fun rememberCodeEditorSelectionUi(): CodeEditorSelectionUi =
    remember { CodeEditorSelectionUi() }

/** 选区菜单的两个锚点：有选区时贴选区，无选区时长按点。 */
internal data class CodeEditorSelectionAnchors(
    val selection: Rect,
    val menu: Rect,
    val menuVisible: Boolean,
)

/** 自绘选区工具条：复制/剪切/粘贴/缩进/反缩进/注释；无选区时长按出「粘贴/全选」。 */
@Composable
internal fun CodeEditorSelectionMenu(
    state: CodeEditorState,
    anchors: CodeEditorSelectionAnchors,
    clipboard: PlainTextClipboard,
    onDismissContextMenu: () -> Unit,
) {
    val hasSelection = !state.selection.collapsed
    if (!hasSelection && !anchors.menuVisible) return
    val anchor = if (hasSelection) anchors.selection else anchors.menu
    if (anchor == Rect.Zero) return
    val actions = buildList {
        addHistoryActions(state = state, onDismiss = onDismissContextMenu)
        if (hasSelection) {
            addSelectionEditActions(state = state, clipboard = clipboard)
        } else {
            addCollapsedActions(state = state, clipboard = clipboard, onDismiss = onDismissContextMenu)
        }
    }
    RichTextSelectionToolbar(
        anchorBounds = anchor,
        actions = actions,
        config = RichTextSelectionToolbarConfig(
            dismissOnClickOutside = !hasSelection && anchors.menuVisible,
            onDismiss = onDismissContextMenu.takeIf { !hasSelection && anchors.menuVisible },
        ),
    )
}

@Composable
private fun MutableList<RichTextSelectionAction>.addHistoryActions(
    state: CodeEditorState,
    onDismiss: () -> Unit,
) {
    if (state.canUndo) {
        add(
            RichTextSelectionAction(
                id = "undo",
                label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.undo),
                glyph = "↶",
                onClick = {
                    onDismiss()
                    state.undo()
                },
            )
        )
    }
    if (state.canRedo) {
        add(
            RichTextSelectionAction(
                id = "redo",
                label = stringResource(com.t8rin.imagetoolbox.core.resources.R.string.redo),
                glyph = "↷",
                onClick = {
                    onDismiss()
                    state.redo()
                },
            )
        )
    }
}

@Composable
private fun MutableList<RichTextSelectionAction>.addSelectionEditActions(
    state: CodeEditorState,
    clipboard: PlainTextClipboard,
) {
    add(
        RichTextSelectionAction(
            id = "copy",
            label = stringResource(android.R.string.copy),
            glyph = "⧉",
            onClick = { clipboard.copy(state.selectedText()) },
        )
    )
    add(
        RichTextSelectionAction(
            id = "cut",
            label = stringResource(android.R.string.cut),
            glyph = "✂",
            onClick = {
                if (clipboard.copy(state.selectedText())) state.deleteSelection()
            },
        )
    )
    add(
        RichTextSelectionAction(
            id = "paste",
            label = stringResource(android.R.string.paste),
            glyph = "▣",
            onClick = { clipboard.paste()?.let(state::insertText) },
        )
    )
    add(
        RichTextSelectionAction(
            id = "indent",
            label = stringResource(R.string.code_editor_indent),
            glyph = "⇥",
            onClick = { state.indentSelection() },
        )
    )
    add(
        RichTextSelectionAction(
            id = "outdent",
            label = stringResource(R.string.code_editor_outdent),
            glyph = "⇤",
            onClick = { state.outdentSelection() },
        )
    )
    add(
        RichTextSelectionAction(
            id = "comment",
            label = stringResource(R.string.code_editor_comment),
            glyph = "//",
            onClick = { state.toggleComment() },
        )
    )
}

@Composable
private fun MutableList<RichTextSelectionAction>.addCollapsedActions(
    state: CodeEditorState,
    clipboard: PlainTextClipboard,
    onDismiss: () -> Unit,
) {
    add(
        RichTextSelectionAction(
            id = "paste",
            label = stringResource(android.R.string.paste),
            glyph = "▣",
            onClick = {
                onDismiss()
                clipboard.paste()?.let(state::insertText)
            },
        )
    )
    add(
        RichTextSelectionAction(
            id = "select_all",
            label = stringResource(android.R.string.selectAll),
            glyph = "全",
            onClick = {
                onDismiss()
                state.selectAll()
            },
        )
    )
}
