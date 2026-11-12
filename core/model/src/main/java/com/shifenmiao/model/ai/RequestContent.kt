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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ContentType(val value: String) {
    TEXT("text"),
    IMAGE_URL("image_url"),
    FILE("file"),
    MIXED("mixed")
}

@Parcelize
@Serializable
data class RequestMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: ListOrStringContent? = null, // nullable: assistant tool_calls 消息可能没有 content
    @SerialName("reasoning_content")
    val reasoningContent: String? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
    @SerialName("name")
    val name: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<ToolCall>? = null
) : Parcelable {
    companion object {
        /** 创建普通文本消息 */
        fun createTextMessage(role: String, text: String, reasoningContent: String? = null): RequestMessage {
            return RequestMessage(
                role = role,
                content = ListOrStringContent.StringContent(text),
                reasoningContent = reasoningContent?.takeIf { it.isNotBlank() }
            )
        }

        /** 创建多模态内容消息 */
        fun createMultiContentMessage(role: String, contentItems: List<ContentItem>): RequestMessage {
            return RequestMessage(role = role, content = ListOrStringContent.ListContent(contentItems))
        }

        /**
         * 创建 assistant 的 tool_calls 回传消息。
         * 用于 Agent Loop 中将 LLM 返回的 tool_calls 加入上下文。
         *
         * @param toolCalls 工具调用列表
         * @param content LLM 同时输出的文本内容（如 "我来帮你搜索一下"），可能为空
         */
        fun createAssistantToolCallMessage(
            toolCalls: List<ToolCall>,
            content: String? = null,
            reasoningContent: String? = null
        ): RequestMessage {
            return RequestMessage(
                role = RoleType.ASSISTANT.value,
                content = content?.takeIf { it.isNotBlank() }?.let { ListOrStringContent.StringContent(it) },
                reasoningContent = reasoningContent?.takeIf { it.isNotBlank() },
                toolCalls = toolCalls
            )
        }

        /**
         * 创建 tool 角色的执行结果消息。
         * @param toolCallId 对应 ToolCall.id
         * @param toolName 工具函数名
         * @param content 工具执行结果文本
         */
        fun createToolResultMessage(
            toolCallId: String,
            toolName: String,
            content: String
        ): RequestMessage {
            return RequestMessage(
                role = RoleType.TOOL.value,
                content = ListOrStringContent.StringContent(content),
                toolCallId = toolCallId,
                name = toolName
            )
        }
    }
}

@Parcelize
@Serializable(ListOrStringContentKSerializer::class)
sealed class ListOrStringContent : Parcelable {
    @Parcelize
    @Serializable
    data class StringContent(val content: String) : ListOrStringContent()

    @Parcelize
    @Serializable
    data class ListContent(val items: List<ContentItem>) : ListOrStringContent()
}

// 用于表示单个内容项
@Parcelize
@Serializable
sealed class ContentItem : Parcelable {
    @Parcelize
    @Serializable
    data class TextContent(
        @SerialName("text")
        val text: String,
        @SerialName("type")
        val type: String = ContentType.TEXT.value
    ) : ContentItem()

    @Parcelize
    @Serializable
    data class ImageContent(
        @SerialName("image_url")
        val imageUrl: ImageUrl,
        @SerialName("type")
        val type: String = ContentType.IMAGE_URL.value
    ) : ContentItem()
}

@Parcelize
@Serializable
data class ImageUrl(
    @SerialName("url")
    val url: String = ""
) : Parcelable