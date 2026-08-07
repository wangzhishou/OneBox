package com.shifenmiao.model.ai

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

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
    @SerializedName("error_code")
    val errorCode: Int,
    @SerializedName("error_msg")
    val errorMsg: String,
    @SerializedName("id")
    val id: String,
    val code: Int,
    val error: String,
    val message: String,
    val method: String,
    @SerializedName("scode")
    val sCode: String,
    val status: Boolean,
    val ua: String,
    val url: String
) : Parcelable


@Parcelize
@Serializable
data class ChatCompletionRequest(
    @SerializedName("messages")
    val messages: List<RequestMessage> = emptyList(),
    @SerializedName("model")
    val model: String = "",
    @SerializedName("frequency_penalty")
    val frequencyPenalty: Int? = null,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null,
    @SerializedName("presence_penalty")
    val presencePenalty: Int? = null,
    @SerializedName("response_format")
    val responseFormat: ResponseFormat? = null,
    @SerializedName("stop")
    val stop: List<String>? = null,
    @SerializedName("stream")
    val stream: Boolean = true,
    @SerializedName("temperature")
    val temperature: Double? = null,
    @SerializedName("top_p")
    val topP: Double? = null,
    @SerializedName("tools")
    val tools: List<ToolDefinition>? = null,
    @SerializedName("enable_web_search")
    val enableWebSearch: Boolean = false,
    @SerializedName("reasoning")
    val reasoning: ReasoningOptions? = null,
    /**
     * 百度AI搜索增强选项
     */
    @SerializedName("web_search")
    val webSearch: BaiduWebSearch? = null
) : Parcelable

@Parcelize
@Serializable
data class ReasoningOptions(
    @SerializedName("effort")
    val effort: String = "medium",
    @SerializedName("enabled")
    val enabled: Boolean? = null,
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
    @SerializedName("enable")
    val enable: Boolean = false,
    @SerializedName("enable_citation")
    val enableCitation: Boolean = false,
    @SerializedName("enable_trace")
    val enableTrace: Boolean = false,
    @SerializedName("enable_status")
    val enableStatus: Boolean = false,
    @SerializedName("search_mode")
    val searchMode: String = "auto",
    @SerializedName("search_number")
    val searchNumber: Int? = null,
    @SerializedName("reference_number")
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
    @SerializedName("id")
    var id: String? = "",
    @SerializedName("object")
    var `object`: String = "chat.completion",
    @SerializedName("created")
    var created: Long = 0L,
    @SerializedName("model")
    var model: String = "",
    @SerializedName("choices")
    var choices: List<ChunkChoice> = emptyList(),
    @SerializedName("system_fingerprint")
    var systemFingerprint: String? = "",
    @SerializedName("usage")
    var usage: Usage? = null,
    /**
     * 搜索结果 - 支持多种大模型 API 的搜索增强返回
     * 百度千帆: search_results / search_info / web_search
     * Perplexity: citations
     * 其他: web_search_results
     */
    @SerializedName("search_results")
    var searchResults: List<SearchCitation>? = null,
    @SerializedName("search_info")
    var searchInfo: SearchInfo? = null,
    @SerializedName("error_code")
    /**
     * 以下是自定义新增字段
     */
    var errorCode: Int = 0,
    @SerializedName("error_msg")
    var errorMsg: String = "",
    @SerializedName("is_end")
    var isEnd: Boolean = false,
) : Parcelable

/**
 * 百度千帆 search_info 格式
 */
@Parcelize
@Serializable
data class SearchInfo(
    @SerializedName("search_results")
    val searchResults: List<BaiduSearchResult>? = null
) : Parcelable

/**
 * 百度千帆搜索结果格式
 */
@Parcelize
@Serializable
data class BaiduSearchResult(
    @SerializedName("index")
    val index: Int = 0,
    @SerializedName("url")
    val url: String = "",
    @SerializedName("title")
    val title: String = "",
    @SerializedName("datasource_id")
    val datasourceId: String = "",
    @SerializedName("site_name")
    val siteName: String = "",
    @SerializedName("content")
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
    @SerializedName("index")
    var index: Int = 0,
    @SerializedName("delta")
    val delta: Delta? = null,
    @SerializedName("message")
    var message: Message? = null,
    @SerializedName("finish_reason")
    var finishReason: String? = null,
    @SerializedName("usage")
    var usage: Usage? = null,
) : Parcelable

@Parcelize
@Serializable
data class Delta(
    @SerializedName("role")
    val role: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("reasoning_content")
    val reasoningContent: String? = null,
    @SerializedName("tool_calls")
    val toolCalls: List<ToolCallDelta>? = null,
) : Parcelable

/**
 * 搜索结果统一数据模型
 * 兼容各大模型 API 的 Web Search 结果格式（Perplexity/Bing/Google等）
 */
@Parcelize
@Serializable
data class SearchResult(
    @SerializedName("query")
    val query: String = "",
    @SerializedName("citations")
    val citations: List<SearchCitation> = emptyList(),
    @SerializedName("search_time")
    val searchTime: Long = 0L
) : Parcelable {
    companion object {
        fun fromJson(json: String?): SearchResult? {
            return try {
                json?.let { com.google.gson.Gson().fromJson(it, SearchResult::class.java) }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toJson(): String {
        return com.google.gson.Gson().toJson(this)
    }
}

/**
 * 搜索引用来源
 */
@Parcelize
@Serializable
data class SearchCitation(
    @SerializedName("index")
    val index: Int = 0,
    @SerializedName("title")
    val title: String = "",
    @SerializedName("url")
    val url: String = "",
    @SerializedName("snippet")
    val snippet: String = "",
    @SerializedName("favicon")
    val favicon: String = "",
    @SerializedName("hostname")
    val hostname: String = "",
    @SerializedName("published_date")
    val publishedDate: String = ""
) : Parcelable



@Parcelize
@Serializable
data class Message(
    @SerializedName("role")
    val role: String,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("reasoning_content")
    val reasoningContent: String? = null,
    @SerializedName("tool_calls")
    val toolCalls: List<ToolCall>? = null
) : Parcelable

@Parcelize
@Serializable
data class ToolCall(
    @SerializedName("id")
    val id: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("function")
    val function: FunctionCall
) : Parcelable

@Parcelize
@Serializable
data class FunctionCall(
    @SerializedName("name")
    val name: String,
    @SerializedName("arguments")
    val arguments: String
) : Parcelable

@Parcelize
@Serializable
data class LogProbs(
    @SerializedName("content")
    val content: List<LogProbContent> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class LogProbContent(
    @SerializedName("token")
    val token: String,
    @SerializedName("logprob")
    val logprob: Double,
    @SerializedName("bytes")
    val bytes: List<Int> = emptyList(),
    @SerializedName("top_logprobs")
    val topLogProbs: List<TopLogProb> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class TopLogProb(
    @SerializedName("token")
    val token: String,
    @SerializedName("logprob")
    val logprob: Double,
    @SerializedName("bytes")
    val bytes: List<Int> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class Usage(
    @SerializedName("completion_tokens")
    val completionTokens: Int = 0,
    @SerializedName("prompt_tokens")
    val promptTokens: Int = 0,
    @SerializedName("prompt_cache_hit_tokens")
    val promptCacheHitTokens: Int = 0,
    @SerializedName("prompt_cache_miss_tokens")
    val promptCacheMissTokens: Int = 0,
    @SerializedName("total_tokens")
    val totalTokens: Int = 0,
    @SerializedName("completion_tokens_details")
    val completionTokensDetails: CompletionTokensDetails? = null
) : Parcelable

@Parcelize
@Serializable
data class CompletionTokensDetails(
    @SerializedName("reasoning_tokens")
    val reasoningTokens: Int = 0
) : Parcelable
