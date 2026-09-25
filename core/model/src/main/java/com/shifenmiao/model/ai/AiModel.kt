package com.shifenmiao.model.ai

import android.os.Parcelable
import com.shifenmiao.core.R
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.model.ai.AiProvider.Default
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date


/**
 * AI 模型配置（不可变数据类）
 *
 * 所有字段 val，需要修改时使用 copy()
 */
@Serializable
@Parcelize
data class AiModel(
    val id: Int = 0,
    val modeId: Int = 0,
    val name: String,
    val title: String,
    val description: String = "",
    val canUploadFile: Boolean = false,
    val canNetwork: Boolean = false,
    val temperature: Double = 0.95,
    val topP: Double = 0.8,
    val free: Boolean = false,
    val provider: AiProvider = Default,
    val basePoints: Float = 1f,
    val maxTokens: Int = 2048,
    val canReasoning: Boolean = false,
    val canEdit: Boolean = false,
    val updateTime: Long = Date().time,
    val canVideo: Boolean = false,
    val canImage: Boolean = false,
    val canUseTempApi: Boolean = false,
    val isFast: Boolean = false,
    val isCode : Boolean = false,
    val supportToolCalls: Boolean = true,
    val hasLocalOverrides: Boolean = false,
    val engineName: String = provider.value,
    /**
     * 模型上下文窗口大小（token 数）。
     * 由后台接口/管理后台下发，表示该模型支持的最大上下文窗口上限。
     * 用于客户端侧上下文裁剪预算计算，不影响 API 请求。
     * 0 表示按模型名从 [resolveContextWindow] 推断。
     */
    val contextWindowTokens: Int = DEFAULT_CONTEXT_WINDOW,
) : Parcelable {

    /**
     * 解析有效的上下文窗口大小。
     * 优先使用显式配置值，否则按模型名从已知列表推断，兜底 [DEFAULT_CONTEXT_WINDOW]（264k）。
     */
    fun effectiveContextWindow(): Int {
        if (contextWindowTokens > 0) return contextWindowTokens
        return resolveContextWindow(name)
    }

    /**
     * 积分倍率展示文本(去尾零、最多两位小数),如 "1x"、"0.65x"、"0x"。
     * 代理中转链路下按 totalTokens × basePoints 结算积分。
     */
    fun pointsMultiplierText(): String {
        val value = BigDecimal(basePoints.toDouble())
            .setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        return "${value}x"
    }

    companion object {

        /**
         * 按模型名推断上下文窗口大小。
         * 维护一张常见模型的前缀匹配表，未命中时返回兜底值。
         */
        fun resolveContextWindow(modelName: String): Int {
            val lower = modelName.lowercase()
            return KNOWN_CONTEXT_WINDOWS.entries
                .firstOrNull { (prefix, _) -> lower.contains(prefix) }
                ?.value
                ?: DEFAULT_CONTEXT_WINDOW
        }

        /** 兜底上下文窗口大小（264k） */
        const val DEFAULT_CONTEXT_WINDOW = 264_000

        /** 已知模型的上下文窗口大小（前缀匹配） */
        private val KNOWN_CONTEXT_WINDOWS = linkedMapOf(
            // OpenAI
            "gpt-6" to 1_050_000,
            "gpt-5.6" to 1_050_000,
            "gpt-4o" to 128_000,
            "gpt-4-turbo" to 128_000,
            "gpt-4" to 8_192,
            "gpt-3.5" to 16_385,
            "o1" to 200_000,
            "o3" to 200_000,
            "o4" to 200_000,
            // DeepSeek
            "deepseek-flash" to 1_000_000,
            "deepseek-r1" to 64_000,
            "deepseek-v3" to 64_000,
            "deepseek-v4" to 64_000,
            "deepseek" to 64_000,
            // QWen
            "qwen3.8-flash" to 1_000_000,
            "qwen3" to 131_072,
            "qwen2.5" to 131_072,
            "qwen2" to 131_072,
            "qwen" to 32_768,
            // Kimi / Moonshot
            "kimi-k3" to 1_000_000,
            "kimi-k2" to 131_072,
            "kimi" to 131_072,
            "moonshot" to 131_072,
            // DouBao / ByteDance
            "doubao" to 128_000,
            // Tencent
            "hy4" to 1_000_000,
            "hy3" to 256_000,
            "hunyuan" to 256_000,
            // Xiaomi Mimo
            "mimo" to 131_072,
            // Claude
            "claude-fable" to 1_000_000,
            "claude-opus-5" to 200_000,
            "claude-sonnet-5" to 200_000,
            "claude-opus-4" to 200_000,
            "claude-sonnet-4" to 200_000,
            "claude-3.5" to 200_000,
            "claude-3" to 200_000,
            // Gemini
            "gemini-3" to 1_000_000,
            "gemini-2.5" to 1_000_000,
            "gemini-2" to 1_048_576,
            "gemini" to 32_768,
            // xAI Grok
            "grok-4.6" to 500_000,
            "grok-4.3" to 1_000_000,
            "grok" to 500_000,
            // Zhipu GLM
            "glm-5" to 1_000_000,
            "glm-4.6" to 200_000,
            "glm" to 128_000,
            // MiniMax
            "minimax-m3" to 1_000_000,
            "minimax" to 205_000,
            // Baidu ERNIE
            "ernie-4.5" to 128_000,
            "ernie" to 128_000,
        )
        /**
         * Returns a sample AI model for the given provider
         *
         * @param provider The AI provider to get a sample model for
         * @return An AiModel instance corresponding to the provider
         */
        fun getDefaultModelForProvider(provider: AiProvider): AiModel {
            return when (provider) {
                AiProvider.OpenAi -> AiModel(
                    name = "gpt-5.6-luna",
                    title = AppContext.getString(R.string.ai_model_default_openai_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.3,
                    topP = 1.0,
                    free = true,
                    provider = AiProvider.OpenAi,
                    basePoints = 0.0f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.QWen -> AiModel(
                    name = "qwen3.8-flash",
                    title = AppContext.getString(R.string.ai_model_default_qwen_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = true,
                    temperature = 0.3,
                    topP = 1.0,
                    free = false,
                    provider = AiProvider.QWen,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Kimi -> AiModel(
                    name = "kimi-k3",
                    title = AppContext.getString(R.string.ai_model_default_kimi_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.3,
                    topP = 1.0,
                    free = false,
                    provider = AiProvider.Kimi,
                    basePoints = 0.4f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.DouBao -> AiModel(
                    name = "doubao-seed-2-1-pro-260915",
                    title = AppContext.getString(R.string.ai_model_default_doubao_title),
                    description = AppContext.getString(R.string.ai_model_default_doubao_description),
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 1.0,
                    topP = 0.7,
                    free = false,
                    provider = AiProvider.DouBao,
                    basePoints = 1.0f,
                    maxTokens = 4096,
                    canReasoning = true,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = true
                )

                AiProvider.Tencent -> AiModel(
                    name = "hy4-preview",
                    title = AppContext.getString(R.string.ai_model_default_tencent_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = true,
                    temperature = 0.95,
                    topP = 0.8,
                    free = false,
                    provider = AiProvider.Tencent,
                    basePoints = 1.0f,
                    maxTokens = 2048,
                    canReasoning = true,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.DeepSeek -> AiModel(
                    name = "deepseek-flash",
                    title = AppContext.getString(R.string.ai_model_default_deepseek_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = true,
                    temperature = 0.95,
                    topP = 0.8,
                    free = false,
                    provider = AiProvider.DeepSeek,
                    basePoints = 0.1f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Mimo -> AiModel(
                    name = "mimo-v2.6-flash",
                    title = AppContext.getString(R.string.ai_model_default_mimo_title),
                    description = "",
                    canUploadFile = true,
                    canNetwork = true,
                    temperature = 1.0,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.Mimo,
                    basePoints = 0.1f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = true,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Gemini -> AiModel(
                    name = "gemini-3.8-flash",
                    title = AppContext.getString(R.string.ai_model_default_gemini_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.Gemini,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = true,
                    canEdit = false,
                    canImage = true,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Grok -> AiModel(
                    name = "grok-4.3",
                    title = AppContext.getString(R.string.ai_model_default_grok_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = true,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.Grok,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = true,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Claude -> AiModel(
                    name = "claude-sonnet-5",
                    title = AppContext.getString(R.string.ai_model_default_claude_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.Claude,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = true,
                    canEdit = false,
                    canImage = true,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.OpenRouter -> AiModel(
                    name = "openrouter/auto",
                    title = AppContext.getString(R.string.ai_model_default_openrouter_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.OpenRouter,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Requesty -> AiModel(
                    name = "openai/gpt-4o-mini",
                    title = AppContext.getString(R.string.ai_model_default_requesty_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.Requesty,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.ZhiPu -> AiModel(
                    name = "glm-5.3-flash",
                    title = AppContext.getString(R.string.ai_model_default_zhipu_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.ZhiPu,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = true,
                    canEdit = false,
                    canImage = true,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.MinMax -> AiModel(
                    name = "MiniMax-M2.7",
                    title = AppContext.getString(R.string.ai_model_default_minimax_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.7,
                    topP = 0.95,
                    free = false,
                    provider = AiProvider.MinMax,
                    basePoints = 0.3f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Baidu -> AiModel(
                    name = "ernie-4.5-turbo-128k",
                    title = AppContext.getString(R.string.ai_model_default_baidu_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = true,
                    temperature = 0.95,
                    topP = 0.8,
                    free = false,
                    provider = AiProvider.Baidu,
                    basePoints = 0.5f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )

                AiProvider.Jev -> AiModel(
                    name = "jev-latest",
                    title = AppContext.getString(R.string.ai_model_default_jev_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.3,
                    topP = 1.0,
                    free = false,
                    provider = AiProvider.Jev,
                    basePoints = 0.1f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false,
                    supportToolCalls = false,
                )

                AiProvider.Pikafish -> AiModel(
                    name = "pikafish",
                    title = AppContext.getString(R.string.ai_model_default_pikafish_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = false,
                    temperature = 0.3,
                    topP = 1.0,
                    free = false,
                    provider = AiProvider.Pikafish,
                    basePoints = 0.1f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false,
                    supportToolCalls = false,
                )

                else -> AiModel(
                    name = "deepseek-flash",
                    title = AppContext.getString(R.string.ai_model_default_deepseek_title),
                    description = "",
                    canUploadFile = false,
                    canNetwork = true,
                    temperature = 0.95,
                    topP = 0.8,
                    free = false,
                    provider = AiProvider.DeepSeek,
                    basePoints = 0.1f,
                    maxTokens = 2048,
                    canReasoning = false,
                    canEdit = false,
                    canImage = false,
                    canVideo = false,
                    canUseTempApi = false
                )
            }
        }
    }
}

