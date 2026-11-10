package com.shifenmiao.tts.service

import com.shifenmiao.model.tts.TTSConfig
import com.shifenmiao.model.tts.TTSProviderDescriptor
import com.shifenmiao.model.tts.TTSProviderType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TTSProviderRegistry @Inject constructor(
    providers: Set<@JvmSuppressWildcards TTSProvider>,
) {
    private val providersByType = providers.associateBy { it.descriptor.type }.also { providerMap ->
        require(providerMap.size == providers.size) { "Duplicate TTS provider type" }
        require(providerMap.isNotEmpty()) { "No TTS provider registered" }
    }

    fun contains(type: TTSProviderType): Boolean = providersByType.containsKey(type)

    fun get(type: TTSProviderType): TTSProvider =
        providersByType[type] ?: error("Unsupported TTS provider: $type")

    fun getDescriptors(): List<TTSProviderDescriptor> = providersByType.values
        .map(TTSProvider::descriptor)
        .sortedBy { it.type.ordinal }

    fun createDefaultConfig(type: TTSProviderType): TTSConfig {
        val descriptor = get(type).descriptor
        return TTSConfig(
            providerType = type,
            model = descriptor.defaultModel,
            defaultVoice = descriptor.defaultVoice,
        )
    }

    fun fallbackType(): TTSProviderType =
        if (contains(TTSProviderType.MIMO)) {
            TTSProviderType.MIMO
        } else {
            getDescriptors().first().type
        }
}
