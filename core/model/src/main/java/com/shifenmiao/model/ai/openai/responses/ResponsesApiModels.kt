package com.shifenmiao.model.ai.openai.responses

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.shifenmiao.model.ai.ReasoningOptions

/**
 * OpenAI Responses API 请求模型。
 * 这里仅承载请求侧稳定结构；流式事件解析放在 Adapter 中完成，避免上层依赖 provider 事件细节。
 */
data class ResponsesApiRequest(
    @SerializedName("model")
    val model: String,
    @SerializedName("input")
    val input: List<ResponsesApiInputItem> = emptyList(),
    @SerializedName("stream")
    val stream: Boolean = true,
    /**
     * Responses API 同时支持 function tool 与内建 tool（如 web_search_preview），
     * 这里统一转成 JsonObject，避免 Any/Parcelize 序列化问题。
     */
    @SerializedName("tools")
    val tools: List<JsonObject>? = null,
    @SerializedName("reasoning")
    val reasoning: ReasoningOptions? = null,
    @SerializedName("previous_response_id")
    val previousResponseId: String? = null,
)

sealed class ResponsesApiInputItem {
    data class Message(
        @SerializedName("type") val type: String = "message",
        @SerializedName("role") val role: String,
        @SerializedName("content") val content: List<ResponsesApiContentItem>
    ) : ResponsesApiInputItem()

    data class FunctionCall(
        @SerializedName("type") val type: String = "function_call",
        @SerializedName("call_id") val callId: String,
        @SerializedName("name") val name: String,
        @SerializedName("arguments") val arguments: String,
    ) : ResponsesApiInputItem()

    data class FunctionCallOutput(
        @SerializedName("type") val type: String = "function_call_output",
        @SerializedName("call_id") val callId: String,
        @SerializedName("output") val output: String,
    ) : ResponsesApiInputItem()
}

sealed class ResponsesApiContentItem {
    data class InputText(
        @SerializedName("type") val type: String = "input_text",
        @SerializedName("text") val text: String,
    ) : ResponsesApiContentItem()

    data class InputImage(
        @SerializedName("type") val type: String = "input_image",
        @SerializedName("image_url") val imageUrl: String,
    ) : ResponsesApiContentItem()
}

/**
 * 内建 web search tool 的轻量占位对象。
 */
data class ResponsesWebSearchTool(
    @SerializedName("type") val type: String = "web_search_preview"
)

