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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.components.text.SuppressSystemTextSelectionMenu
import com.shifenmiao.common.editor.NativeEditorDock
import com.wanbaohe.code.editor.R
import com.wanbaohe.codeeditor.CodeEditorState
import com.wanbaohe.codeeditor.CodeLanguage
import com.wanbaohe.codeeditor.ui.CodeEditor
import com.wanbaohe.codeeditor.ui.CodeEditorCallbacks
import com.wanbaohe.codeeditor.ui.CodeEditorTheme
import kotlinx.coroutines.flow.drop

/**
 * 原生代码编辑器外壳：正文 + 查找栏 + 底部工具坞 + 自绘选区菜单。
 *
 * 与 Markdown 编辑器同一套交互语言：
 * - 正文占满，操作全部下到底部工具坞（可收起）；
 * - 选中文字后浮出自绘工具条，屏蔽系统 ActionMode，避免两套菜单叠加；
 * - 长按空白处浮出「粘贴/全选」。
 */
@Composable
fun NativeCodeEditor(
    state: CodeEditorState,
    modifier: Modifier = Modifier,
    onContentChanged: () -> Unit = {},
    onLanguageChange: (CodeLanguage) -> Unit = {},
) {
    val clipboard = rememberPlainTextClipboard()
    val theme = rememberNativeCodeEditorTheme()
    val selectionUi = rememberCodeEditorSelectionUi()
    var showSymbols by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        snapshotFlow { state.userContentVersion }.drop(1).collect { onContentChanged() }
    }

    Column(modifier = modifier.fillMaxSize()) {
        CodeEditorPane(
            state = state,
            theme = theme,
            callbacks = selectionUi.callbacks,
            modifier = Modifier.weight(1f),
        )
        AnimatedVisibility(
            visible = state.isFindVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            CodeEditorFindBar(state = state)
        }
        Box {
            NativeEditorDock(
                actions = codeEditorDockActions(
                    state = state,
                    clipboard = clipboard,
                    onFind = {
                        if (state.isFindVisible) state.closeFind() else state.openFind()
                    },
                    onSymbols = { showSymbols = true },
                ),
                leading = { expanded ->
                    CodeEditorLanguageChip(
                        state = state,
                        expanded = expanded,
                        onLanguageChange = onLanguageChange,
                    )
                },
            )
            CodeEditorSymbolMenu(
                expanded = showSymbols,
                onDismiss = { showSymbols = false },
                state = state,
            )
        }
    }

    CodeEditorSelectionMenu(
        state = state,
        anchors = selectionUi.anchors,
        clipboard = clipboard,
        onDismissContextMenu = selectionUi::dismissMenu,
    )
}

/** 正文区：屏蔽系统文本菜单，交给自绘工具条。 */
@Composable
private fun CodeEditorPane(
    state: CodeEditorState,
    theme: CodeEditorTheme,
    callbacks: CodeEditorCallbacks,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        SuppressSystemTextSelectionMenu {
            CodeEditor(
                state = state,
                theme = theme,
                modifier = Modifier.fillMaxSize(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.code_editor_placeholder),
                        style = theme.textStyle.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        ),
                    )
                },
                callbacks = callbacks,
            )
        }
    }
}
