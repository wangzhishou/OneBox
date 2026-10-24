package com.shifenmiao.model.ai.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonArray

/**
 * AI 配置响应
 */
@Serializable
data class AiConfigResponse(
    val code: Int,
    val message: String,
    val data: AiConfigData?
)

/**
 * AI 配置数据
 */
@Serializable
data class AiConfigData(
    val version: String = "",
    @Serializable(EngineConfigListSerializer::class)
    val engines: List<EngineConfig> = emptyList(),
    @Serializable(ModelConfigListSerializer::class)
    val models: List<ModelConfig> = emptyList()
)

/**
 * 引擎配置
 */
@Serializable
data class EngineConfig(
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    @SerialName("requestUrl")
    val requestUrl: String? = null,
    @SerialName("requestPath")
    val requestPath: String? = null,
    @SerialName("proxyUrl")
    val proxyUrl: String? = null,
    @SerialName("proxyPath")
    val proxyPath: String? = null,
    @SerialName("requestProtocol")
    val requestProtocol: String? = null,
    @SerialName("authType")
    val authType: String? = null,
    val stream: Boolean? = true,
    @SerialName("vipLevel")
    val vipLevel: Int? = 0,
    val enabled: Boolean? = true,
    @SerialName("sortOrder")
    val sortOrder: Int? = 0,
    @SerialName("supportToolCalls")
    val supportToolCalls: Boolean? = false
)

/**
 * 模型配置
 */
@Serializable
data class ModelConfig(
    @SerialName("id")
    val remoteId: Int? = null,
    val name: String? = null,
    val title: String? = null,
    val description: String? = null,
    val provider: String? = null,
    @SerialName("canUploadFile")
    val canUploadFile: Boolean? = false,
    @SerialName("canNetwork")
    val canNetwork: Boolean? = false,
    @SerialName("canReasoning")
    val canReasoning: Boolean? = false,
    @SerialName("canImage")
    val canImage: Boolean? = false,
    @SerialName("canVideo")
    val canVideo: Boolean? = false,
    @SerialName("apiCanSet")
    val apiCanSet: Boolean? = false,
    @SerialName("canUseTempApi")
    val canUseTempApi: Boolean? = false,
    @SerialName("isFast")
    val isFast: Boolean? = false,
    @SerialName("isCode")
    val isCode: Boolean? = false,
    @SerialName("supportToolCalls")
    val supportToolCalls: Boolean? = true,
    @SerialName("canEdit")
    val canEdit: Boolean? = false,
    val temperature: Double? = 0.95,
    @SerialName("topP")
    val topP: Double? = 0.8,
    @SerialName("maxTokens")
    val maxTokens: Int? = 2048,
    @SerialName("contextWindowTokens")
    val contextWindowTokens: Int? = 264000,
    val free: Boolean? = false,
    @SerialName("basePoints")
    val basePoints: Float? = 1.0f,
    val enabled: Boolean? = true,
    @SerialName("sortOrder")
    val sortOrder: Int? = 0
)


// ── Gson alternate 兼容层(阶段⑤b) ─────────────────────────────
// Gson @SerializedName(alternate=[...]) 在 kotlinx 无对应能力,这里在反序列化前
// 把 alternate 键归一到主键。语义对齐:主键缺失时才用 alternate 补齐
// (Gson 对同 JSON 同时含主键与 alternate 时按出现顺序后者胜,此处主键优先,
// 线上配置同一语义键只会下发一种写法,无实际差异)。

/** EngineConfig 主键 ← alternate 键(与 Gson 注解逐条对应) */
private val ENGINE_KEY_ALTERNATES: Map<String, List<String>> = mapOf(
    "name" to listOf("engineName"),
    "title" to listOf("engineTitle"),
    "requestUrl" to listOf("request_url", "baseUrl", "base_url", "apiUrl", "api_url"),
    "requestPath" to listOf("request_path", "path"),
    "proxyUrl" to listOf("proxy_url"),
    "proxyPath" to listOf("proxy_path"),
    "requestProtocol" to listOf("request_protocol", "protocol"),
    "authType" to listOf("auth_type"),
    "vipLevel" to listOf("vip_level"),
    "sortOrder" to listOf("sort_order"),
    "supportToolCalls" to listOf("support_tool_calls"),
)

/** ModelConfig 主键 ← alternate 键(与 Gson 注解逐条对应) */
private val MODEL_KEY_ALTERNATES: Map<String, List<String>> = mapOf(
    "name" to listOf("modelName"),
    "title" to listOf("modelTitle"),
    "provider" to listOf("engineName", "engine_name"),
    "canUploadFile" to listOf("can_upload_file"),
    "canNetwork" to listOf("can_network"),
    "canReasoning" to listOf("can_reasoning"),
    "canImage" to listOf("can_image"),
    "canVideo" to listOf("can_video"),
    "apiCanSet" to listOf("api_can_set"),
    "canUseTempApi" to listOf("can_use_temp_api"),
    "isFast" to listOf("is_fast", "fast"),
    "isCode" to listOf("is_code", "code", "canCode", "can_code"),
    "supportToolCalls" to listOf("support_tool_calls"),
    "canEdit" to listOf("can_edit"),
    "topP" to listOf("top_p"),
    "maxTokens" to listOf("max_tokens"),
    "contextWindowTokens" to listOf("context_window_tokens"),
    "basePoints" to listOf("base_points"),
    "sortOrder" to listOf("sort_order"),
)

private fun JsonElement.normalizeAlternateKeys(alternates: Map<String, List<String>>): JsonElement {
    val obj = this as? JsonObject ?: return this
    val normalized = obj.toMutableMap()
    alternates.forEach { (primary, aliases) ->
        if (normalized[primary] == null) {
            aliases.firstNotNullOfOrNull { normalized[it] }
                ?.let { normalized[primary] = it }
        }
    }
    return JsonObject(normalized)
}

object EngineConfigListSerializer : JsonTransformingSerializer<List<EngineConfig>>(
    ListSerializer(EngineConfig.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        JsonArray(element.jsonArray.map { it.normalizeAlternateKeys(ENGINE_KEY_ALTERNATES) })
}

object ModelConfigListSerializer : JsonTransformingSerializer<List<ModelConfig>>(
    ListSerializer(ModelConfig.serializer())
) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        JsonArray(element.jsonArray.map { it.normalizeAlternateKeys(MODEL_KEY_ALTERNATES) })
}
