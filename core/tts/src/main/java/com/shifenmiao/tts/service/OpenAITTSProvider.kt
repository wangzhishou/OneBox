package com.shifenmiao.tts.service

import com.shifenmiao.network.api.TTSSpeechApi
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSSpeechRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenAITTSProvider @Inject constructor(
    private val api: TTSSpeechApi,
) : TTSProvider {

    override suspend fun synthesize(config: TTSConfig, request: TTSSpeechRequest): Result<ByteArray> {
        return runCatching {
            val baseUrl = config.resolveBaseUrl().trim().removeSuffix("/")
            val apiToken = config.resolveApiToken()
            require(baseUrl.isNotBlank()) { "OpenAI baseUrl 或 proxyUrl 未配置" }
            require(apiToken.isNotBlank()) { "OpenAI apiToken 或 proxyToken 未配置" }

            val auth = "Bearer $apiToken"
            val response = api.synthesizeSpeech(baseUrl, auth, request)
            response.bytes()
        }
    }

    override fun supportedVoices(): List<String> = OPENAI_VOICES

    override fun defaultVoice(): String = "alloy"

    override fun defaultModel(): String = "tts-1"

    companion object {
        private val OPENAI_VOICES = listOf(
            "alloy", "echo", "fable", "onyx", "nova", "shimmer",
            "ash", "ballad", "coral", "sage", "verse"
        )
    }
}
