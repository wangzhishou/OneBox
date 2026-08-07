package com.shifenmiao.ai.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.shifenmiao.ai.chat.NewTextInputField
import com.shifenmiao.ai.component.AIChatComponent
import com.shifenmiao.ai.logic.ChatInputComponent
import com.shifenmiao.base.utils.ActionUtils
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.model.ai.AttachmentProcessingState
import com.shifenmiao.model.ai.ChatInputEventHandler
import com.shifenmiao.model.ai.Conversation
import com.shifenmiao.model.ai.ProcessingStep
import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.state.ChatUIState
import com.shifenmiao.model.state.PageState
import com.shifenmiao.storage.AIChatStorage
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedAlertDialog
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassStyle
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassBackground
import com.t8rin.imagetoolbox.core.ui.widget.system.OnePrimaryButton
import com.t8rin.imagetoolbox.core.ui.widget.system.OneSecondaryButton
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.Add
import com.t8rin.imagetoolbox.core.resources.icons.ArrowUpward
import com.t8rin.imagetoolbox.core.resources.icons.Close
import com.t8rin.imagetoolbox.core.resources.icons.Fullscreen
import com.t8rin.imagetoolbox.core.resources.icons.DeleteSweep
import com.t8rin.imagetoolbox.core.resources.icons.line.LineStopCircle


@Composable
fun NewChatInput(
    modifier: Modifier = Modifier,
    chatUIState: ChatUIState,
    conversation: State<Conversation>,
    aiChatComponent: AIChatComponent,
    appComponent: AppComponent,
    chatInputComponent: ChatInputComponent,
    enabledToolCount: Int = 0,
    onShowToolCenter: () -> Unit = {},
    onSendMessage: () -> Unit = {},
    onImagePreview: (Uri) -> Unit = {}
) {

    val inputState = chatInputComponent.chatInputState.collectAsState()
    val isEnableReasoning = AIChatStorage.isEnableReasoning.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    var showUnavailableEngineDialog by remember { mutableStateOf(false) }

    val eventHandler = remember(
        chatUIState,
        conversation.value,
        inputState,
        aiChatComponent,
        chatInputComponent
    ) {
        ChatInputEventHandler(
            sendMessage = {
                if (!conversation.value.engine.hasAvailableChatRoute()) {
                    showUnavailableEngineDialog = true
                } else {
                    ActionUtils.userAIChatInputCheck(conversation.value) {
                        onSendMessage()
                        aiChatComponent.startChatWithStreaming(
                            messageContent = inputState.value.inputText,
                            attachments = inputState.value.attachedMedia,
                            enableReasoning = conversation.value.engine.model.canReasoning && isEnableReasoning.value,
                            systemPromptOverride = inputState.value.selectedSystemPromptText
                                ?.takeIf { it.isNotBlank() },
                            onNext = {
                                chatInputComponent.clearInputText()
                                // 不在这里清空附件，等处理完成后再清空
                            }
                        )
                        coroutineScope.launch {
                            keyboardController?.hide()
                        }
                    }
                }
            },
            cancelFetch = {
                aiChatComponent.requestCancelFetch()
            },
            toggleExpand = {
                chatInputComponent.toggleExpand(!inputState.value.isExpanded)
            }
        )
    }

    val isMorePanelOpen = inputState.value.inputMore
    val containerShape = RoundedCornerShape(28.dp)
    val containerColor = MaterialTheme.colorScheme.surfaceContainer
    AnimatedVisibility(
        visible = chatUIState.pageState != PageState.INITIALIZING,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            // 附件预览条
            AttachmentPreviewRow(
                chatInputComponent = chatInputComponent,
                onImagePreview = onImagePreview,
                modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp)
            )
            // 已选系统提示词 chip
            SelectedSystemPromptChip(
                chatInputComponent = chatInputComponent,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            // 整体容器：输入框 + 展开面板一体化背景
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .then(
                        if (isMorePanelOpen) Modifier
                            .clip(containerShape)
                            .glassBackground(
                                color = containerColor,
                                shape = containerShape
                            )
                        else Modifier
                    )
            ) {
                StandardInputSection(
                    conversation = conversation.value,
                    chatUIState = chatUIState,
                    eventHandler = eventHandler,
                    chatInputComponent = chatInputComponent,
                )

                AnimatedVisibility(
                    visible = isMorePanelOpen,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    MoreInputSection(
                        appComponent = appComponent,
                        chatInputComponent = chatInputComponent,
                        enabledToolCount = enabledToolCount,
                        onShowToolCenter = onShowToolCenter
                    )
                }
            }
        }
    }

    // 全屏写作态：用 ModalBottomSheet 占满屏幕
    if (inputState.value.isExpanded) {
        FullScreenChatInputSheet(
            chatInputComponent = chatInputComponent,
            chatUIState = chatUIState,
            eventHandler = eventHandler,
        )
    }

    if (showUnavailableEngineDialog) {
        EnhancedAlertDialog(
            visible = true,
            onDismissRequest = { showUnavailableEngineDialog = false },
            title = {
                Text(text = stringResource(R.string.ai_chat_engine_unavailable_dialog_title))
            },
            text = {
                Text(
                    text = stringResource(
                        if (FlavorType.fromName() == FlavorType.GOOGLE) {
                            R.string.ai_chat_engine_unavailable_dialog_message_overseas
                        } else {
                            R.string.ai_chat_engine_unavailable_dialog_message
                        }
                    )
                )
            },
            confirmButton = {
                OnePrimaryButton(
                    text = stringResource(R.string.ai_chat_engine_unavailable_banner_action),
                    onClick = {
                        showUnavailableEngineDialog = false
                        appComponent.showAIChatSettings()
                    }
                )
            },
            dismissButton = {
                OneSecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = { showUnavailableEngineDialog = false }
                )
            }
        )
    }
}

@Composable
private fun StandardInputSection(
    conversation: Conversation,
    chatUIState: ChatUIState,
    eventHandler: ChatInputEventHandler,
    chatInputComponent: ChatInputComponent,
) {
    val inputState = chatInputComponent.chatInputState.collectAsState()
    val hasAttachments by remember(inputState.value.attachedMedia) {
        derivedStateOf { inputState.value.attachedMedia.isNotEmpty() }
    }
    val textEmpty by remember(inputState.value.inputText) {
        derivedStateOf { inputState.value.inputText.isEmpty() }
    }
    val showClear by remember(inputState.value.inputText) {
        derivedStateOf { inputState.value.inputText.length >= MIN_CHARS_FOR_CLEAR }
    }
    val isLoading = chatUIState.chatActive
    val attachmentsReady by remember(inputState.value.attachedMedia) {
        derivedStateOf { chatInputComponent.areAttachmentsReady() }
    }
    val sendDisabled = (textEmpty && !hasAttachments) || (hasAttachments && !attachmentsReady)
    NewTextInputField(
        inputState = inputState,
        conversation = conversation,
        onValueChange = { text, start, end ->
            chatInputComponent.onInputValueChange(text, start, end)
        },
        leadingIcon = {
            // 左侧 + / X 按钮：切换底部更多面板
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .glassBackground(
                        style = if (inputState.value.inputMore) {
                            GlassStyle.Medium
                        } else {
                            GlassStyle.None
                        },
                        color = if (inputState.value.inputMore) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            Color.Transparent
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    modifier = Modifier.size(40.dp),
                    onClick = { chatInputComponent.toggleInputMore() }
                ) {
                    Icon(
                        imageVector = if (inputState.value.inputMore)
                            com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close
                        else
                            com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Add,
                        contentDescription = if (inputState.value.inputMore)
                            stringResource(R.string.close)
                        else
                            stringResource(R.string.nav_add),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (inputState.value.inputMore) 20.dp else 24.dp)
                    )
                }
            }
        },
        // 单行：仅显示发送按钮
        singleLineTrailing = {
            ActionButton(
                isLoading = isLoading,
                textEmpty = sendDisabled,
                onSend = eventHandler.sendMessage,
                onCancel = eventHandler.cancelFetch,
            )
        },
        // 多行：底部 action bar — (清空) | 全屏 | 发送
        multilineBottomActions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 0.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (showClear && !isLoading) {
                    SmallIconBtn(
                        icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.DeleteSweep,
                        contentDescription = stringResource(R.string.clear),
                        onClick = { chatInputComponent.clearInputText() }
                    )
                }
                SmallIconBtn(
                    icon = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Fullscreen,
                    contentDescription = stringResource(R.string.create_ai_agent_expand),
                    onClick = eventHandler.toggleExpand
                )
                ActionButton(
                    isLoading = isLoading,
                    textEmpty = sendDisabled,
                    onSend = eventHandler.sendMessage,
                    onCancel = eventHandler.cancelFetch,
                )
            }
        }
    )
}

@Composable
private fun SmallIconBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.size(36.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SelectedSystemPromptChip(
    chatInputComponent: ChatInputComponent,
    modifier: Modifier = Modifier
) {
    val inputState = chatInputComponent.chatInputState.collectAsState()
    val title = inputState.value.selectedSystemPromptTitle
    val hasPrompt = !title.isNullOrBlank()

    AnimatedVisibility(
        visible = hasPrompt,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .glassBackground(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.ai_system_prompt_selected_label) + (title ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            IconButton(
                onClick = { chatInputComponent.clearSystemPrompt() },
                modifier = Modifier.size(18.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.clear),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun AttachmentPreviewRow(
    chatInputComponent: ChatInputComponent,
    onImagePreview: (Uri) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val inputState = chatInputComponent.chatInputState.collectAsState()
    val media = inputState.value.attachedMedia
    if (media.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(media, key = { it.uri.toString() }) { item ->
            AttachmentItem(
                item = item,
                onRemove = { chatInputComponent.removeAttachment(item.uri) },
                onPreview = { onImagePreview(chatInputComponent.getPreviewUri(item)) }
            )
        }
    }
}

@Composable
private fun AttachmentItem(
    item: com.shifenmiao.model.ai.AttachedMedia,
    onRemove: () -> Unit,
    onPreview: () -> Unit = {}
) {
    val backgroundColor = when (item.processingState) {
        is AttachmentProcessingState.ERROR -> MaterialTheme.colorScheme.errorContainer
        is AttachmentProcessingState.COMPLETED -> MaterialTheme.colorScheme.primaryContainer.copy(
            alpha = 0.5f
        )

        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val stateText = getStateText(item)
    val isImage = item.mimeType.startsWith("image/") || item.isImage

    // 统一左右布局：左侧缩略图/图标 + 右侧文字 + 删除按钮
    Row(
        modifier = Modifier
            .widthIn(max = 240.dp)
            .clip(RoundedCornerShape(16.dp))
            .glassBackground(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onPreview)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isImage) {
            // 图片缩略图 48dp
            val imageModel = remember(item) {
                item.localPath?.let { path ->
                    val file = java.io.File(path)
                    if (file.exists()) file else null
                } ?: item.thumbnailBase64?.takeIf { it.isNotBlank() }
                ?: item.localContent?.takeIf { it.isNotBlank() }
                ?: item.uri
            }
            Box(modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))) {
                AsyncImage(
                    model = imageModel,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // 处理中/错误覆盖层
                when (item.processingState) {
                    is AttachmentProcessingState.PROCESSING -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                    }

                    is AttachmentProcessingState.ERROR -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        // 文件名 + 状态
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing(30.dp),
                    velocity = 30.dp,
                    repeatDelayMillis = 1000
                ),
                text = item.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
            Text(
                modifier = Modifier.basicMarquee(
                    iterations = Int.MAX_VALUE,
                    spacing = MarqueeSpacing(30.dp),
                    velocity = 30.dp,
                    repeatDelayMillis = 1000
                ),
                text = stateText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.dp.value.sp),
                color = getStateColor(item.processingState),
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(18.dp)
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Rounded.Close,
                contentDescription = stringResource(R.string.delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

private fun getStateText(media: com.shifenmiao.model.ai.AttachedMedia): String {
    val state = media.processingState
    return when (state) {
        is AttachmentProcessingState.IDLE -> {
            val isImage = media.mimeType.startsWith("image/") || media.isImage
            if (isImage) {
                // 图片：提示将压缩优化，显示原图大小作为参考
                val sizeKB = media.size / 1024
                val sizeText = if (sizeKB > 1024) {
                    "${"%.1f".format(sizeKB / 1024.0)}MB"
                } else {
                    "${sizeKB}KB"
                }
                // 估算压缩后大小（原图 * 0.3 系数）和 tokens
                val estimatedCompressedKB = (media.size * 3 / 10) / 1024
                val estimatedTokens = ((estimatedCompressedKB * 1024 * 4 / 3) / 4).toInt()
                "就绪 · $sizeText → 压缩后约${estimatedCompressedKB}KB · ~${estimatedTokens}tokens"
            } else {
                val sizeKB = media.size / 1024
                val sizeText = if (sizeKB > 1024) {
                    "${"%.1f".format(sizeKB / 1024.0)}MB"
                } else {
                    "${sizeKB}KB"
                }
                val estimatedTokens = ((media.size * 4 / 3) / 4).toInt()
                "就绪 · ${sizeText} · ~${estimatedTokens}tokens"
            }
        }

        is AttachmentProcessingState.PROCESSING -> {
            when (state.step) {
                ProcessingStep.CHECKING -> "检查中..."
                ProcessingStep.RESIZING -> "压缩尺寸..."
                ProcessingStep.CONVERTING -> "转换格式..."
                ProcessingStep.COMPRESSING -> "压缩中..."
                ProcessingStep.ENCODING -> "编码中..."
                ProcessingStep.UPLOADING -> "上传中..."
            }
        }

        is AttachmentProcessingState.COMPLETED -> {
            val sizeKB = state.processedSize / 1024
            val processedSizeText = if (sizeKB > 1024) {
                "${"%.1f".format(sizeKB / 1024.0)}MB"
            } else {
                "${sizeKB}KB"
            }
            // 估算 tokens
            val estimatedTokens = ((state.processedSize * 4 / 3) / 4).toInt()
            "✓ ${processedSizeText} · ~${estimatedTokens}tokens"
        }

        is AttachmentProcessingState.ERROR -> "失败: ${state.message}"
    }
}

@Composable
private fun getStateColor(state: AttachmentProcessingState): Color {
    return when (state) {
        is AttachmentProcessingState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
        is AttachmentProcessingState.PROCESSING -> MaterialTheme.colorScheme.primary
        is AttachmentProcessingState.COMPLETED -> MaterialTheme.colorScheme.primary
        is AttachmentProcessingState.ERROR -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    textEmpty: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit,
) {
    val size = 40.dp
    val iconSize = 24.dp
    val activeContainerColor = MaterialTheme.colorScheme.primaryContainer
    if (isLoading) {
        Box(
            modifier = modifier
                .padding(start = 4.dp)
                .size(size)
                .clip(CircleShape)
                .glassBackground(
                    style = GlassStyle.Dense,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = CircleShape,
                    borderWidth = 0.dp
                )
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineStopCircle,
                contentDescription = stringResource(R.string.ai_duel_stop),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(iconSize)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .glassBackground(
                    style = GlassStyle.Dense,
                    color = if (textEmpty) MaterialTheme.colorScheme.surfaceContainerHighest
                    else activeContainerColor,
                    shape = CircleShape,
                    borderWidth = 0.dp
                )
                .clickable(enabled = !textEmpty, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.ArrowUpward,
                contentDescription = stringResource(R.string.ai_chat_send),
                tint = if (textEmpty) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
