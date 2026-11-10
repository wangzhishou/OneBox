package com.shifenmiao.model.tts

data class TTSProviderDescriptor(
    val type: TTSProviderType,
    val defaultModel: String,
    val defaultVoice: String,
    val supportedVoices: List<String> = emptyList(),
    val isSelectable: Boolean = true,
)
