package com.shifenmiao.ai.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.AIDuelChatComponent
import com.shifenmiao.ai.component.ChatLoadingIndicator
import com.shifenmiao.ai.component.RobotReasoningBlock
import com.shifenmiao.ai.component.RobotReasoningContent
import com.shifenmiao.ai.component.RobotReasoningHeader
import com.shifenmiao.ai.model.AIDuelConfig
import com.shifenmiao.ai.model.DuelMode
import com.shifenmiao.ai.model.DuelSpeaker
import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.ai.ui.AIChatBottom
import com.shifenmiao.ai.ui.AIContentNotice
import com.shifenmiao.ai.ui.AIDuelPersonaHistoryPickerBottomSheet
import com.shifenmiao.ai.ui.AIPromptsPickerBottomSheet
import com.shifenmiao.base.ui.CustomChatCard
import com.shifenmiao.base.ui.DisableContainer
import com.shifenmiao.base.ui.icon.LetterIcon
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.common.ui.ai.AIModelsPickerBottomSheet
import com.shifenmiao.common.ui.rememberCodeBlockClickListener
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.state.PageState
import com.shifenmiao.model.state.LocalChatUIState
import com.shifenmiao.theme.AppTheme
import com.shifenmiao.webview.mermaid.ProvideMermaidRenderer
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCardSegment
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassFilterChip
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassCardSegment
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineText
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus

@Composable
fun AIDuelChatScreen(
    appComponent: AppComponent,
    duelChatComponent: AIDuelChatComponent
) {
    val chatUIState by duelChatComponent.chatUIState.collectAsState()
    val conversation by duelChatComponent.conversation.collectAsState()
    val messageUiModels by duelChatComponent.messageUiModels.collectAsState()
    val duelState by duelChatComponent.duelState.collectAsState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val onNavigate = LocalOnNavigate.current
    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0
        }
    }

    CompositionLocalProvider(
        LocalChatUIState provides chatUIState,
    ) {
        BaseScreen(
            title = conversation.appTitle.ifBlank {
                stringResource(id = R.string.ai_duel_chat_title)
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
                                onNavigate(Screen.AIHistoryCenter(initialFilter = AIConversationEntryType.DUEL))
                            }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                duelChatComponent.startNewConversation()
                            },
                            enabled = !duelState.running
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddAiChat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            onGoBack = {
                if (chatUIState.showHistory) {
                    duelChatComponent.hideHistory()
                } else {
                    appComponent.onGoBack()
                }
            },
            foreground = {
                if (!chatUIState.showHistory) {
                    AIChatBottom(
                        showTop = showScrollToTop
                    ) {
                        coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                    }
                }
            },
            showNavigationBarsPadding = false
        ) {
            DuelChatContent(
                duelChatComponent = duelChatComponent,
                messageUiModels = messageUiModels,
                lazyListState = lazyListState,
                appComponent = appComponent,
                conversation = conversation,
            )
        }
    }
}

@Composable
private fun DuelChatContent(
    duelChatComponent: AIDuelChatComponent,
    messageUiModels: List<MessageUiModel>,
    lazyListState: LazyListState,
    appComponent: AppComponent,
    conversation: Conversation,
) {
    val duelConfig by duelChatComponent.duelConfig.collectAsState()
    val duelState by duelChatComponent.duelState.collectAsState()

    val roleNameA = remember { mutableStateOf(duelConfig.roleNameA) }
    val roleNameB = remember { mutableStateOf(duelConfig.roleNameB) }
    val personaA = remember { mutableStateOf(duelConfig.personaA) }
    val personaB = remember { mutableStateOf(duelConfig.personaB) }
    val mode = remember { mutableStateOf(duelConfig.mode) }
    val topic = remember { mutableStateOf(duelConfig.topic) }
    val maxRounds = remember { mutableIntStateOf(duelConfig.maxRounds) }
    val pickingModelRole = remember { mutableStateOf<DuelSpeaker?>(null) }
    val showModelPicker = remember { mutableStateOf(false) }
    val pickingPromptRole = remember { mutableStateOf<DuelSpeaker?>(null) }
    val showPromptPicker = remember { mutableStateOf(false) }
    val selectedPromptGroupId = remember { mutableIntStateOf(0) }
    val showPersonaHistoryPicker = remember { mutableStateOf(false) }
    val pickingPersonaHistoryRole = remember { mutableStateOf<DuelSpeaker?>(null) }
    val showRoleNameHistoryPicker = remember { mutableStateOf(false) }
    val pickingRoleNameHistoryRole = remember { mutableStateOf<DuelSpeaker?>(null) }
    val showTopicHistoryPicker = remember { mutableStateOf(false) }
    val isExporting = remember { mutableStateOf(false) }

    LaunchedEffect(duelConfig) {
        roleNameA.value = duelConfig.roleNameA
        roleNameB.value = duelConfig.roleNameB
        personaA.value = duelConfig.personaA
        personaB.value = duelConfig.personaB
        mode.value = duelConfig.mode
        topic.value = duelConfig.topic
        maxRounds.intValue = duelConfig.maxRounds
    }

    LaunchedEffect(duelState.running) {
        if (duelState.running) return@LaunchedEffect
        snapshotFlow {
            AIDuelConfig(
                roleNameA = roleNameA.value,
                roleNameB = roleNameB.value,
                personaA = personaA.value,
                personaB = personaB.value,
                mode = mode.value,
                topic = topic.value,
                maxRounds = maxRounds.intValue,
                engineA = duelConfig.engineA,
                engineB = duelConfig.engineB,
                promptIdA = duelConfig.promptIdA,
                promptIdB = duelConfig.promptIdB,
                promptNameA = duelConfig.promptNameA,
                promptNameB = duelConfig.promptNameB,
            )
        }
            .debounce(300L)
            .drop(1)
            .distinctUntilChanged()
            .collectLatest { duelChatComponent.updateDraftConfig(it) }
    }

    // Determine whether to show inline config (empty messages & not running & not showing a replayed conversation)
    val showInlineConfig =
        messageUiModels.isEmpty() && !duelState.running && !conversation.showLastMessage

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showInlineConfig) {
            // Inline config panel (scrollable)
            DuelInlineConfigPanel(
                modifier = Modifier.weight(1f),
                duelConfig = duelConfig,
                duelState = duelState,
                roleNameA = roleNameA,
                roleNameB = roleNameB,
                personaA = personaA,
                personaB = personaB,
                mode = mode,
                topic = topic,
                maxRounds = maxRounds,
                pickingModelRole = pickingModelRole,
                showModelPicker = showModelPicker,
                pickingPromptRole = pickingPromptRole,
                showPromptPicker = showPromptPicker,
                showPersonaHistoryPicker = showPersonaHistoryPicker,
                pickingPersonaHistoryRole = pickingPersonaHistoryRole,
                showRoleNameHistoryPicker = showRoleNameHistoryPicker,
                pickingRoleNameHistoryRole = pickingRoleNameHistoryRole,
                showTopicHistoryPicker = showTopicHistoryPicker,
            )
        } else {
            DuelMessagesList(
                modifier = Modifier.weight(1f),
                messageUiModels = messageUiModels,
                lazyListState = lazyListState,
                appComponent = appComponent,
                conversation = conversation,
            )
        }
        val showShare = messageUiModels.isNotEmpty() && !duelState.running
        DuelStatusBar(
            duelConfig = duelConfig,
            duelState = duelState,
            showInlineConfig = showInlineConfig,
            showShare = showShare,
            onStartDuel = {
                duelChatComponent.startDuel(
                    config = AIDuelConfig(
                        roleNameA = roleNameA.value,
                        roleNameB = roleNameB.value,
                        personaA = personaA.value,
                        personaB = personaB.value,
                        mode = mode.value,
                        topic = topic.value,
                        maxRounds = maxRounds.intValue,
                        engineA = duelConfig.engineA,
                        engineB = duelConfig.engineB,
                        promptIdA = duelConfig.promptIdA,
                        promptIdB = duelConfig.promptIdB,
                        promptNameA = duelConfig.promptNameA,
                        promptNameB = duelConfig.promptNameB,
                    )
                )
            },
            onStop = duelChatComponent::stopDuel,
            onShare = {
                isExporting.value = true
                duelChatComponent.exportDuelChatHistory { html, aIgcInfo ->
                    isExporting.value = false
                    appComponent.showWebView(
                        WebViewParams(
                            baseUrl = UrlConstants.WEB_VIEW_BASE_URL,
                            isHtml = true,
                            htmlData = html,
                            enableSlowWholeDocumentDraw = true,
                            enableCustomTouch = false,
                            aIgcInfo = aIgcInfo
                        )
                    )
                }
            }
        )
    }

    if (isExporting.value) {
        AlertDialog(
            onDismissRequest = { isExporting.value = false },
            confirmButton = { },
            title = {
                Text(
                    text = stringResource(R.string.exporting_share),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )
    }

    // Picker bottom sheets (always available)
    if (!conversation.showLastMessage) {
        DuelPickerBottomSheets(
            appComponent = appComponent,
            duelChatComponent = duelChatComponent,
            duelConfig = duelConfig,
            roleNameA = roleNameA,
            roleNameB = roleNameB,
            personaA = personaA,
            personaB = personaB,
            topic = topic,
            pickingModelRole = pickingModelRole,
            showModelPicker = showModelPicker,
            pickingPromptRole = pickingPromptRole,
            showPromptPicker = showPromptPicker,
            selectedPromptGroupId = selectedPromptGroupId,
            showPersonaHistoryPicker = showPersonaHistoryPicker,
            pickingPersonaHistoryRole = pickingPersonaHistoryRole,
            showRoleNameHistoryPicker = showRoleNameHistoryPicker,
            pickingRoleNameHistoryRole = pickingRoleNameHistoryRole,
            showTopicHistoryPicker = showTopicHistoryPicker,
        )
    }
}

// ──────────────────────────────────────────────────────────────
//  Inline config panel — shown when no messages
// ──────────────────────────────────────────────────────────────

@Composable
private fun DuelInlineConfigPanel(
    modifier: Modifier = Modifier,
    duelConfig: AIDuelConfig,
    duelState: com.shifenmiao.ai.model.AIDuelState,
    roleNameA: MutableState<String>,
    roleNameB: MutableState<String>,
    personaA: MutableState<String>,
    personaB: MutableState<String>,
    mode: MutableState<DuelMode>,
    topic: MutableState<String>,
    maxRounds: MutableState<Int>,
    pickingModelRole: MutableState<DuelSpeaker?>,
    showModelPicker: MutableState<Boolean>,
    pickingPromptRole: MutableState<DuelSpeaker?>,
    showPromptPicker: MutableState<Boolean>,
    showPersonaHistoryPicker: MutableState<Boolean>,
    pickingPersonaHistoryRole: MutableState<DuelSpeaker?>,
    showRoleNameHistoryPicker: MutableState<Boolean>,
    pickingRoleNameHistoryRole: MutableState<DuelSpeaker?>,
    showTopicHistoryPicker: MutableState<Boolean>,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = AppTheme.dimens.paddingNormal)
            .padding(top = AppTheme.dimens.paddingNormal),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingNormal)
    ) {
        // ── Page header ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppTheme.dimens.paddingSmall,
                    vertical = AppTheme.dimens.paddingSmall
                ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_duel_config_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = duelConfig.mode.subtitle(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Role A section ──
        DuelModeSection(
            selectedMode = mode.value,
            enabled = !duelState.running,
            onModeSelected = { mode.value = it }
        )

        // ── Role A section ──
        DuelRoleConfigSection(
            speaker = DuelSpeaker.A,
            roleName = roleNameA,
            persona = personaA,
            duelConfig = duelConfig,
            duelState = duelState,
            pickingModelRole = pickingModelRole,
            showModelPicker = showModelPicker,
            pickingPromptRole = pickingPromptRole,
            showPromptPicker = showPromptPicker,
            showPersonaHistoryPicker = showPersonaHistoryPicker,
            pickingPersonaHistoryRole = pickingPersonaHistoryRole,
            showRoleNameHistoryPicker = showRoleNameHistoryPicker,
            pickingRoleNameHistoryRole = pickingRoleNameHistoryRole,
        )

        // ── Role B section ──
        DuelRoleConfigSection(
            speaker = DuelSpeaker.B,
            roleName = roleNameB,
            persona = personaB,
            duelConfig = duelConfig,
            duelState = duelState,
            pickingModelRole = pickingModelRole,
            showModelPicker = showModelPicker,
            pickingPromptRole = pickingPromptRole,
            showPromptPicker = showPromptPicker,
            showPersonaHistoryPicker = showPersonaHistoryPicker,
            pickingPersonaHistoryRole = pickingPersonaHistoryRole,
            showRoleNameHistoryPicker = showRoleNameHistoryPicker,
            pickingRoleNameHistoryRole = pickingRoleNameHistoryRole,
        )

        // ── Topic ──
        GlassOutlinedTextField(
            value = topic.value,
            onValueChange = { topic.value = it },
            enabled = !duelState.running,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(duelConfig.mode.topicLabel()) },
            placeholder = { Text(duelConfig.mode.topicPlaceholder()) },
            trailingIcon = {
                IconButton(
                    enabled = !duelState.running,
                    onClick = {
                        showTopicHistoryPicker.value = true
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                        contentDescription = stringResource(R.string.ai_duel_choose_history_topic),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            minLines = 3,
            maxLines = 5,
            shape = MaterialTheme.shapes.medium,
            colors = AppTheme.colors.getOutlinedTextFieldColors()
        )

        // ── Rounds stepper ──
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            style = GlassStyle.Thin,
            shape = MaterialTheme.shapes.medium,
            borderWidth = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppTheme.dimens.paddingNormal,
                        vertical = AppTheme.dimens.paddingSmall
                    ),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ai_duel_rounds),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    enabled = !duelState.running && maxRounds.value > 1,
                    onClick = { maxRounds.value = (maxRounds.value - 1).coerceAtLeast(1) }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMinus,
                        contentDescription = stringResource(R.string.ai_duel_decrease_rounds),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = maxRounds.value.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    enabled = !duelState.running && maxRounds.value < 50,
                    onClick = { maxRounds.value = (maxRounds.value + 1).coerceAtMost(50) }
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.ai_duel_increase_rounds),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
    }
}

@Composable
private fun DuelModeSection(
    selectedMode: DuelMode,
    enabled: Boolean,
    onModeSelected: (DuelMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall)
    ) {
        Text(
            text = stringResource(R.string.ai_duel_mode_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall)
        ) {
            DuelMode.entries.forEach { duelMode ->
                GlassFilterChip(
                    selected = selectedMode == duelMode,
                    onClick = {
                        if (enabled) onModeSelected(duelMode)
                    },
                    enabled = enabled,
                    label = { Text(text = duelMode.label()) }
                )
            }
        }
    }
}

@Composable
private fun DuelRoleConfigSection(
    speaker: DuelSpeaker,
    roleName: MutableState<String>,
    persona: MutableState<String>,
    duelConfig: AIDuelConfig,
    duelState: com.shifenmiao.ai.model.AIDuelState,
    pickingModelRole: MutableState<DuelSpeaker?>,
    showModelPicker: MutableState<Boolean>,
    pickingPromptRole: MutableState<DuelSpeaker?>,
    showPromptPicker: MutableState<Boolean>,
    showPersonaHistoryPicker: MutableState<Boolean>,
    pickingPersonaHistoryRole: MutableState<DuelSpeaker?>,
    showRoleNameHistoryPicker: MutableState<Boolean>,
    pickingRoleNameHistoryRole: MutableState<DuelSpeaker?>,
) {
    val roleNameLabel = if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_name_a_label)
    } else {
        stringResource(R.string.ai_duel_role_name_b_label)
    }
    val personaHint = duelConfig.mode.personaHint(speaker)
    val promptName =
        if (speaker == DuelSpeaker.A) duelConfig.promptNameA else duelConfig.promptNameB
    val engine = if (speaker == DuelSpeaker.A) duelConfig.engineA else duelConfig.engineB
    val modelDisplayName = engine?.model?.title?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.ai_duel_model_default)
    val roleHeaderText = duelConfig.mode.roleCardTitle(speaker)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        borderWidth = 0.dp,
        containerAlpha = 0.5f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall)
        ) {
            // ── Card header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = roleHeaderText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (speaker == DuelSpeaker.A)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                ) {
                    LetterIcon(
                        name = roleHeaderText,
                        modifier = Modifier.fillMaxSize(),
                        tint = if (speaker == DuelSpeaker.A)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(0.12f)
                        else
                            MaterialTheme.colorScheme.onTertiaryContainer.copy(0.12f),
                        showOutline = false
                    )
                }
            }

            // ── Role name ──
            GlassOutlinedTextField(
                value = roleName.value,
                onValueChange = { roleName.value = it },
                enabled = !duelState.running,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(roleNameLabel) },
                placeholder = { Text(stringResource(R.string.ai_duel_role_name_optional_hint)) },
                trailingIcon = {
                    IconButton(
                        enabled = !duelState.running,
                        onClick = {
                            pickingRoleNameHistoryRole.value = speaker
                            showRoleNameHistoryPicker.value = true
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                            contentDescription = stringResource(R.string.ai_duel_choose_history_role_name),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )

            // ── Persona / prompt ──
            GlassOutlinedTextField(
                value = persona.value,
                onValueChange = { persona.value = it },
                enabled = !duelState.running,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(buildPersonaLabel(speaker, duelConfig)) },
                placeholder = { Text(personaHint) },
                trailingIcon = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            enabled = !duelState.running,
                            onClick = {
                                pickingPersonaHistoryRole.value = speaker
                                showPersonaHistoryPicker.value = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                contentDescription = stringResource(R.string.ai_duel_choose_history_prompt),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            enabled = !duelState.running,
                            onClick = {
                                pickingPromptRole.value = speaker
                                showPromptPicker.value = true
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineText,
                                contentDescription = stringResource(R.string.ai_duel_choose_prompt),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                supportingText = {
                    if (promptName.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.ai_duel_selected_prompt, promptName)
                        )
                    }
                },
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )

            // ── Model selection row ──
            GlassSurface(
                onClick = {
                    if (!duelState.running) {
                        pickingModelRole.value = speaker
                        showModelPicker.value = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !duelState.running,
                style = GlassStyle.Thin,
                shape = MaterialTheme.shapes.medium,
                borderWidth = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppTheme.dimens.paddingNormal,
                            vertical = 14.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.ai_duel_model_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = modelDisplayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ────────────────────────────���─────────────────────────────────
//  Status bar — fixed at bottom
// ──────────────────────────────────────────────────────────────

@Composable
private fun DuelStatusBar(
    duelConfig: AIDuelConfig,
    duelState: com.shifenmiao.ai.model.AIDuelState,
    showInlineConfig: Boolean,
    showShare: Boolean,
    onStartDuel: () -> Unit,
    onStop: () -> Unit,
    onShare: () -> Unit,
) {
    GlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        style = GlassStyle.Thin,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
        borderWidth = 0.5.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = AppTheme.dimens.paddingNormal,
                    vertical = AppTheme.dimens.paddingSmall
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showShare) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineShare,
                        contentDescription = stringResource(R.string.exporting_share),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
            if (duelState.running) {
                Spacer(modifier = Modifier.width(AppTheme.dimens.paddingSmall))
                val speakerName = if (duelState.speaker == DuelSpeaker.A) {
                    duelConfig.roleNameA.ifBlank {
                        duelConfig.promptNameA.ifBlank { stringResource(R.string.ai_duel_speaker_a) }
                    }
                } else {
                    duelConfig.roleNameB.ifBlank {
                        duelConfig.promptNameB.ifBlank { stringResource(R.string.ai_duel_speaker_b) }
                    }
                }
                Text(
                    text = stringResource(
                        R.string.ai_duel_running_detail_format,
                        duelState.round + 1,
                        duelConfig.maxRounds,
                        speakerName
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!duelState.running) {
                GlassTonalButton(
                    modifier = Modifier,
                    onClick = onStartDuel,
                    enabled = showInlineConfig
                ) {
                    Text(text = stringResource(R.string.ai_duel_start))
                }
            } else {
                GlassTonalButton(
                    modifier = Modifier,
                    onClick = onStop
                ) {
                    Text(text = stringResource(R.string.ai_duel_stop))
                }
            }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  Picker bottom sheets (model, prompt, history)
// ──────────────────────────────────────────────────────────────

@Composable
private fun DuelPickerBottomSheets(
    appComponent: AppComponent,
    duelChatComponent: AIDuelChatComponent,
    duelConfig: AIDuelConfig,
    roleNameA: MutableState<String>,
    roleNameB: MutableState<String>,
    personaA: MutableState<String>,
    personaB: MutableState<String>,
    topic: MutableState<String>,
    pickingModelRole: MutableState<DuelSpeaker?>,
    showModelPicker: MutableState<Boolean>,
    pickingPromptRole: MutableState<DuelSpeaker?>,
    showPromptPicker: MutableState<Boolean>,
    selectedPromptGroupId: MutableState<Int>,
    showPersonaHistoryPicker: MutableState<Boolean>,
    pickingPersonaHistoryRole: MutableState<DuelSpeaker?>,
    showRoleNameHistoryPicker: MutableState<Boolean>,
    pickingRoleNameHistoryRole: MutableState<DuelSpeaker?>,
    showTopicHistoryPicker: MutableState<Boolean>,
) {
    val onNavigate = LocalOnNavigate.current
    val modelRole = pickingModelRole.value ?: DuelSpeaker.A
    val selectedEngine = if (modelRole == DuelSpeaker.A) duelConfig.engineA else duelConfig.engineB
    val selectedEngineName = selectedEngine?.identityKey().orEmpty()
    val selectedModelName = selectedEngine?.model?.name.orEmpty()
    val allEngines by duelChatComponent.allEngines.collectAsState()
    val modelsByProvider by duelChatComponent.modelsByProvider.collectAsState()
    AIModelsPickerBottomSheet(
        visible = showModelPicker.value,
        allEngines = allEngines,
        modelsByProvider = modelsByProvider,
        selectedEngineName = selectedEngineName,
        selectedModelName = selectedModelName,
        onSelected = { engine, _ ->
            val updated = if (modelRole == DuelSpeaker.A) {
                duelConfig.copy(engineA = engine)
            } else {
                duelConfig.copy(engineB = engine)
            }
            duelChatComponent.updateDraftConfig(updated)
            showModelPicker.value = false
            pickingModelRole.value = null
        },
        onDismiss = {
            showModelPicker.value = false
            pickingModelRole.value = null
        }
    )

    val promptCategories by duelChatComponent.promptCategories.collectAsState()
    val prompts by duelChatComponent.promptListFlow(selectedPromptGroupId.value)
        .collectAsState(initial = emptyList())

    val personaHistoryItems = remember { mutableStateOf<List<String>>(emptyList()) }
    val roleNameHistoryItems = remember { mutableStateOf<List<String>>(emptyList()) }
    val topicHistoryItems = remember { mutableStateOf<List<String>>(emptyList()) }

    BackHandler(
        enabled = showTopicHistoryPicker.value ||
                showRoleNameHistoryPicker.value ||
                showPersonaHistoryPicker.value ||
                showPromptPicker.value ||
                showModelPicker.value
    ) {
        when {
            showTopicHistoryPicker.value -> {
                showTopicHistoryPicker.value = false
            }

            showRoleNameHistoryPicker.value -> {
                showRoleNameHistoryPicker.value = false
                pickingRoleNameHistoryRole.value = null
            }

            showPersonaHistoryPicker.value -> {
                showPersonaHistoryPicker.value = false
                pickingPersonaHistoryRole.value = null
            }

            showPromptPicker.value -> {
                showPromptPicker.value = false
                pickingPromptRole.value = null
            }

            showModelPicker.value -> {
                showModelPicker.value = false
                pickingModelRole.value = null
            }
        }
    }

    LaunchedEffect(showPersonaHistoryPicker.value, pickingPersonaHistoryRole.value) {
        if (!showPersonaHistoryPicker.value) return@LaunchedEffect
        val role = pickingPersonaHistoryRole.value ?: DuelSpeaker.A
        personaHistoryItems.value = duelChatComponent.loadPersonaHistory(role)
    }

    LaunchedEffect(showRoleNameHistoryPicker.value, pickingRoleNameHistoryRole.value) {
        if (!showRoleNameHistoryPicker.value) return@LaunchedEffect
        val role = pickingRoleNameHistoryRole.value ?: DuelSpeaker.A
        roleNameHistoryItems.value = duelChatComponent.loadRoleNameHistory(role)
    }

    LaunchedEffect(showTopicHistoryPicker.value) {
        if (!showTopicHistoryPicker.value) return@LaunchedEffect
        topicHistoryItems.value = duelChatComponent.loadTopicHistory()
    }

    AIPromptsPickerBottomSheet(
        visible = showPromptPicker.value,
        categories = promptCategories,
        selectedCategoryId = selectedPromptGroupId.value,
        prompts = prompts,
        onCategorySelected = { selectedPromptGroupId.value = it },
        onPromptSelected = { prompt ->
            val role = pickingPromptRole.value ?: DuelSpeaker.A
            duelChatComponent.applyPromptToRole(role, prompt)
            showPromptPicker.value = false
            pickingPromptRole.value = null
        },
        onCreatePrompt = {
            showPromptPicker.value = false
            pickingPromptRole.value = null
            onNavigate(Screen.CreateAIChatPrompt())
        },
        onDismiss = {
            showPromptPicker.value = false
            pickingPromptRole.value = null
        }
    )

    val roleNameHistoryRoleText =
        if ((pickingRoleNameHistoryRole.value ?: DuelSpeaker.A) == DuelSpeaker.A) {
            stringResource(R.string.ai_duel_role_a)
        } else {
            stringResource(R.string.ai_duel_role_b)
        }
    AIDuelPersonaHistoryPickerBottomSheet(
        visible = showRoleNameHistoryPicker.value,
        title = stringResource(
            R.string.ai_duel_choose_history_role_name_title_format,
            roleNameHistoryRoleText
        ),
        items = roleNameHistoryItems.value,
        onSelected = { roleName ->
            val normalized = roleName.trim()
            val role = pickingRoleNameHistoryRole.value ?: DuelSpeaker.A
            val updated = if (role == DuelSpeaker.A) {
                roleNameA.value = normalized
                duelConfig.copy(roleNameA = normalized)
            } else {
                roleNameB.value = normalized
                duelConfig.copy(roleNameB = normalized)
            }
            duelChatComponent.updateDraftConfig(updated)
            showRoleNameHistoryPicker.value = false
            pickingRoleNameHistoryRole.value = null
        },
        onDismiss = {
            showRoleNameHistoryPicker.value = false
            pickingRoleNameHistoryRole.value = null
        }
    )

    val personaHistoryRoleText =
        if ((pickingPersonaHistoryRole.value ?: DuelSpeaker.A) == DuelSpeaker.A) {
            stringResource(R.string.ai_duel_role_a)
        } else {
            stringResource(R.string.ai_duel_role_b)
        }
    AIDuelPersonaHistoryPickerBottomSheet(
        visible = showPersonaHistoryPicker.value,
        title = stringResource(
            R.string.ai_duel_choose_history_prompt_title_format,
            personaHistoryRoleText
        ),
        items = personaHistoryItems.value,
        onSelected = { persona ->
            val role = pickingPersonaHistoryRole.value ?: DuelSpeaker.A
            val updated = if (role == DuelSpeaker.A) {
                personaA.value = persona
                duelConfig.copy(personaA = persona, promptIdA = 0, promptNameA = "")
            } else {
                personaB.value = persona
                duelConfig.copy(personaB = persona, promptIdB = 0, promptNameB = "")
            }
            duelChatComponent.updateDraftConfig(updated)
            showPersonaHistoryPicker.value = false
            pickingPersonaHistoryRole.value = null
        },
        onDismiss = {
            showPersonaHistoryPicker.value = false
            pickingPersonaHistoryRole.value = null
        }
    )

    AIDuelPersonaHistoryPickerBottomSheet(
        visible = showTopicHistoryPicker.value,
        title = stringResource(R.string.ai_duel_choose_history_topic_title),
        items = topicHistoryItems.value,
        onSelected = { selected ->
            topic.value = selected
            showTopicHistoryPicker.value = false
        },
        onDismiss = {
            showTopicHistoryPicker.value = false
        }
    )
}

// ──────────────────────────────────────────────────────────────
//  Helper composables
// ──────────────────────────────────────────────────────────────

@Composable
private fun buildPersonaLabel(role: DuelSpeaker, duelConfig: AIDuelConfig): String {
    val baseLabel = when (duelConfig.mode) {
        DuelMode.DEBATE -> stringResource(R.string.ai_duel_persona_label_debate)
        DuelMode.DIALOGUE -> stringResource(R.string.ai_duel_persona_label_dialogue)
        DuelMode.INTERVIEW -> if (role == DuelSpeaker.A) {
            stringResource(R.string.ai_duel_persona_label_interview_a)
        } else {
            stringResource(R.string.ai_duel_persona_label_interview_b)
        }

        DuelMode.ROLEPLAY -> stringResource(R.string.ai_duel_persona_label_roleplay)
    }
    val modelTitle = when (role) {
        DuelSpeaker.A -> duelConfig.engineA?.model?.title.orEmpty()
        DuelSpeaker.B -> duelConfig.engineB?.model?.title.orEmpty()
    }
    return if (modelTitle.isBlank()) baseLabel else "$baseLabel（$modelTitle）"
}

@Composable
private fun DuelMode.label(): String = when (this) {
    DuelMode.DEBATE -> stringResource(R.string.ai_duel_mode_debate)
    DuelMode.DIALOGUE -> stringResource(R.string.ai_duel_mode_dialogue)
    DuelMode.INTERVIEW -> stringResource(R.string.ai_duel_mode_interview)
    DuelMode.ROLEPLAY -> stringResource(R.string.ai_duel_mode_roleplay)
}

@Composable
private fun DuelMode.subtitle(): String = when (this) {
    DuelMode.DEBATE -> stringResource(R.string.ai_duel_mode_subtitle_debate)
    DuelMode.DIALOGUE -> stringResource(R.string.ai_duel_mode_subtitle_dialogue)
    DuelMode.INTERVIEW -> stringResource(R.string.ai_duel_mode_subtitle_interview)
    DuelMode.ROLEPLAY -> stringResource(R.string.ai_duel_mode_subtitle_roleplay)
}

@Composable
private fun DuelMode.roleCardTitle(speaker: DuelSpeaker): String = when (this) {
    DuelMode.DEBATE -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_card_debate_a)
    } else {
        stringResource(R.string.ai_duel_role_card_debate_b)
    }

    DuelMode.DIALOGUE -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_card_dialogue_a)
    } else {
        stringResource(R.string.ai_duel_role_card_dialogue_b)
    }

    DuelMode.INTERVIEW -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_card_interview_a)
    } else {
        stringResource(R.string.ai_duel_role_card_interview_b)
    }

    DuelMode.ROLEPLAY -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_card_roleplay_a)
    } else {
        stringResource(R.string.ai_duel_role_card_roleplay_b)
    }
}

@Composable
private fun DuelMode.personaHint(speaker: DuelSpeaker): String = when (this) {
    DuelMode.DEBATE -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_persona_hint_debate_a)
    } else {
        stringResource(R.string.ai_duel_persona_hint_debate_b)
    }

    DuelMode.DIALOGUE -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_persona_hint_dialogue_a)
    } else {
        stringResource(R.string.ai_duel_persona_hint_dialogue_b)
    }

    DuelMode.INTERVIEW -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_persona_hint_interview_a)
    } else {
        stringResource(R.string.ai_duel_persona_hint_interview_b)
    }

    DuelMode.ROLEPLAY -> if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_persona_hint_roleplay_a)
    } else {
        stringResource(R.string.ai_duel_persona_hint_roleplay_b)
    }
}

@Composable
private fun DuelMode.topicLabel(): String = when (this) {
    DuelMode.DEBATE -> stringResource(R.string.ai_duel_topic_label_debate)
    DuelMode.DIALOGUE -> stringResource(R.string.ai_duel_topic_label_dialogue)
    DuelMode.INTERVIEW -> stringResource(R.string.ai_duel_topic_label_interview)
    DuelMode.ROLEPLAY -> stringResource(R.string.ai_duel_topic_label_roleplay)
}

@Composable
private fun DuelMode.topicPlaceholder(): String = when (this) {
    DuelMode.DEBATE -> stringResource(R.string.ai_duel_topic_placeholder_debate)
    DuelMode.DIALOGUE -> stringResource(R.string.ai_duel_topic_placeholder_dialogue)
    DuelMode.INTERVIEW -> stringResource(R.string.ai_duel_topic_placeholder_interview)
    DuelMode.ROLEPLAY -> stringResource(R.string.ai_duel_topic_placeholder_roleplay)
}


// ──────────────────────────────────────────────────────────────
//  Messages list
// ──────────────────────────────────────────────────────────────

@Composable
private fun DuelMessagesList(
    modifier: Modifier = Modifier,
    messageUiModels: List<MessageUiModel>,
    lazyListState: LazyListState,
    appComponent: AppComponent,
    conversation: Conversation,
) {
    val codeBlockClickListener = rememberCodeBlockClickListener(appComponent = appComponent)

    ProvideMermaidRenderer {
        SelectionContainer(
            modifier = modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = lazyListState,
                reverseLayout = true,
                contentPadding = PaddingValues(
                    horizontal = AppTheme.dimens.paddingNormal,
                    vertical = AppTheme.dimens.paddingSmall
                ),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                val fixedKeyPrefix = "duel_fixed_${conversation.id}_"
                if (messageUiModels.isNotEmpty()) {
                    item(key = "${fixedKeyPrefix}ai_content_notice") {
                        AIContentNotice(true)
                    }
                }
                itemsIndexed(
                    items = messageUiModels,
                    key = { _, item -> item.id }
                ) { index, item ->
                    RenderChatMessageItem(
                        item = item,
                        index = index,
                        codeBlockClickListener = codeBlockClickListener,
                        onUserContainerHeader = { DuelUserMessageHeader(userContainerHeader = it) },
                        onUserVerticalSpace = { userSpace ->
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(userSpace.height)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            )
                        },
                        onUserLoading = {
                            ChatLoadingIndicator(
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        },
                        onUserReasoningContent = { reasoningContent, listener ->
                            RobotReasoningContent(
                                reasoningContent = reasoningContent.toRobotReasoningContent(),
                                codeBlockClickListener = listener,
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leftLine = MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
                            )
                        },
                        onUserReasoningHeader = { reasoningHeader ->
                            RobotReasoningHeader(
                                reasoningHeader = reasoningHeader.toRobotReasoningHeader(),
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        },
                        onUserReasoningBlock = { reasoningBlock, listener ->
                            RobotReasoningBlock(
                                reasoningBlock = reasoningBlock.toRobotReasoningBlock(),
                                codeBlockClickListener = listener,
                                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                leftLine = MaterialTheme.colorScheme.primary.copy(alpha = 0.48f)
                            )
                        },
                        onRobotContainerHeader = { DuelRobotMessageHeader(robotContainerHeader = it) },
                        onRobotContainerFooter = { _, _ -> DuelRobotFooter() },
                        onRobotError = {}
                    )
                }

                if (messageUiModels.isEmpty()) {
                    item(key = "${fixedKeyPrefix}placeholder_message") {
                        CustomChatCard(
                            isHuman = false,
                            showAvatar = false
                        ) {
                            Column(
                                modifier = Modifier.padding(AppTheme.dimens.spaceNormal),
                                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.ai_duel_chat_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTheme.colors.getPrimaryTextColor()
                                )
                                Text(
                                    text = stringResource(id = R.string.ai_duel_chat_description),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AppTheme.colors.getPrimaryTextColor()
                                )
                                Text(
                                    text = stringResource(id = R.string.ai_duel_placeholder_content),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppTheme.colors.getPrimaryTextColor()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────
//  Message header / footer composables
// ──────────────────────────────────────────────────────────────

@Composable
private fun DuelRobotFooter() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 15.dp)
            .height(12.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Bottom,
                shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
    )
}

@Composable
private fun DuelUserMessageHeader(userContainerHeader: MessageUiModel.UserContainerHeader) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Top,
                shape = RoundedCornerShape(12.dp, 0.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            )
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val showRoleAvatar = userContainerHeader.modelSubtitle.isNotBlank()
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = userContainerHeader.modelName,
                style = MaterialTheme.typography.labelMedium,
                color = if (showRoleAvatar) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (userContainerHeader.modelSubtitle.isNotBlank()) {
                Text(
                    text = userContainerHeader.modelSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
        if (showRoleAvatar) {
            Spacer(modifier = Modifier.size(8.dp))
            val avatarBackground = MaterialTheme.colorScheme.primaryContainer
            val avatarTint = MaterialTheme.colorScheme.onPrimaryContainer
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = avatarBackground,
                        shape = CircleShape
                    )
                    .padding(4.dp)
            ) {
                LetterIcon(
                    name = userContainerHeader.modelName,
                    modifier = Modifier.fillMaxSize(),
                    tint = avatarTint.copy(0.12f),
                    showOutline = false
                )
            }
        }
    }
}

@Composable
private fun DuelRobotMessageHeader(robotContainerHeader: MessageUiModel.RobotContainerHeader) {
    Spacer(
        modifier = Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .height(12.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Top,
                shape = RoundedCornerShape(0.dp, 12.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp, 6.dp),
    ) {
        val showRoleAvatar = robotContainerHeader.modelSubtitle.isNotBlank()
        if (showRoleAvatar) {
            val avatarBackground = MaterialTheme.colorScheme.tertiaryContainer
            val avatarTint = MaterialTheme.colorScheme.onTertiaryContainer
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = avatarBackground,
                        shape = CircleShape
                    )
                    .padding(4.dp)
            ) {
                LetterIcon(
                    name = robotContainerHeader.modelName,
                    modifier = Modifier.fillMaxSize(),
                    tint = avatarTint.copy(0.12f),
                    showOutline = false
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
        }
        Column {
            Text(
                text = robotContainerHeader.modelName,
                style = MaterialTheme.typography.labelMedium,
                color = if (showRoleAvatar) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (robotContainerHeader.modelSubtitle.isNotBlank()) {
                Text(
                    text = robotContainerHeader.modelSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}
