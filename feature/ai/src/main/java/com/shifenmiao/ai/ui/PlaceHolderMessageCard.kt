package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.halilibo.richtext.ui.material3.RichMarkdown
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.base.utils.LoginUtils
import com.shifenmiao.base.ui.CustomChatCard
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.database.data_draft.DataDraftHelper
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.Source
import com.shifenmiao.model.ai.AIConversationEntryType
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassTonalIconButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload

private const val CHAT_QUICK_START_VISIBLE_COUNT = 4
private const val CHAT_QUICK_START_REFRESH_THRESHOLD = 8

@Composable
fun PlaceHolderMessageCard(
    conversation: Conversation,
    onSuggestionClick: (String) -> Unit = {},
    onShowToolCenter: () -> Unit = {},
    onPushToRemote: () -> Unit = {},
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    chatInputComponent: ChatInputComponent
) {
    val currentAIModel = appComponent.aiEngineManager.currentAIModel.collectAsState().value
    val toolCenterUiState = aiChatComponent.toolCenterUiState.collectAsState().value
    val promptCardState = aiChatComponent.promptCardState.collectAsState().value
    val promptBadgeLabel = if (promptCardState.isSystemPrompt) {
        stringResource(R.string.ai_prompt_badge_system)
    } else {
        null
    }
    when (conversation.entryType) {
        AIConversationEntryType.DUEL -> {
            CustomChatCard(
                isHuman = false,
                showAvatar = false
            ) {
                Column(
                    modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.ai_duel_placeholder_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.getPrimaryTextColor()
                    )
                    Text(
                        text = stringResource(id = R.string.ai_duel_placeholder_content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.getPrimaryTextColor()
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
                    ChatSessionStatusBar(
                        currentModelTitle = currentAIModel.title.ifBlank { currentAIModel.name },
                        onModelClick = { chatInputComponent.showModelPicker() },
                    )
                }
            }
        }

        AIConversationEntryType.PROMPT -> {
            PromptWorkCard(
                message = conversation.prompt,
                promptId = conversation.promptId,
                promptBadgeLabel = promptBadgeLabel,
                updatedAtMillis = promptCardState.updatedAtMillis,
                isEditable = !promptCardState.isSystemPrompt &&
                        (promptCardState.source != Source.REMOTE || LoginUtils.isAdmin()),
                currentModelTitle = currentAIModel.title.ifBlank { currentAIModel.name },
                enabledToolCount = toolCenterUiState.enabledToolNames.size,
                onModelClick = { chatInputComponent.showModelPicker() },
                onToolClick = onShowToolCenter,
                onPushToRemote = onPushToRemote,
                emptyStateText = if (conversation.promptId != null) {
                    stringResource(R.string.ai_prompt_placeholder_loading_desc)
                } else {
                    stringResource(R.string.ai_prompt_placeholder_missing_desc)
                }
            )
        }

        else -> {
            CustomChatCard(
                isHuman = false,
                showAvatar = false
            ) {
                Column(
                    modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
                ) {
                    Text(
                        text = stringResource(id = R.string.ai_chat_placeholder_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.getPrimaryTextColor()
                    )
                    Text(
                        text = stringResource(id = R.string.ai_chat_placeholder_content),
                        style = MaterialTheme.typography.bodyLarge,
                        color = AppTheme.colors.getPrimaryTextColor()
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
                    Text(
                        text = stringResource(R.string.ai_chat_quick_start_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ChatWorkingModeSelector(
                        currentMode = toolCenterUiState.workingMode,
                        onModeSelected = aiChatComponent::setWorkingMode,
                    )
                    Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))

                    ChatQuickStartSection(
                        onSuggestionClick = onSuggestionClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptWorkCard(
    message: String,
    promptId: Int?,
    promptBadgeLabel: String?,
    updatedAtMillis: Long?,
    isEditable: Boolean,
    currentModelTitle: String,
    enabledToolCount: Int,
    onModelClick: () -> Unit,
    onToolClick: () -> Unit,
    onPushToRemote: () -> Unit = {},
    emptyStateText: String,
) {
    val onNavigate = LocalOnNavigate.current
    val dataDraftHelper: DataDraftHelper = LocalDataDraftHelper.current
    val coroutineScope = rememberCoroutineScope()
    val expandedState = rememberSaveable(promptId) {
        mutableStateOf(false)
    }
    val expanded = expandedState.value
    val previewText = message.lineSequence()
        .map(String::trim)
        .filter(String::isNotBlank)
        .joinToString(separator = " ")
    val updatedAtText = updatedAtMillis?.takeIf { it > 0L }?.let(::formatPromptUpdatedAt)

    CustomChatCard(
        isHuman = false,
        showAvatar = false,
        onClick = {
            if (message.isNotBlank()) {
                expandedState.value = !expandedState.value
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
            verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (promptBadgeLabel != null) {
                    Text(
                        text = promptBadgeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (updatedAtText != null) {
                    Text(
                        text = updatedAtText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (message.isNotBlank()) {
                    GlassTonalIconButton(
                        onClick = { Clipboard.copy(message) },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 2.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.copy),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (isEditable && promptId != null) {
                    GlassTonalIconButton(
                        onClick = {
                            coroutineScope.launch {
                                val safePromptId = promptId
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
                                onNavigate(Screen.CreateAIChatPrompt(draftId = draftId))
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 2.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val showPushButton = LoginUtils.isAdmin()
                if (showPushButton && promptId != null) {
                    GlassTonalIconButton(
                        onClick = onPushToRemote,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 2.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload,
                            contentDescription = stringResource(R.string.prompt_push_to_remote),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                if (message.isNotBlank()) {
                    GlassTonalIconButton(
                        onClick = { expandedState.value = !expandedState.value },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (expanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (message.isBlank()) {
                Text(
                    text = emptyStateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.getPrimaryTextColor()
                )
            } else if (expanded) {
                Column {
                    RichMarkdown(content = message)
                }
            } else {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.getPrimaryTextColor(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(AppTheme.dimens.spaceNormal))
            ChatSessionStatusBar(
                currentModelTitle = currentModelTitle,
                enabledToolCount = enabledToolCount,
                onModelClick = onModelClick,
                onToolClick = onToolClick
            )
        }
    }
}

private fun formatPromptUpdatedAt(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}


@Composable
private fun ChatSessionStatusBar(
    currentModelTitle: String,
    enabledToolCount: Int? = null,
    onModelClick: () -> Unit,
    onToolClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusCapsule(
            modifier = Modifier.weight(1f),
            title = "",
            value = currentModelTitle,
            onClick = onModelClick
        )
        if(enabledToolCount != null && onToolClick != null) {
            StatusCapsule(
                modifier = Modifier.weight(1f),
                title = stringResource(R.string.ai_chat_status_tools),
                value = stringResource(R.string.ai_chat_status_tools_count, enabledToolCount),
                highlighted = enabledToolCount > 0,
                onClick = onToolClick
            )
        }
    }
}

@Composable
fun ChatQuickStartSection(
    onSuggestionClick: (String) -> Unit
) {
    val fallbackStarters = listOf(
        stringResource(R.string.ai_chat_quick_start_1),
        stringResource(R.string.ai_chat_quick_start_2),
        stringResource(R.string.ai_chat_quick_start_3),
        stringResource(R.string.ai_chat_quick_start_4),
        stringResource(R.string.ai_chat_quick_start_5),
        stringResource(R.string.ai_chat_quick_start_6),
        stringResource(R.string.ai_chat_quick_start_7),
        stringResource(R.string.ai_chat_quick_start_8),
        stringResource(R.string.ai_chat_quick_start_9),
        stringResource(R.string.ai_chat_quick_start_10),
        stringResource(R.string.ai_chat_quick_start_11),
        stringResource(R.string.ai_chat_quick_start_12)
    )
    val starters = RemoteConfigStorage.getRemoteConfig()
        .chatQuickStartPrompts
        .toAvailableQuickStartPrompts(fallback = fallbackStarters)
    var visibleStarters by remember(starters) {
        mutableStateOf(pickQuickStartPrompts(starters))
    }
    var refreshNonce by rememberSaveable(starters) {
        mutableIntStateOf(0)
    }
    val refreshRotation by animateFloatAsState(
        targetValue = refreshNonce * 180f,
        animationSpec = tween(durationMillis = 320),
        label = "ChatQuickStartRefreshRotation"
    )
    val showRefreshButton = starters.size > CHAT_QUICK_START_REFRESH_THRESHOLD

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_chat_quick_start_subtitle),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            if (showRefreshButton) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.see_random),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            visibleStarters = pickQuickStartPrompts(
                                prompts = starters,
                                previous = visibleStarters
                            )
                            refreshNonce += 1
                        }
                        .padding(6.dp)
                        .size(18.dp)
                        .graphicsLayer { rotationZ = refreshRotation },
                    tint = LocalContentColor.current.copy(alpha = 0.72f)
                )
            }
        }
        AnimatedContent(
            targetState = refreshNonce,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) + scaleIn(animationSpec = tween(220), initialScale = 0.96f))
                    .togetherWith(
                        fadeOut(animationSpec = tween(160)) + scaleOut(animationSpec = tween(160), targetScale = 0.96f)
                    )
            },
            label = "ChatQuickStartSwitcher"
        ) { refreshRound ->
            val displayedStarters = remember(refreshRound, visibleStarters) {
                visibleStarters
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayedStarters.forEach { starter ->
                    GlassSurface(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .clickable { onSuggestionClick(starter) },
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(
                            text = starter,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

private fun List<String>?.toAvailableQuickStartPrompts(fallback: List<String>): List<String> {
    val normalizedPrompts = this.orEmpty()
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()

    if (normalizedPrompts.isNotEmpty()) {
        return normalizedPrompts
    }

    return fallback
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()
}

private fun pickQuickStartPrompts(
    prompts: List<String>,
    previous: List<String>? = null,
    visibleCount: Int = CHAT_QUICK_START_VISIBLE_COUNT
): List<String> {
    if (prompts.size <= visibleCount) {
        return prompts.take(visibleCount)
    }

    var next = prompts.shuffled().take(visibleCount)
    if (previous.isNullOrEmpty()) {
        return next
    }

    repeat(5) {
        if (next != previous) {
            return next
        }
        next = prompts.shuffled().take(visibleCount)
    }

    return next
}

@Composable
private fun StatusCapsule(
    title: String? = null,
    value: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    GlassSurface(
        modifier = modifier,
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onClick).clip(MaterialTheme.shapes.large).padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineTune,
                contentDescription = null,
                tint = if (highlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
