package com.wanbaohe.gomoku.data

import android.content.Context
import android.content.SharedPreferences
import com.wanbaohe.gomoku.application.port.outbound.AudioSettings
import com.wanbaohe.gomoku.application.port.outbound.SettingsStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPrefsAdapter @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun observe(): Flow<AudioSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(load())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(load())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override suspend fun get(): AudioSettings = load()

    override suspend fun update(settings: AudioSettings) {
        prefs.edit().apply {
            putString(KEY_MOVE_SOUND_URL, settings.moveSoundUrl)
            putString(KEY_BACKGROUND_MUSIC_URL, settings.backgroundMusicUrl)
            putString(KEY_CHECK_SOUND_URL, settings.checkSoundUrl)
            putBoolean(KEY_TTS_ENABLED, settings.ttsEnabled)
            putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            TTS_TAGS.forEach { remove("tts_text_$it") }
            settings.ttsTemplateTexts.forEach { (tag, text) ->
                putString("tts_text_$tag", text)
            }
            apply()
        }
    }

    private fun load(): AudioSettings {
        val ttsTexts = mutableMapOf<String, String>()
        TTS_TAGS.forEach { tag ->
            prefs.getString("tts_text_$tag", null)?.let { ttsTexts[tag] = it }
        }
        return AudioSettings(
            moveSoundUrl = prefs.getString(KEY_MOVE_SOUND_URL, "").orEmpty(),
            backgroundMusicUrl = prefs.getString(KEY_BACKGROUND_MUSIC_URL, "").orEmpty(),
            checkSoundUrl = prefs.getString(KEY_CHECK_SOUND_URL, "").orEmpty(),
            ttsEnabled = prefs.getBoolean(KEY_TTS_ENABLED, true),
            ttsTemplateTexts = ttsTexts,
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
        )
    }

    companion object {
        private const val PREFS_NAME = "gomoku_settings"
        private const val KEY_MOVE_SOUND_URL = "move_sound_url"
        private const val KEY_BACKGROUND_MUSIC_URL = "background_music_url"
        private const val KEY_CHECK_SOUND_URL = "check_sound_url"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private val TTS_TAGS = listOf("gomoku-win", "gomoku-draw")
    }
}
