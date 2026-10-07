package com.shifenmiao.online.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.shifenmiao.base.ui.empty.EmptyStateGuide
import com.shifenmiao.base.ui.empty.EmptyStateGuideAction
import com.shifenmiao.core.R
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.RemoteConfigStorage
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAgent
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoAwesomeMosaic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAutoFix
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCodeEditor
import com.t8rin.imagetoolbox.core.resources.icons.line.LineNote
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePrompt
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassThin

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
        description = stringResource(
            if (isFiltered) R.string.home_filter_empty_description
            else R.string.home_empty_description,
        ),
        actions = if (isFiltered) {
            listOf(
                EmptyStateGuideAction(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                    title = stringResource(R.string.home_clear_filter),
                    description = stringResource(R.string.home_clear_filter_description),
                    onClick = onClearFilter,
                ),
            )
        } else {
            buildList {
                add(
                    EmptyStateGuideAction(
                        icon = Icons.Outlined.Edit,
                        title = stringResource(R.string.home_create_manual),
                        description = stringResource(R.string.home_create_manual_description, typeName),
                        onClick = onManualCreate,
                        emphasized = !showAiCreate,
                    )
                )
                if (showAiCreate) {
                    add(
                        EmptyStateGuideAction(
                            icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                            title = stringResource(R.string.home_create_with_ai),
                            description = stringResource(R.string.home_create_with_ai_description, typeName),
                            onClick = onAiCreate,
                            emphasized = true,
                        )
                    )
                }
            }
        },
        footerHint = if (isFiltered) {
            null
        } else {
            stringResource(R.string.home_created_content_hint)
        },
        modifier = modifier,
    )
}

@Composable
fun CreateChoiceCard(
    listType: ListItemType,
    onManualCreate: () -> Unit,
    onAiCreate: () -> Unit,
    modifier: Modifier = Modifier,
    /** 相邻普通卡片的实测高度,双列时据此与旁边的卡片对齐;为 null 时退回最小高度模式 */
    siblingHeight: Dp? = null,
    showAiCreate: Boolean = true,
) {
    val typeName = listTypeDisplayName(listType)
    val shape = MaterialTheme.shapes.extraLarge
    val fixedHeight = siblingHeight?.let { maxOf(it, 180.dp) }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (fixedHeight != null) Modifier.height(fixedHeight)
                else Modifier.heightIn(min = 220.dp)
            )
            .glassThin(
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            .clip(shape)
            .padding(12.dp),
    ) {
        val showActionIcons = maxWidth >= 140.dp
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    text = stringResource(R.string.home_create_new_title, typeName),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.home_create_new_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(10.dp))
            CreationActionCard(
                icon = Icons.Outlined.Edit,
                title = stringResource(R.string.home_create_manual),
                description = stringResource(R.string.home_create_manual_description, typeName),
                onClick = onManualCreate,
                emphasized = !showAiCreate,
                compact = true,
                showIcon = showActionIcons,
                modifier = if (fixedHeight != null) Modifier.weight(1f) else Modifier,
                enforceMinHeight = fixedHeight == null,
            )
            if (showAiCreate) {
                Spacer(Modifier.height(8.dp))
                CreationActionCard(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAutoFix,
                    title = stringResource(R.string.home_create_with_ai),
                    description = stringResource(R.string.home_create_with_ai_description, typeName),
                    onClick = onAiCreate,
                    emphasized = true,
                    compact = true,
                    showIcon = showActionIcons,
                    modifier = if (fixedHeight != null) Modifier.weight(1f) else Modifier,
                    enforceMinHeight = fixedHeight == null,
                )
            }
        }
    }
}


@Composable
private fun CreationActionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    emphasized: Boolean = false,
    compact: Boolean = false,
    showIcon: Boolean = true,
    modifier: Modifier = Modifier,
    enforceMinHeight: Boolean = true,
) {
    val shape = MaterialTheme.shapes.large
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = if (enforceMinHeight) (if (compact) 60.dp else 76.dp) else 0.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .glassThin(
                shape = shape,
                color = if (emphasized) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceContainerLow,
            )
            .padding(
                horizontal = if (compact) 10.dp else 14.dp,
                vertical = if (compact) 8.dp else 10.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showIcon) {
            CreationIcon(
                icon = icon,
                emphasized = emphasized,
                modifier = Modifier.size(if (compact) 42.dp else 52.dp),
            )
            Spacer(Modifier.width(if (compact) 10.dp else 14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = if (compact) MaterialTheme.typography.titleSmall
                else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!compact) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        )
    }
}

@Composable
private fun CreationIcon(
    icon: ImageVector,
    emphasized: Boolean,
    modifier: Modifier = Modifier.size(52.dp),
) {
    Box(
        modifier = modifier.glassThin(
            shape = CircleShape,
            color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.14f)
            else MaterialTheme.colorScheme.surfaceContainerHighest,
        ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun listTypeDisplayName(listType: ListItemType): String = stringResource(
    when (listType) {
        ListItemType.NOTE -> R.string.record_tab_title
        ListItemType.NORMAL -> R.string.type_default
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


