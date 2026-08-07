package com.shifenmiao.ai.screen

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.ai.AIConversationEntryType
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import java.util.Date

@Composable
fun AITabChatScreen(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent
) {
    val chatUIState by aiChatComponent.chatUIState.collectAsState()
    val conversation by aiChatComponent.conversation.collectAsState()
    val showCancelConfirm by aiChatComponent.showCancelConfirmDialog.collectAsState()
    val onNavigate = LocalOnNavigate.current
    var isTextSelectionMode by remember { mutableStateOf(false) }
    LaunchedEffect(conversation.id) {
        isTextSelectionMode = false
    }
    LaunchedEffect(chatUIState.chatActive) {
        if (chatUIState.chatActive) {
            isTextSelectionMode = false
        }
    }
    BaseScreen(
        isBackHandler = false,
        showNavigationBarsPadding = false,
        title = if (isTextSelectionMode) {
            stringResource(id = com.shifenmiao.core.R.string.select_text)
        } else {
            when (conversation.entryType) {
                AIConversationEntryType.PROMPT,
                AIConversationEntryType.AGENT -> conversation.appTitle.ifBlank { conversation.title }
                AIConversationEntryType.DUEL -> conversation.appTitle.ifBlank {
                    stringResource(id = com.shifenmiao.core.R.string.ai_duel_chat_title)
                }
                AIConversationEntryType.CHAT -> stringResource(id = com.shifenmiao.core.R.string.ai_chat_title)
                AIConversationEntryType.ASSISTANT -> stringResource(id = com.shifenmiao.core.R.string.ai_tab_chat_title)
                else -> {
                    stringResource(id = com.shifenmiao.core.R.string.ai_stream_answer_title)
                }
            }
        },
        navigationIcon = {
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
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = {
                        aiChatComponent.hideHistory()
                    }
                ) {
                    Icon(
                        com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
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
                        stringResource(com.shifenmiao.core.R.string.close)
                    } else {
                        stringResource(com.shifenmiao.core.R.string.nav_add)
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        onGoBack = {
            if (isTextSelectionMode) {
                isTextSelectionMode = false
            } else if (chatUIState.showHistory) {
                aiChatComponent.hideHistory()
            } else if (chatUIState.chatActive) {
                aiChatComponent.requestCancelFetch()
            } else {
                appComponent.onGoBack()
            }
        },
        supportGlassEffect = true,
    ) {
        AIChatBody(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent,
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

        // 取消确认弹窗：AI 正在回复时用户点击取消或返回
        CancelFetchConfirmDialog(
            visible = showCancelConfirm,
            onStop = { aiChatComponent.confirmCancelFetch() },
            onDismiss = { aiChatComponent.dismissCancelConfirm() },
            messageResId = com.shifenmiao.core.R.string.ai_chat_cancel_confirm_message_bg,
            dismissLabelResId = com.shifenmiao.core.R.string.ai_chat_cancel_confirm_background
        )
    }
}
