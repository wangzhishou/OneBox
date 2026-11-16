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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifenmiao.common.R as CommonR
import com.shifenmiao.common.components.category.AddCategoryDialog
import com.shifenmiao.common.editor.NativeMarkdownEditor
import com.shifenmiao.common.editor.rememberNativeMarkdownEditorState
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.database.item.entity.toModel
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.model.item.ItemDataUiState
import com.shifenmiao.online.component.CreateNoteComponent
import com.shifenmiao.online.ui.NoteSaveConfirmDialog
import com.t8rin.imagetoolbox.core.resources.Icons as OneBoxIcons
import com.t8rin.imagetoolbox.core.resources.icons.Check
import com.t8rin.imagetoolbox.core.resources.icons.NoteAdd
import com.t8rin.imagetoolbox.core.settings.presentation.provider.LocalSettingsState
import com.wanbaohe.com.string.MarkdownSummary
import com.wanbaohe.core.ui.utils.helper.AppToastHost
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.enhanced.EnhancedAlertDialog
import com.wanbaohe.core.ui.widget.enhanced.EnhancedTopAppBarType
import com.wanbaohe.core.ui.widget.glass.GlassFilterChip
import com.wanbaohe.core.ui.widget.glass.GlassStyle
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

    BaseScreen(
        modifier = Modifier,
        title = {
            NoteTitleField(
                title = uiState.title,
                onTitleChange = createNoteComponent::onTitleChange,
            )
        },
        type = EnhancedTopAppBarType.Center,
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
            IconButton(
                onClick = ::openSaveConfirm,
                enabled = !uiState.isSaving,
            ) {
                Icon(
                    imageVector = OneBoxIcons.Outlined.Check,
                    contentDescription = stringResource(R.string.save_file),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
    ) {
        NoteEditorCategoryStrip(
            createNoteComponent = createNoteComponent,
            uiState = uiState,
            categories = categories,
        )
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
            TextButton(
                onClick = {
                    showExitDialog.value = false
                    openSaveConfirm()
                }
            ) {
                Text(text = stringResource(R.string.save_file))
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        showExitDialog.value = false
                        appComponent.onGoBack()
                    }
                ) {
                    Text(
                        text = stringResource(CommonR.string.note_discard_and_exit),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                TextButton(
                    onClick = { showExitDialog.value = false }
                ) {
                    Text(text = stringResource(CommonR.string.note_keep_editing))
                }
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
            textAlign = TextAlign.Center,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (title.isBlank()) {
                    Text(
                        text = stringResource(CommonR.string.note_untitled),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun NoteEditorCategoryStrip(
    createNoteComponent: CreateNoteComponent,
    uiState: ItemDataUiState,
    categories: List<com.shifenmiao.database.item.entity.Category>,
) {
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }
    val selectedIds = uiState.selectedCategories.map { it.id }.toSet()

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item(key = "add-category") {
            GlassFilterChip(
                selected = false,
                onClick = { showAddCategoryDialog = true },
                label = {
                    Text(
                        text = stringResource(R.string.creation_meta_add_category),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                style = GlassStyle.Thin,
            )
        }
        items(categories, key = { it.id }) { category ->
            val selected = category.id in selectedIds
            GlassFilterChip(
                selected = selected,
                onClick = {
                    val updated = if (selected) {
                        uiState.selectedCategories.filterNot { it.id == category.id }
                    } else {
                        uiState.selectedCategories + category.toModel()
                    }
                    createNoteComponent.saveCategories(updated.toList())
                },
                label = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            imageVector = OneBoxIcons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                } else {
                    null
                },
                style = GlassStyle.Thin,
            )
        }
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onConfirm = { name ->
                createNoteComponent.addCategory(name)
                showAddCategoryDialog = false
            },
        )
    }
}

private const val NOTE_TITLE_MAX_LENGTH = 120
