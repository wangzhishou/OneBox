package com.wanbaohe.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.screen.AIChatBody
import com.shifenmiao.base.ui.DisableContainer
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.state.PageState
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.resources.icons.ArrowBack
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAddAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineHistory
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedModalBottomSheet
import java.util.Date
import com.shifenmiao.core.R as CoreR

/**
 * 全局持久化 AI 聊天底部弹窗。
 *
 * [aiChatComponent] 由 [com.t8rin.imagetoolbox.feature.root.presentation.screenLogic.RootComponent]
 * 创建并持有，生命周期与 App 相同；本 Composable 仅是该 Component 状态的观察窗口，
 * 弹窗关闭后流式输出依然在后台进行。
 */
@Composable
fun AIChatModalBottomSheet(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
) {
    val chatUIState by aiChatComponent.chatUIState.collectAsState()
    val conversation by aiChatComponent.conversation.collectAsState()
    EnhancedModalBottomSheet(
        visible = true,
        onDismiss = {
            appComponent.hideAIChat()
        },
        dragHandle = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (conversation.entryType) {
                            AIConversationEntryType.ASSISTANT -> stringResource(CoreR.string.ai_tab_chat_title)
                            else -> conversation.title
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.paddingTooSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!chatUIState.showHistory) {
                            IconButton(onClick = {
                                aiChatComponent.showHistory()
                            }) {
                                Icon(
                                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineHistory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    contentDescription = "Back"
                                )
                            }
                        }
                    }
                },
                actions = {
                    DisableContainer(
                        enabled = chatUIState.pageState != PageState.INITIALIZING
                    ) {
                        Row {
                            Icon(
                                modifier = Modifier.clickable {
                                    aiChatComponent.changeConversationId(Date().time.toString())
                                },
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAddAiChat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(
                                modifier = Modifier.width(
                                    AppTheme.dimens.paddingNormal
                                )
                            )
                        }
                    }
                }
            )
        },
        enableBackHandler = false,
        enableBottomContentWeight = false
    ) {
        AIChatBody(
            appComponent = appComponent,
            aiChatComponent = aiChatComponent,
            showNavigationBarsPadding = true,
        )
    }
    BackHandler {
        if (chatUIState.showHistory) {
            aiChatComponent.hideHistory()
        } else {
            appComponent.hideAIChat()
        }
    }
}