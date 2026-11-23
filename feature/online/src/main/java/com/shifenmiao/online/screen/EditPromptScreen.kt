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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import com.shifenmiao.common.editor.NativeMarkdownEditor
import com.shifenmiao.common.editor.rememberNativeMarkdownEditorState
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.R as OnlineR
import com.shifenmiao.online.ui.ContentSaveConfirmDialog
import com.shifenmiao.online.ui.PromptRewriteSheet
import com.shifenmiao.online.service.PromptRewriteAction
import com.t8rin.imagetoolbox.core.resources.Icons as BoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatLineSpacing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShortText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.icons.LineEditorIcons
import kotlinx.coroutines.launch

@Composable
fun EditPromptScreen(
    editPromptComponent: EditPromptComponent,
    appComponent: AppComponent
) {
    val uiState by editPromptComponent.uiState.collectAsState()
    val rewrite by editPromptComponent.rewrite.collectAsState()
    val engine by editPromptComponent.rewriteEngine.collectAsState()
    val categories by editPromptComponent.categories.collectAsState()
    val settings = LocalSettingsState.current
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val onNavigate = LocalOnNavigate.current
    var pendingMarkdown by remember { mutableStateOf<String?>(null) }
    var isPreparing by remember { mutableStateOf(false) }
    var draftTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var draftDescription by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategoryIds by rememberSaveable { mutableStateOf<IntArray?>(null) }

    val editorState = rememberNativeMarkdownEditorState(uiState.data)
    var showMoreMenu by remember { mutableStateOf(false) }
    val autoFocusBody = !uiState.isLoading &&
        !uiState.isEditing &&
        !uiState.isDirty &&
        uiState.title.isBlank() &&
        uiState.data.isBlank() &&
        editorState.isEmpty

    val onBack = {
        if (uiState.isSaving || isPreparing) {
            Unit
        } else if (
            settings.enableToolExitConfirmation &&
            (uiState.isDirty || draftTitle != null || draftDescription != null || selectedCategoryIds != null)
        ) {
            showExitDialog.value = true
        } else {
            appComponent.onGoBack()
        }
    }

    // 当数据加载完成时，设置初始内容（内容没变时 load 不会重置撤销栈与光标）
    LaunchedEffect(uiState.data) {
        if (uiState.data.isNotEmpty()) {
            editorState.load(uiState.data)
        }
    }
    fun openSaveConfirm() {
        if (isPreparing || uiState.isLoading || uiState.isSaving) return
        focusManager.clearFocus(force = true)
        isPreparing = true
        scope.launch {
            try {
                val markdown = editorState.exportMarkdown()
                if (markdown.isBlank()) {
                    AppToastHost.showToast(AppContext.getString(R.string.error_data_empty))
                    return@launch
                }
                pendingMarkdown = markdown
            } finally {
                isPreparing = false
            }
        }
    }

    fun tryPrompt() {
        focusManager.clearFocus()
        scope.launch {
            val body = editorState.exportMarkdown()
            if (body.isBlank()) return@launch
            onNavigate(Screen.AiChatScreen(Conversation(
                entryType = AIConversationEntryType.PROMPT,
                appTitle = draftTitle?.takeIf { it.isNotBlank() }
                    ?: uiState.title.ifBlank { AppContext.getString(R.string.create_ai_chat_prompt_preview_default_title) },
                prompt = body,
                placeholder = uiState.placeholder,
            )))
        }
    }

    BaseScreen(
        title = {},
        type = EnhancedTopAppBarType.Normal,
        onGoBack = onBack,
        actions = {
            IconButton(
                onClick = ::openSaveConfirm,
                enabled = !uiState.isLoading && !uiState.isSaving && !isPreparing,
            ) {
                Icon(
                    imageVector = LineEditorIcons.Save,
                    contentDescription = stringResource(com.shifenmiao.common.R.string.common_save),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Box {
                IconButton(
                    enabled = !uiState.isLoading && !uiState.isSaving && !isPreparing,
                    onClick = { showMoreMenu = true },
                ) {
                    Icon(
                        imageVector = LineEditorIcons.More,
                        contentDescription = stringResource(R.string.card_more_actions),
                    )
                }
                EnhancedDropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                ) {
                    if (!editorState.isEmpty) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.create_ai_chat_prompt_preview)) },
                            leadingIcon = { Icon(BoxIcons.Outlined.LineVisibility, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                tryPrompt()
                            },
                        )
                    }
                    PromptRewriteAction.entries.filter { action ->
                        (action == PromptRewriteAction.Create) == editorState.isEmpty
                    }.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(stringResource(action.label)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (action) {
                                        PromptRewriteAction.Create -> BoxIcons.Outlined.LineAddAiChat
                                        PromptRewriteAction.Enhance -> BoxIcons.Outlined.LineMagic
                                        PromptRewriteAction.Format -> BoxIcons.Outlined.LineFormatLineSpacing
                                        PromptRewriteAction.Condense -> BoxIcons.Outlined.LineShortText
                                    },
                                    contentDescription = null,
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                focusManager.clearFocus()
                                scope.launch {
                                    val original = editorState.exportMarkdown()
                                    if ((action == PromptRewriteAction.Create) == original.isBlank()) {
                                        editPromptComponent.openRewrite(action, original)
                                    }
                                }
                            },
                        )
                    }
                }
            }
        },
        showNavigationBarsPadding = false,
    ) {
        NativeMarkdownEditor(
            state = editorState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            autoFocus = autoFocusBody,
            useVectorDockIcons = true,
            onContentChanged = editPromptComponent::markAsDirty,
        )
    }

    rewrite?.let { state ->
        PromptRewriteSheet(
            state = state,
            engine = editPromptComponent.effectiveRewriteEngine() ?: engine,
            onRequirementChange = editPromptComponent::updateRequirement,
            onSwitchEngine = appComponent::showAIModelsModalSheet,
            onGenerate = editPromptComponent::generateRewrite,
            onStop = editPromptComponent::stopRewrite,
            onDismiss = editPromptComponent::closeRewrite,
            onApply = {
                scope.launch {
                    if (!state.running && editorState.exportMarkdown() == state.original &&
                        editPromptComponent.rewrite.value === state && state.candidate.isNotBlank()
                    ) {
                        editorState.load(state.candidate, keepHistory = true)
                        editPromptComponent.markAsDirty()
                        editPromptComponent.closeRewrite()
                    } else {
                        AppToastHost.showToast(OnlineR.string.prompt_ai_stale)
                    }
                }
            },
        )
    }

    val markdown = pendingMarkdown
    if (markdown != null) {
        val suggested = remember(markdown) { MarkdownSummary.derive(markdown) }
        val title = draftTitle ?: uiState.title.ifBlank { suggested.title }
        val description = draftDescription ?: uiState.description.ifBlank { suggested.description }
        val selected = selectedCategoryIds?.toSet() ?: uiState.selectedCategories.map { it.id }.toSet()
        ContentSaveConfirmDialog(
            visible = true,
            dialogTitle = stringResource(OnlineR.string.prompt_save_confirm_title),
            dialogIcon = LineEditorIcons.Save,
            title = title,
            description = description,
            categories = categories,
            selectedCategoryIds = selected,
            isSaving = uiState.isSaving,
            onTitleChange = { draftTitle = it },
            onDescriptionChange = { draftDescription = it },
            onToggleCategory = { category ->
                selectedCategoryIds = (if (category.id in selected) selected - category.id else selected + category.id)
                    .toIntArray()
            },
            onConfirm = {
                editPromptComponent.savePromptData(
                    markdown = markdown,
                    title = title.trim(),
                    description = description.trim(),
                    selectedCategoryIds = selected,
                    onSuccess = {
                        pendingMarkdown = null
                        AppToastHost.showSaveSuccessToast(R.string.save_success)
                        appComponent.onGoBack()
                    },
                    onFailure = { AppToastHost.showToast(it) },
                )
            },
            onDismiss = { pendingMarkdown = null },
            onCreateCategory = { name ->
                editPromptComponent.addCategory(name) { id ->
                    val ids = selectedCategoryIds?.toSet() ?: uiState.selectedCategories.map { it.id }.toSet()
                    selectedCategoryIds = (ids + id).toIntArray()
                }
            },
            onManageCategories = {
                pendingMarkdown = null
                onNavigate(Screen.Reorderable(type = editPromptComponent.reorderableType))
            },
        )
    }

    ExitWithoutSavingDialog(
        onExit = {
            appComponent.onGoBack()
        },
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value
    )
}
