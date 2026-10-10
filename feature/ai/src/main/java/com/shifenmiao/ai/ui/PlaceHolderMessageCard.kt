package com.shifenmiao.ai.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import com.shifenmiao.model.remote.ChatQuickStartItem
import com.shifenmiao.model.remote.defaultChatQuickStartItems
import com.shifenmiao.model.remote.defaultChatQuickStartPrompts
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.helper.Clipboard
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.utils.navigation.screenIconByModule
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.t8rin.imagetoolbox.core.resources.icons.ContentCopy
import com.t8rin.imagetoolbox.core.resources.icons.Edit
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineAiChat
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineMagic
import com.t8rin.imagetoolbox.core.resources.icons.line.LineTune
import com.t8rin.imagetoolbox.core.resources.icons.line.LineCloudUpload

private const val CHAT_QUICK_START_VISIBLE_COUNT = 4
private const val CHAT_QUICK_START_REFRESH_THRESHOLD = 8

@Composable
fun PlaceHolderMessageCard(
    conversation: Conversation,
    onSuggestionClick: (String) -> Unit = {},
    onPushToRemote: () -> Unit = {},
    onContentHeightDelta: (Int) -> Unit = {},
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    chatInputComponent: ChatInputComponent
) {
    val currentAIModel = appComponent.aiEngineManager.currentAIModel.collectAsState().value
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
                onPushToRemote = onPushToRemote,
                onContentHeightDelta = onContentHeightDelta,
                emptyStateText = if (conversation.promptId != null) {
                    stringResource(R.string.ai_prompt_placeholder_loading_desc)
                } else {
                    stringResource(R.string.ai_prompt_placeholder_missing_desc)
                }
            )
        }

        else -> {
            Column(
                modifier = Modifier.padding(AppTheme.dimens.paddingNormal),
                verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.spaceSmall)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineAiChat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.ai_chat_placeholder_title),
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppTheme.colors.getPrimaryTextColor()
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.ai_chat_placeholder_content),
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppTheme.colors.getPrimaryTextColor()
                        )
                    }
                }

                ChatQuickStartSection(
                    onSuggestionClick = onSuggestionClick
                )
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
    onPushToRemote: () -> Unit = {},
    onContentHeightDelta: (Int) -> Unit = {},
    emptyStateText: String,
) {
    val onNavigate = LocalOnNavigate.current
    val dataDraftHelper: DataDraftHelper = LocalDataDraftHelper.current
    val coroutineScope = rememberCoroutineScope()
    val expanded by AppSharedStorage.isExpandedPrompt.collectAsState()
    val toggleExpanded: () -> Unit = {
        if (message.isNotBlank()) {
            AppSharedStorage.saveIsExpandedPrompt(!expanded)
        }
    }
    val updatedAtText = updatedAtMillis?.takeIf { it > 0L }?.let(::formatPromptUpdatedAt)
    val collapsedMaxHeight = (LocalConfiguration.current.screenHeightDp / 2).dp
    var lastContentHeight by remember(promptId) { mutableIntStateOf(-1) }

    CustomChatCard(
        isHuman = false,
        showAvatar = false,
        onClick = toggleExpanded
    ) {
        Column(
            modifier = Modifier
                .padding(AppTheme.dimens.paddingNormal)
                .animateContentSize()
                .onGloballyPositioned { coordinates ->
                    val newHeight = coordinates.size.height
                    val previousHeight = lastContentHeight
                    lastContentHeight = newHeight
                    if (previousHeight >= 0 && newHeight != previousHeight) {
                        onContentHeightDelta(newHeight - previousHeight)
                    }
                },
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                if (isEditable && promptId != null) {
                    CardActionIcon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.edit),
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
                        }
                    )
                }
                val showPushButton = LoginUtils.isAdmin()
                if (showPushButton && promptId != null) {
                    CardActionIcon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineCloudUpload,
                        contentDescription = stringResource(R.string.prompt_push_to_remote),
                        onClick = onPushToRemote
                    )
                }
            }

            if (message.isBlank()) {
                Text(
                    text = emptyStateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.getPrimaryTextColor()
                )
            } else {
                if (expanded) {
                    RichMarkdown(content = message)
                } else {
                    val previewScrollState = rememberScrollState()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = collapsedMaxHeight)
                    ) {
                        Column(
                            modifier = Modifier.verticalScroll(
                                state = previewScrollState,
                                enabled = false
                            )
                        ) {
                            RichMarkdown(content = message)
                        }
                        if (previewScrollState.maxValue > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    CardActionIcon(
                        imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.ContentCopy,
                        contentDescription = stringResource(R.string.copy),
                        onClick = { Clipboard.copy(message) }
                    )
                    CardActionIcon(
                        imageVector = if (expanded) com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess else com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore,
                        contentDescription = null,
                        iconSize = 22.dp,
                        onClick = toggleExpanded
                    )
                }
            }
        }
    }
}

@Composable
private fun CardActionIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    iconSize: Dp = 18.dp,
    onClick: () -> Unit
) {
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp)
            .size(iconSize)
    )
}

private fun formatPromptUpdatedAt(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}


@Composable
private fun ChatSessionStatusBar(
    currentModelTitle: String,
    onModelClick: () -> Unit,
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
    }
}

@Composable
fun ChatQuickStartSection(
    onSuggestionClick: (String) -> Unit
) {
    val remoteConfig = RemoteConfigStorage.getRemoteConfig()
    val starters = remoteConfig.chatQuickStartItems.toAvailableQuickStartItems(
        fallbackPrompts = remoteConfig.chatQuickStartPrompts,
        localFallback = defaultChatQuickStartItems()
    )
    var visibleStarters by remember(starters) {
        mutableStateOf(pickQuickStartItems(starters))
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
                text = stringResource(R.string.ai_chat_quick_start_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (showRefreshButton) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.see_random),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            visibleStarters = pickQuickStartItems(
                                items = starters,
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
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayedStarters.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { item ->
                            ChatQuickStartCard(
                                item = item,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                onClick = { onSuggestionClick(item.text) }
                            )
                        }
                        if (rowItems.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatQuickStartCard(
    item: ChatQuickStartItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val moduleIcon = remember(item.module) { screenIconByModule(item.module) }
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = moduleIcon
                        ?: com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineMagic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = item.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun List<ChatQuickStartItem>?.toAvailableQuickStartItems(
    fallbackPrompts: List<String>?,
    localFallback: List<ChatQuickStartItem>
): List<ChatQuickStartItem> {
    val normalizedItems = this.orEmpty()
        .map { it.copy(text = it.text.trim()) }
        .filter { it.text.isNotEmpty() }
        .distinct()

    if (normalizedItems.isNotEmpty()) {
        return normalizedItems
    }

    val normalizedPrompts = fallbackPrompts.toAvailableQuickStartPrompts(fallback = emptyList())
    // 旧字段的默认值就是本地化默认列表,等于默认列表说明服务端并未显式下发,
    // 此时应落到带 module 的 localFallback,否则兜底卡片永远拿不到模块图标
    val isLocalizedDefault = normalizedPrompts.isNotEmpty() &&
        normalizedPrompts == defaultChatQuickStartPrompts()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .distinct()
    if (normalizedPrompts.isNotEmpty() && !isLocalizedDefault) {
        return normalizedPrompts.map { ChatQuickStartItem(text = it) }
    }

    return localFallback
        .map { it.copy(text = it.text.trim()) }
        .filter { it.text.isNotEmpty() }
        .distinct()
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

private fun pickQuickStartItems(
    items: List<ChatQuickStartItem>,
    previous: List<ChatQuickStartItem>? = null,
    visibleCount: Int = CHAT_QUICK_START_VISIBLE_COUNT
): List<ChatQuickStartItem> {
    if (items.size <= visibleCount) {
        return items.take(visibleCount)
    }

    var next = items.shuffled().take(visibleCount)
    if (previous.isNullOrEmpty()) {
        return next
    }

    repeat(5) {
        if (next != previous) {
            return next
        }
        next = items.shuffled().take(visibleCount)
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
            MaterialTheme.colorScheme.primaryContainer
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
