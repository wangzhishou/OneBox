/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.xiangqi.data

import android.content.Context
import android.content.SharedPreferences
import com.wanbaohe.xiangqi.application.port.outbound.AudioSettings
import com.wanbaohe.xiangqi.application.port.outbound.SettingsStore
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
        private const val PREFS_NAME = "xiangqi_settings"
        private const val KEY_MOVE_SOUND_URL = "move_sound_url"
        private const val KEY_BACKGROUND_MUSIC_URL = "background_music_url"
        private const val KEY_CHECK_SOUND_URL = "check_sound_url"
        private const val KEY_TTS_ENABLED = "tts_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private val TTS_TAGS = listOf(
            "xiangqi-capture", "xiangqi-check",
            "xiangqi-checkmate", "xiangqi-draw",
        )
    }
}
