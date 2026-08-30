package com.shifenmiao.ai.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.a2ui.LocalA2uiRenderer
import com.shifenmiao.ai.a2ui.A2uiRendererImpl
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.component.AgentToolCallUIState
import com.shifenmiao.ai.component.ChatErrorMessage
import com.shifenmiao.ai.component.RobotDirectErrorContent
import com.shifenmiao.ai.component.RobotMessageFooter
import com.shifenmiao.ai.component.ToolCallHistoryCard
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.ai.ui.AIChatBottom
import com.shifenmiao.ai.ui.AIContentNotice
import com.shifenmiao.ai.ui.AIPromptsPickerBottomSheet
import com.shifenmiao.ai.ui.NewChatInput
import com.shifenmiao.ai.ui.PlaceHolderMessageCard
import com.shifenmiao.ai.ui.RecentQuestionsBottomSheet
import com.shifenmiao.ai.ui.ToolCenterBottomSheet
import com.shifenmiao.ai.ui.rememberUniqueMessageLazyKeys
import com.shifenmiao.base.ui.DisableContainer
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.common.ui.rememberCodeBlockClickListener
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.image.ImageViewerInfo
import com.shifenmiao.model.state.ChatUIState
import com.shifenmiao.model.state.LocalChatUIState
import com.shifenmiao.model.state.PageState
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.utils.getString
import kotlinx.coroutines.launch
import java.util.Date
import com.t8rin.imagetoolbox.core.resources.icons.line.LineInfo
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettingsSuggest
import com.t8rin.imagetoolbox.core.resources.icons.line.LineRadioChecked

@Composable
fun AIChatScreen(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent
) {
    val chatUIState by aiChatComponent.chatUIState.collectAsState()
    val conversation by aiChatComponent.conversation.collectAsState()
    val messageUiModels by aiChatComponent.messageUiModels.collectAsState()
    val showCancelConfirm by aiChatComponent.showCancelConfirmDialog.collectAsState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val onNavigate = LocalOnNavigate.current
    var isTextSelectionMode by remember { mutableStateOf(false) }
    val isAtBottom by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0
        }
    }
    val showButton by remember {
        derivedStateOf {
            !isAtBottom
        }
    }
    var hasUnseenNewMessage by remember { mutableStateOf(false) }

    LaunchedEffect(messageUiModels.size, chatUIState.pageState) {
        if (messageUiModels.isNotEmpty() && chatUIState.pageState != PageState.INITIALIZING) {
            if (isAtBottom) {
                lazyListState.scrollToItem(0)
                hasUnseenNewMessage = false
            } else {
                hasUnseenNewMessage = true
            }
        }
    }
    LaunchedEffect(isAtBottom) {
        if (isAtBottom) hasUnseenNewMessage = false
    }
    LaunchedEffect(conversation.id) {
        isTextSelectionMode = false
    }
    LaunchedEffect(chatUIState.chatActive) {
        if (chatUIState.chatActive) {
            isTextSelectionMode = false
        }
    }

    CompositionLocalProvider(
        LocalChatUIState provides chatUIState,
    ) {
        BaseScreen(
            title = if (isTextSelectionMode) {
                stringResource(R.string.select_text)
            } else {
                conversation.miniAppTitle()
            },
            actions = {
                DisableContainer(
                    enabled = chatUIState.pageState != PageState.INITIALIZING
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingTooSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!chatUIState.showHistory) {
                            IconButton(onClick = {
                                onNavigate(
                                    Screen.AIHistoryCenter(
                                        initialFilter = when (conversation.entryType) {
                                            AIConversationEntryType.PROMPT -> AIConversationEntryType.PROMPT
                                            AIConversationEntryType.AGENT -> AIConversationEntryType.AGENT
                                            AIConversationEntryType.DUEL -> AIConversationEntryType.DUEL
                                            AIConversationEntryType.ASSISTANT -> AIConversationEntryType.ASSISTANT
                                            else -> null
                                        }
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                    contentDescription = stringResource(R.string.history),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = {
                            if (isTextSelectionMode) {
                                isTextSelectionMode = false
                            } else {
                                aiChatComponent.changeConversationId(Date().time.toString())
                            }
                        }) {
                            Icon(
                                imageVector = if (isTextSelectionMode) com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddAiChat,
                                contentDescription = if (isTextSelectionMode) {
                                    stringResource(R.string.close)
                                } else {
                                    stringResource(R.string.nav_add)
                                },
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            onGoBack = {
                when {
                    isTextSelectionMode -> {
                        isTextSelectionMode = false
                    }

                    chatUIState.showHistory -> {
                        aiChatComponent.hideHistory()
                    }

                    chatUIState.chatActive -> {
                        // AI 正在回复时返回，弹取消确认弹窗
                        aiChatComponent.requestCancelFetch()
                    }

                    else -> {
                        appComponent.onGoBack()
                    }
                }
            },
            foreground = {
                if (!chatUIState.showHistory) {
                    AnimatedVisibility(
                        visible = showButton && (chatUIState.chatActive || hasUnseenNewMessage),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 132.dp),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        NewMessageHintChip(
                            isStreaming = chatUIState.chatActive,
                            onClick = {
                                coroutineScope.launch {
                                    lazyListState.animateScrollToItem(0)
                                    hasUnseenNewMessage = false
                                }
                            }
                        )
                    }
                    AIChatBottom(
                        showTop = showButton
                    ) {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                            hasUnseenNewMessage = false
                        }
                    }
                }
            }
        ) {
            ChatContent(
                chatUIState = chatUIState,
                messageUiModels = messageUiModels,
                lazyListState = lazyListState,
                appComponent = appComponent,
                aiChatComponent = aiChatComponent,
                onNavigate = onNavigate,
                isTextSelectionMode = isTextSelectionMode,
                onEnterTextSelectionMode = {
                    enterChatTextSelectionMode(
                        isTextSelectionMode = isTextSelectionMode,
                        onEnter = {
                            isTextSelectionMode = true
                        }
                    )
                },
                onExitTextSelectionMode = {
                    isTextSelectionMode = false
                }
            )
        }

        // 取消确认弹窗：AI 正在回复时用户点击取消或返回
        CancelFetchConfirmDialog(
            visible = showCancelConfirm,
            onStop = { aiChatComponent.confirmCancelFetch() },
            onDismiss = { aiChatComponent.dismissCancelConfirm() }
        )
    }
}

@Composable
fun ChatContent(
    chatUIState: ChatUIState,
    messageUiModels: List<MessageUiModel>,
    lazyListState: LazyListState,
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    onNavigate: (Screen) -> Unit = {},
    isTextSelectionMode: Boolean = false,
    onEnterTextSelectionMode: () -> Unit = {},
    onExitTextSelectionMode: () -> Unit = {}
) {
    val conversation = aiChatComponent.conversation.collectAsState()
    val chatInputComponent = aiChatComponent.chatInputComponent
    val inputState by chatInputComponent.chatInputState.collectAsState()

    // 提示词选择相关状态
    val selectedPromptCategoryId = remember { mutableStateOf(0) }
    val promptCategories by chatInputComponent.promptCategories.collectAsState()
    val prompts by chatInputComponent.promptListFlow(selectedPromptCategoryId.value)
        .collectAsState(initial = emptyList())

    // 最近问题相关状态
    val recentQuestions by chatInputComponent.recentQuestions.collectAsState()

    // 模型选择相关状态
    val currentAIModel by appComponent.aiEngineManager.currentAIModel.collectAsState()
    val currentAIEngine by appComponent.aiEngineManager.currentAIEngine.collectAsState()
    val toolCenterUiState by aiChatComponent.toolCenterUiState.collectAsState()
    var showToolCenter by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        aiChatComponent.refreshToolCenter()
    }

    LaunchedEffect(showToolCenter) {
        if (showToolCenter) {
            aiChatComponent.refreshToolCenter()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (!currentAIEngine.hasAvailableChatRoute()) {
                UnavailableEngineBanner(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    onClick = { appComponent.showAIChatSettings() }
                )
            }
            ChatMessagesList(
                modifier = Modifier.weight(1f),
                chatUIState = chatUIState,
                chatInputComponent = chatInputComponent,
                messageUiModels = messageUiModels,
                lazyListState = lazyListState,
                appComponent = appComponent,
                aiChatComponent = aiChatComponent,
                conversation = conversation.value,
                isTextSelectionMode = isTextSelectionMode,
                onEnterTextSelectionMode = onEnterTextSelectionMode,
                onExitTextSelectionMode = onExitTextSelectionMode,
                onSuggestionClick = { suggestion ->
                    chatInputComponent.onInputTextChange(suggestion)
                },
                onShowToolCenter = { showToolCenter = true }
            )
            NewChatInput(
                chatUIState = chatUIState,
                conversation = conversation,
                aiChatComponent = aiChatComponent,
                appComponent = appComponent,
                chatInputComponent = chatInputComponent,
                enabledToolCount = toolCenterUiState.enabledToolNames.size,
                onShowToolCenter = {
                    showToolCenter = true
                },
                onSendMessage = onExitTextSelectionMode,
                onImagePreview = { uri ->
                    onNavigate(
                        Screen.ImageViewer(
                            imageViewerInfo = ImageViewerInfo(
                                images = listOf(uri.toString()),
                                initialIndex = 0
                            )
                        )
                    )
                }
            )
        }

        // 提示词选择底部弹窗
        AIPromptsPickerBottomSheet(
            visible = inputState.showPromptPicker,
            categories = promptCategories,
            selectedCategoryId = selectedPromptCategoryId.value,
            prompts = prompts,
            onCategorySelected = { selectedPromptCategoryId.value = it },
            onPromptSelected = { prompt ->
                // 将选中的提示词设置为系统提示词（注入发送而非填入输入框）
                chatInputComponent.selectSystemPrompt(
                    title = prompt.title,
                    promptText = prompt.prompt
                )
                chatInputComponent.hidePromptPicker()
            },
            onCreatePrompt = {
                chatInputComponent.hidePromptPicker()
                onNavigate(Screen.CreateAIChatPrompt())
            },
            onDismiss = {
                chatInputComponent.hidePromptPicker()
            }
        )

        // 最近问题选择底部弹窗
        RecentQuestionsBottomSheet(
            visible = inputState.showRecentPicker,
            questions = recentQuestions,
            onQuestionSelected = { question ->
                chatInputComponent.onInputTextChange(question)
                chatInputComponent.hideRecentPicker()
            },
            onDismiss = {
                chatInputComponent.hideRecentPicker()
            }
        )

        // 模型选择底部弹窗
        AIModelsPickerBottomSheet(
            visible = inputState.showModelPicker,
            allEngines = aiChatComponent.allEngines.collectAsState().value,
            modelsByProvider = aiChatComponent.modelsByProvider.collectAsState().value,
            selectedEngineName = currentAIEngine.identityKey(),
            selectedModelName = currentAIModel.name,
            onSelected = { engine, model ->
                appComponent.aiEngineManager.switchModel(engine, model)
                chatInputComponent.hideModelPicker()
            },
            onDismiss = {
                chatInputComponent.hideModelPicker()
            }
        )

        ToolCenterBottomSheet(
            visible = showToolCenter,
            uiState = toolCenterUiState,
            onDismiss = { showToolCenter = false },
            onToggleTool = { toolName, enabled ->
                aiChatComponent.setToolEnabled(toolName, enabled)
            },
            onToggleTools = { toolNames, enabled ->
                aiChatComponent.setToolsEnabled(toolNames, enabled)
            }
        )
    }
}

@Composable
fun ChatMessagesList(
    modifier: Modifier = Modifier,
    messageUiModels: List<MessageUiModel>,
    lazyListState: LazyListState,
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    chatUIState: ChatUIState,
    conversation: Conversation,
    isTextSelectionMode: Boolean = false,
    onEnterTextSelectionMode: () -> Unit = {},
    onExitTextSelectionMode: () -> Unit = {},
    onSuggestionClick: (String) -> Unit,
    onShowToolCenter: () -> Unit = {},
    chatInputComponent: ChatInputComponent
) {
    val codeBlockClickListener = rememberCodeBlockClickListener(
        appComponent = appComponent,
        onA2uiSubmit = { formData ->
            onExitTextSelectionMode()
            aiChatComponent.startChatWithStreaming(formData)
        }
    )
    val agentToolCallStatus by aiChatComponent.agentToolCallStatus.collectAsState()
    val displayMessageUiModels = remember(messageUiModels, agentToolCallStatus) {
        messageUiModels.withLiveToolCallHistory(agentToolCallStatus)
    }
    val messageLazyKeys = rememberUniqueMessageLazyKeys(displayMessageUiModels)
    val a2uiRenderer = remember { A2uiRendererImpl(aiChatComponent.a2uiRenderProvider) }

    CompositionLocalProvider(
        LocalA2uiRenderer provides a2uiRenderer
    ) {
        ProvideMermaidRenderer {
            val listModifier = modifier
                .fillMaxSize()
                .let { baseModifier ->
                    if (isTextSelectionMode) {
                        baseModifier
                    } else {
                        baseModifier.pointerInput(onEnterTextSelectionMode) {
                            detectTapGestures(onLongPress = { onEnterTextSelectionMode() })
                        }
                    }
                }

            val chatListContent: @Composable () -> Unit = {
                LazyColumn(
                    modifier = listModifier,
                    state = lazyListState,
                    reverseLayout = true,
                    contentPadding = PaddingValues(
                        horizontal = AppTheme.dimens.paddingNormal,
                        vertical = AppTheme.dimens.paddingSmall
                    ),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val fixedKeyPrefix = "fixed_${conversation.id}_"
                    if (displayMessageUiModels.isNotEmpty() && chatUIState.pageState != PageState.INITIALIZING) {
                        item(key = "${fixedKeyPrefix}ai_content_notice") {
                            AIContentNotice(
                                true,
                            )
                        }
                    }
                    itemsIndexed(
                        items = displayMessageUiModels,
                        key = { index, _ -> messageLazyKeys[index] },
                        contentType = { _, item -> item::class }
                    ) { index, item ->
                        RenderChatMessageItem(
                            item = item,
                            index = index,
                            codeBlockClickListener = codeBlockClickListener,
                            onRobotToolCallHistory = { historyItem ->
                                if (historyItem.isLive && agentToolCallStatus !is AgentToolCallUIState.Idle) {
                                    ToolCallHistoryCard(
                                        agentToolCallStatus = agentToolCallStatus,
                                        onCancelTool = aiChatComponent::cancelCurrentTool,
                                        onContinueIteration = aiChatComponent::continueAgentLoop
                                    )
                                } else {
                                    ToolCallHistoryCard(toolCallsJson = historyItem.toolCallsJson)
                                }
                            },
                            onRobotContainerFooter = { footerItem, itemIndex ->
                                RobotMessageFooter(
                                    robotContainerFooter = footerItem,
                                    index = itemIndex,
                                    aiChatComponent = aiChatComponent,
                                    appComponent = appComponent
                                )
                            },
                            onRobotError = { errorItem ->
                                if (errorItem.isDirectConnection) {
                                    RobotDirectErrorContent(
                                        errorMessage = errorItem.errorMessage
                                    )
                                } else {
                                    ChatErrorMessage(
                                        appComponent = appComponent,
                                        aiChatComponent = aiChatComponent,
                                        errorMessage = errorItem.errorMessage
                                    )
                                }
                            }
                        )
                    }
                    if (displayMessageUiModels.isNotEmpty() && chatUIState.showMessageLimitNotice) {
                        item(key = "${fixedKeyPrefix}message_limit_notice") {
                            MessageLimitNoticeCard()
                        }
                    }
                    if (displayMessageUiModels.isEmpty()) {
                        item(key = "${fixedKeyPrefix}placeholder_message") {
                            PlaceHolderMessageCard(
                                conversation = conversation,
                                onSuggestionClick = onSuggestionClick,
                                onShowToolCenter = onShowToolCenter,
                                onPushToRemote = {
                                    aiChatComponent.pushPromptToRemote(
                                        onSuccess = {
                                            AppToastHost.showToast(
                                                getString(R.string.prompt_push_success)
                                            )
                                        },
                                        onFailure = { message ->
                                            AppToastHost.showToast(message)
                                        }
                                    )
                                },
                                appComponent = appComponent,
                                aiChatComponent = aiChatComponent,
                                chatInputComponent = chatInputComponent
                            )
                        }
                    }
                }
            }

            if (isTextSelectionMode) {
                SelectionContainer {
                    chatListContent()
                }
            } else {
                chatListContent()
            }
        }
    } // CompositionLocalProvider
}

@Composable
private fun UnavailableEngineBanner(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.72f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettingsSuggest,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.ai_chat_engine_unavailable_banner_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = stringResource(
                        if (FlavorType.fromName().isOverseas) {
                            R.string.ai_chat_engine_unavailable_banner_desc_overseas
                        } else {
                            R.string.ai_chat_engine_unavailable_banner_desc
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.92f),
                )
            }
            Text(
                text = stringResource(R.string.ai_chat_engine_unavailable_banner_action),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
private fun NewMessageHintChip(
    isStreaming: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineRadioChecked,
                contentDescription = null
            )
            Text(
                text = if (isStreaming) {
                    stringResource(R.string.ai_chat_jump_to_latest_streaming)
                } else {
                    stringResource(R.string.ai_chat_jump_to_latest_message)
                },
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun List<MessageUiModel>.withLiveToolCallHistory(
    agentToolCallStatus: AgentToolCallUIState
): List<MessageUiModel> {
    if (agentToolCallStatus is AgentToolCallUIState.Idle) return this

    val activeFooter = firstOrNull { it is MessageUiModel.RobotContainerFooter }
        as? MessageUiModel.RobotContainerFooter
        ?: return this
    val messageIdPrefix = activeFooter.id.removeSuffix("_footer")
    val liveHistoryId = "${messageIdPrefix}_tool_calls"

    var hasHistoryItem = false
    val modelsWithLiveFlag = map { item ->
        if (item is MessageUiModel.RobotToolCallHistory && item.id == liveHistoryId) {
            hasHistoryItem = true
            item.copy(isLive = true)
        } else {
            item
        }
    }
    if (hasHistoryItem) return modelsWithLiveFlag

    val liveHistoryItem = MessageUiModel.RobotToolCallHistory(
        id = liveHistoryId,
        toolCallsJson = "",
        isLive = true
    )
    val insertIndex = modelsWithLiveFlag.indexOfFirst {
        it is MessageUiModel.RobotContainerHeader && it.id == "${messageIdPrefix}_header"
    }.takeIf { it >= 0 } ?: (modelsWithLiveFlag.indexOf(activeFooter) + 1).coerceAtMost(modelsWithLiveFlag.size)

    return modelsWithLiveFlag.toMutableList().apply {
        add(insertIndex, liveHistoryItem)
    }
}



@Composable
private fun MessageLimitNoticeCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo,
                contentDescription = null,
                modifier = Modifier
                    .width(16.dp)
                    .height(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.ai_chat_message_limit_notice),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal fun enterChatTextSelectionMode(
    isTextSelectionMode: Boolean,
    onEnter: () -> Unit
) {
    if (isTextSelectionMode) return

    onEnter()
    AppToastHost.showToast(
        message = R.string.ai_chat_text_selection_mode_hint,
        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineInfo
    )
}

/**
 * 取消确认弹窗：AI 正在回复时用户点击取消或返回时弹出。
 *
 * @param messageResId 弹窗消息资源 ID（区分是否支持后台继续）
 * @param dismissLabelResId dismiss 按钮文案资源 ID
 */
@Composable
internal fun CancelFetchConfirmDialog(
    visible: Boolean,
    onStop: () -> Unit,
    onDismiss: () -> Unit,
    @androidx.annotation.StringRes messageResId: Int = R.string.ai_chat_cancel_confirm_message,
    @androidx.annotation.StringRes dismissLabelResId: Int = R.string.ai_chat_cancel_confirm_cancel,
) {
    if (!visible) return
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.ai_chat_cancel_confirm_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onStop) {
                Text(
                    text = stringResource(R.string.ai_chat_cancel_confirm_stop),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(dismissLabelResId))
            }
        }
    )
}
