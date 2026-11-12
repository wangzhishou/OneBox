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

package com.shifenmiao.ai.screen

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.model.ai.AIConversationEntryType
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.wanbaohe.core.ui.utils.helper.scrollToContentStart
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import kotlinx.coroutines.launch
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
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    BaseScreen(
        isBackHandler = false,
        showNavigationBarsPadding = false,
        title = when (conversation.entryType) {
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
                        contentDescription = stringResource(com.shifenmiao.core.R.string.back_button),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = {
                aiChatComponent.changeConversationId(Date().time.toString())
            }) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddAiChat,
                    contentDescription = stringResource(com.shifenmiao.core.R.string.nav_add),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        onGoBack = {
            if (chatUIState.showHistory) {
                aiChatComponent.hideHistory()
            } else if (chatUIState.chatActive) {
                aiChatComponent.requestCancelFetch()
            } else {
                appComponent.onGoBack()
            }
        },
        supportGlassEffect = true,
        // 标题栏点击只回到首条内容；聊天列表是 reverseLayout，首条在最后一个 index
        onTitleClick = {
            coroutineScope.launch {
                lazyListState.scrollToContentStart(reverseLayout = true)
            }
        },
    ) {
        AIChatBody(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent,
            onNavigate = onNavigate,
            lazyListState = lazyListState,
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
