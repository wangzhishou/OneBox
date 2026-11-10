package com.shifenmiao.tts.service

import com.shifenmiao.model.channel.FlavorType
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSProviderType
import com.shifenmiao.model.tts.TTSSpeechRequest
import com.shifenmiao.network.api.TTSSpeechApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAITTSProvider @Inject constructor(
    private val api: TTSSpeechApi,
) : TTSProvider {

    override val descriptor = TTSProviderDescriptor(
        type = TTSProviderType.OPENAI_COMPATIBLE,
        defaultModel = "tts-1",
        defaultVoice = "alloy",
        supportedVoices = listOf(
            "alloy", "echo", "fable", "onyx", "nova", "shimmer",
            "ash", "ballad", "coral", "sage", "verse",
        ),
        isSelectable = FlavorType.fromName() == FlavorType.GOOGLE,
    )

    override suspend fun synthesize(config: TTSConfig, request: TTSSpeechRequest): Result<ByteArray> {
        return runCatching {
            val useProxy = isProxyRoute(config)
            val baseUrl = if (useProxy) config.proxyUrl else config.baseUrl
            val apiToken = if (useProxy) config.proxyToken else config.apiToken
            require(baseUrl.isNotBlank()) { "OpenAI baseUrl 或 proxyUrl 未配置" }
            require(apiToken.isNotBlank()) { "OpenAI apiToken 或 proxyToken 未配置" }

            val response = api.synthesizeSpeech(
                baseUrl.trim().removeSuffix("/"),
                "Bearer ${apiToken.trim()}",
                request,
            )
            response.bytes()
        }
    }

    override fun isProxyRoute(config: TTSConfig): Boolean =
        !config.hasDirectConfig && config.proxyUrl.isNotBlank()
}
