package com.shifenmiao.model.ai

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

enum class RoleType(val value: String) {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    FUNCTION("function"),
    TOOL("tool"),
}

@Parcelize
@Serializable
enum class FinishReason(val value: String) : Parcelable {
    STOP("stop"),
    LENGTH("length"),
    CONTENT_FILTER("content_filter"),
    TOOL_CALLS("tool_calls"),
    INSUFFICIENT_SYSTEM_RESOURCE("insufficient_system_resource"),
    SENSITIVE("sensitive"),
}

@Parcelize
@Serializable
data class ErrorResponse(
    @SerialName("error_code")
    val errorCode: Int,
    @SerialName("error_msg")
    val errorMsg: String,
    @SerialName("id")
    val id: String,
    val code: Int,
    val error: String,
    val message: String,
    val method: String,
    @SerialName("scode")
    val sCode: String,
    val status: Boolean,
    val ua: String,
    val url: String
) : Parcelable


@Parcelize
@Serializable
data class ChatCompletionRequest(
    @SerialName("messages")
    val messages: List<RequestMessage> = emptyList(),
    @SerialName("model")
    val model: String = "",
    @SerialName("frequency_penalty")
    val frequencyPenalty: Int? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("presence_penalty")
    val presencePenalty: Int? = null,
    @SerialName("response_format")
    val responseFormat: ResponseFormat? = null,
    @SerialName("stop")
    val stop: List<String>? = null,
    @SerialName("stream")
    val stream: Boolean = true,
    /**
     * 流式请求时要求上游在最后一帧返回 usage(OpenAI 标准字段)。
     * 缺省时上游不下发 usage, 客户端只能本地估算, 思考类模型的 reasoning token 会全部丢失。
     */
    @SerialName("stream_options")
    val streamOptions: StreamOptions? = null,
    @SerialName("temperature")
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null,
    @SerialName("tools")
    val tools: List<ToolDefinition>? = null,
    @SerialName("enable_web_search")
    val enableWebSearch: Boolean = false,
    @SerialName("reasoning")
    val reasoning: ReasoningOptions? = null,
    /**
     * 百度AI搜索增强选项
     */
    @SerialName("web_search")
    val webSearch: BaiduWebSearch? = null
) : Parcelable

@Parcelize
@Serializable
data class ReasoningOptions(
    @SerialName("effort")
    val effort: String = "medium",
    @SerialName("enabled")
    val enabled: Boolean? = null,
) : Parcelable

@Parcelize
@Serializable
data class StreamOptions(
    @SerialName("include_usage")
    val includeUsage: Boolean = true,
) : Parcelable

/**
 * 百度AI web_search 搜索增强选项
 * @param enable 是否开启实时搜索功能，默认false
 * @param enableCitation 是否开启上角标返回，默认false
 * @param enableTrace 是否返回搜索溯源信息，默认false
 * @param enableStatus 是否返回搜索信号，默认false
 * @param searchMode 联网搜索模式: auto(默认)/required
 * @param searchNumber 检索的文献数量，范围1-28
 * @param referenceNumber 用于给大模型总结的文献数量，范围1-28
 */
@Parcelize
@Serializable
data class BaiduWebSearch(
    @SerialName("enable")
    val enable: Boolean = false,
    @SerialName("enable_citation")
    val enableCitation: Boolean = false,
    @SerialName("enable_trace")
    val enableTrace: Boolean = false,
    @SerialName("enable_status")
    val enableStatus: Boolean = false,
    @SerialName("search_mode")
    val searchMode: String = "auto",
    @SerialName("search_number")
    val searchNumber: Int? = null,
    @SerialName("reference_number")
    val referenceNumber: Int? = null
) : Parcelable

@Parcelize
@Serializable
sealed class ResponseFormat : Parcelable {
    @Serializable
    @Parcelize
    data class Text(val value: String) : ResponseFormat()

    @Serializable
    @Parcelize
    data class Object(val value: Map<String, String>) : ResponseFormat()
}

@Parcelize
@Serializable
data class ChatCompletionChunk(
    @SerialName("id")
    var id: String? = "",
    @SerialName("object")
    var `object`: String = "chat.completion",
    @SerialName("created")
    var created: Long = 0L,
    @SerialName("model")
    var model: String = "",
    @SerialName("choices")
    var choices: List<ChunkChoice> = emptyList(),
    @SerialName("system_fingerprint")
    var systemFingerprint: String? = "",
    @SerialName("usage")
    var usage: Usage? = null,
    /**
     * 搜索结果 - 支持多种大模型 API 的搜索增强返回
     * 百度千帆: search_results / search_info / web_search
     * Perplexity: citations
     * 其他: web_search_results
     */
    @SerialName("search_results")
    var searchResults: List<SearchCitation>? = null,
    @SerialName("search_info")
    var searchInfo: SearchInfo? = null,
    @SerialName("error_code")
    /**
     * 以下是自定义新增字段
     */
    var errorCode: Int = 0,
    @SerialName("error_msg")
    var errorMsg: String = "",
    /**
     * 稳定的错误语义标识(网关 `error.code`,或客户端内部 NETWORK_OFFLINE 等)。
     * UI 按它选择错误卡片类型,不按文案分支。
     */
    @SerialName("error_code_name")
    var errorCodeName: String = "",
    @SerialName("is_end")
    var isEnd: Boolean = false,
) : Parcelable

/**
 * 百度千帆 search_info 格式
 */
@Parcelize
@Serializable
data class SearchInfo(
    @SerialName("search_results")
    val searchResults: List<BaiduSearchResult>? = null
) : Parcelable

/**
 * 百度千帆搜索结果格式
 */
@Parcelize
@Serializable
data class BaiduSearchResult(
    @SerialName("index")
    val index: Int = 0,
    @SerialName("url")
    val url: String = "",
    @SerialName("title")
    val title: String = "",
    @SerialName("datasource_id")
    val datasourceId: String = "",
    @SerialName("site_name")
    val siteName: String = "",
    @SerialName("content")
    val content: String = ""
) : Parcelable {
    /**
     * 转换为统一的 SearchCitation 格式
     */
    fun toSearchCitation(): SearchCitation {
        return SearchCitation(
            index = index,
            title = title,
            url = url,
            snippet = content,
            hostname = siteName
        )
    }
}

@Parcelize
@Serializable
data class ChunkChoice(
    @SerialName("index")
    var index: Int = 0,
    @SerialName("delta")
    val delta: Delta? = null,
    @SerialName("message")
    var message: Message? = null,
    @SerialName("finish_reason")
    var finishReason: String? = null,
    @SerialName("usage")
    var usage: Usage? = null,
) : Parcelable

@Parcelize
@Serializable
data class Delta(
    @SerialName("role")
    val role: String? = null,
    @SerialName("content")
    val content: String? = null,
    @SerialName("reasoning_content")
    val reasoningContent: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<ToolCallDelta>? = null,
) : Parcelable

/**
 * 搜索结果统一数据模型
 * 兼容各大模型 API 的 Web Search 结果格式（Perplexity/Bing/Google等）
 */
@Parcelize
@Serializable
data class SearchResult(
    @SerialName("query")
    val query: String = "",
    @SerialName("citations")
    val citations: List<SearchCitation> = emptyList(),
    @SerialName("search_time")
    val searchTime: Long = 0L
) : Parcelable {
    companion object {
        fun fromJson(json: String?): SearchResult? {
            return try {
                json?.let { com.shifenmiao.model.ModelProvider.AppJson.decodeFromString<SearchResult>(it) }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toJson(): String {
        return com.shifenmiao.model.ModelProvider.AppJson.encodeToString(this)
    }
}

/**
 * 搜索引用来源
 */
@Parcelize
@Serializable
data class SearchCitation(
    @SerialName("index")
    val index: Int = 0,
    @SerialName("title")
    val title: String = "",
    @SerialName("url")
    val url: String = "",
    @SerialName("snippet")
    val snippet: String = "",
    @SerialName("favicon")
    val favicon: String = "",
    @SerialName("hostname")
    val hostname: String = "",
    @SerialName("published_date")
    val publishedDate: String = ""
) : Parcelable



@Parcelize
@Serializable
data class Message(
    @SerialName("role")
    val role: String = "",
    @SerialName("content")
    val content: String? = null,
    @SerialName("reasoning_content")
    val reasoningContent: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<ToolCall>? = null
) : Parcelable

@Parcelize
@Serializable
data class ToolCall(
    @SerialName("id")
    val id: String = "",
    @SerialName("type")
    val type: String = "",
    @SerialName("function")
    val function: FunctionCall = FunctionCall()
) : Parcelable

@Parcelize
@Serializable
data class FunctionCall(
    @SerialName("name")
    val name: String = "",
    @SerialName("arguments")
    val arguments: String = ""
) : Parcelable

@Parcelize
@Serializable
data class LogProbs(
    @SerialName("content")
    val content: List<LogProbContent> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class LogProbContent(
    @SerialName("token")
    val token: String,
    @SerialName("logprob")
    val logprob: Double,
    @SerialName("bytes")
    val bytes: List<Int> = emptyList(),
    @SerialName("top_logprobs")
    val topLogProbs: List<TopLogProb> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class TopLogProb(
    @SerialName("token")
    val token: String,
    @SerialName("logprob")
    val logprob: Double,
    @SerialName("bytes")
    val bytes: List<Int> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class Usage(
    @SerialName("completion_tokens")
    val completionTokens: Int = 0,
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerialName("prompt_cache_hit_tokens")
    val promptCacheHitTokens: Int = 0,
    @SerialName("prompt_cache_miss_tokens")
    val promptCacheMissTokens: Int = 0,
    @SerialName("total_tokens")
    val totalTokens: Int = 0,
    @SerialName("completion_tokens_details")
    val completionTokensDetails: CompletionTokensDetails? = null
) : Parcelable

@Parcelize
@Serializable
data class CompletionTokensDetails(
    @SerialName("reasoning_tokens")
    val reasoningTokens: Int = 0
) : Parcelable
