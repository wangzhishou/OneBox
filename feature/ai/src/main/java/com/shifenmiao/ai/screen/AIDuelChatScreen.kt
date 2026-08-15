package com.shifenmiao.ai.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.shifenmiao.ai.component.AIDuelChatComponent
import com.shifenmiao.ai.component.ChatLoadingIndicator
import com.shifenmiao.ai.component.RobotReasoningBlock
import com.shifenmiao.ai.component.RobotReasoningContent
import com.shifenmiao.ai.component.RobotReasoningHeader
import com.shifenmiao.ai.model.AIDuelConfig
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
import com.t8rin.imagetoolbox.core.ui.utils.content_pickers.rememberImagePicker
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitBackHandler
import com.t8rin.imagetoolbox.core.ui.widget.dialogs.ExitWithoutSavingDialog
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCard
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCardSegment
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassOutlinedTextField
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalButton
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassCardSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.resources.icons.line.LineShare
import com.t8rin.imagetoolbox.core.resources.icons.line.LineChevronRight
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwapVert
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMinus

// 提示词输入框字数上限
private const val PERSONA_MAX_CHARS = 800
// 头像压缩后的最长边像素
private const val AVATAR_MAX_SIZE_PX = 512

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
        // 与 DuelChatContent 一致：无消息且未运行时处于入口配置页
        val showInlineConfig =
            messageUiModels.isEmpty() && !duelState.running && !conversation.showLastMessage
        // 互聊进行中退出/新建的确认弹窗开关
        val showExitConfirm = remember { mutableStateOf(false) }
        val showNewConfirm = remember { mutableStateOf(false) }

        // 互聊进行中拦截返回键，先弹确认（遵循设置里的退出确认开关）
        ExitBackHandler(enabled = duelState.running && !chatUIState.showHistory) {
            showExitConfirm.value = true
        }

        BaseScreen(
            title = conversation.appTitle.ifBlank {
                stringResource(id = R.string.ai_duel_chat_title)
            },
            actions = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingTooSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 历史入口始终可用（不随页面初始化置灰），跳转历史中心
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
                    // 新建只在互聊会话页显示（入口配置页本身就是“新建”）
                    if (!chatUIState.showHistory && !showInlineConfig) {
                        DisableContainer(
                            enabled = chatUIState.pageState != PageState.INITIALIZING
                        ) {
                            IconButton(
                                onClick = {
                                    if (duelState.running) {
                                        showNewConfirm.value = true
                                    } else {
                                        duelChatComponent.startNewConversation()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddAiChat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            onGoBack = {
                when {
                    chatUIState.showHistory -> duelChatComponent.hideHistory()
                    duelState.running -> showExitConfirm.value = true
                    else -> appComponent.onGoBack()
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

        // 互聊进行中退出确认（复用公共退出确认弹窗，遵循设置开关）
        ExitWithoutSavingDialog(
            visible = showExitConfirm.value,
            onDismiss = { showExitConfirm.value = false },
            onExit = {
                duelChatComponent.stopDuel()
                appComponent.onGoBack()
            },
            title = stringResource(R.string.ai_duel_chat_title),
            text = stringResource(R.string.ai_duel_exit_confirm_message)
        )

        // 互聊进行中新建确认
        if (showNewConfirm.value) {
            AlertDialog(
                onDismissRequest = { showNewConfirm.value = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showNewConfirm.value = false
                            duelChatComponent.stopDuel()
                            duelChatComponent.startNewConversation()
                        }
                    ) {
                        Text(text = stringResource(R.string.button_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewConfirm.value = false }) {
                        Text(text = stringResource(R.string.button_cancel))
                    }
                },
                text = {
                    Text(text = stringResource(R.string.ai_duel_new_confirm_message))
                }
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
    val avatarA = remember { mutableStateOf(duelConfig.avatarA) }
    val avatarB = remember { mutableStateOf(duelConfig.avatarB) }
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
    val isExporting = remember { mutableStateOf(false) }

    LaunchedEffect(duelConfig) {
        roleNameA.value = duelConfig.roleNameA
        roleNameB.value = duelConfig.roleNameB
        personaA.value = duelConfig.personaA
        personaB.value = duelConfig.personaB
        avatarA.value = duelConfig.avatarA
        avatarB.value = duelConfig.avatarB
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
                avatarA = avatarA.value,
                avatarB = avatarB.value,
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

    // 交换位置：对调 A/B 的全部配置（角色 A 固定先发言，交换后即换先手）
    val onSwapRoles = {
        duelChatComponent.updateDraftConfig(
            duelConfig.copy(
                roleNameA = roleNameB.value,
                roleNameB = roleNameA.value,
                personaA = personaB.value,
                personaB = personaA.value,
                avatarA = avatarB.value,
                avatarB = avatarA.value,
                engineA = duelConfig.engineB,
                engineB = duelConfig.engineA,
                promptIdA = duelConfig.promptIdB,
                promptIdB = duelConfig.promptIdA,
                promptNameA = duelConfig.promptNameB,
                promptNameB = duelConfig.promptNameA,
            )
        )
    }

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
                avatarA = avatarA,
                avatarB = avatarB,
                maxRounds = maxRounds,
                pickingModelRole = pickingModelRole,
                showModelPicker = showModelPicker,
                pickingPromptRole = pickingPromptRole,
                showPromptPicker = showPromptPicker,
                showPersonaHistoryPicker = showPersonaHistoryPicker,
                pickingPersonaHistoryRole = pickingPersonaHistoryRole,
                showRoleNameHistoryPicker = showRoleNameHistoryPicker,
                pickingRoleNameHistoryRole = pickingRoleNameHistoryRole,
                onSwapRoles = onSwapRoles,
            )
        } else {
            DuelMessagesList(
                modifier = Modifier.weight(1f),
                messageUiModels = messageUiModels,
                lazyListState = lazyListState,
                appComponent = appComponent,
                conversation = conversation,
                duelConfig = duelConfig,
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
                        avatarA = avatarA.value,
                        avatarB = avatarB.value,
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
            pickingModelRole = pickingModelRole,
            showModelPicker = showModelPicker,
            pickingPromptRole = pickingPromptRole,
            showPromptPicker = showPromptPicker,
            selectedPromptGroupId = selectedPromptGroupId,
            showPersonaHistoryPicker = showPersonaHistoryPicker,
            pickingPersonaHistoryRole = pickingPersonaHistoryRole,
            showRoleNameHistoryPicker = showRoleNameHistoryPicker,
            pickingRoleNameHistoryRole = pickingRoleNameHistoryRole,
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
    avatarA: MutableState<String>,
    avatarB: MutableState<String>,
    maxRounds: MutableState<Int>,
    pickingModelRole: MutableState<DuelSpeaker?>,
    showModelPicker: MutableState<Boolean>,
    pickingPromptRole: MutableState<DuelSpeaker?>,
    showPromptPicker: MutableState<Boolean>,
    showPersonaHistoryPicker: MutableState<Boolean>,
    pickingPersonaHistoryRole: MutableState<DuelSpeaker?>,
    showRoleNameHistoryPicker: MutableState<Boolean>,
    pickingRoleNameHistoryRole: MutableState<DuelSpeaker?>,
    onSwapRoles: () -> Unit,
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
                text = stringResource(R.string.ai_duel_config_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ── Role A section ──
        DuelRoleConfigSection(
            speaker = DuelSpeaker.A,
            roleName = roleNameA,
            persona = personaA,
            avatar = avatarA,
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
            avatar = avatarB,
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
                        modifier = Modifier.size(12.dp),
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
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Swap roles（角色 A 固定先发言，交换位置即换先手）──
        GlassTonalButton(
            onClick = onSwapRoles,
            enabled = !duelState.running,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwapVert,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(AppTheme.dimens.paddingSmall))
            Text(text = stringResource(R.string.ai_duel_swap_roles))
        }

        Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
    }
}

@Composable
private fun DuelRoleConfigSection(
    speaker: DuelSpeaker,
    roleName: MutableState<String>,
    persona: MutableState<String>,
    avatar: MutableState<String>,
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val roleTitle = if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_a)
    } else {
        stringResource(R.string.ai_duel_role_b)
    }
    val roleNameLabel = if (speaker == DuelSpeaker.A) {
        stringResource(R.string.ai_duel_role_name_a_label)
    } else {
        stringResource(R.string.ai_duel_role_name_b_label)
    }
    val promptName =
        if (speaker == DuelSpeaker.A) duelConfig.promptNameA else duelConfig.promptNameB
    val engine = if (speaker == DuelSpeaker.A) duelConfig.engineA else duelConfig.engineB
    val modelDisplayName = engine?.model?.title?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.ai_duel_model_default)
    val avatarContainerColor = if (speaker == DuelSpeaker.A) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.tertiaryContainer
    }
    val avatarLetterTint = if (speaker == DuelSpeaker.A) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onTertiaryContainer
    }

    // 点击头像从相册选图，压缩后存到 filesDir/duel_avatars/，路径写入 avatar 状态
    val imagePicker = rememberImagePicker(onSuccess = { uri: Uri ->
        coroutineScope.launch(Dispatchers.IO) {
            val path = saveDuelAvatar(context, uri, speaker, avatar.value)
            if (path != null) avatar.value = path
        }
    })
    // 大面积编辑提示词弹窗开关
    val showExpandEditor = remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        borderWidth = 0.dp,
        // 卡片底色与头像同色系（A=primary / B=tertiary），玻璃效果调淡
        colors = CardDefaults.cardColors(containerColor = avatarContainerColor),
        containerAlpha = 0.22f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall)
        ) {
            // ── Card header: 头像 + 角色标题（A 卡带“先发言”标记）──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DuelRoleAvatar(
                    avatarPath = avatar.value,
                    fallbackName = roleTitle,
                    containerColor = avatarContainerColor,
                    letterTint = avatarLetterTint,
                    modifier = Modifier
                        .size(56.dp)
                        .clickable(enabled = !duelState.running) { imagePicker.pickImage() }
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = roleTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (speaker == DuelSpeaker.A) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ai_duel_first_speaker_badge),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    enabled = !duelState.running,
                    onClick = {
                        pickingPromptRole.value = speaker
                        showPromptPicker.value = true
                    }
                ) {
                    Text(
                        text = stringResource(R.string.ai_duel_choose_prompt),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Icon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp)
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
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Persona / prompt: 标签行（历史入口）──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.ai_duel_persona_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
            }

            // ── Persona / prompt: 输入框 ──
            GlassOutlinedTextField(
                value = persona.value,
                onValueChange = { persona.value = it.take(PERSONA_MAX_CHARS) },
                enabled = !duelState.running,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.ai_duel_persona_hint)) },
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.medium,
                colors = AppTheme.colors.getOutlinedTextFieldColors()
            )

            // ── 已选提示词 + 字数统计 + 放大编辑：独立一行，两端与输入框边缘对齐 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (promptName.isNotBlank()) {
                    Text(
                        text = stringResource(R.string.ai_duel_selected_prompt, promptName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(
                        R.string.ai_duel_persona_counter,
                        persona.value.length,
                        PERSONA_MAX_CHARS
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen,
                    contentDescription = stringResource(R.string.edit_prompt),
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                        // 图标字形在 24dp 视口内自带约 2dp 右内边距，右移补偿使其视觉右缘与输入框右边框贴齐
                        .offset(x = 1.5.dp)
                        .clickable(enabled = !duelState.running) {
                            showExpandEditor.value = true
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 大面积查看/编辑提示词弹窗
    if (showExpandEditor.value) {
        DuelPersonaExpandEditorDialog(
            roleTitle = roleTitle,
            persona = persona,
            onDismiss = { showExpandEditor.value = false }
        )
    }
}

// 全屏提示词编辑弹窗：更大面积查看和编辑当前角色的提示词
@Composable
private fun DuelPersonaExpandEditorDialog(
    roleTitle: String,
    persona: MutableState<String>,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // 不透明背景，避免下层聊天内容透过来干扰编辑
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.dimens.paddingNormal)
                .navigationBarsPadding()
                .imePadding(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppTheme.dimens.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingSmall)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.edit_prompt) + " · " + roleTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(
                            R.string.ai_duel_persona_counter,
                            persona.value.length,
                            PERSONA_MAX_CHARS
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                GlassOutlinedTextField(
                    value = persona.value,
                    onValueChange = { persona.value = it.take(PERSONA_MAX_CHARS) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text(stringResource(R.string.ai_duel_persona_hint)) },
                    shape = MaterialTheme.shapes.medium,
                    colors = AppTheme.colors.getOutlinedTextFieldColors()
                )
                GlassTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(R.string.done))
                }
            }
        }
    }
}

// 角色头像：已设置头像文件则圆形裁剪显示图片，否则回退首字母 LetterIcon
@Composable
private fun DuelRoleAvatar(
    avatarPath: String,
    fallbackName: String,
    containerColor: Color,
    letterTint: Color,
    modifier: Modifier = Modifier,
) {
    val avatarFile = remember(avatarPath) {
        avatarPath.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = containerColor, shape = CircleShape)
    ) {
        if (avatarFile != null) {
            AsyncImage(
                model = avatarFile,
                contentDescription = fallbackName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LetterIcon(
                name = fallbackName,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                tint = letterTint.copy(0.12f),
                showOutline = false
            )
        }
    }
}

// 把选中的头像图压缩（最长边 ≤512px JPEG）保存到 filesDir/duel_avatars/，返回绝对路径；失败返回 null
private fun saveDuelAvatar(
    context: Context,
    uri: Uri,
    speaker: DuelSpeaker,
    oldPath: String
): String? {
    return runCatching {
        val source = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null
        val longestSide = maxOf(source.width, source.height)
        val bitmap = if (longestSide > AVATAR_MAX_SIZE_PX) {
            val scale = AVATAR_MAX_SIZE_PX.toFloat() / longestSide
            Bitmap.createScaledBitmap(
                source,
                (source.width * scale).toInt().coerceAtLeast(1),
                (source.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            source
        }
        val dir = File(context.filesDir, "duel_avatars").apply { mkdirs() }
        // 文件名带时间戳，避免 Coil 按路径命中旧缓存；替换后尽力删除旧文件
        val file = File(dir, "avatar_${speaker.name.lowercase()}_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        if (oldPath.isNotBlank() && oldPath != file.absolutePath) {
            runCatching { File(oldPath).delete() }
        }
        file.absolutePath
    }.getOrNull()
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
            // 开始/停止按钮始终右对齐
            Spacer(modifier = Modifier.weight(1f))
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
    pickingModelRole: MutableState<DuelSpeaker?>,
    showModelPicker: MutableState<Boolean>,
    pickingPromptRole: MutableState<DuelSpeaker?>,
    showPromptPicker: MutableState<Boolean>,
    selectedPromptGroupId: MutableState<Int>,
    showPersonaHistoryPicker: MutableState<Boolean>,
    pickingPersonaHistoryRole: MutableState<DuelSpeaker?>,
    showRoleNameHistoryPicker: MutableState<Boolean>,
    pickingRoleNameHistoryRole: MutableState<DuelSpeaker?>,
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

    BackHandler(
        enabled = showRoleNameHistoryPicker.value ||
                showPersonaHistoryPicker.value ||
                showPromptPicker.value ||
                showModelPicker.value
    ) {
        when {
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
        },
        onManualCreatePrompt = {
            // 手动创建：跳转提示词编辑页（同首页 + 面板的提示词入口）
            showPromptPicker.value = false
            pickingPromptRole.value = null
            onNavigate(Screen.EditPromptItem())
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
    duelConfig: AIDuelConfig,
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
                        onUserContainerHeader = {
                            DuelUserMessageHeader(
                                userContainerHeader = it,
                                avatarPath = duelConfig.avatarA,
                            )
                        },
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
                        onRobotContainerHeader = {
                            DuelRobotMessageHeader(
                                robotContainerHeader = it,
                                avatarPath = duelConfig.avatarB,
                            )
                        },
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
private fun DuelUserMessageHeader(
    userContainerHeader: MessageUiModel.UserContainerHeader,
    avatarPath: String,
) {
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
            DuelRoleAvatar(
                avatarPath = avatarPath,
                fallbackName = userContainerHeader.modelName,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                letterTint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun DuelRobotMessageHeader(
    robotContainerHeader: MessageUiModel.RobotContainerHeader,
    avatarPath: String,
) {
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
            DuelRoleAvatar(
                avatarPath = avatarPath,
                fallbackName = robotContainerHeader.modelName,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                letterTint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(32.dp)
            )
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
