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

package com.shifenmiao.online.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wanbaohe.base.ui.empty.EmptyStateGuide
import com.wanbaohe.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.wanbaohe.core.ui.utils.navigation.Screen
import com.wanbaohe.core.ui.widget.glass.GlassTonalButton
import com.wanbaohe.core.ui.widget.glass.glassThin

@Composable
fun HomeEmptyState(
    listType: ListItemType,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
    isFiltered: Boolean = false,
    onClearFilter: () -> Unit = {},
    showAiCreate: Boolean = true,
) {
    val typeName = listTypeDisplayName(listType)
    EmptyStateGuide(
        icon = if (isFiltered) {
            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix
        } else {
            listTypeIcon(listType)
        },
        title = stringResource(
            if (isFiltered) R.string.home_filter_empty_title else R.string.home_empty_title,
            typeName,
        ),
        description = if (isFiltered) {
            stringResource(R.string.home_filter_empty_description)
        } else {
            null
        },
        actions = if (isFiltered) {
            listOf(
                EmptyStateGuideAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                    title = stringResource(R.string.home_clear_filter),
                    onClick = onClearFilter,
                ),
            )
        } else {
            buildList {
                add(
                    EmptyStateGuideAction(
                        icon = Icons.Outlined.Edit,
                        title = stringResource(R.string.home_create_manual),
                        onClick = onManualCreate,
                    )
                )
                if (showAiCreate) {
                    add(
                        EmptyStateGuideAction(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat,
                            title = stringResource(R.string.home_create_with_ai),
                            onClick = onAiCreate,
                        )
                    )
                }
            }
        },
        modifier = modifier,
    )
}

@Composable
fun CreateChoiceCard(
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
    /** 相邻普通卡片的实测高度,双列时据此与旁边的卡片对齐;为 null 时退回最小高度模式 */
    siblingHeight: Dp? = null,
    showAiCreate: Boolean = true,
) {
    val shape = MaterialTheme.shapes.extraLarge
    val fixedHeight = siblingHeight?.let { maxOf(it, 180.dp) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fixedHeight != null) Modifier.height(fixedHeight)
                else Modifier.heightIn(min = 180.dp)
            )
            .glassThin(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .clip(shape)
            .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Bottom,
    ) {
        CreationActionButton(
            icon = Icons.Outlined.Edit,
            title = stringResource(R.string.home_create_manual),
            onClick = onManualCreate,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        )
        if (showAiCreate) {
            Spacer(Modifier.height(12.dp))
            CreationActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat,
                title = stringResource(R.string.home_create_with_ai),
                onClick = onAiCreate,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun CreationActionButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
) {
    GlassTonalButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun listTypeDisplayName(listType: ListItemType): String = stringResource(
    when (listType) {
        ListItemType.NOTE -> R.string.record_tab_title
        ListItemType.NORMAL -> R.string.home_tab_tools
        ListItemType.AGENT -> R.string.type_agent
        ListItemType.PROMPT -> R.string.type_prompt
        ListItemType.HTML -> R.string.home_tab_web_title
        else -> R.string.placeholder_empty_title
    }
)

private fun listTypeIcon(listType: ListItemType): ImageVector = when (listType) {
    ListItemType.NOTE -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineNote
    ListItemType.AGENT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAgent
    ListItemType.PROMPT -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePrompt
    ListItemType.HTML -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCodeEditor
    else -> com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoAwesomeMosaic
}

/**
 * 各 tab「AI 创建」按钮的点击行为：
 * - 笔记/网址：跳助手 Tab 开新会话，并预填输入框；预填词与助手页快捷开始区
 *   共用 chatQuickStartPrompts(下标约定见 [QUICK_START_INDEX_NOTE_FILL_IN])，远程可配；
 * - 应用：跳助手 Tab 开新会话(无专属预填词)；
 * - 提示词：跳「创建提示词」页（本身就是 AI 生成）；
 * - 其余类型走 [fallback]。
 */
@Composable
fun aiCreateActionFor(
    listType: ListItemType,
    onNavigator: (Screen) -> Unit,
    fallback: () -> Unit,
): () -> Unit {
    val chatTitle = stringResource(R.string.ai_chat_title)
    // 本地兜底,与 defaultChatQuickStartPrompts 末尾两条一致;点击时远程配置优先
    val noteFillIn = stringResource(R.string.ai_chat_quick_start_17)
    val webFillIn = stringResource(R.string.ai_chat_quick_start_18)
    return when (listType) {
        // 带非空 title 会触发 startGuidedConversation:清空历史 + 新会话 id;
        // template 只预填到输入框,不自动发送
        ListItemType.NOTE, ListItemType.HTML -> {
            {
                val prompts = RemoteConfigStorage.getRemoteConfig().chatQuickStartPrompts
                val index = if (listType == ListItemType.NOTE) {
                    QUICK_START_INDEX_NOTE_FILL_IN
                } else {
                    QUICK_START_INDEX_WEB_FILL_IN
                }
                onNavigator(
                    Screen.AITabChatScreen(
                        Conversation(
                            entryType = AIConversationEntryType.ASSISTANT,
                            title = chatTitle,
                            template = prompts?.getOrNull(index)?.takeIf { it.isNotBlank() }
                                ?: if (listType == ListItemType.NOTE) noteFillIn else webFillIn,
                        )
                    )
                )
            }
        }

        ListItemType.NORMAL -> {
            {
                onNavigator(
                    Screen.AITabChatScreen(
                        Conversation(
                            entryType = AIConversationEntryType.ASSISTANT,
                            title = chatTitle,
                        )
                    )
                )
            }
        }

        ListItemType.PROMPT -> {
            { onNavigator(Screen.CreateAIChatPrompt()) }
        }

        else -> fallback
    }
}

// chatQuickStartPrompts 下标约定(见 RemoteConfig.chatQuickStartPrompts 注释):
// 16=笔记「AI 创建」预填词,17=网址「AI 创建」预填词
private const val QUICK_START_INDEX_NOTE_FILL_IN = 16
private const val QUICK_START_INDEX_WEB_FILL_IN = 17


