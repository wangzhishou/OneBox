package com.shifenmiao.model.ai.config

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerializedName(value = "version")
    val version: String = "",
    @SerializedName(value = "engines")
    val engines: List<EngineConfig> = emptyList(),
    @SerializedName(value = "models")
    val models: List<ModelConfig> = emptyList()
)

/**
 * 引擎配置
 */
@Serializable
data class EngineConfig(
    @SerializedName(value = "name", alternate = ["engineName"])
    val name: String? = null,
    @SerializedName(value = "title", alternate = ["engineTitle"])
    val title: String? = null,
    val description: String? = null,
    @SerialName("requestUrl")
    @SerializedName(value = "requestUrl", alternate = ["request_url", "baseUrl", "base_url", "apiUrl", "api_url"])
    val requestUrl: String? = null,
    @SerialName("requestPath")
    @SerializedName(value = "requestPath", alternate = ["request_path", "path"])
    val requestPath: String? = null,
    @SerialName("proxyUrl")
    @SerializedName(value = "proxyUrl", alternate = ["proxy_url"])
    val proxyUrl: String? = null,
    @SerialName("proxyPath")
    @SerializedName(value = "proxyPath", alternate = ["proxy_path"])
    val proxyPath: String? = null,
    @SerialName("requestProtocol")
    @SerializedName(value = "requestProtocol", alternate = ["request_protocol", "protocol"])
    val requestProtocol: String? = null,
    @SerialName("authType")
    @SerializedName(value = "authType", alternate = ["auth_type"])
    val authType: String? = null,
    @SerializedName(value = "stream")
    val stream: Boolean? = true,
    @SerialName("vipLevel")
    @SerializedName(value = "vipLevel", alternate = ["vip_level"])
    val vipLevel: Int? = 0,
    @SerializedName(value = "enabled")
    val enabled: Boolean? = true,
    @SerialName("sortOrder")
    @SerializedName(value = "sortOrder", alternate = ["sort_order"])
    val sortOrder: Int? = 0,
    @SerialName("supportToolCalls")
    @SerializedName(value = "supportToolCalls", alternate = ["support_tool_calls"])
    val supportToolCalls: Boolean? = false
)

/**
 * 模型配置
 */
@Serializable
data class ModelConfig(
    @SerializedName(value = "name", alternate = ["modelName"])
    val name: String? = null,
    @SerializedName(value = "title", alternate = ["modelTitle"])
    val title: String? = null,
    val description: String? = null,
    @SerializedName(value = "provider", alternate = ["engineName", "engine_name"])
    val provider: String? = null,
    @SerialName("canUploadFile")
    @SerializedName(value = "canUploadFile", alternate = ["can_upload_file"])
    val canUploadFile: Boolean? = false,
    @SerialName("canNetwork")
    @SerializedName(value = "canNetwork", alternate = ["can_network"])
    val canNetwork: Boolean? = false,
    @SerialName("canReasoning")
    @SerializedName(value = "canReasoning", alternate = ["can_reasoning"])
    val canReasoning: Boolean? = false,
    @SerialName("canImage")
    @SerializedName(value = "canImage", alternate = ["can_image"])
    val canImage: Boolean? = false,
    @SerialName("canVideo")
    @SerializedName(value = "canVideo", alternate = ["can_video"])
    val canVideo: Boolean? = false,
    @SerialName("apiCanSet")
    @SerializedName(value = "apiCanSet", alternate = ["api_can_set"])
    val apiCanSet: Boolean? = false,
    @SerialName("canUseTempApi")
    @SerializedName(value = "canUseTempApi", alternate = ["can_use_temp_api"])
    val canUseTempApi: Boolean? = false,
    @SerialName("isFast")
    @SerializedName(value = "isFast", alternate = ["is_fast", "fast"])
    val isFast: Boolean? = false,
    @SerialName("isCode")
    @SerializedName(value = "isCode", alternate = ["is_code", "code", "canCode", "can_code"])
    val isCode: Boolean? = false,
    @SerialName("supportToolCalls")
    @SerializedName(value = "supportToolCalls", alternate = ["support_tool_calls"])
    val supportToolCalls: Boolean? = true,
    @SerialName("canEdit")
    @SerializedName(value = "canEdit", alternate = ["can_edit"])
    val canEdit: Boolean? = false,
    @SerializedName(value = "temperature")
    val temperature: Double? = 0.95,
    @SerialName("topP")
    @SerializedName(value = "topP", alternate = ["top_p"])
    val topP: Double? = 0.8,
    @SerialName("maxTokens")
    @SerializedName(value = "maxTokens", alternate = ["max_tokens"])
    val maxTokens: Int? = 2048,
    @SerialName("contextWindowTokens")
    @SerializedName(value = "contextWindowTokens", alternate = ["context_window_tokens"])
    val contextWindowTokens: Int? = 264000,
    @SerializedName(value = "free")
    val free: Boolean? = false,
    @SerialName("basePoints")
    @SerializedName(value = "basePoints", alternate = ["base_points"])
    val basePoints: Float? = 1.0f,
    @SerializedName(value = "enabled")
    val enabled: Boolean? = true,
    @SerialName("sortOrder")
    @SerializedName(value = "sortOrder", alternate = ["sort_order"])
    val sortOrder: Int? = 0
)

