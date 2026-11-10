package com.shifenmiao.tts.service

import android.util.Base64
import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSProviderType
import com.shifenmiao.model.tts.TTSSpeechRequest
import com.shifenmiao.model.tts.mimo.MimoAudioConfig
import com.shifenmiao.model.tts.mimo.MimoMessage
import com.shifenmiao.model.tts.mimo.MimoTTSRequest
import com.shifenmiao.network.api.MimoTTSApi
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class MimoTTSProvider @Inject constructor(
    @Named("MimoDirectApi") private val directApi: MimoTTSApi,
    @Named("MimoProxyApi") private val proxyApi: MimoTTSApi,
) : TTSProvider {

    override val descriptor = TTSProviderDescriptor(
        type = TTSProviderType.MIMO,
        defaultModel = "mimo-v2.5-tts",
        defaultVoice = "mimo_default",
        supportedVoices = listOf(
            "mimo_default", "冰糖", "茉莉", "苏打", "白桦",
            "Mia", "Chloe", "Milo", "Dean",
        ),
    )

    override suspend fun synthesize(config: TTSConfig, request: TTSSpeechRequest): Result<ByteArray> {
        return runCatching {
            val mimoRequest = MimoTTSRequest(
                model = config.model,
                messages = buildMessages(request),
                audio = MimoAudioConfig(
                    format = "wav",
                    voice = request.voice,
                ),
            )

            val response = if (isProxyRoute(config)) {
                proxyApi.synthesizeViaProxy(proxyEndpointUrl, mimoRequest)
            } else {
                val url = config.baseUrl.trim().removeSuffix("/")
                directApi.synthesize(url, config.apiToken, mimoRequest)
            }
            if (!response.isSuccessful) {
                throw IllegalStateException("MiMo API error: ${response.code()} ${response.errorBody()?.string()}")
            }

            val body = response.body()
                ?: throw IllegalStateException("MiMo API returned empty body")

            val base64Data = body.choices.firstOrNull()
                ?.message?.audio?.data
                ?: throw IllegalStateException("MiMo API returned no audio data")

            Base64.decode(base64Data, Base64.DEFAULT)
        }
    }

    override fun isProxyRoute(config: TTSConfig): Boolean = !config.hasDirectConfig

    private fun buildMessages(request: TTSSpeechRequest): List<MimoMessage> {
        val text = request.input
        val styleRegex = Regex("^([（(][^）)]+[）)])")
        val match = styleRegex.find(text)
        val style = match?.groupValues?.get(1) ?: ""
        val content = if (match != null) text.substring(match.range.last + 1).trim() else text

        return listOf(
            MimoMessage(role = "user", content = style),
            MimoMessage(role = "assistant", content = content),
        )
    }

    companion object {
        private val proxyEndpointUrl: String
            get() = UrlConstants.RELEASE_URL + UrlConstants.MIMO_TTS_PROXY_PATH
    }
}
