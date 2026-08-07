package com.shifenmiao.model.tts

data class TTSAudioEntry(
    val id: String,
    val text: String,
    val voice: String,
    val speed: Double,
    val filePath: String,
    val createdAt: Long,
    val tag: String,
    val providerType: TTSProviderType,
)
