package com.shifenmiao.ai.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifenmiao.ai.component.CreateAIPromptComponent
import com.shifenmiao.ai.component.GenerationStatus
import com.shifenmiao.model.ai.tool.ToolCategory
import com.shifenmiao.base.ui.DeleteConfirmDialog
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.DraftsCarousel
import com.shifenmiao.core.R
import com.shifenmiao.database.data_draft.entity.DataDraftEntity
import com.shifenmiao.database.item.entity.Category
import com.shifenmiao.model.ai.ChatPrompt
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.tool.ToolCatalogItem
import com.shifenmiao.model.HomeTabKey
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.common.ui.CreationMetaConfigSheet
import com.shifenmiao.common.ui.SelectedToolSummary
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.utils.getString
import java.util.Date
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineError
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSave
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSend
import com.t8rin.imagetoolbox.core.resources.icons.line.LineVisibility

private val ContentCardMinHeight = 280.dp
private val ContentCardMaxHeight = 420.dp

@Composable
fun CreateAIPromptScreen(
    component: CreateAIPromptComponent
) {
    val uiState by component.uiState.collectAsState()
    val drafts by component.drafts.collectAsState()
    val categories by component.categories.collectAsState()
    val availableTools by component.availableTools.collectAsState()
    val systemToolNames = remember(availableTools) {
        availableTools.filter { it.category == ToolCategory.SYSTEM }.map { it.name }.toSet()
    }
    val currentAIEngine by component.aiEngineManager.currentAIEngine.collectAsState()
    val currentAIModel by component.aiEngineManager.currentAIModel.collectAsState()
    val allEngines by component.aiEngineCatalogManager.observeAvailableEngines()
        .collectAsState(initial = emptyList())
    val modelsByProvider by component.aiEngineCatalogManager.observeModelsByProvider()
        .collectAsState(initial = emptyMap())
    val selectedToolsPreview = remember(uiState.selectedToolNames, availableTools) {
        availableTools
            .filter { it.name in uiState.selectedToolNames }
            .sortedWith(compareBy<ToolCatalogItem> { it.sortOrder }.thenBy { it.title })
    }
    val selectedCategoryNames = remember(uiState.selectedCategoryIds, categories) {
        categories
            .filter { it.id in uiState.selectedCategoryIds }
            .map(Category::name)
    }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val windowSize = LocalWindowInfo.current.containerSize
    val contentCardHeight = remember(windowSize) {
        val screenHeight = if (windowSize != IntSize.Zero) {
            windowSize.height.dp
        } else {
            ContentCardMaxHeight
        }
        (screenHeight * 0.45f).coerceIn(ContentCardMinHeight, ContentCardMaxHeight)
    }

    var showConfigSheet by rememberSaveable { mutableStateOf(false) }
    var showModelPicker by rememberSaveable { mutableStateOf(false) }
    val showDeleteDialogState = remember { mutableStateOf(false) }
    var draftToDelete by remember { mutableStateOf<DataDraftEntity?>(null) }
    var showDiscardDialog by rememberSaveable { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun handleActionWithConfirm(action: () -> Unit) {
        if (component.hasUnsavedChanges()) {
            pendingAction = action
            showDiscardDialog = true
        } else {
            action()
        }
    }

    BaseScreen(
        title = stringResource(R.string.create_ai_chat_prompt_title),
        onGoBack = {
            handleActionWithConfirm(component.onGoBack)
        },
        showNavigationBarsPadding = false,
        actions = {
            IconButton(
                onClick = {
                    handleActionWithConfirm(component::resetForNew)
                },
                colors = AppTheme.colors.iconButtonColors()
            ) {
                Icon(com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add, contentDescription = stringResource(R.string.new_add))
            }
        }
    ) {
        val contentScrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(contentScrollState)
        ) {
            if (drafts.isNotEmpty()) {
                DraftsCarousel(
                    drafts = drafts,
                    activeDraftId = uiState.currentDraftId,
                    title = stringResource(R.string.create_ai_chat_prompt_drafts),
                    collapseContentDescription = stringResource(R.string.create_ai_chat_prompt_collapse),
                    expandContentDescription = stringResource(R.string.create_ai_chat_prompt_expand),
                    activeColor = MaterialTheme.colorScheme.tertiary,
                    onDraftClick = { draft ->
                        handleActionWithConfirm {
                            component.loadDraft(draft)
                        }
                    },
                    onDeleteRequest = { draft ->
                        draftToDelete = draft
                        showDeleteDialogState.value = true
                    },
                    draftStatusText = { draft ->
                        when (draft.status) {
                            DataDraftEntity.STATUS_SUCCESS -> stringResource(R.string.create_ai_chat_prompt_draft_success)
                            DataDraftEntity.STATUS_FAILED -> stringResource(R.string.create_ai_chat_prompt_draft_failed)
                            else -> stringResource(R.string.create_ai_chat_prompt_draft_label)
                        }
                    },
                    draftStatusColor = { draft ->
                        when (draft.status) {
                            DataDraftEntity.STATUS_SUCCESS -> MaterialTheme.colorScheme.primary
                            DataDraftEntity.STATUS_FAILED -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            AnimatedContent(
                targetState = uiState.status,
                label = "result_area",
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(
                        initialScale = 0.94f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                    )).togetherWith(
                        fadeOut(tween(200)) + scaleOut(
                            targetScale = 0.94f,
                            animationSpec = tween(200)
                        )
                    )
                }
            ) { status ->
                when (status) {
                    GenerationStatus.IDLE -> {
                        PromptPlaceholderSection(contentCardHeight = contentCardHeight)
                    }

                    GenerationStatus.STREAMING -> {
                        PromptStreamingSection(
                            rawJson = uiState.rawJson,
                            reasoningText = uiState.reasoningText,
                            contentCardHeight = contentCardHeight
                        )
                    }

                    GenerationStatus.SUCCESS -> {
                        uiState.parsedPrompt?.let { chatPrompt ->
                            PromptPreviewSection(
                                chatPrompt = chatPrompt,
                                conversationRound = uiState.conversationRound,
                                selectedCategoryCount = uiState.selectedCategoryIds.size,
                                selectedCategoryNames = selectedCategoryNames,
                                selectedToolCount = uiState.selectedToolNames.size,
                                selectedToolsPreview = selectedToolsPreview,
                                isSaving = uiState.isSaving,
                                contentCardHeight = contentCardHeight,
                                onPreviewClick = {
                                    ActionUtils.showLogin(source = "create_ai_prompt_preview") {
                                        component.saveAndPreview(
                                            onReady = { savedPrompt ->
                                                component.onNavigate(
                                                    Screen.AiChatScreen(
                                                        conversation = Conversation(
                                                            promptId = savedPrompt.id,
                                                            appTitle = savedPrompt.title.orEmpty(),
                                                            prompt = savedPrompt.prompt.orEmpty(),
                                                            entryType = AIConversationEntryType.PROMPT,
                                                            entryRefId = savedPrompt.id.toString(),
                                                            id = Date().time.toString(),
                                                            placeholder = savedPrompt.placeholder.orEmpty(),
                                                        )
                                                    )
                                                )
                                            },
                                            onBlock = { msg ->
                                                AppToastHost.showToast(msg)
                                            }
                                        )
                                    }
                                },
                                onEditClick = {
                                    ActionUtils.showLogin(source = "create_ai_prompt_edit") {
                                        component.editPromptManually()
                                    }
                                },
                                onConfigureClick = { showConfigSheet = true },
                                onTitleChange = component::updatePromptTitle,
                                onDescriptionChange = component::updatePromptDescription,
                                onSave = {
                                    ActionUtils.showLogin(source = "create_ai_prompt_save") {
                                        component.savePrompt(
                                            onSuccess = { savedResult ->
                                                AppToastHost.showToast(
                                                    getString(R.string.create_ai_chat_prompt_save_success)
                                                )
                                                component.onNavigateReplacingCurrent(
                                                    Screen.NewApp(initialTab = HomeTabKey.PROMPT)
                                                )
                                            },
                                            onFailure = { msg ->
                                                AppToastHost.showToast(msg)
                                            },
                                        )
                                    }
                                },
                            )
                        } ?: PromptPlaceholderSection(contentCardHeight = contentCardHeight)
                    }

                    GenerationStatus.FAILED -> {
                        PromptErrorSection(
                            message = uiState.errorMessage
                                ?: stringResource(R.string.create_ai_chat_prompt_parse_error),
                            rawJson = uiState.rawJson,
                            contentCardHeight = contentCardHeight,
                            onEditRaw = {
                                ActionUtils.showLogin(source = "create_ai_prompt_edit_raw") {
                                    component.editRawResultInEditor()
                                }
                            },
                            onRetry = {
                                ActionUtils.showLogin(source = "create_ai_prompt_retry") {
                                    component.generate()
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        PromptInputSection(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            inputText = uiState.inputText,
            isGenerating = uiState.status == GenerationStatus.STREAMING,
            isMultiTurn = uiState.isMultiTurn,
            currentModelTitle = currentAIModel.title.ifBlank { currentAIModel.name },
            onInputChanged = component::onInputChanged,
            onGenerate = {
                ActionUtils.showLogin(source = "create_ai_prompt_generate") {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    component.generate()
                }
            },
            onCancel = component::cancelGeneration,
            onConfigClick = { showConfigSheet = true },
            onModelSelectorClick = { showModelPicker = true },
        )
    }

    if (showConfigSheet) {
        CreationMetaConfigSheet(
            title = stringResource(R.string.create_ai_chat_prompt_config_title),
            categories = categories,
            selectedCategoryIds = uiState.selectedCategoryIds,
            availableTools = availableTools,
            selectedToolNames = uiState.selectedToolNames,
            systemToolNames = systemToolNames,
            categoryHint = "",
            toolTitle = stringResource(R.string.create_ai_chat_prompt_select_tool),
            toolEmptyHint = stringResource(R.string.create_ai_common_tools_empty),
            toolSelectedHint = { getString(R.string.creation_meta_selected_tool_count, it) },
            confirmText = stringResource(R.string.create_ai_chat_prompt_apply_config),
            onDismiss = { showConfigSheet = false },
            onAddCategory = { component.insertCategoryByName(it) },
            onConfirm = { selectedCategoryIds, selectedToolNames ->
                component.updateSelectedCategories(
                    categories.filter { it.id in selectedCategoryIds }
                )
                component.updateSelectedToolNames(selectedToolNames.toList())
                showConfigSheet = false
            },
        )
    }

    AIModelsPickerBottomSheet(
        visible = showModelPicker,
        allEngines = allEngines,
        modelsByProvider = modelsByProvider,
        selectedEngineName = currentAIEngine.identityKey(),
        selectedModelName = currentAIModel.name,
        onSelected = { engine, model ->
            component.aiEngineManager.switchModel(engine, model)
            showModelPicker = false
        },
        onDismiss = { showModelPicker = false },
    )

    if (showDeleteDialogState.value && draftToDelete != null) {
        DeleteConfirmDialog(
            onDelete = {
                draftToDelete?.let { component.deleteDraft(it) }
                AppToastHost.showToast(
                    getString(R.string.create_ai_chat_prompt_draft_deleted)
                )
                draftToDelete = null
            },
            showDeleteDialogState = showDeleteDialogState,
            message = stringResource(R.string.create_ai_chat_prompt_draft_delete_confirm),
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        pendingAction?.invoke()
                        pendingAction = null
                    }
                ) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(R.string.unsaved_changes)) },
            text = { Text(stringResource(R.string.unsaved_changes_message)) },
        )
    }
}

@Composable
private fun PromptPlaceholderSection(contentCardHeight: androidx.compose.ui.unit.Dp) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(contentCardHeight)
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .glassBackground(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.create_ai_chat_prompt_canvas_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

        }
    }
}

@Composable
private fun PromptInputSection(
    modifier: Modifier = Modifier,
    inputText: String,
    isGenerating: Boolean,
    isMultiTurn: Boolean,
    currentModelTitle: String,
    onInputChanged: (String) -> Unit,
    onGenerate: () -> Unit,
    onCancel: () -> Unit,
    onConfigClick: () -> Unit,
    onModelSelectorClick: () -> Unit,
) {
    GlassCard(
        modifier = modifier
            .padding(bottom = 8.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            GlassOutlinedTextField(
                value = inputText,
                onValueChange = onInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = if (isMultiTurn)
                            stringResource(R.string.create_ai_chat_prompt_multi_turn_placeholder)
                        else
                            stringResource(R.string.create_ai_chat_prompt_hint),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                minLines = if (isMultiTurn) 2 else 3,
                maxLines = 6,
                enabled = !isGenerating,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onGenerate() }),
                shape = MaterialTheme.shapes.medium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AIModelSelectorChip(
                        modelTitle = currentModelTitle,
                        onClick = onModelSelectorClick,
                    )
                }
                if (isGenerating) {
                    FilledTonalButton(
                        onClick = onCancel,
                        colors = AppTheme.colors.filledTonalButtonColors(),
                    ) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.create_ai_chat_prompt_cancel))
                    }
                } else {
                    FilledTonalButton(
                        onClick = onGenerate,
                        enabled = inputText.isNotBlank(),
                        colors = AppTheme.colors.filledTonalButtonColors(),
                    ) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSend,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (isMultiTurn)
                                stringResource(R.string.create_ai_chat_prompt_refine)
                            else
                                stringResource(R.string.create_ai_chat_prompt_generate)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptStreamingSection(
    rawJson: String,
    reasoningText: String,
    contentCardHeight: androidx.compose.ui.unit.Dp,
) {
    val stageLabel = when {
        rawJson.isEmpty() -> stringResource(R.string.create_ai_chat_prompt_stream_stage_structure)
        rawJson.contains("\"prompt\"") -> {
            if (rawJson.length > 200) stringResource(R.string.create_ai_chat_prompt_stream_stage_finalizing)
            else stringResource(R.string.create_ai_chat_prompt_stream_stage_content)
        }

        else -> stringResource(R.string.create_ai_chat_prompt_stream_stage_structure)
    }

    var showRawJson by rememberSaveable { mutableStateOf(false) }
    val reasoningScrollState = rememberScrollState()
    val rawScrollState = rememberScrollState()

    LaunchedEffect(reasoningText.length) {
        if (reasoningText.isNotEmpty()) {
            reasoningScrollState.scrollTo(reasoningScrollState.maxValue)
        }
    }

    LaunchedEffect(showRawJson, rawJson.length) {
        if (showRawJson && rawJson.isNotEmpty()) {
            rawScrollState.scrollTo(rawScrollState.maxValue)
        }
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(contentCardHeight)
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = stageLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (rawJson.isNotEmpty()) {
                    IconButton(
                        onClick = { showRawJson = !showRawJson },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor,
                            contentDescription = stringResource(R.string.create_ai_chat_prompt_stream_raw_toggle),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .glassBackground(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                                shape = MaterialTheme.shapes.large,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    Text(
                        text = stringResource(R.string.create_ai_chat_prompt_generating),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.create_ai_stream_output_chars, rawJson.length),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .glassBackground(
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f),
                                shape = MaterialTheme.shapes.medium,
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.create_ai_stream_reasoning_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 76.dp)
                                .verticalScroll(reasoningScrollState)
                        ) {
                            Text(
                                text = reasoningText.ifBlank {
                                    stringResource(R.string.create_ai_stream_reasoning_waiting)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = if (reasoningText.isBlank()) 0.65f else 0.85f
                                ),
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = showRawJson) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 120.dp)
                            .verticalScroll(rawScrollState)
                            .glassBackground(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = rawJson,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptPreviewSection(
    chatPrompt: ChatPrompt,
    conversationRound: Int,
    selectedCategoryCount: Int,
    selectedCategoryNames: List<String>,
    selectedToolCount: Int,
    selectedToolsPreview: List<ToolCatalogItem>,
    isSaving: Boolean,
    contentCardHeight: androidx.compose.ui.unit.Dp,
    onPreviewClick: () -> Unit,
    onEditClick: () -> Unit,
    onConfigureClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    var isMetaEditing by rememberSaveable { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(contentCardHeight)
            .padding(horizontal = 16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                EditableMetaSection(
                    modifier = Modifier.weight(1f),
                    title = chatPrompt.title,
                    description = chatPrompt.description,
                    defaultTitle = stringResource(R.string.create_ai_chat_prompt_preview_default_title),
                    accentColor = MaterialTheme.colorScheme.tertiary,
                    isEditing = isMetaEditing,
                    onToggleEditing = { isMetaEditing = !isMetaEditing },
                    onTitleChange = onTitleChange,
                    onDescriptionChange = onDescriptionChange,
                )
                if (conversationRound > 1) {
                    Text(
                        text = stringResource(
                            R.string.create_ai_chat_prompt_round,
                            conversationRound
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .glassBackground(
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(
                    R.string.create_ai_common_config_summary_compact,
                    selectedCategoryCount,
                    selectedToolCount
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
            )
            if (selectedCategoryNames.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedCategoryNames.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            SelectedToolSummary(
                tools = selectedToolsPreview,
                emptyText = stringResource(R.string.create_ai_common_tools_empty)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { onPreviewClick() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (!chatPrompt.prompt.isNullOrEmpty()) {
                        Text(
                            text = stringResource(R.string.create_ai_chat_prompt_system_prompt_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chatPrompt.prompt.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (!chatPrompt.placeholder.isNullOrEmpty()) {
                        Text(
                            text = stringResource(R.string.create_ai_chat_prompt_placeholder_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = chatPrompt.placeholder.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.surface,
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineVisibility,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary,
                        )
                        Text(
                            text = stringResource(R.string.create_ai_chat_prompt_tap_preview),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = onConfigureClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.create_ai_chat_prompt_config))
                }
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.create_ai_chat_prompt_edit))
                }
                ElevatedButton(
                    onClick = onSave,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSave,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.create_ai_chat_prompt_save))
                }
            }
        }
    }
}

@Composable
private fun PromptErrorSection(
    message: String,
    rawJson: String,
    contentCardHeight: androidx.compose.ui.unit.Dp,
    onEditRaw: () -> Unit,
    onRetry: () -> Unit,
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(contentCardHeight)
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineError,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.create_ai_chat_prompt_error_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            if (rawJson.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(R.string.create_ai_chat_prompt_stream_raw_toggle),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .verticalScroll(rememberScrollState())
                        .glassBackground(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = rawJson,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (rawJson.isNotBlank()) {
                    GlassTonalButton(onClick = onEditRaw) {
                        Icon(
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.create_ai_chat_prompt_edit_raw))
                    }
                }
                GlassTonalButton(
                    onClick = onRetry,
                    colors = AppTheme.colors.filledTonalButtonColors(),
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.create_ai_chat_prompt_retry))
                }
            }
        }
    }
}

