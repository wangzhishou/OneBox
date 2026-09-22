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
    /**
     * 声音总开关（落子音效 + 背景音乐一起管）。
     *
     * 复盘页顶上那个小喇叭就是它：关掉之后整页安静，连 BGM 一起停。
     */
    val soundEnabled: Boolean = true,
)
