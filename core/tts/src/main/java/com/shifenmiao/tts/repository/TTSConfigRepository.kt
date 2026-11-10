package com.shifenmiao.tts.repository

import android.content.Context
import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderType
import com.shifenmiao.tts.service.TTSProviderRegistry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSConfigRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val providerRegistry: TTSProviderRegistry,
) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _config = MutableStateFlow(loadConfig())
    val config: StateFlow<TTSConfig> = _config.asStateFlow()

    fun getConfig(): TTSConfig = _config.value

    fun updateConfig(config: TTSConfig) {
        _config.value = config
        preferences.edit()
            .putString(KEY_PROVIDER_TYPE, config.providerType.name)
            .putString(KEY_BASE_URL, config.baseUrl)
            .putString(KEY_API_TOKEN, config.apiToken)
            .putString(KEY_PROXY_URL, config.proxyUrl)
            .putString(KEY_PROXY_PATH, config.proxyPath)
            .putString(KEY_PROXY_TOKEN, config.proxyToken)
            .putString(KEY_MODEL, config.model)
            .putString(KEY_VOICE, config.defaultVoice)
            .putFloat(KEY_SPEED, config.defaultSpeed.toFloat())
            .apply()
    }

    private fun loadConfig(): TTSConfig {
        val storedProviderType = runCatching {
            TTSProviderType.valueOf(
                preferences.getString(KEY_PROVIDER_TYPE, TTSProviderType.MIMO.name)
                    ?: TTSProviderType.MIMO.name
            )
        }.getOrDefault(TTSProviderType.MIMO)
        val providerType = storedProviderType.takeIf(providerRegistry::contains)
            ?: providerRegistry.fallbackType()
        val defaults = providerRegistry.createDefaultConfig(providerType)
        val shouldUseProviderDefaults = storedProviderType != providerType

        return TTSConfig(
            providerType = providerType,
            baseUrl = preferences.getString(KEY_BASE_URL, "") ?: "",
            apiToken = preferences.getString(KEY_API_TOKEN, "") ?: "",
            proxyUrl = preferences.getString(KEY_PROXY_URL, "") ?: "",
            proxyPath = preferences.getString(KEY_PROXY_PATH, "") ?: "",
            proxyToken = preferences.getString(KEY_PROXY_TOKEN, "") ?: "",
            model = if (shouldUseProviderDefaults) {
                defaults.model
            } else {
                preferences.getString(KEY_MODEL, defaults.model) ?: defaults.model
            },
            defaultVoice = if (shouldUseProviderDefaults) {
                defaults.defaultVoice
            } else {
                preferences.getString(KEY_VOICE, defaults.defaultVoice) ?: defaults.defaultVoice
            },
            defaultSpeed = preferences.getFloat(KEY_SPEED, 1.0f).toDouble(),
        )
    }

    companion object {
        private const val PREFS_NAME = "tts_config"
        private const val KEY_PROVIDER_TYPE = "provider_type"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_TOKEN = "api_token"
        private const val KEY_PROXY_URL = "proxy_url"
        private const val KEY_PROXY_PATH = "proxy_path"
        private const val KEY_PROXY_TOKEN = "proxy_token"
        private const val KEY_MODEL = "model"
        private const val KEY_VOICE = "voice"
        private const val KEY_SPEED = "speed"
    }
}
