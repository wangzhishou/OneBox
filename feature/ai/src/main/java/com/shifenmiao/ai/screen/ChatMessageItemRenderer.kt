package com.shifenmiao.ai.screen

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifenmiao.ai.component.ChatLoadingIndicator
import com.shifenmiao.ai.component.RobotMessageContent
import com.shifenmiao.ai.component.RobotMessageHeader
import com.shifenmiao.ai.component.RobotMessageMarkdownBlock
import com.shifenmiao.ai.component.RobotReasoningBlock
import com.shifenmiao.ai.component.RobotReasoningContent
import com.shifenmiao.ai.component.RobotReasoningHeader
import com.shifenmiao.ai.component.RobotSearchResultsBlock
import com.shifenmiao.ai.component.ToolCallHistoryCard
import com.shifenmiao.ai.component.UserAttachmentsBlock
import com.shifenmiao.ai.component.UserMessageBlock
import com.shifenmiao.ai.component.UserMessageContent
import com.shifenmiao.ai.component.UserMessageFooter
import com.shifenmiao.ai.component.UserMessageHeader
import com.shifenmiao.ai.component.UserMessageMarkdownBlock
import com.shifenmiao.ai.component.UserMessageTextContent
import com.shifenmiao.ai.model.MessageUiModel
import com.t8rin.imagetoolbox.core.ui.widget.glass.GlassCardSegment
import com.t8rin.imagetoolbox.core.ui.widget.glass.glassCardSegment
import io.noties.markwon.plugins.codeblock.CodeBlockClickListener

/**
 * 扁平消息 item 的统一渲染入口。
 *
 * 目标不是把大消息重新收回一个巨型 item，而是保留现有 block 级 LazyColumn 性能模型，
 * 同时把屏幕层里冗长的 `when(item)` 分发逻辑收敛到一个可复用位置。
 */
@Composable
internal fun RenderChatMessageItem(
    item: MessageUiModel,
    index: Int,
    codeBlockClickListener: CodeBlockClickListener,
    // Header / footer 继续保留插槽，屏幕层只覆盖风格差异，不改扁平 item 结构。
    onUserContainerHeader: @Composable (MessageUiModel.UserContainerHeader) -> Unit = { UserMessageHeader(it) },
    onUserContainerFooter: @Composable (MessageUiModel.UserContainerFooter) -> Unit = { UserMessageFooter(it) },
    onUserVerticalSpace: @Composable (MessageUiModel.UserVerticalSpace) -> Unit = {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(it.height)
        )
    },
    onUserLoading: @Composable () -> Unit = { ChatLoadingIndicator() },
    onUserReasoningContent: @Composable (MessageUiModel.UserReasoningContent, CodeBlockClickListener) -> Unit = { reasoningContent, listener ->
        RobotReasoningContent(
            reasoningContent = reasoningContent.toRobotReasoningContent(),
            codeBlockClickListener = listener,
        )
    },
    onUserReasoningHeader: @Composable (MessageUiModel.UserReasoningHeader) -> Unit = {
        RobotReasoningHeader(reasoningHeader = it.toRobotReasoningHeader())
    },
    onUserReasoningBlock: @Composable (MessageUiModel.UserReasoningBlock, CodeBlockClickListener) -> Unit = { reasoningBlock, listener ->
        UserMessageMarkdownBlock(
            userMarkdownBlock = reasoningBlock.toUserMarkdownBlock(),
            codeBlockClickListener = listener
        )
    },
    onRobotContainerHeader: @Composable (MessageUiModel.RobotContainerHeader) -> Unit = { RobotMessageHeader(it) },
    onRobotToolCallHistory: @Composable (MessageUiModel.RobotToolCallHistory) -> Unit = {
        ToolCallHistoryCard(toolCallsJson = it.toolCallsJson)
    },
    // 机器人正文 footer 与 reasoning footer 不是一回事：正文 footer 仍独立成 item，
    // reasoning 多 block 的收口由最后一个 reasoning block 自己负责，避免多出无意义 Lazy item。
    onRobotContainerFooter: @Composable (MessageUiModel.RobotContainerFooter, Int) -> Unit,
    onRobotLoading: @Composable () -> Unit = { ChatLoadingIndicator() },
    onRobotError: @Composable (MessageUiModel.RobotError) -> Unit,
) {
    when (item) {
        is MessageUiModel.UserAttachments -> {
            UserAttachmentsBlock(userAttachments = item)
        }

        is MessageUiModel.UserBlock -> {
            UserMessageBlock(userBlock = item)
        }

        is MessageUiModel.UserContainerHeader -> {
            onUserContainerHeader(item)
        }

        is MessageUiModel.UserContainerFooter -> {
            onUserContainerFooter(item)
        }

        is MessageUiModel.UserContent -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            UserMessageContent(
                userContent = item,
                codeBlockClickListener = codeBlockClickListener
            )
        }

        is MessageUiModel.UserTextContent -> {
            UserMessageTextContent(userTextContent = item)
        }

        is MessageUiModel.UserMarkdownBlock -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            UserMessageMarkdownBlock(
                userMarkdownBlock = item,
                codeBlockClickListener = codeBlockClickListener
            )
        }

        is MessageUiModel.UserVerticalSpace -> {
            onUserVerticalSpace(item)
        }

        is MessageUiModel.UserLoading -> {
            onUserLoading()
        }

        is MessageUiModel.UserError -> {
            UserMessageTextContent(userTextContent = item.toUserTextContent())
        }

        is MessageUiModel.UserSearchResults -> {
            RobotSearchResultsBlock(searchResults = item.toRobotSearchResults())
        }

        is MessageUiModel.UserReasoningContent -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            onUserReasoningContent(item, codeBlockClickListener)
        }

        is MessageUiModel.UserReasoningHeader -> {
            onUserReasoningHeader(item)
        }

        is MessageUiModel.UserReasoningBlock -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            onUserReasoningBlock(item, codeBlockClickListener)
        }

        is MessageUiModel.RobotContainerHeader -> {
            onRobotContainerHeader(item)
        }

        is MessageUiModel.RobotSearchResults -> {
            RobotSearchResultsBlock(searchResults = item)
        }

        is MessageUiModel.RobotToolCallHistory -> {
            onRobotToolCallHistory(item)
        }

        is MessageUiModel.RobotContent -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            RobotMessageContent(
                robotContent = item,
                codeBlockClickListener = codeBlockClickListener
            )
        }

        is MessageUiModel.MarkdownBlock -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            RobotMessageMarkdownBlock(
                markdownBlock = item,
                codeBlockClickListener = codeBlockClickListener
            )
        }

        is MessageUiModel.RobotVerticalSpace -> {
            DefaultRobotVerticalSpace(item)
        }

        is MessageUiModel.RobotReasoningContent -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            RobotReasoningContent(
                reasoningContent = item,
                codeBlockClickListener = codeBlockClickListener
            )
        }

        is MessageUiModel.RobotReasoningHeader -> {
            RobotReasoningHeader(reasoningHeader = item)
        }

        is MessageUiModel.RobotReasoningBlock -> {
            codeBlockClickListener.isHighlighted = item.isHighlighted
            RobotReasoningBlock(
                reasoningBlock = item,
                codeBlockClickListener = codeBlockClickListener
            )
        }

        is MessageUiModel.RobotContainerFooter -> {
            onRobotContainerFooter(item, index)
        }

        is MessageUiModel.RobotLoading -> {
            onRobotLoading()
        }

        is MessageUiModel.RobotError -> {
            onRobotError(item)
        }
    }
}

private fun MessageUiModel.UserError.toUserTextContent(): MessageUiModel.UserTextContent {
    return MessageUiModel.UserTextContent(
        id = id,
        text = errorMessage,
        isHighlighted = false,
        isFirst = true,
        isLast = true
    )
}

private fun MessageUiModel.UserSearchResults.toRobotSearchResults(): MessageUiModel.RobotSearchResults {
    return MessageUiModel.RobotSearchResults(
        id = id,
        searchResult = searchResult,
        isExpanded = isExpanded
    )
}

internal fun MessageUiModel.UserReasoningContent.toRobotReasoningContent(): MessageUiModel.RobotReasoningContent {
    return MessageUiModel.RobotReasoningContent(
        id = id,
        reasoningAstNode = node,
        isHighlighted = isHighlighted,
        isStreaming = false
    )
}

internal fun MessageUiModel.UserReasoningHeader.toRobotReasoningHeader(): MessageUiModel.RobotReasoningHeader {
    return MessageUiModel.RobotReasoningHeader(
        id = id,
        time = time,
        preview = preview,
        isStreaming = isStreaming,
    )
}

internal fun MessageUiModel.UserReasoningBlock.toRobotReasoningBlock(): MessageUiModel.RobotReasoningBlock {
    return MessageUiModel.RobotReasoningBlock(
        id = id,
        node = node,
        isHighlighted = isHighlighted,
        isStreaming = false,
        blockType = blockType,
        isFirst = isFirst,
        isLast = isLast
    )
}

internal fun MessageUiModel.UserReasoningBlock.toUserMarkdownBlock(): MessageUiModel.UserMarkdownBlock {
    return MessageUiModel.UserMarkdownBlock(
        id = id,
        isHighlighted = isHighlighted,
        node = node,
        blockType = blockType,
        isFirst = isFirst,
        isLast = isLast
    )
}

@Composable
private fun DefaultRobotVerticalSpace(item: MessageUiModel.RobotVerticalSpace) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(item.height)
            .glassCardSegment(
                segment = GlassCardSegment.Middle,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
    )
}

