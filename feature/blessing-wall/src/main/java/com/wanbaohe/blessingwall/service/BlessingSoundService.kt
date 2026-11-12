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

package com.wanbaohe.blessingwall.service

import android.content.Context
import android.media.SoundPool
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.tts.service.TTSService
import com.t8rin.logger.makeLog
import com.wanbaohe.blessingwall.R
import com.wanbaohe.blessingwall.model.BlessingType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 祈福音效服务。
 *
 * - 木鱼：SoundPool 播放本地 raw 资源，低延迟、支持快速连击；
 * - 其它祈福：以当前 tab 主标题为文本做 TTS 合成，复用 [TTSService] 缓存（首次生成，之后命中缓存直接播放）；
 * - 音效开关持久化到 SharedPreferences。
 */
@Singleton
class BlessingSoundService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ttsService: TTSService,
    private val networkAudioPlayer: NetworkAudioPlayer,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val soundPool = SoundPool.Builder().setMaxStreams(4).build()
    private val blessingSpeechActive = AtomicBoolean(false)
    private val blessingSpeechGeneration = AtomicInteger(0)

    @Volatile
    private var woodenFishSoundId: Int = 0

    @Volatile
    private var woodenFishLoaded: Boolean = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == woodenFishSoundId) woodenFishLoaded = true
        }
        woodenFishSoundId = soundPool.load(context, R.raw.blessing_wooden_fish_tap, 1)
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    fun playWoodenFish() {
        if (!_soundEnabled.value || !woodenFishLoaded) return
        runCatching {
            soundPool.play(woodenFishSoundId, 1f, 1f, 1, 0, 1f)
        }.onFailure { it.makeLog("BlessingSoundService") }
    }

    suspend fun playBlessingAudio(type: BlessingType, text: String) {
        if (!_soundEnabled.value || text.isBlank()) return
        if (type == BlessingType.WOODEN_FISH) return
        // 一句还没说完就再点时，次数照加，语音不叠。
        if (!blessingSpeechActive.compareAndSet(false, true)) return
        val generation = blessingSpeechGeneration.incrementAndGet()
        runCatching {
            val tag = "blessing-${type.key}"
            val cached = ttsService.getAudioByTextAndTag(text, tag)
            val file = cached?.let { File(it.filePath) }
                ?: ttsService.synthesize(text = text, tag = tag).getOrNull()
            if (file == null || !file.exists() || file.length() <= 0L) {
                finishBlessingSpeech(generation)
                return@runCatching
            }
            networkAudioPlayer.playLocalFile(file) { finishBlessingSpeech(generation) }
        }.onFailure {
            finishBlessingSpeech(generation)
            it.makeLog("BlessingSoundService")
        }
        delay(BLESSING_SPEECH_GUARD_MS)
        finishBlessingSpeech(generation)
    }

    private fun finishBlessingSpeech(generation: Int) {
        if (blessingSpeechGeneration.get() == generation) {
            blessingSpeechActive.set(false)
        }
    }

    private companion object {
        const val PREFS_NAME = "blessing_wall_settings"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val BLESSING_SPEECH_GUARD_MS = 15_000L
    }
}
