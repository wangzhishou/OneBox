package com.shifenmiao.model.tts

data class TTSConfig(
    val providerType: TTSProviderType = TTSProviderType.MIMO,
    val baseUrl: String = "",
    val apiToken: String = "",
    val proxyUrl: String = "",
    val proxyPath: String = "",
    val proxyToken: String = "",
    val model: String = "mimo-v2.5-tts",
    val defaultVoice: String = "mimo_default",
    val defaultSpeed: Double = 1.0,
) {
    val hasDirectConfig: Boolean
        get() = baseUrl.isNotBlank() && apiToken.isNotBlank()

    fun isValid(): Boolean = when (providerType) {
        TTSProviderType.MIMO -> hasDirectConfig || proxyUrl.isNotBlank()
        TTSProviderType.OPENAI_COMPATIBLE -> hasDirectConfig || proxyUrl.isNotBlank()
    }

    fun useProxy(): Boolean = !hasDirectConfig && proxyUrl.isNotBlank()

    fun resolveBaseUrl(): String =
        if (baseUrl.isNotBlank()) baseUrl else proxyUrl

    fun resolveApiToken(): String =
        if (apiToken.isNotBlank()) apiToken else proxyToken
}
