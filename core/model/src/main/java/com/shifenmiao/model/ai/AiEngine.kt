package com.shifenmiao.model.ai

import android.os.Parcelable
import com.shifenmiao.core.R
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.BuildConfig
import com.shifenmiao.model.channel.FlavorType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

private fun providerForWorkingSlot(slot: AiWorkingModelSlot): AiProvider {
    val engineName = AiModelSlotDefaults.get(slot).engineName
    return AiProvider.fromValue(engineName).takeUnless { it == AiProvider.Default }
        ?: error("Unsupported default AI provider for slot=$slot engineName=$engineName")
}

private fun defaultModelForDefaultSlot(): AiModel {
    return AiModel.getDefaultModelForProvider(providerForWorkingSlot(AiWorkingModelSlot.DEFAULT))
}

private fun normalizeAuthorizationCode(value: String?): String {
    return value
        ?.trim()
        ?.takeUnless { it.isBlank() || it.equals("null", ignoreCase = true) }
        .orEmpty()
}

/**
 * AI 引擎配置（不可变数据类）
 *
 * 设计要点：
 * - 所有字段 val，保证线程安全，需要修改时使用 copy()
 * - 各 provider 的默认配置通过 companion 工厂方法获取（每次返回新实例）
 * - 不再使用 data object 单例，避免共享状态被意外修改
 */
@Serializable
@Parcelize
data class AiEngine(
    val name: String = "",
    val iconName: String = "",
    val title: String = "",
    val description: String = "",
    val apiCanSet: Boolean = false,
    val requestUrl: String = "",
    val requestPath: String = "",
    val proxyUrl: String = "",
    val proxyPath: String = "",
    val authorizationCode: String = "",
    val model: AiModel = defaultModelForDefaultSlot(),
    val requestProtocol: AiRequestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
    /**
     * 认证方式通常跟随 requestProtocol 推断，但仍允许显式覆盖，兼容少数“非典型” provider。
     */
    val authType: AuthType = AuthType.defaultFor(requestProtocol),
    val stream: Boolean = true,
    val isUrlError: Boolean = false,
    val isDetestPassed: Boolean = false,
    val vipLevel: Int = 0,
    val fileUploadStrategy: FileUploadStrategy = FileUploadStrategy.BASE64,
    /** 云存储配置ID（CLOUD模式时使用） */
    val cloudStorageConnectionId: String? = null,
    /** 云存储Bucket名称（CLOUD模式时使用） */
    val cloudStorageBucket: String? = null,
    /** 云存储上传前缀（如 "ai-uploads/"） */
    val cloudStoragePrefix: String = "ai-uploads/"
) : Parcelable {

    fun identityKey(): String {
        return buildIdentityKey(name = name, requestProtocol = requestProtocol)
    }

    fun hasDirectConnectionReady(): Boolean {
        return isDetestPassed
    }

    fun hasOwnToken(): Boolean {
        return authorizationCode.isNotBlank()
    }

    /**
     * 是否可以直连官方 API 聊天。
     * 海外渠道(google / foss)：用户自带 token 即可直连，无需先通过设置里的手动连通性测试；
     * 其他渠道维持原有语义（必须 isDetestPassed）。
     */
    fun canChatDirectly(): Boolean {
        return hasDirectConnectionReady() ||
            (FlavorType.fromName().isOverseas && hasOwnToken())
    }

    fun hasProxyRouteConfigured(): Boolean {
        return proxyUrl.isNotBlank() && proxyPath.isNotBlank()
    }

    /**
     * 当前实际走 App 中转代理链路：无法直连官方 API,但代理路由已配置。
     * 该链路下聊天按模型 basePoints 倍率扣积分。
     */
    fun usesProxyRoute(): Boolean {
        return !canChatDirectly() && hasProxyRouteConfigured()
    }

    fun hasAvailableChatRoute(): Boolean {
        return requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE ||
            canChatDirectly() ||
            hasProxyRouteConfigured()
    }

    companion object {

        fun buildIdentityKey(name: String, requestProtocol: AiRequestProtocol): String {
            return "${name.trim().lowercase()}#${requestProtocol.name}"
        }

        fun fastEngine(): AiEngine {
            return builtInEngine(providerForWorkingSlot(AiWorkingModelSlot.FAST))
        }

        fun defaultEngine(): AiEngine {
            return builtInEngine(providerForWorkingSlot(AiWorkingModelSlot.DEFAULT))
        }

        fun builtInEngine(provider: AiProvider): AiEngine {
            return when (provider) {
                AiProvider.DouBao -> AiEngine(
                    name = AiProvider.DouBao.value,
                    iconName = "AutoAwesome",
                    title = AppContext.getString(R.string.ai_engine_seed_doubao_title),
                    description = "",
                    requestUrl = UrlConstants.DOUBAO_AI_BASE_URL,
                    requestPath = UrlConstants.DOUBAO_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.DOU_BAO_AI_PROXY_PATH,
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.DouBao),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.DeepSeek -> AiEngine(
                    name = AiProvider.DeepSeek.value,
                    iconName = "Search",
                    title = AppContext.getString(R.string.ai_engine_seed_deepseek_title),
                    description = "",
                    requestUrl = UrlConstants.DEEP_SEEK_AI_BASE_URL,
                    requestPath = UrlConstants.DEEP_SEEK_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.DEEP_SEEK_AI_PROXY_PATH,
                    authorizationCode = normalizeAuthorizationCode(BuildConfig.DeepSeekAuthorizationCode),
                    model = AiModel.getDefaultModelForProvider(AiProvider.DeepSeek),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.QWen -> AiEngine(
                    name = AiProvider.QWen.value,
                    iconName = "Language",
                    title = AppContext.getString(R.string.ai_engine_seed_qwen_title),
                    description = "",
                    requestUrl = UrlConstants.Q_WEN_AI_BASE_URL,
                    requestPath = UrlConstants.Q_WEN_AI_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.ALIBABA_AI_PROXY_PATH,
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.QWen),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.Kimi -> AiEngine(
                    name = AiProvider.Kimi.value,
                    iconName = "Explore",
                    title = AppContext.getString(R.string.ai_engine_seed_kimi_title),
                    description = "",
                    requestUrl = UrlConstants.KIMI_AI_BASE_URL,
                    requestPath = UrlConstants.KIMI_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.KIM_AI_PROXY_PATH,
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.Kimi),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.Tencent -> AiEngine(
                    name = AiProvider.Tencent.value,
                    iconName = "Chat",
                    title = AppContext.getString(R.string.ai_engine_seed_tencent_title),
                    description = "",
                    requestUrl = UrlConstants.TENCENT_AI_BASE_URL,
                    requestPath = UrlConstants.TENCENT_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.TENCENT_AI_PROXY_PATH,
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.Tencent),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.Mimo -> AiEngine(
                    name = AiProvider.Mimo.value,
                    iconName = "Settings",
                    title = AppContext.getString(R.string.ai_engine_seed_mimo_title),
                    description = "",
                    requestUrl = UrlConstants.XIAOMI_AI_BASE_URL,
                    requestPath = UrlConstants.XIAOMI_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.XIAOMI_AI_PROXY_PATH,
                    authorizationCode = normalizeAuthorizationCode(BuildConfig.XiaomiAuthorizationCode),
                    model = AiModel.getDefaultModelForProvider(AiProvider.Mimo),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.OpenAi -> AiEngine(
                    name = AiProvider.OpenAi.value,
                    iconName = "SmartToy",
                    title = AppContext.getString(R.string.ai_engine_seed_openai_title),
                    description = "",
                    requestUrl = UrlConstants.OPENAI_BASE_URL,
                    requestPath = UrlConstants.OPENAI_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.OpenAi),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                // 海外引擎(Google 渠道预制): 不走 Go 网关代理, 用户自带 token 直连官方 API
                AiProvider.Gemini -> AiEngine(
                    name = AiProvider.Gemini.value,
                    iconName = "Star",
                    title = AppContext.getString(R.string.ai_engine_seed_gemini_title),
                    description = "",
                    requestUrl = UrlConstants.GEMINI_AI_BASE_URL,
                    requestPath = UrlConstants.GEMINI_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = "",
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.Gemini),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.Grok -> AiEngine(
                    name = AiProvider.Grok.value,
                    iconName = "Psychology",
                    title = AppContext.getString(R.string.ai_engine_seed_grok_title),
                    description = "",
                    requestUrl = UrlConstants.GROK_AI_BASE_URL,
                    requestPath = UrlConstants.GROK_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = "",
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.Grok),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.Claude -> AiEngine(
                    name = AiProvider.Claude.value,
                    iconName = "Cloud",
                    title = AppContext.getString(R.string.ai_engine_seed_claude_title),
                    description = "",
                    requestUrl = UrlConstants.CLAUDE_AI_BASE_URL,
                    requestPath = UrlConstants.CLAUDE_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = "",
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.Claude),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.OpenRouter -> AiEngine(
                    name = AiProvider.OpenRouter.value,
                    iconName = "TravelExplore",
                    title = AppContext.getString(R.string.ai_engine_seed_openrouter_title),
                    description = "",
                    requestUrl = UrlConstants.OPENROUTER_AI_BASE_URL,
                    requestPath = UrlConstants.OPENROUTER_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = "",
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.OpenRouter),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                // 智谱 / MiniMax(仅海外渠道): 代理路径经 defaultProxyPathFor 走 Go 网关
                AiProvider.ZhiPu -> AiEngine(
                    name = AiProvider.ZhiPu.value,
                    iconName = "SmartToy",
                    title = AppContext.getString(R.string.ai_engine_seed_zhipu_title),
                    description = "",
                    requestUrl = UrlConstants.ZHIPU_AI_BASE_URL,
                    requestPath = UrlConstants.ZHIPU_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = "",
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.ZhiPu),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.MinMax -> AiEngine(
                    name = AiProvider.MinMax.value,
                    iconName = "SmartToy",
                    title = AppContext.getString(R.string.ai_engine_seed_minimax_title),
                    description = "",
                    requestUrl = UrlConstants.MINIMAX_AI_BASE_URL,
                    requestPath = UrlConstants.MINIMAX_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = "",
                    proxyPath = "",
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.MinMax),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                AiProvider.Baidu -> AiEngine(
                    name = AiProvider.Baidu.value,
                    iconName = "Search",
                    title = AppContext.getString(R.string.ai_engine_seed_baidu_title),
                    description = "",
                    requestUrl = UrlConstants.BAIDU_AI_BASE_URL,
                    requestPath = UrlConstants.BAIDU_TEXT_COMPLETIONS_ENDPOINT,
                    proxyUrl = UrlConstants.RELEASE_URL,
                    proxyPath = UrlConstants.BAIDU_AI_PROXY_PATH,
                    authorizationCode = "",
                    model = AiModel.getDefaultModelForProvider(AiProvider.Baidu),
                    requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                    stream = true,
                    fileUploadStrategy = FileUploadStrategy.BASE64
                )

                else -> defaultEngineFallback(provider)
            }
        }

        private fun defaultEngineFallback(@Suppress("UNUSED_PARAMETER") provider: AiProvider): AiEngine {
            return defaultEngine()
        }
    }
}
