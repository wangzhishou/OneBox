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

package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.editor.NativeEditorExtraAction
import com.shifenmiao.common.editor.NativeMarkdownEditor
import com.shifenmiao.common.editor.rememberNativeMarkdownEditorState
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.ui.NoteCategoryDialog
import com.shifenmiao.online.ui.NoteTitleDialog
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CreateNoteScreen(
    createNoteComponent: CreateNoteComponent,
    appComponent: AppComponent
) {
    val uiState by createNoteComponent.uiState.collectAsState()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val showCategoryDialog = rememberSaveable { mutableStateOf(false) }
    val showTitleDialog = rememberSaveable { mutableStateOf(false) }
    var titleContentHint by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val editorState = rememberNativeMarkdownEditorState(uiState.data)

    val onBack = {
        if (uiState.isDirty) {
            showExitDialog.value = true
        } else {
            appComponent.onGoBack()
        }
    }

    // 当数据加载完成时，设置初始内容
    LaunchedEffect(uiState.data) {
        if (uiState.data.isNotEmpty()) {
            editorState.load(uiState.data)
        }
    }
    val titlePlaceholder = if (uiState.isEditing) {
        stringResource(R.string.edit_note)
    } else {
        stringResource(R.string.new_note)
    }

    BaseScreen(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        title = {
            Text(
                text = titlePlaceholder,
                style = MaterialTheme.typography.titleLarge
            )
        },
        type = EnhancedTopAppBarType.Center,
        onGoBack = onBack,
        actions = {
            IconButton(
                onClick = {
                    scope.launch {
                        val markdown = editorState.exportMarkdown()
                        createNoteComponent.saveMarkDownData(
                            markdown = markdown,
                            onSuccess = {
                                scope.launch(Dispatchers.Main) {
                                    AppToastHost.showToast(
                                        AppContext.getString(R.string.save_success)
                                    )
                                    appComponent.onNavigateReplacingCurrent(
                                        Screen.NewApp(initialTab = HomeTabKey.TEXT)
                                    )
                                }
                            },
                            onFailure = { errorMsg ->
                                AppToastHost.showToast(errorMsg)
                            }
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Check,
                    contentDescription = stringResource(R.string.done_button),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        supportGlassEffect = false,
        showNavigationBarsPadding = true
    ) {
        NativeMarkdownEditor(
            state = editorState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            extraActions = listOf(
                NativeEditorExtraAction(
                    label = stringResource(R.string.title),
                    icon = Icons.Outlined.Title,
                    onClick = {
                        scope.launch {
                            titleContentHint = MarkdownSummary.derive(editorState.exportMarkdown()).title
                            showTitleDialog.value = true
                        }
                    },
                ),
                NativeEditorExtraAction(
                    label = stringResource(R.string.note_select_category),
                    icon = Icons.Outlined.Folder,
                    onClick = { showCategoryDialog.value = true },
                ),
            ),
            onContentChanged = createNoteComponent::markAsDirty,
        )
    }

    BackHandler(onBack = onBack)

    ExitWithoutSavingDialog(
        onExit = {
            appComponent.onGoBack()
        },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value
    )

    // 分类选择/管理浮动弹窗
    NoteCategoryDialog(
        visible = showCategoryDialog.value,
        createNoteComponent = createNoteComponent,
        uiState = uiState,
        onDismiss = { showCategoryDialog.value = false }
    )

    // 自定义标题弹窗：无标题时以文章开头内容作占位提示
    NoteTitleDialog(
        visible = showTitleDialog.value,
        initialTitle = uiState.title,
        contentHint = titleContentHint,
        onConfirm = { newTitle ->
            createNoteComponent.onTitleChange(newTitle)
            showTitleDialog.value = false
        },
        onDismiss = { showTitleDialog.value = false }
    )
}
