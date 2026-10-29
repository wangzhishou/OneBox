package com.wanbaohe.setting.ai.component

import android.util.Base64
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.AuthType
import com.shifenmiao.model.ai.FileUploadStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 引擎配置导入/导出的唯一序列化入口(剪贴板与深链共用同一套)。
 *
 * 字段与 [AiEngine] 对齐、收敛到用户配置必需的最小集合; 导出的 JSON 含鉴权 Token,
 * 仅用于用户自己的备份/分享。
 */
object AiEngineConfigCodec {

    private const val CONFIG_TYPE = "onebox-ai-engine"
    private const val CONFIG_VERSION = 2

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    fun encode(engine: AiEngine): String {
        return json.encodeToString(
            AiEngineConfigDto(
                type = CONFIG_TYPE,
                name = engine.name.trim(),
                title = engine.title.trim(),
                description = engine.description,
                requestUrl = engine.requestUrl.trim(),
                requestPath = engine.requestPath.trim(),
                authorizationCode = engine.authorizationCode,
                requestProtocol = engine.requestProtocol.name,
                authType = engine.authType.name,
                stream = engine.stream,
                proxyUrl = engine.proxyUrl.trim(),
                proxyPath = engine.proxyPath.trim(),
                iconName = engine.iconName,
                fileUploadStrategy = engine.fileUploadStrategy.name,
                // CLOUD 上传策略的配套字段(R3): 策略导出了但连接配置丢了就是半残
                cloudStorageConnectionId = engine.cloudStorageConnectionId.orEmpty(),
                cloudStorageBucket = engine.cloudStorageBucket.orEmpty(),
                cloudStoragePrefix = engine.cloudStoragePrefix,
                modelName = engine.model.name.trim(),
                modelTitle = engine.model.title.trim(),
                modelTemperature = engine.model.temperature,
                modelTopP = engine.model.topP,
                modelMaxTokens = engine.model.maxTokens,
                modelCanUploadFile = engine.model.canUploadFile,
                modelCanNetwork = engine.model.canNetwork,
                modelCanReasoning = engine.model.canReasoning,
                modelCanImage = engine.model.canImage,
                modelCanVideo = engine.model.canVideo,
                modelContextWindowTokens = engine.model.contextWindowTokens,
                modelIsFast = engine.model.isFast,
                modelIsCode = engine.model.isCode,
                modelSupportToolCalls = engine.model.supportToolCalls,
            )
        )
    }

    /**
     * 导出为导入深链: `onebox://action/import_engine?config=<URL-safe base64>`(F12)。
     * 必须用 URL-safe alphabet —— resolveDeeplink 会对整条 URL 先 Uri.decode,
     * 标准 base64 里的 `+`/`%xx` 会被拆坏。
     */
    fun encodeAsImportLink(engine: AiEngine): String {
        val payload = Base64.encodeToString(
            encode(engine).toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
        return "onebox://action/import_engine?config=$payload"
    }

    /**
     * 解析导入文本: 支持裸 JSON, 或 base64(标准/URL-safe) 包裹的 JSON。
     * 任何一步失败都返回 null, 由调用方统一提示"不是有效的引擎配置"。
     */
    fun decode(raw: String): AiEngine? {
        var text = raw.trim()
        if (text.isEmpty()) return null
        // 复制导入链接后直接粘进「从剪贴板导入」也要能解(R4): 先取 config 参数再解码
        if (text.contains("?config=")) {
            text = text.substringAfter("?config=").substringBefore('&')
                .let { runCatching { android.net.Uri.decode(it) }.getOrDefault(it) }
        }
        val jsonText = if (text.startsWith("{")) text else decodeBase64Json(text) ?: return null
        val dto = runCatching { json.decodeFromString<AiEngineConfigDto>(jsonText) }.getOrNull()
            ?: return null
        if (dto.type != CONFIG_TYPE) return null
        return dto.toEngine()
    }

    private fun decodeBase64Json(text: String): String? {
        // 深链场景推荐 URL-safe; 标准 alphabet 也兼容, 两种都试
        val flags = listOf(
            Base64.URL_SAFE or Base64.NO_WRAP,
            Base64.DEFAULT,
        )
        flags.forEach { flag ->
            runCatching {
                String(Base64.decode(text, flag), Charsets.UTF_8)
            }.getOrNull()?.takeIf { it.trimStart().startsWith("{") }?.let { return it }
        }
        return null
    }

    private fun AiEngineConfigDto.toEngine(): AiEngine {
        val protocol = AiRequestProtocol.fromValue(requestProtocol)
        val engineName = name.trim()
        return AiEngine(
            name = engineName,
            title = title.trim(),
            description = description,
            apiCanSet = true,
            requestUrl = requestUrl.trim(),
            requestPath = requestPath.trim(),
            authorizationCode = authorizationCode.trim(),
            requestProtocol = protocol,
            authType = AuthType.resolve(authType, protocol),
            stream = stream,
            proxyUrl = proxyUrl.trim(),
            proxyPath = proxyPath.trim(),
            iconName = iconName,
            fileUploadStrategy = runCatching { FileUploadStrategy.valueOf(fileUploadStrategy) }
                .getOrDefault(FileUploadStrategy.BASE64),
            cloudStorageConnectionId = cloudStorageConnectionId.ifBlank { null },
            cloudStorageBucket = cloudStorageBucket.ifBlank { null },
            cloudStoragePrefix = cloudStoragePrefix.ifBlank { "ai-uploads/" },
            model = AiModel(
                id = 0,
                name = modelName.trim(),
                title = modelTitle.trim(),
                provider = AiProvider.Default,
                canEdit = true,
                engineName = engineName,
                temperature = modelTemperature,
                topP = modelTopP,
                maxTokens = modelMaxTokens,
                canUploadFile = modelCanUploadFile,
                canNetwork = modelCanNetwork,
                canReasoning = modelCanReasoning,
                canImage = modelCanImage,
                canVideo = modelCanVideo,
                contextWindowTokens = modelContextWindowTokens,
                isFast = modelIsFast,
                isCode = modelIsCode,
                supportToolCalls = modelSupportToolCalls,
            ),
        )
    }

    @Serializable
    private data class AiEngineConfigDto(
        // type 不可给默认值(R8): 给了默认值 {"a":1} 这种垃圾 JSON 也能过校验,
        // 解出一个空引擎顶掉用户已填的表单
        val type: String? = null,
        val version: Int = CONFIG_VERSION,
        val name: String = "",
        val title: String = "",
        val description: String = "",
        val requestUrl: String = "",
        val requestPath: String = "",
        val authorizationCode: String = "",
        val requestProtocol: String = "",
        val authType: String = "",
        val stream: Boolean = true,
        // v2 起补齐: 代理链路/图标/上传策略与模型参数, 旧版 payload 缺省时回落 AiModel/AiEngine 默认
        val proxyUrl: String = "",
        val proxyPath: String = "",
        val iconName: String = "",
        val fileUploadStrategy: String = "",
        val cloudStorageConnectionId: String = "",
        val cloudStorageBucket: String = "",
        val cloudStoragePrefix: String = "",
        val modelName: String = "",
        val modelTitle: String = "",
        val modelTemperature: Double = 0.95,
        val modelTopP: Double = 0.8,
        val modelMaxTokens: Int = 2048,
        val modelCanUploadFile: Boolean = false,
        val modelCanNetwork: Boolean = false,
        val modelCanReasoning: Boolean = false,
        val modelCanImage: Boolean = false,
        val modelCanVideo: Boolean = false,
        val modelContextWindowTokens: Int = 0,
        val modelIsFast: Boolean = false,
        val modelIsCode: Boolean = false,
        val modelSupportToolCalls: Boolean = true,
    )
}
