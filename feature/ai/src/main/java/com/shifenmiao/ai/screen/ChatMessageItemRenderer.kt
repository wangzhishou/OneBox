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
import com.shifenmiao.ai.component.RobotReasoningContent
import com.shifenmiao.ai.component.RobotReasoningHeader
import com.shifenmiao.ai.component.RobotSearchResultsBlock
import com.shifenmiao.ai.component.ToolCallHistoryCard
import com.shifenmiao.ai.component.UserAttachmentsBlock
import com.shifenmiao.ai.component.UserMessageBlock
import com.shifenmiao.ai.component.UserMessageContent
import com.shifenmiao.ai.component.UserMessageFooter
import com.shifenmiao.ai.component.UserMessageHeader
import com.shifenmiao.ai.component.UserMessageTextContent
import com.shifenmiao.ai.model.MessageUiModel
import com.wanbaohe.core.ui.widget.glass.GlassCardSegment
import com.wanbaohe.core.ui.widget.glass.flatGlassCardSegment
import com.wanbaohe.markdown.OneBoxMarkdownCodeActions

@Composable
internal fun RenderChatMessageItem(
    item: MessageUiModel,
    index: Int,
    codeActions: OneBoxMarkdownCodeActions,
    onUserContainerHeader: @Composable (MessageUiModel.UserContainerHeader) -> Unit = { UserMessageHeader(it) },
    onUserContainerFooter: @Composable (MessageUiModel.UserContainerFooter) -> Unit = { UserMessageFooter(it) },
    onUserVerticalSpace: @Composable (MessageUiModel.UserVerticalSpace) -> Unit = {
        Spacer(modifier = Modifier.fillMaxWidth().height(it.height))
    },
    onUserLoading: @Composable () -> Unit = { ChatLoadingIndicator() },
    onUserReasoningContent: @Composable (MessageUiModel.UserReasoningContent, OneBoxMarkdownCodeActions) -> Unit = { content, actions ->
        RobotReasoningContent(
            reasoningContent = content.toRobotReasoningContent(),
            codeActions = actions,
        )
    },
    onUserReasoningHeader: @Composable (MessageUiModel.UserReasoningHeader) -> Unit = {
        RobotReasoningHeader(reasoningHeader = it.toRobotReasoningHeader())
    },
    onRobotContainerHeader: @Composable (MessageUiModel.RobotContainerHeader) -> Unit = { RobotMessageHeader(it) },
    onRobotToolCallHistory: @Composable (MessageUiModel.RobotToolCallHistory) -> Unit = {
        ToolCallHistoryCard(toolCallsJson = it.toolCallsJson)
    },
    onRobotContainerFooter: @Composable (MessageUiModel.RobotContainerFooter, Int) -> Unit,
    onRobotLoading: @Composable () -> Unit = { ChatLoadingIndicator() },
    onRobotError: @Composable (MessageUiModel.RobotError) -> Unit,
) {
    when (item) {
        is MessageUiModel.UserAttachments -> UserAttachmentsBlock(userAttachments = item)
        is MessageUiModel.UserBlock -> UserMessageBlock(userBlock = item)
        is MessageUiModel.UserContainerHeader -> onUserContainerHeader(item)
        is MessageUiModel.UserContainerFooter -> onUserContainerFooter(item)
        is MessageUiModel.UserContent -> UserMessageContent(userContent = item, codeActions = codeActions)
        is MessageUiModel.UserTextContent -> UserMessageTextContent(userTextContent = item)
        is MessageUiModel.UserVerticalSpace -> onUserVerticalSpace(item)
        is MessageUiModel.UserLoading -> onUserLoading()
        is MessageUiModel.UserError -> UserMessageTextContent(userTextContent = item.toUserTextContent())
        is MessageUiModel.UserSearchResults -> RobotSearchResultsBlock(searchResults = item.toRobotSearchResults())
        is MessageUiModel.UserReasoningContent -> onUserReasoningContent(item, codeActions)
        is MessageUiModel.UserReasoningHeader -> onUserReasoningHeader(item)
        is MessageUiModel.RobotContainerHeader -> onRobotContainerHeader(item)
        is MessageUiModel.RobotSearchResults -> RobotSearchResultsBlock(searchResults = item)
        is MessageUiModel.RobotToolCallHistory -> onRobotToolCallHistory(item)
        is MessageUiModel.RobotContent -> RobotMessageContent(robotContent = item, codeActions = codeActions)
        is MessageUiModel.RobotVerticalSpace -> DefaultRobotVerticalSpace(item)
        is MessageUiModel.RobotReasoningContent -> RobotReasoningContent(
            reasoningContent = item,
            codeActions = codeActions,
        )
        is MessageUiModel.RobotReasoningHeader -> RobotReasoningHeader(reasoningHeader = item)
        is MessageUiModel.RobotContainerFooter -> onRobotContainerFooter(item, index)
        is MessageUiModel.RobotLoading -> onRobotLoading()
        is MessageUiModel.RobotError -> onRobotError(item)
    }
}

private fun MessageUiModel.UserError.toUserTextContent() = MessageUiModel.UserTextContent(
    id = id,
    text = errorMessage,
    isHighlighted = false,
    isFirst = true,
    isLast = true,
)

private fun MessageUiModel.UserSearchResults.toRobotSearchResults() = MessageUiModel.RobotSearchResults(
    id = id,
    searchResult = searchResult,
    isExpanded = isExpanded,
)

internal fun MessageUiModel.UserReasoningContent.toRobotReasoningContent() =
    MessageUiModel.RobotReasoningContent(
        id = id,
        reasoningSource = source,
        isStreaming = false,
        showCollapseFooter = showCollapseFooter,
    )

internal fun MessageUiModel.UserReasoningHeader.toRobotReasoningHeader() =
    MessageUiModel.RobotReasoningHeader(
        id = id,
        time = time,
        preview = preview,
        isStreaming = isStreaming,
    )

@Composable
private fun DefaultRobotVerticalSpace(item: MessageUiModel.RobotVerticalSpace) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(item.height)
            .flatGlassCardSegment(
                segment = GlassCardSegment.Middle,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
    )
}
