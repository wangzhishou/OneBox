package com.shifenmiao.model.ai

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Anthropic Messages API 请求模型
 *
 * 用于 Anthropic 兼容协议（如小米 MiMo）的请求格式。
 * 与 OpenAI Chat Completion 格式的主要区别：
 * 1. system 字段单独提取（不在 messages 数组中）
 * 2. messages 中的 content 支持数组格式（多模态）
 * 3. 使用 max_tokens 而非 max_completion_tokens
 */
@Parcelize
@Serializable
data class AnthropicMessagesRequest(
    @SerializedName("model")
    val model: String = "",

    @SerializedName("max_tokens")
    val maxTokens: Int = 4096,

    @SerializedName("system")
    val system: String? = null,

    @SerializedName("messages")
    val messages: List<AnthropicMessage> = emptyList(),

    @SerializedName("stream")
    val stream: Boolean = true,

    @SerializedName("temperature")
    val temperature: Double? = null,

    @SerializedName("top_p")
    val topP: Double? = null,

    @SerializedName("tools")
    val tools: List<AnthropicTool>? = null,

    @SerializedName("metadata")
    val metadata: AnthropicMetadata? = null
) : Parcelable

/**
 * Anthropic 消息格式
 *
 * content 可以是：
 * - String: 纯文本
 * - List<ContentBlock>: 多模态内容（文本、图片等）
 */
@Parcelize
@Serializable
data class AnthropicMessage(
    @SerializedName("role")
    val role: String,  // "user" 或 "assistant"

    @SerializedName("content")
    @Contextual
    val content: @RawValue Any  // String 或 List<ContentBlock>
) : Parcelable

/**
 * Anthropic 内容块
 *
 * 支持多种类型：
 * - text: 文本内容
 * - image: 图片内容（base64编码）
 * - tool_use: 工具调用（assistant 发出）
 * - tool_result: 工具执行结果（user 传回）
 */
@Parcelize
@Serializable
data class ContentBlock(
    @SerializedName("type")
    val type: String = "text",

    @SerializedName("text")
    val text: String? = null,

    @SerializedName("source")
    val source: ImageSource? = null,

    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("input")
    val input: @Contextual @RawValue Any? = null,

    @SerializedName("tool_use_id")
    val toolUseId: String? = null,

    @SerializedName("content")
    val toolContent: String? = null,

    @SerializedName("is_error")
    val isError: Boolean? = null
) : Parcelable

/**
 * Anthropic 图片源
 *
 * 支持两种类型：
 * - base64: { "type": "base64", "media_type": "image/jpeg", "data": "<base64>" }
 * - url:    { "type": "url", "url": "https://..." }
 */
@Parcelize
@Serializable
data class ImageSource(
    @SerializedName("type")
    val type: String = "base64",

    @SerializedName("media_type")
    val mediaType: String = "image/jpeg",

    @SerializedName("data")
    val data: String = "",

    @SerializedName("url")
    val url: String? = null
) : Parcelable

/**
 * Anthropic 工具定义
 *
 * 与 OpenAI 的 function tool 格式不同，Anthropic 使用：
 * - name: 工具名称
 * - description: 工具描述
 * - input_schema: JSON Schema 格式的参数定义
 */
@Parcelize
@Serializable
data class AnthropicTool(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("input_schema")
    val inputSchema: @Contextual @RawValue Any
) : Parcelable

/**
 * Anthropic 元数据
 */
@Parcelize
@Serializable
data class AnthropicMetadata(
    @SerializedName("user_id")
    val userId: String? = null
) : Parcelable

/**
 * Anthropic Messages API 响应模型
 */
@Parcelize
@Serializable
data class AnthropicMessagesResponse(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("type")
    val type: String = "message",

    @SerializedName("role")
    val role: String = "assistant",

    @SerializedName("content")
    val content: List<ContentBlock> = emptyList(),

    @SerializedName("model")
    val model: String = "",

    @SerializedName("stop_reason")
    val stopReason: String? = null,

    @SerializedName("stop_sequence")
    val stopSequence: String? = null,

    @SerializedName("usage")
    val usage: AnthropicUsage? = null
) : Parcelable

/**
 * Anthropic Usage 统计
 */
@Parcelize
@Serializable
data class AnthropicUsage(
    @SerializedName("input_tokens")
    val inputTokens: Int = 0,

    @SerializedName("output_tokens")
    val outputTokens: Int = 0
) : Parcelable

/**
 * Anthropic SSE 事件类型
 */
enum class AnthropicEventType(val value: String) {
    MESSAGE_START("message_start"),
    CONTENT_BLOCK_START("content_block_start"),
    CONTENT_BLOCK_DELTA("content_block_delta"),
    CONTENT_BLOCK_STOP("content_block_stop"),
    MESSAGE_DELTA("message_delta"),
    MESSAGE_STOP("message_stop"),
    PING("ping"),
    ERROR("error");

    companion object {
        fun fromValue(value: String): AnthropicEventType? {
            return entries.firstOrNull { it.value == value }
        }
    }
}

/**
 * Anthropic SSE 事件数据
 */
@Parcelize
@Serializable
data class AnthropicStreamEvent(
    @SerializedName("type")
    val type: String = "",

    @SerializedName("index")
    val index: Int? = null,

    @SerializedName("delta")
    val delta: AnthropicDelta? = null,

    @SerializedName("message")
    val message: AnthropicMessagesResponse? = null,

    @SerializedName("content_block")
    val contentBlock: ContentBlock? = null,

    @SerializedName("usage")
    val usage: AnthropicUsage? = null,

    @SerializedName("error")
    val error: AnthropicError? = null
) : Parcelable

/**
 * Anthropic Delta 内容
 *
 * 支持的 type：
 * - text_delta: 文本增量
 * - input_json_delta: 工具调用参数增量（tool_use 场景）
 */
@Parcelize
@Serializable
data class AnthropicDelta(
    @SerializedName("type")
    val type: String = "",

    @SerializedName("text")
    val text: String? = null,

    @SerializedName("stop_reason")
    val stopReason: String? = null,

    @SerializedName("partial_json")
    val partialJson: String? = null
) : Parcelable

/**
 * Anthropic 错误
 */
@Parcelize
@Serializable
data class AnthropicError(
    @SerializedName("type")
    val type: String = "",

    @SerializedName("message")
    val message: String = ""
) : Parcelable
