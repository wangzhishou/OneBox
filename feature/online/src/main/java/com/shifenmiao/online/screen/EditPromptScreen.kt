/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shifenmiao.online.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.shifenmiao.common.ui.CreationMetaConfigSheet
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.event.MainClickEvent
import com.shifenmiao.model.ai.event.MainClickEventFrom
import com.shifenmiao.model.ai.event.MainShowType
import com.shifenmiao.model.event.AppEventBus
import com.shifenmiao.online.R as OnlineR
import com.shifenmiao.online.component.EditPromptComponent
import com.shifenmiao.online.service.PromptRewriteAction
import com.shifenmiao.online.ui.ContentSaveConfirmDialog
import com.shifenmiao.online.ui.PromptRewriteSheet
import com.t8rin.imagetoolbox.core.resources.Icons as BoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineFormatLineSpacing
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShortText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.icons.LineEditorIcons
import kotlinx.coroutines.launch

@Composable
fun EditPromptScreen(
    editPromptComponent: EditPromptComponent,
    appComponent: AppComponent,
) {
    val uiState by editPromptComponent.uiState.collectAsState()
    val rewrite by editPromptComponent.rewrite.collectAsState()
    val engine by editPromptComponent.rewriteEngine.collectAsState()
    val categories by editPromptComponent.categories.collectAsState()
    val availableTools by editPromptComponent.availableTools.collectAsState()
    val settings = LocalSettingsState.current
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val onNavigate = LocalOnNavigate.current
    var pendingMarkdown by remember { mutableStateOf<String?>(null) }
    var isPreparing by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showConfigSheet by rememberSaveable { mutableStateOf(false) }

    val editorState = rememberNativeMarkdownEditorState(uiState.data)
    val autoFocusBody = !uiState.isLoading &&
        !uiState.isEditing &&
        !uiState.isDirty &&
        uiState.title.isBlank() &&
        uiState.data.isBlank() &&
        editorState.isEmpty

    val onBack = {
        if (uiState.isSaving || isPreparing) {
            Unit
        } else if (settings.enableToolExitConfirmation && uiState.isDirty) {
            showExitDialog.value = true
        } else {
            appComponent.onGoBack()
        }
    }

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
                val suggested = MarkdownSummary.derive(markdown)
                if (uiState.title.isBlank()) editPromptComponent.updateTitle(suggested.title)
                if (uiState.description.isBlank()) {
                    editPromptComponent.updateDescription(suggested.description)
                }
                pendingMarkdown = markdown
            } finally {
                isPreparing = false
            }
        }
    }

    fun openAiAction(action: PromptRewriteAction) {
        focusManager.clearFocus()
        scope.launch {
            val original = editorState.exportMarkdown()
            val effectiveAction = if (original.isBlank()) PromptRewriteAction.Create else action
            if (effectiveAction == PromptRewriteAction.Create && original.isNotBlank()) return@launch
            editPromptComponent.openRewrite(effectiveAction, original)
        }
    }

    fun tryPrompt() {
        focusManager.clearFocus()
        scope.launch {
            val body = editorState.exportMarkdown()
            if (body.isBlank()) return@launch
            onNavigate(
                Screen.AiChatScreen(
                    Conversation(
                        entryType = AIConversationEntryType.PROMPT,
                        appTitle = uiState.title.ifBlank { AppContext.getString(R.string.prompt_create) },
                        prompt = body,
                        placeholder = uiState.placeholder,
                    )
                )
            )
        }
    }

    BaseScreen(
        title = {
            Text(
                stringResource(
                    if (uiState.isEditing) R.string.edit_prompt else R.string.ai_prompt_picker_create
                )
            )
        },
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
                )
            }
            Box {
                IconButton(
                    enabled = !uiState.isLoading && !uiState.isSaving && !isPreparing,
                    onClick = { showMoreMenu = true },
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMore,
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
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.create_ai_chat_prompt_config_title)) },
                        leadingIcon = { Icon(BoxIcons.Outlined.LineTune, contentDescription = null) },
                        onClick = {
                            showMoreMenu = false
                            showConfigSheet = true
                        },
                    )
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
                                openAiAction(action)
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
            onGetPoints = {
                AppEventBus.emit(
                    MainClickEvent(
                        from = MainClickEventFrom.AI_PROMPT_CREATE,
                        type = MainShowType.BUY_COFFEE,
                    )
                )
            },
            loadHistory = { editPromptComponent.recentRewriteResults(state.action) },
            onHistorySelected = editPromptComponent::selectRewriteResult,
            onApply = {
                scope.launch {
                    if (!state.running && editorState.exportMarkdown() == state.original &&
                        editPromptComponent.rewrite.value === state && state.candidate.isNotBlank()
                    ) {
                        editorState.load(state.candidate, keepHistory = true)
                        editPromptComponent.markAsDirty()
                        editPromptComponent.closeRewrite()
                        AppToastHost.showToast(OnlineR.string.prompt_ai_applied_undo)
                    } else {
                        AppToastHost.showToast(OnlineR.string.prompt_ai_stale)
                    }
                }
            },
        )
    }

    if (showConfigSheet) {
        CreationMetaConfigSheet(
            title = stringResource(R.string.create_ai_chat_prompt_config_title),
            categories = categories,
            selectedCategoryIds = uiState.selectedCategoryIds,
            availableTools = availableTools,
            selectedToolNames = uiState.selectedToolNames,
            categoryHint = "",
            toolTitle = stringResource(R.string.create_ai_chat_prompt_select_tool),
            toolEmptyHint = stringResource(R.string.create_ai_common_tools_empty),
            toolSelectedHint = {
                AppContext.getContext().getString(R.string.creation_meta_selected_tool_count, it)
            },
            confirmText = stringResource(R.string.create_ai_chat_prompt_apply_config),
            onDismiss = { showConfigSheet = false },
            onAddCategory = { name ->
                editPromptComponent.addCategory(name) { id ->
                    editPromptComponent.updateSelectedCategoryIds(uiState.selectedCategoryIds + id)
                }
            },
            onConfirm = { selectedCategoryIds, selectedToolNames ->
                editPromptComponent.updateAdvancedConfiguration(selectedCategoryIds, selectedToolNames)
                showConfigSheet = false
            },
        )
    }

    val markdown = pendingMarkdown
    if (markdown != null) {
        val selected = uiState.selectedCategoryIds
        ContentSaveConfirmDialog(
            visible = true,
            dialogTitle = stringResource(OnlineR.string.prompt_save_confirm_title),
            dialogIcon = LineEditorIcons.Save,
            title = uiState.title,
            description = uiState.description,
            categories = categories,
            selectedCategoryIds = selected,
            isSaving = uiState.isSaving,
            onTitleChange = editPromptComponent::updateTitle,
            onDescriptionChange = editPromptComponent::updateDescription,
            onToggleCategory = { category ->
                editPromptComponent.updateSelectedCategoryIds(
                    if (category.id in selected) selected - category.id else selected + category.id
                )
            },
            onConfirm = {
                editPromptComponent.savePromptData(
                    markdown = markdown,
                    title = uiState.title.trim(),
                    description = uiState.description.trim(),
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
                    editPromptComponent.updateSelectedCategoryIds(uiState.selectedCategoryIds + id)
                }
            },
            onManageCategories = {
                pendingMarkdown = null
                onNavigate(Screen.Reorderable(type = editPromptComponent.reorderableType))
            },
        )
    }

    ExitWithoutSavingDialog(
        onExit = appComponent.onGoBack,
        onDismiss = { showExitDialog.value = false },
        visible = showExitDialog.value,
    )
}
