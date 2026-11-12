/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.shifenmiao.model.ai

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
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
    @SerialName("model")
    val model: String = "",

    @SerialName("max_tokens")
    val maxTokens: Int = 4096,

    @SerialName("system")
    val system: String? = null,

    @SerialName("messages")
    val messages: List<AnthropicMessage> = emptyList(),

    @SerialName("stream")
    val stream: Boolean = true,

    @SerialName("temperature")
    val temperature: Double? = null,

    @SerialName("top_p")
    val topP: Double? = null,

    @SerialName("tools")
    val tools: List<AnthropicTool>? = null,

    @SerialName("metadata")
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
    @SerialName("role")
    val role: String,  // "user" 或 "assistant"

    @SerialName("content")
    @Serializable(with = AnthropicAnySerializer::class)
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
    @SerialName("type")
    val type: String = "text",

    @SerialName("text")
    val text: String? = null,

    @SerialName("source")
    val source: ImageSource? = null,

    @SerialName("id")
    val id: String? = null,

    @SerialName("name")
    val name: String? = null,

    @SerialName("input")
    @Serializable(with = AnthropicAnySerializer::class)
    val input: @RawValue Any? = null,

    @SerialName("tool_use_id")
    val toolUseId: String? = null,

    @SerialName("content")
    val toolContent: String? = null,

    @SerialName("is_error")
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
    @SerialName("type")
    val type: String = "base64",

    @SerialName("media_type")
    val mediaType: String = "image/jpeg",

    @SerialName("data")
    val data: String = "",

    @SerialName("url")
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
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String = "",

    @SerialName("input_schema")
    val inputSchema: ToolParameters = ToolParameters()
) : Parcelable

/**
 * Anthropic 元数据
 */
@Parcelize
@Serializable
data class AnthropicMetadata(
    @SerialName("user_id")
    val userId: String? = null
) : Parcelable

/**
 * Anthropic Messages API 响应模型
 */
@Parcelize
@Serializable
data class AnthropicMessagesResponse(
    @SerialName("id")
    val id: String = "",

    @SerialName("type")
    val type: String = "message",

    @SerialName("role")
    val role: String = "assistant",

    @SerialName("content")
    val content: List<ContentBlock> = emptyList(),

    @SerialName("model")
    val model: String = "",

    @SerialName("stop_reason")
    val stopReason: String? = null,

    @SerialName("stop_sequence")
    val stopSequence: String? = null,

    @SerialName("usage")
    val usage: AnthropicUsage? = null
) : Parcelable

/**
 * Anthropic Usage 统计
 */
@Parcelize
@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens")
    val inputTokens: Int = 0,

    @SerialName("output_tokens")
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
    @SerialName("type")
    val type: String = "",

    @SerialName("index")
    val index: Int? = null,

    @SerialName("delta")
    val delta: AnthropicDelta? = null,

    @SerialName("message")
    val message: AnthropicMessagesResponse? = null,

    @SerialName("content_block")
    val contentBlock: ContentBlock? = null,

    @SerialName("usage")
    val usage: AnthropicUsage? = null,

    @SerialName("error")
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
    @SerialName("type")
    val type: String = "",

    @SerialName("text")
    val text: String? = null,

    @SerialName("stop_reason")
    val stopReason: String? = null,

    @SerialName("partial_json")
    val partialJson: String? = null
) : Parcelable

/**
 * Anthropic 错误
 */
@Parcelize
@Serializable
data class AnthropicError(
    @SerialName("type")
    val type: String = "",

    @SerialName("message")
    val message: String = ""
) : Parcelable
