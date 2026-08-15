package com.shifenmiao.ai.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.a2ui.LocalIsMessageStreaming
import com.halilibo.richtext.ui.material3.RichText
import com.shifenmiao.ai.model.MessageUiModel
import com.shifenmiao.ai.ui.AIUsageBar
import com.shifenmiao.base.provider.LocalDataDraftHelper
import com.shifenmiao.base.ui.AILoadingRow
import com.shifenmiao.base.ui.ExpandableMarkdownContent
import com.shifenmiao.base.ui.MarkdownContent
import com.shifenmiao.common.handle.LocalUrlNavigator
import com.shifenmiao.common.logic.AppComponent
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ListItemType
import com.shifenmiao.model.ai.AttachmentPayloadDto
import com.shifenmiao.model.image.ImageViewerInfo
import com.shifenmiao.model.state.LocalChatUIState
import com.shifenmiao.model.webview.WebViewParams
import com.shifenmiao.storage.AppSharedStorage
import com.shifenmiao.storage.RemoteConfigStorage
import com.shifenmiao.theme.AppTheme
import com.t8rin.imagetoolbox.core.ui.utils.navigation.LocalOnNavigate
import com.t8rin.imagetoolbox.core.ui.utils.navigation.Screen
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCardSegment
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassSurface
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassCardSegment
import com.t8rin.imagetoolbox.core.utils.appContext
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener
import kotlinx.coroutines.launch
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSettings
import com.t8rin.imagetoolbox.core.resources.icons.Refresh
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandLess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineExpandMore
import com.t8rin.imagetoolbox.core.resources.icons.line.LineSwitchAccess
import com.t8rin.imagetoolbox.core.resources.icons.line.LineLiveHelp

/**
 * 消息头部组件，显示用户或AI头像和角色信息
 */
@Composable
fun UserMessageBlock(
    userBlock: MessageUiModel.UserBlock
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .glassCardSegment(
                    segment = GlassCardSegment.Solo,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
        ) {
            if (userBlock.attachments.isNullOrEmpty().not()) {
                UserAttachmentsContent(userBlock.attachments)
            }
            Text(
                modifier = Modifier
                    .padding(12.dp),
                text = userBlock.text,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }

}

/**
 * 用户消息附件展示 — 图片缩略图网格 + 文件 chip。
 *
 * 接收预解析的 AttachmentPayloadDto 列表（由 MessageUiModel 创建时反序列化），
 * 图片类附件通过 Coil AsyncImage 显示缩略图（使用 localPath 或 uri），
 * 非图片附件显示文件名 chip。
 */
@Composable
fun UserAttachmentsBlock(
    userAttachments: MessageUiModel.UserAttachments
) {
    val attachments = userAttachments.attachments
    if (attachments.isEmpty()) return
    UserAttachmentsContent(attachments)
}

@Composable
fun UserAttachmentsContent(
    attachments: List<AttachmentPayloadDto>
) {
    val imageAttachments = attachments.filter { it.isImage }
    val fileAttachments = attachments.filter { !it.isImage }
    val imagePreviewNavigate = LocalOnNavigate.current
    Column(
        modifier = Modifier
            .padding(all = 12.dp),
        horizontalAlignment = Alignment.End
    ) {
        // 图片缩略图
        if (imageAttachments.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                reverseLayout = true,
                modifier = Modifier.padding(bottom = if (fileAttachments.isNotEmpty()) 4.dp else 0.dp)
            ) {
                items(imageAttachments, key = { it.uri }) { dto ->
                    val imageModel = remember(dto) {
                        dto.localPath?.let { path ->
                            val file = java.io.File(path)
                            if (file.exists()) file else null
                        } ?: dto.localContent?.takeIf { it.isNotBlank() }
                        ?: dto.uri.toUri()
                    }
                    AsyncImage(
                        model = imageModel,
                        contentDescription = dto.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                imagePreviewNavigate(
                                    Screen.ImageViewer(
                                        imageViewerInfo = ImageViewerInfo(
                                            images = listOf(imageModel.toString()),
                                            initialIndex = 0
                                        )
                                    )
                                )
                            }
                    )
                }
            }
        }

        // 非图片文件 chip
        if (fileAttachments.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                fileAttachments.forEach { dto ->
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = dto.name.ifBlank { "file" },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                            val sizeKB = dto.size / 1024
                            val sizeText =
                                if (sizeKB > 1024) "${"%.1f".format(sizeKB / 1024.0)}MB" else "${sizeKB}KB"
                            Text(
                                text = "${dto.mimeType} · $sizeText",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserMessageHeader(
    userContainerHeader: MessageUiModel.UserContainerHeader
) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Top,
                shape = RoundedCornerShape(16.dp, 0.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            )
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 6.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            val titleColor =
                if (userContainerHeader.modelSubtitle.isNotBlank()) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = userContainerHeader.modelName,
                style = MaterialTheme.typography.labelMedium,
                color = titleColor
            )
            if (userContainerHeader.modelSubtitle.isNotBlank()) {
                Text(
                    text = userContainerHeader.modelSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun UserMessageFooter(
    userContainerFooter: MessageUiModel.UserContainerFooter
) {
    Spacer(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .height(16.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Bottom,
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            )
    )
}

@Composable
fun UserMessageContent(
    userContent: MessageUiModel.UserContent,
    codeBlockClickListener: CodeBlockClickListener
) {
    RichText(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        CompositionLocalProvider(
            LocalIsMessageStreaming provides false
        ) {
            BasicMarkdown(
                astNode = userContent.node,
                codeBlockClickListener = codeBlockClickListener
            )
        }
    }
}

@Composable
fun UserMessageTextContent(userTextContent: MessageUiModel.UserTextContent) {
    val segment = when {
        userTextContent.isFirst && userTextContent.isLast -> GlassCardSegment.Solo
        userTextContent.isFirst -> GlassCardSegment.Top
        userTextContent.isLast -> GlassCardSegment.Bottom
        else -> GlassCardSegment.Middle
    }
    val shape = when {
        userTextContent.isFirst && userTextContent.isLast ->
            RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)

        userTextContent.isFirst ->
            RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)

        userTextContent.isLast ->
            RoundedCornerShape(0.dp, 0.dp, 0.dp, 12.dp)

        else -> RoundedCornerShape(0.dp)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            modifier = Modifier
                .glassCardSegment(
                    segment = segment,
                    shape = shape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            text = userTextContent.text,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun UserMessageMarkdownBlock(
    userMarkdownBlock: MessageUiModel.UserMarkdownBlock,
    codeBlockClickListener: CodeBlockClickListener
) {
    val verticalPadding = when (userMarkdownBlock.blockType) {
        MessageUiModel.MarkdownBlock.BlockType.HEADING -> 12.dp
        MessageUiModel.MarkdownBlock.BlockType.PARAGRAPH -> 4.dp
        MessageUiModel.MarkdownBlock.BlockType.CODE_BLOCK -> 8.dp
        MessageUiModel.MarkdownBlock.BlockType.THEMATIC_BREAK -> 16.dp
        else -> 8.dp
    }
    RichText(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = MaterialTheme.colorScheme.primaryContainer,
            )
            .padding(horizontal = 12.dp, vertical = verticalPadding)
    ) {
        CompositionLocalProvider(
            LocalIsMessageStreaming provides false
        ) {
            BasicMarkdown(
                astNode = userMarkdownBlock.node,
                codeBlockClickListener = codeBlockClickListener
            )
        }
    }
}

@Composable
fun RobotMessageHeader(robotContainerHeader: MessageUiModel.RobotContainerHeader) {
    Spacer(
        modifier = Modifier
            .padding(top = 2.dp)
            .fillMaxWidth()
            .height(18.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Top,
                shape = RoundedCornerShape(0.dp, 16.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .padding(top = 2.dp)
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(0.dp, 6.dp),
    ) {
        Column {
            val titleColor =
                if (robotContainerHeader.modelSubtitle.isNotBlank()) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            Text(
                text = robotContainerHeader.modelName,
                style = MaterialTheme.typography.labelMedium,
                color = titleColor
            )
            if (robotContainerHeader.modelSubtitle.isNotBlank()) {
                Text(
                    text = robotContainerHeader.modelSubtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
    )
}


@Composable
fun RobotMessageFooter(
    robotContainerFooter: MessageUiModel.RobotContainerFooter,
    index: Int,
    aiChatComponent: AIChatComponent,
    appComponent: AppComponent
) {
    val chatUIState = LocalChatUIState.current
    val messageEntity = robotContainerFooter.messageEntity
    val isShowBar by remember {
        derivedStateOf {
            chatUIState.showTokens && robotContainerFooter.showTokens && index == 0
        }
    }
    // 添加控制导出HTML加载对话框显示的状态
    var isExporting by remember { mutableStateOf(false) }

    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .height(12.dp)
            .glassCardSegment(
                segment = GlassCardSegment.Bottom,
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
    )
    val localNavigator = LocalUrlNavigator.current
    val dataDraftHelper = LocalDataDraftHelper.current
    val noteScope = rememberCoroutineScope()
    AnimatedVisibility(
        visible = isShowBar,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        AIUsageBar(
            modifier = Modifier.glassCardSegment(
                segment = GlassCardSegment.Middle,
                shape = RoundedCornerShape(0.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ),
            message = robotContainerFooter.messageEntity,
            onDelete = aiChatComponent::deleteMessage,
            reGenerate = {
                aiChatComponent.reGenerateMessage(robotContainerFooter.messageEntity.completionId)
            },
            isShowReGenerate = !robotContainerFooter.messageEntity.expired,
            onShare = {
                // 显示导出加载对话框
                isExporting = true
                aiChatComponent.exportChatHistory { html, aIgc ->
                    // 隐藏导出加载对话框
                    isExporting = false
                    appComponent.showWebView(
                        WebViewParams(
                            baseUrl = UrlConstants.WEB_VIEW_BASE_URL,
                            isHtml = true,
                            htmlData = html,
                            enableSlowWholeDocumentDraw = true,
                            enableCustomTouch = false,
                            aIgcInfo = aIgc
                        )
                    )
                }
            },
            onNoteAdd = {
                noteScope.launch {
                    val draftId = dataDraftHelper.createDraft(
                        draftType = ListItemType.NOTE.id,
                        data = messageEntity.answer
                    )
                    localNavigator.navigate(Screen.CreateNote(draftId = draftId))
                }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    // 显示导出HTML时的加载对话框
    if (isExporting) {
        AlertDialog(
            onDismissRequest = {
                isExporting = false
            },
            confirmButton = { },
            title = {
                Text(
                    text = stringResource(R.string.exporting_share),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        )
    }
}

@Composable
fun RobotMessageContent(
    robotContent: MessageUiModel.RobotContent,
    codeBlockClickListener: CodeBlockClickListener
) {
    if (robotContent.answerAstNode != null) {
        RichText(
            modifier = Modifier
                .fillMaxWidth()
                .glassCardSegment(
                    segment = GlassCardSegment.Middle,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                )
                .padding(horizontal = 12.dp)
        ) {
            CompositionLocalProvider(
                LocalIsMessageStreaming provides robotContent.isStreaming
            ) {
                BasicMarkdown(
                    astNode = robotContent.answerAstNode,
                    codeBlockClickListener = codeBlockClickListener,
                    showCursor = robotContent.showCursor
                )
            }
        }
    }
}

@Composable
fun RobotMessageMarkdownBlock(
    markdownBlock: MessageUiModel.MarkdownBlock,
    codeBlockClickListener: CodeBlockClickListener
) {
    val verticalPadding = when (markdownBlock.blockType) {
        MessageUiModel.MarkdownBlock.BlockType.HEADING -> 12.dp
        MessageUiModel.MarkdownBlock.BlockType.PARAGRAPH -> 4.dp
        MessageUiModel.MarkdownBlock.BlockType.CODE_BLOCK -> 8.dp
        MessageUiModel.MarkdownBlock.BlockType.THEMATIC_BREAK -> 16.dp
        else -> 8.dp
    }

    RichText(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .padding(horizontal = 12.dp, vertical = verticalPadding)
    ) {
        CompositionLocalProvider(
            LocalIsMessageStreaming provides markdownBlock.isStreaming
        ) {
            BasicMarkdown(
                astNode = markdownBlock.node,
                codeBlockClickListener = codeBlockClickListener,
                showCursor = markdownBlock.showCursor
            )
        }
    }
}

@Composable
fun RobotReasoningHeader(
    reasoningHeader: MessageUiModel.RobotReasoningHeader,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val isExpandedReasoningChat = AppSharedStorage.isExpandedReasoningChat.collectAsState()
    val expanded = isExpandedReasoningChat.value
    val isStreaming = reasoningHeader.isStreaming
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = backgroundColor,
            )
            .padding(
                start = 10.dp,
                end = 10.dp,
                top = 6.dp,
                bottom = if (expanded) 0.dp else 6.dp
            )
    ) {
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = if (expanded) {
                RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 0.dp,
                    bottomEnd = 0.dp
                )
            } else {
                RoundedCornerShape(16.dp)
            },
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            borderWidth = 0.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        AppSharedStorage.saveIsExpandedReasoningChat(!expanded)
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwitchAccess,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.ai_reasoning_time, reasoningHeader.time),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!expanded && reasoningHeader.preview.isNotBlank()) {
                        Text(
                            text = reasoningHeader.preview,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isStreaming) {
                                contentColor.copy(alpha = 0.85f)
                            } else {
                                contentColor
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    modifier = Modifier.size(9.dp),
                    imageVector = if (expanded) {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandLess
                    } else {
                        com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineExpandMore
                    },
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    }
}

@Composable
fun RobotReasoningContent(
    reasoningContent: MessageUiModel.RobotReasoningContent,
    codeBlockClickListener: CodeBlockClickListener,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    leftLine: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
) {
    if (reasoningContent.reasoningAstNode != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCardSegment(
                    segment = GlassCardSegment.Middle,
                    color = backgroundColor,
                )
                .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 6.dp)
        ) {
            GlassSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                borderWidth = 0.0.dp,
            ) {
                val lineWidth = 2.dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 14.dp, top = 2.dp, bottom = 12.dp)
                        .drawBehind {
                            val widthPx = lineWidth.toPx()
                            val topOffsetPx = 2.dp.toPx()
                            drawRoundRect(
                                color = leftLine,
                                topLeft = Offset(0f, topOffsetPx),
                                size = Size(
                                    widthPx,
                                    (size.height - topOffsetPx).coerceAtLeast(0f)
                                ),
                                cornerRadius = CornerRadius(
                                    x = widthPx / 2,
                                    y = widthPx / 2
                                )
                            )
                        }
                ) {
                    RichText(
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        ),
                        style = RichTextStyle(paragraphSpacing = 0.sp),
                        contentColor = contentColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp)
                    ) {
                        CompositionLocalProvider(
                            LocalIsMessageStreaming provides reasoningContent.isStreaming
                        ) {
                            BasicMarkdown(
                                reasoningContent.reasoningAstNode,
                                codeBlockClickListener = codeBlockClickListener,
                                compactBlocks = true
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RobotReasoningBlock(
    reasoningBlock: MessageUiModel.RobotReasoningBlock,
    codeBlockClickListener: CodeBlockClickListener,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    leftLine: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),
) {
    // 多 block reasoning 视觉上应表现为“一个连续 section”：
    // - 中间 block 不收底部圆角；
    // - 只有最后一个 block 负责底部收口；
    // - 左侧竖线在所有 block 中连续贯通，而不是每段都像独立小卡片。
    val containerBottomPadding = if (reasoningBlock.isLast) 6.dp else 0.dp
    val surfaceShape = when {
        reasoningBlock.isFirst && reasoningBlock.isLast -> RoundedCornerShape(
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        reasoningBlock.isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }
    val contentTopPadding = if (reasoningBlock.isFirst) 6.dp else 2.dp
    val contentBottomPadding = if (reasoningBlock.isLast) 14.dp else 2.dp
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = backgroundColor,
            )
            .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = containerBottomPadding)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .glassCardSegment(
                    segment = GlassCardSegment.Middle,
                    color = backgroundColor,
                    shape = surfaceShape,
                ),
        ) {
            val lineWidth = 2.dp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 14.dp)
                    .drawBehind {
                        val widthPx = lineWidth.toPx()
                        drawRoundRect(
                            color = leftLine,
                            topLeft = Offset.Zero,
                            size = Size(widthPx, size.height),
                            cornerRadius = CornerRadius(
                                x = widthPx / 2,
                                y = widthPx / 2
                            )
                        )
                    }
            ) {
                RichText(
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    ),
                    style = RichTextStyle(paragraphSpacing = 0.sp),
                    contentColor = contentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            top = contentTopPadding,
                            bottom = contentBottomPadding
                        )
                ) {
                    CompositionLocalProvider(
                        LocalIsMessageStreaming provides reasoningBlock.isStreaming
                    ) {
                        BasicMarkdown(
                            reasoningBlock.node,
                            codeBlockClickListener = codeBlockClickListener,
                            compactBlocks = true
                        )
                    }
                }
            }
        }
    }
}

/**
 * 直连模式下的错误展示组件：以原始 HTTP 调试信息形式显示在助手消息卡片内。
 * 与代理协议的 [ChatErrorMessage] 错误卡片区分，直连错误不提供重试/切换等操作按钮。
 */
@Composable
fun RobotDirectErrorContent(
    errorMessage: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor,
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
            shape = RoundedCornerShape(12.dp)
        ) {
            SelectionContainer {
                Text(
                    text = errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(scrollState)
                        .padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun ChatLoadingIndicator(
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = backgroundColor,
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        LoadingStateContent()
    }
}

@Composable
fun ChatErrorMessage(
    appComponent: AppComponent,
    aiChatComponent: AIChatComponent,
    errorMessage: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            .padding(horizontal = 12.dp)
    ) {
        ErrorStateContent(
            errorMessage = errorMessage,
            onRetry = {
                aiChatComponent.retryChat()
            },
            onAISetting = {
                appComponent.showAIChatSettings()
            },
            onAISwitch = {
                appComponent.showAIModelsModalSheet()
            },
            onHelp = {
                appComponent.hideAIChat()
                RemoteConfigStorage.getRemoteConfig().helpBlogIds?.let { helpBlogIds ->
                    if (helpBlogIds.isEmpty()) {
                        return@let
                    }
                    val blogId = helpBlogIds
                        .getValue(
                            "AISetting"
                        )
                    appComponent.showBlogModalBottomSheet(blogId)
                }
            }
        )
    }
}

@Composable
private fun EmptyChatMessage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_messages),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun LoadingStateContent() {
    AILoadingRow()
}

@Composable
private fun SuccessStateContent(
    answer: String,
    reasoningContent: String
) {
    if (reasoningContent.isNotEmpty()) {
        ExpandableMarkdownContent(reasoningContent)
    }
    Spacer(modifier = Modifier.height(4.dp))
    MarkdownContent(message = answer)
}


@Composable
fun ErrorStateContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onAISetting: () -> Unit,
    onAISwitch: () -> Unit,
    onHelp: () -> Unit
) {
    Spacer(modifier = Modifier.padding(top = AppTheme.dimens.paddingNormal))
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            space = AppTheme.dimens.paddingNormal,
            alignment = Alignment.Top
        ),
    ) {
        Text(
            text = stringResource(id = R.string.ai_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppTheme.dimens.paddingNormal),
            color = MaterialTheme.colorScheme.errorContainer,
            shape = AppTheme.shapes.getSmallShape()
        ) {
            SelectionContainer {
                MarkdownContent(
                    message = errorMessage,
                    paddingValues = PaddingValues(AppTheme.dimens.paddingNormal)
                )
            }
        }
        Text(
            text = stringResource(id = R.string.ai_error_intro),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(id = R.string.ai_error_network),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(id = R.string.ai_error_busy),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(id = R.string.ai_error_content),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = stringResource(id = R.string.ai_error_length),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

    }
    Spacer(modifier = Modifier.padding(top = AppTheme.dimens.paddingLarge))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = AppTheme.dimens.paddingNormal)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        TextButton(
            onClick = {
                onAISetting()
            },
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSettings,
                modifier = Modifier.size(12.dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.settings_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = {
                onAISwitch()
            },
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineSwitchAccess,
                modifier = Modifier.size(12.dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.select),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = onRetry,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.Refresh,
                modifier = Modifier.size(12.dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.button_retry),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(
            onClick = onHelp,
        ) {
            Icon(
                imageVector = com.t8rin.imagetoolbox.core.resources.Icons.Outlined.LineLiveHelp,
                modifier = Modifier.size(12.dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.button_help),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
    Spacer(modifier = Modifier.padding(top = AppTheme.dimens.paddingNormal))
}
