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
 */

package com.shifenmiao.online.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons as MaterialIcons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R as CommonR
import com.shifenmiao.common.editor.NativeMarkdownEditor
import com.shifenmiao.common.editor.rememberNativeMarkdownEditorState
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.component.NoteAiAction
import com.shifenmiao.online.component.NoteAiApplyMode
import com.shifenmiao.online.R as OnlineR
import com.shifenmiao.online.ui.NoteAiRewriteSheet
import com.shifenmiao.online.ui.NoteSaveConfirmDialog
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedDropdownMenu
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.system.OneBoxDangerButton
import com.wanbaohe.core.ui.widget.system.OnePrimaryButton
import com.wanbaohe.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.launch

@Composable
fun CreateNoteScreen(
    createNoteComponent: CreateNoteComponent,
    appComponent: AppComponent,
) {
    val uiState by createNoteComponent.uiState.collectAsState()
    val categories by createNoteComponent.categories.collectAsState()
    val showExitDialog = rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val settingsState = LocalSettingsState.current

    val editorState = rememberNativeMarkdownEditorState(uiState.data)

    var showSaveConfirm by rememberSaveable { mutableStateOf(false) }
    var draftTitle by rememberSaveable { mutableStateOf("") }
    var draftDescription by rememberSaveable { mutableStateOf("") }
    var pendingMarkdown by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var draftCategoryIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    val onNavigate = LocalOnNavigate.current

    val aiState by createNoteComponent.aiState.collectAsState()
    val currentAiEngine by createNoteComponent.currentAiEngine.collectAsState()
    // 应用 AI 结果后保留一份「应用前」的正文，面板关闭前可一键撤销。
    var aiUndoMarkdown by remember { mutableStateOf<String?>(null) }

    val onBack = {
        if (uiState.isDirty && settingsState.enableToolExitConfirmation) {
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

    fun openSaveConfirm() {
        // 打开确认弹窗前先收起键盘，避免输入法浮条压住标题/分类字段。
        focusManager.clearFocus(force = true)
        scope.launch {
            val markdown = editorState.exportMarkdown()
            val derived = MarkdownSummary.derive(markdown)
            pendingMarkdown = markdown
            draftTitle = uiState.title.ifBlank { derived.title }
            draftDescription = uiState.description.ifBlank { derived.description }
            draftCategoryIds = uiState.selectedCategories.map { it.id }.toSet()
            showSaveConfirm = true
        }
    }

    fun generateMetadata() {
        if (isGenerating) return
        // 与写作面板共用准入逻辑：直连直接跑，代理引擎先拉起登录，再交给 MANAGED 执行器做积分闸门。
        createNoteComponent.runWithAiAccess(source = "note_ai_metadata") {
            scope.launch {
                isGenerating = true
                try {
                    val metadata = runCatching {
                        createNoteComponent.generateMetadata(pendingMarkdown)
                    }.getOrNull()
                    if (metadata == null) {
                        AppToastHost.showToast(
                            AppContext.getString(CommonR.string.note_ai_generate_failed)
                        )
                        return@launch
                    }
                    if (metadata.title.isNotBlank()) {
                        draftTitle = metadata.title
                        createNoteComponent.onTitleChange(metadata.title)
                    }
                    if (metadata.description.isNotBlank()) {
                        draftDescription = metadata.description
                        createNoteComponent.onDescriptionChange(metadata.description)
                    }
                    val suggestedCategory = metadata.categoryId
                        ?.let { id -> categories.firstOrNull { it.id == id } }
                    if (suggestedCategory != null) {
                        val current = createNoteComponent.uiState.value.selectedCategories
                        if (current.none { it.id == suggestedCategory.id }) {
                            draftCategoryIds = draftCategoryIds + suggestedCategory.id
                            createNoteComponent.saveCategories(
                                (current + suggestedCategory.toModel()).toList()
                            )
                        }
                    }
                    if (metadata.usedFallback) {
                        AppToastHost.showToast(
                            metadata.errorMessage
                                ?: AppContext.getString(CommonR.string.note_ai_generate_failed)
                        )
                    }
                } finally {
                    isGenerating = false
                }
            }
        }
    }

    fun confirmSave() {
        val title = draftTitle.trim()
        if (title.isBlank()) {
            AppToastHost.showToast(AppContext.getString(R.string.error_title_empty))
            return
        }
        createNoteComponent.saveNoteData(
            markdown = pendingMarkdown,
            title = title,
            description = draftDescription.trim(),
            categories = categories
                .filter { it.id in draftCategoryIds }
                .map { it.toModel() },
            onSuccess = {
                showSaveConfirm = false
                AppToastHost.showToast(AppContext.getString(R.string.save_success))
                if (uiState.isEditing) {
                    // 编辑已有笔记：保存后回到笔记详情，而不是把用户甩回首页。
                    appComponent.onGoBack()
                } else {
                    appComponent.onNavigateReplacingCurrent(
                        Screen.NewApp(initialTab = HomeTabKey.TEXT)
                    )
                }
            },
            onFailure = { errorMsg ->
                AppToastHost.showToast(errorMsg)
            },
        )
    }

    fun openAiAction(action: NoteAiAction) {
        focusManager.clearFocus(force = true)
        scope.launch {
            val markdown = editorState.exportMarkdown()
            if (markdown.isBlank()) {
                AppToastHost.showToast(
                    AppContext.getString(OnlineR.string.note_ai_too_short)
                )
                return@launch
            }
            aiUndoMarkdown = null
            createNoteComponent.openAiAction(action, markdown)
        }
    }

    fun applyAiResult(result: String) {
        val original = aiState.originalMarkdown
        val applied = when (aiState.action.applyMode) {
            NoteAiApplyMode.Replace -> result
            NoteAiApplyMode.Append -> joinNoteParts(original, result)
            NoteAiApplyMode.Prepend -> joinNoteParts(result, original)
        }
        if (applied.isBlank()) return
        aiUndoMarkdown = original
        editorState.load(applied)
        createNoteComponent.markAsDirty()
    }

    fun undoAiApply() {
        val before = aiUndoMarkdown ?: return
        aiUndoMarkdown = null
        editorState.load(before)
        createNoteComponent.markAsDirty()
    }

    BaseScreen(
        modifier = Modifier,
        title = {
            NoteTitleField(
                title = uiState.title,
                onTitleChange = createNoteComponent::onTitleChange,
            )
        },
        type = EnhancedTopAppBarType.Normal,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onGoBack = onBack,
        supportGlassEffect = true,
        showNavigationBarsPadding = false,
        actions = {
            // 保存图标与 Markdown 编辑器统一（LineSave），不再用对勾。
            IconButton(
                onClick = ::openSaveConfirm,
                enabled = !uiState.isSaving,
            ) {
                Icon(
                    imageVector = OneBoxIcons.Outlined.LineSave,
                    contentDescription = stringResource(CommonR.string.common_save),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Box {
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = MaterialIcons.Filled.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                EnhancedDropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                ) {
                    // 标题/摘要/分类统一放到保存（设置）弹窗里维护，不再单独显示分类栏。
                    DropdownMenuItem(
                        text = { Text(stringResource(OnlineR.string.note_menu_settings)) },
                        onClick = {
                            showMoreMenu = false
                            openSaveConfirm()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = OneBoxIcons.Outlined.LineSettings,
                                contentDescription = null,
                            )
                        },
                    )
                    HorizontalDivider()
                    listOf(
                        NoteAiAction.Format,
                        NoteAiAction.Expand,
                        NoteAiAction.Polish,
                    ).forEach { action ->
                        DropdownMenuItem(
                            text = { Text(stringResource(action.labelRes)) },
                            onClick = {
                                showMoreMenu = false
                                openAiAction(action)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = OneBoxIcons.Outlined.LineMagic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) {
        NativeMarkdownEditor(
            state = editorState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onContentChanged = createNoteComponent::markAsDirty,
        )
    }

    BackHandler(onBack = onBack)

    EnhancedAlertDialog(
        visible = showExitDialog.value,
        onDismissRequest = { showExitDialog.value = false },
        icon = {
            Icon(
                imageVector = OneBoxIcons.Outlined.NoteAdd,
                contentDescription = null,
            )
        },
        title = { Text(text = stringResource(CommonR.string.note_unsaved_title)) },
        text = { Text(text = stringResource(CommonR.string.note_unsaved_message)) },
        confirmButton = {
            // 对话框按钮统一走共用胶囊按钮，不再用纯文字按钮。
            OnePrimaryButton(
                text = stringResource(CommonR.string.common_save),
                leadingIcon = OneBoxIcons.Outlined.LineSave,
                onClick = {
                    showExitDialog.value = false
                    openSaveConfirm()
                },
            )
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OneBoxDangerButton(
                    text = stringResource(CommonR.string.note_discard_and_exit),
                    leadingIcon = MaterialIcons.Filled.Close,
                    onClick = {
                        showExitDialog.value = false
                        appComponent.onGoBack()
                    },
                )
                OneSecondaryButton(
                    text = stringResource(CommonR.string.note_keep_editing),
                    onClick = { showExitDialog.value = false },
                )
            }
        },
    )

    NoteSaveConfirmDialog(
        visible = showSaveConfirm,
        title = draftTitle,
        description = draftDescription,
        categories = categories,
        selectedCategoryIds = draftCategoryIds,
        isSaving = uiState.isSaving,
        isGenerating = isGenerating,
        onTitleChange = {
            draftTitle = it
            createNoteComponent.onTitleChange(it)
        },
        onDescriptionChange = {
            draftDescription = it
            createNoteComponent.onDescriptionChange(it)
        },
        onToggleCategory = { category ->
            draftCategoryIds = if (category.id in draftCategoryIds) {
                draftCategoryIds - category.id
            } else {
                draftCategoryIds + category.id
            }
            createNoteComponent.saveCategories(
                categories
                    .filter { it.id in draftCategoryIds }
                    .map { it.toModel() }
            )
        },
        onGenerate = ::generateMetadata,
        onConfirm = ::confirmSave,
        onDismiss = { showSaveConfirm = false },
        onCreateCategory = { name ->
            createNoteComponent.addCategory(name) { added ->
                draftCategoryIds = draftCategoryIds + added.id
            }
        },
        onManageCategories = {
            showSaveConfirm = false
            onNavigate(Screen.Reorderable(type = createNoteComponent.reorderableType))
        },
    )

    NoteAiRewriteSheet(
        state = aiState,
        currentEngine = currentAiEngine,
        onActionChange = createNoteComponent::changeAiAction,
        onPromptChange = createNoteComponent::updateAiPrompt,
        onSwitchEngine = appComponent::showAIModelsModalSheet,
        onGenerate = createNoteComponent::runAiGeneration,
        onStop = createNoteComponent::cancelAiGeneration,
        onDismiss = {
            aiUndoMarkdown = null
            createNoteComponent.dismissAi()
        },
        onApply = ::applyAiResult,
        canUndo = aiUndoMarkdown != null,
        onUndo = ::undoAiApply,
    )
}

@Composable
private fun NoteTitleField(
    title: String,
    onTitleChange: (String) -> Unit,
) {
    BasicTextField(
        value = title,
        onValueChange = {
            if (it.length <= NOTE_TITLE_MAX_LENGTH) onTitleChange(it)
        },
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.titleLarge.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (title.isBlank()) {
                    Text(
                        text = stringResource(CommonR.string.note_untitled),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                innerTextField()
            }
        },
    )
}

private fun joinNoteParts(first: String, second: String): String =
    listOf(first.trim(), second.trim())
        .filter { it.isNotBlank() }
        .joinToString("\n\n")

private const val NOTE_TITLE_MAX_LENGTH = 120
