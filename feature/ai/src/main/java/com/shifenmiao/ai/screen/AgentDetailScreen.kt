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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.ai.component.AgentDetailComponent
import com.shifenmiao.ai.ui.AIChatBottom
import com.shifenmiao.ai.ui.AIContentRecommend
import com.wanbaohe.base.ui.DisableContainer
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.common.ui.BaseScreen
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.state.PageState
import com.shifenmiao.model.state.LocalChatUIState
import com.shifenmiao.theme.AppTheme
import com.wanbaohe.core.ui.utils.helper.scrollToContentStart
import com.wanbaohe.core.ui.utils.navigation.LocalOnNavigate
import com.wanbaohe.core.ui.utils.navigation.Screen
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory

@Composable
fun AgentDetailScreen(
    appComponent: AppComponent,
    agentDetailComponent: AgentDetailComponent
) {
    val chatUIState by agentDetailComponent.chatUIState.collectAsState()
    val messageUiModels by agentDetailComponent.messageUiModels.collectAsState()
    val conversation by agentDetailComponent.conversation.collectAsState()
    val showCancelConfirm by agentDetailComponent.showCancelConfirmDialog.collectAsState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val onNavigate = LocalOnNavigate.current
    val showButton by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 && lazyListState.firstVisibleItemScrollOffset > 0
        }
    }
    val isRecommendVisible = agentDetailComponent.isRecommendState.collectAsState()

    // 滚动到底部，但只在加载完成时执行。
    // 使用 messageUiModels.size 而非整个列表作为 key，避免流式期间每帧重启协程。
    LaunchedEffect(messageUiModels.size, chatUIState.pageState) {
        if (messageUiModels.isNotEmpty() && chatUIState.pageState != PageState.INITIALIZING) {
            lazyListState.scrollToItem(0)
        }
    }
    CompositionLocalProvider(
        LocalChatUIState provides chatUIState,
    ) {
        BaseScreen(
            title = conversation.appTitle.ifBlank {
                stringResource(id = R.string.ai_chat_title)
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
                                        initialFilter = AIConversationEntryType.AGENT
                                    )
                                )
                            }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
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
                    showCancelConfirm -> {
                        agentDetailComponent.dismissCancelConfirm()
                    }
                    chatUIState.chatActive -> {
                        // 流式输出期间返回，先弹出挽留/停止确认弹窗
                        agentDetailComponent.requestCancelFetch()
                    }
                    chatUIState.showHistory -> {
                        if (messageUiModels.isNotEmpty()) {
                            agentDetailComponent.hideHistory()
                        } else {
                            appComponent.onGoBack()
                        }
                    }
                    else -> {
                        appComponent.onGoBack()
                    }
                }
            },
            // 标题栏点击只回到首条内容；聊天列表是 reverseLayout，首条在最后一个 index
            onTitleClick = {
                coroutineScope.launch {
                    lazyListState.scrollToContentStart(reverseLayout = true)
                }
            },
            foreground = {
                if (!chatUIState.showHistory) {
                    AIChatBottom(
                        showTop = showButton
                    ) {
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    }
                }
            }
        ) {
            ChatMessagesList(
                modifier = Modifier.weight(1f),
                messageUiModels = messageUiModels,
                lazyListState = lazyListState,
                appComponent = appComponent,
                aiChatComponent = agentDetailComponent,
                chatUIState = chatUIState,
                conversation = conversation,
                onNavigate = onNavigate,
                onSuggestionClick = {},
                chatInputComponent = agentDetailComponent.chatInputComponent
            )
            AIContentRecommend(
                isShow = isRecommendVisible.value,
                appComponent = appComponent
            )
        }

        // 取消确认弹窗：AI 正在回复时用户点击返回或停止
        CancelFetchConfirmDialog(
            visible = showCancelConfirm,
            onStop = { agentDetailComponent.confirmCancelFetch() },
            onDismiss = { agentDetailComponent.dismissCancelConfirm() }
        )
    }

}
