package com.shifenmiao.model.ai.unified

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.shifenmiao.model.ai.ContentItem
import com.shifenmiao.model.ai.ImageUrl
import com.shifenmiao.model.ai.ListOrStringContent
import com.shifenmiao.model.ai.RequestMessage
import com.shifenmiao.model.ai.RoleType
import com.shifenmiao.model.ai.SearchCitation
import com.shifenmiao.model.ai.SearchInfo
import com.shifenmiao.model.ai.ToolCall
import com.shifenmiao.model.ai.ToolCallDelta
import com.shifenmiao.model.ai.ToolDefinition
import com.shifenmiao.model.ai.Usage
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * 协议无关的 LLM 请求模型。
 *
 * 设计目标：
 * 1. UI / Agent Loop 只依赖这一层，不再直接依赖 Chat Completions / Responses 的细节。
 * 2. Provider Adapter 负责把该模型转换为具体协议请求。
 */
@Parcelize
@Serializable
data class LlmTurnRequest(
    val model: String,
    val stream: Boolean = true,
    val messages: List<LlmMessage> = emptyList(),
    val tools: List<ToolDefinition>? = null,
    val builtinTools: Set<LlmBuiltinTool> = emptySet(),
    val reasoningEnabled: Boolean = false,
    val previousResponseId: String? = null,
) : Parcelable {
    val webSearchEnabled: Boolean
        get() = builtinTools.contains(LlmBuiltinTool.WEB_SEARCH)
}

/**
 * 内建能力开关，供不同协议 Adapter 各自映射。
 * 例如：
 * - Chat Completions: enable_web_search / web_search
 * - Responses API: web_search_preview tool
 */
enum class LlmBuiltinTool {
    WEB_SEARCH
}

@Parcelize
@Serializable
data class LlmMessage(
    val role: String,
    val parts: List<LlmContentPart> = emptyList(),
    val reasoningContent: String? = null,
    val toolCalls: List<ToolCall> = emptyList(),
    val toolCallId: String? = null,
    val name: String? = null,
) : Parcelable {
    companion object {
        fun createTextMessage(role: String, text: String, reasoningContent: String? = null): LlmMessage {
            return LlmMessage(
                role = role,
                parts = listOf(LlmContentPart.Text(text)),
                reasoningContent = reasoningContent?.takeIf { it.isNotBlank() }
            )
        }

        fun createAssistantToolCallMessage(
            toolCalls: List<ToolCall>,
            content: String? = null,
            reasoningContent: String? = null
        ): LlmMessage {
            return LlmMessage(
                role = RoleType.ASSISTANT.value,
                parts = content?.takeIf { it.isNotBlank() }?.let { listOf(LlmContentPart.Text(it)) }
                    ?: emptyList(),
                reasoningContent = reasoningContent?.takeIf { it.isNotBlank() },
                toolCalls = toolCalls
            )
        }

        fun createToolResultMessage(
            toolCallId: String,
            toolName: String,
            content: String,
            imageUrls: List<String> = emptyList(),
        ): LlmMessage {
            val parts = buildList<LlmContentPart> {
                if (content.isNotEmpty()) add(LlmContentPart.Text(content))
                imageUrls.forEach { url -> add(LlmContentPart.ImageUrlPart(url)) }
            }
            return LlmMessage(
                role = RoleType.TOOL.value,
                parts = parts,
                toolCallId = toolCallId,
                name = toolName
            )
        }
    }

    fun textContent(): String {
        return parts.filterIsInstance<LlmContentPart.Text>()
            .joinToString("\n") { it.text }
    }

    fun toRequestMessage(): RequestMessage {
        val content = when {
            parts.isEmpty() -> null
            parts.size == 1 && parts.first() is LlmContentPart.Text -> {
                ListOrStringContent.StringContent((parts.first() as LlmContentPart.Text).text)
            }
            else -> {
                ListOrStringContent.ListContent(parts.mapNotNull { part ->
                    when (part) {
                        is LlmContentPart.Text -> ContentItem.TextContent(text = part.text)
                        is LlmContentPart.ImageUrlPart -> ContentItem.ImageContent(
                            imageUrl = ImageUrl(url = part.url)
                        )
                    }
                })
            }
        }
        return RequestMessage(
            role = role,
            content = content,
            reasoningContent = reasoningContent,
            toolCallId = toolCallId,
            name = name,
            toolCalls = toolCalls.takeIf { it.isNotEmpty() }
        )
    }
}

fun RequestMessage.toLlmMessage(): LlmMessage {
    val parts = when (val messageContent = content) {
        is ListOrStringContent.StringContent -> listOf(LlmContentPart.Text(messageContent.content))
        is ListOrStringContent.ListContent -> messageContent.items.mapNotNull { item ->
            when (item) {
                is ContentItem.TextContent -> LlmContentPart.Text(item.text)
                is ContentItem.ImageContent -> LlmContentPart.ImageUrlPart(item.imageUrl.url)
            }
        }
        null -> emptyList()
    }
    return LlmMessage(
        role = role,
        parts = parts,
        reasoningContent = reasoningContent,
        toolCalls = toolCalls ?: emptyList(),
        toolCallId = toolCallId,
        name = name
    )
}

@Parcelize
@Serializable
sealed class LlmContentPart : Parcelable {
    @Parcelize
    @Serializable
    data class Text(@SerializedName("text") val text: String) : LlmContentPart()

    @Parcelize
    @Serializable
    data class ImageUrlPart(@SerializedName("url") val url: String) : LlmContentPart()
}

/**
 * 协议无关的流式事件。
 * UI / Agent Loop / 持久化只消费该层，底层协议差异由 Adapter 层吸收。
 */
@Serializable
sealed class LlmStreamEvent {
    @Serializable
    data class ResponseStarted(
        val responseId: String,
        val model: String? = null,
    ) : LlmStreamEvent()

    @Serializable
    data class TextDelta(val text: String) : LlmStreamEvent()

    @Serializable
    data class ReasoningDelta(val text: String) : LlmStreamEvent()

    @Serializable
    data class ToolCallDeltaEvent(val deltas: List<ToolCallDelta>) : LlmStreamEvent()

    @Serializable
    data class UsageUpdated(val usage: Usage) : LlmStreamEvent()

    @Serializable
    data class SearchResultsEvent(
        val searchResults: List<SearchCitation>? = null,
        val searchInfo: SearchInfo? = null,
    ) : LlmStreamEvent()

    @Serializable
    data class Completed(
        val responseId: String? = null,
        val finishReason: String? = null,
        val outputItemsJson: String? = null,
    ) : LlmStreamEvent()

    @Serializable
    data class Error(
        val errorCode: Int = 1,
        val errorMessage: String,
    ) : LlmStreamEvent()
}

