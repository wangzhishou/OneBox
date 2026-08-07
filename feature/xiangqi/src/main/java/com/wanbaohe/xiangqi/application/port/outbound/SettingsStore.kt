package com.wanbaohe.xiangqi.application.port.outbound

import kotlinx.coroutines.flow.Flow

interface SettingsStore {
    fun observe(): Flow<AudioSettings>
    suspend fun get(): AudioSettings
    suspend fun update(settings: AudioSettings)
}

data class AudioSettings(
    val moveSoundUrl: String = "",
    val backgroundMusicUrl: String = "",
    val checkSoundUrl: String = "",
    val ttsEnabled: Boolean = true,
    val ttsTemplateTexts: Map<String, String> = emptyMap(),
)
