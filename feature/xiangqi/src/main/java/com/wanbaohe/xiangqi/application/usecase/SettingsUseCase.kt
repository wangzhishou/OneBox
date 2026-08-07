package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.port.outbound.AudioSettings
import com.wanbaohe.xiangqi.application.port.outbound.SettingsStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsUseCase @Inject constructor(
    private val store: SettingsStore,
) {

    fun observe(): Flow<AudioSettings> = store.observe()

    suspend fun current(): AudioSettings = store.get()

    suspend fun updateMoveSoundUrl(url: String) {
        store.update(store.get().copy(moveSoundUrl = url.trim()))
    }

    suspend fun updateBackgroundMusicUrl(url: String) {
        store.update(store.get().copy(backgroundMusicUrl = url.trim()))
    }

    suspend fun updateCheckSoundUrl(url: String) {
        store.update(store.get().copy(checkSoundUrl = url.trim()))
    }

    suspend fun updateTTSEnabled(enabled: Boolean) {
        store.update(store.get().copy(ttsEnabled = enabled))
    }

    suspend fun updateTTSTemplateText(tag: String, text: String) {
        val current = store.get()
        val next = current.ttsTemplateTexts.toMutableMap()
        if (text.isBlank()) next.remove(tag) else next[tag] = text.trim()
        store.update(current.copy(ttsTemplateTexts = next))
    }
}
