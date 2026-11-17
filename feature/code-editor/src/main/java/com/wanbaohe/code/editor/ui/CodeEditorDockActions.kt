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

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.editor.NativeEditorDockAction
import com.shifenmiao.common.editor.NativeEditorDockChip
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropDown
import com.t8rin.imagetoolbox.core.resources.icons.line.LineArrowDropUp
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentCut
import com.t8rin.imagetoolbox.core.resources.icons.line.LineContentPaste
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatLineSpacing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRedo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSearch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineUndo
import com.wanbaohe.code.editor.R
import com.wanbaohe.codeeditor.CodeEditorState
import com.wanbaohe.codeeditor.CodeLanguage
import com.wanbaohe.codeeditor.deleteSelection
import com.wanbaohe.codeeditor.duplicateLines
import com.wanbaohe.codeeditor.indentSelection
import com.wanbaohe.codeeditor.insertText
import com.wanbaohe.codeeditor.moveLines
import com.wanbaohe.codeeditor.outdentSelection
import com.wanbaohe.codeeditor.selectAll
import com.wanbaohe.codeeditor.toggleComment
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import androidx.compose.material.icons.Icons as MaterialIcons
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons

/** 工具坞动作表：撤销/重做 → 查找 → 缩进/注释/整行 → 软换行 → 剪贴板。 */
@Composable
internal fun codeEditorDockActions(
    state: CodeEditorState,
    clipboard: PlainTextClipboard,
    onFind: () -> Unit,
): List<NativeEditorDockAction> =
    dockHistoryActions(state = state, onFind = onFind) +
        dockEditActions(state = state) +
        dockClipboardActions(state = state, clipboard = clipboard)

@Composable
private fun dockHistoryActions(
    state: CodeEditorState,
    onFind: () -> Unit,
): List<NativeEditorDockAction> = listOf(
    NativeEditorDockAction(
        id = "undo",
        label = stringResource(R.string.code_editor_toolbar_undo),
        icon = OneBoxIcons.Outlined.LineUndo,
        enabled = state.canUndo,
        onClick = state::undo,
    ),
    NativeEditorDockAction(
        id = "redo",
        label = stringResource(R.string.code_editor_toolbar_redo),
        icon = OneBoxIcons.Outlined.LineRedo,
        enabled = state.canRedo,
        onClick = state::redo,
    ),
    NativeEditorDockAction(
        id = "find",
        label = stringResource(R.string.code_editor_toolbar_find),
        icon = OneBoxIcons.Outlined.LineSearch,
        selected = state.isFindVisible,
        dividerBefore = true,
        onClick = onFind,
    ),
)

@Composable
private fun dockEditActions(state: CodeEditorState): List<NativeEditorDockAction> = listOf(
    NativeEditorDockAction(
        id = "indent",
        label = stringResource(R.string.code_editor_indent),
        glyph = "⇥",
        onClick = state::indentSelection,
    ),
    NativeEditorDockAction(
        id = "outdent",
        label = stringResource(R.string.code_editor_outdent),
        glyph = "⇤",
        onClick = state::outdentSelection,
    ),
    NativeEditorDockAction(
        id = "comment",
        label = stringResource(R.string.code_editor_comment),
        glyph = "//",
        dividerBefore = true,
        onClick = state::toggleComment,
    ),
    NativeEditorDockAction(
        id = "duplicate",
        label = stringResource(R.string.code_editor_duplicate_line),
        glyph = "⎘",
        onClick = state::duplicateLines,
    ),
    NativeEditorDockAction(
        id = "move_up",
        label = stringResource(R.string.code_editor_move_line_up),
        icon = OneBoxIcons.Outlined.LineArrowDropUp,
        onClick = { state.moveLines(up = true) },
    ),
    NativeEditorDockAction(
        id = "move_down",
        label = stringResource(R.string.code_editor_move_line_down),
        icon = OneBoxIcons.Outlined.LineArrowDropDown,
        onClick = { state.moveLines(up = false) },
    ),
    NativeEditorDockAction(
        id = "wrap",
        label = stringResource(R.string.code_editor_toolbar_word_wrap),
        icon = OneBoxIcons.Outlined.LineFormatLineSpacing,
        selected = state.wrapLines,
        dividerBefore = true,
        onClick = state::toggleWrapLines,
    ),
)

@Composable
private fun dockClipboardActions(
    state: CodeEditorState,
    clipboard: PlainTextClipboard,
): List<NativeEditorDockAction> = listOf(
    NativeEditorDockAction(
        id = "copy",
        label = stringResource(R.string.code_editor_toolbar_copy),
        glyph = "⧉",
        enabled = !state.selection.collapsed,
        onClick = { clipboard.copy(state.selectedText()) },
    ),
    NativeEditorDockAction(
        id = "cut",
        label = stringResource(R.string.code_editor_cut),
        icon = OneBoxIcons.Outlined.LineContentCut,
        enabled = !state.selection.collapsed,
        onClick = {
            if (clipboard.copy(state.selectedText())) state.deleteSelection()
        },
    ),
    NativeEditorDockAction(
        id = "paste",
        label = stringResource(R.string.code_editor_toolbar_paste),
        icon = OneBoxIcons.Outlined.LineContentPaste,
        onClick = { clipboard.paste()?.let(state::insertText) },
    ),
    NativeEditorDockAction(
        id = "select_all",
        label = stringResource(R.string.code_editor_toolbar_select_all),
        glyph = "全",
        onClick = state::selectAll,
    ),
)

/** 工具坞左侧的语言芯片：点开切换高亮语言。 */
@Composable
internal fun CodeEditorLanguageChip(
    state: CodeEditorState,
    expanded: Boolean,
    onLanguageChange: (CodeLanguage) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        NativeEditorDockChip(
            label = state.language.displayName,
            glyph = state.language.shortLabel,
            expanded = expanded,
            onClick = { menuExpanded = true },
        )
        EnhancedDropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            CodeLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        menuExpanded = false
                        onLanguageChange(language)
                    },
                    leadingIcon = {
                        if (language == state.language) {
                            Icon(
                                imageVector = MaterialIcons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    },
                )
            }
        }
    }
}
