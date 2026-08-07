package com.shifenmiao.base.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.material3.RichMarkdown
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineEmojiObjects
import com.t8rin.imagetoolbox.core.resources.icons.line.LinePsychology

@Composable
fun ExpandableCardMarkdownContent(
    message: String,
    type: AIConversationEntryType,
    entryRefId: String?,
    title: String = "",
    badgeLabel: String? = null,
    isEditable: Boolean = true,
) {
    val isExpandedPrompt = AppSharedStorage.isExpandedPrompt.collectAsState()
    val onNavigator = LocalOnNavigate.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val editScope = rememberCoroutineScope()
    val promptId = remember(entryRefId) { entryRefId?.toIntOrNull()?.takeIf { it > 0 } }
    CustomChatCard(
        isHuman = false,
        showAvatar = false,
        onClick = {
            AppSharedStorage.saveIsExpandedPrompt(!isExpandedPrompt.value)
        }
    ) {
        Column(
            modifier = Modifier.padding(
                vertical = AppTheme.dimens.paddingNormal,
                horizontal = AppTheme.dimens.paddingNormal
            )
        ) {
            if (title.isNotBlank() || !badgeLabel.isNullOrBlank()) {
                PromptIdentityBlock(
                    title = title,
                    badgeLabel = badgeLabel
                )
                Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
            }

            ExpandableHeader(
                isExpanded = isExpandedPrompt.value,
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmojiObjects,
                title = stringResource(com.shifenmiao.core.R.string.prompt_prompt),
                onToggle = {
                    AppSharedStorage.saveIsExpandedPrompt(!isExpandedPrompt.value)
                }
            )

            ExpandableContent(
                isExpanded = isExpandedPrompt.value,
                message = message,
                actionContent = {
                    Spacer(modifier = Modifier.height(AppTheme.dimens.paddingSmall))
                    ActionButtonsRow(
                        showEditButton = type == AIConversationEntryType.PROMPT && promptId != null && isEditable,
                        onEditClick = {
                            editScope.launch {
                                val safePromptId = promptId ?: return@launch
                                val draftId = dataDraftHelper
                                    .getLatestByTypeAndRelatedEntityId(
                                        draftType = ListItemType.PROMPT.id,
                                        relatedEntityId = safePromptId
                                    )
                                    ?.id
                                    ?: dataDraftHelper.createDraft(
                                        draftType = ListItemType.PROMPT.id,
                                        relatedEntityId = safePromptId
                                    )
                                onNavigator(Screen.EditPromptItem(draftId = draftId))
                            }
                        },
                        onCopyClick = {
                            Clipboard.copy(message)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun PromptIdentityBlock(
    title: String,
    badgeLabel: String?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        badgeLabel
            ?.takeIf { it.isNotBlank() }
            ?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
    }
}

@Composable
fun ExpandableMarkdownContent(
    message: String
) {
    val isExpandedReasoningChat = AppSharedStorage.isExpandedReasoningChat.collectAsState()
    val currentExpandedReasoningChat = remember { mutableStateOf(isExpandedReasoningChat.value) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                MaterialTheme.shapes.medium.copy(
                    bottomEnd = CornerSize(0.0.dp),
                    bottomStart = CornerSize(0.0.dp)
                )
            )
            .clickable {
                AppSharedStorage.saveIsExpandedReasoningChat(!currentExpandedReasoningChat.value)
                currentExpandedReasoningChat.value = !currentExpandedReasoningChat.value
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    vertical = AppTheme.dimens.paddingNormal,
                    horizontal = AppTheme.dimens.paddingNormal
                )
        ) {
            if (!currentExpandedReasoningChat.value) {
                CollapsedReasoningHeader(message = message)
            } else {
                ExpandableHeader(
                    isExpanded = true,
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineEmojiObjects,
                    title = stringResource(com.shifenmiao.core.R.string.ai_reasoning_title)
                )
            }
            ExpandableContent(
                isExpanded = currentExpandedReasoningChat.value,
                message = message
            )
        }
    }
}

@Composable
private fun CollapsedReasoningHeader(message: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LinePsychology,
            contentDescription = "Reasoning",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = message,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
            contentDescription = "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandableHeader(
    isExpanded: Boolean,
    icon: ImageVector,
    title: String,
    onToggle: (Boolean) -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Black
            ),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = if (isExpanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ExpandableContent(
    isExpanded: Boolean,
    message: String,
    paddingValues: PaddingValues = PaddingValues(
        top = AppTheme.dimens.paddingNormal,
        start = AppTheme.dimens.paddingTooSmall,
        end = AppTheme.dimens.paddingTooSmall
    ),
    actionContent: @Composable () -> Unit = {}
) {
    AnimatedVisibility(visible = isExpanded) {
        Spacer(modifier = Modifier.height(AppTheme.dimens.paddingNormal))
        MarkdownContent(
            message = message,
            paddingValues = paddingValues,
            content = actionContent
        )
    }
}

@Composable
private fun ActionButtonsRow(
    showEditButton: Boolean,
    onEditClick: () -> Unit,
    onCopyClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showEditButton) {
            ActionButton(
                icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                contentDescription = "Edit Prompt",
                onClick = onEditClick
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        ActionButton(
            icon = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
            contentDescription = "Copy",
            onClick = onCopyClick
        )
    }
}

@Composable
fun MarkdownContent(
    message: String,
    paddingValues: PaddingValues = PaddingValues(
        horizontal = AppTheme.dimens.paddingNormal,
        vertical = AppTheme.dimens.paddingNormal
    ),
    content: @Composable () -> Unit = {}
) {
    if (message.isNotBlank()) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .heightIn(max = 200.dp)
                .verticalScroll(rememberScrollState())
        ) {
            RichMarkdown(
                content = message
            )
            content.invoke()
        }
    }
}