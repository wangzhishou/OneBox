package com.wanbaohe.blessingwall.service

import android.content.Context
import android.media.SoundPool
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.tts.service.TTSService
import com.t8rin.logger.makeLog
import com.wanbaohe.blessingwall.R
import com.wanbaohe.blessingwall.model.BlessingType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
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
        val tag = when (type) {
            BlessingType.WEALTH_GOD -> TAG_WEALTH_GOD
            BlessingType.GUANYIN -> TAG_GUANYIN
            BlessingType.INCENSE -> TAG_INCENSE
            BlessingType.WOODEN_FISH -> return
        }
        runCatching {
            val cached = ttsService.getAudioByTextAndTag(text, tag)
            if (cached != null) {
                networkAudioPlayer.playLocalFile(File(cached.filePath))
            } else {
                ttsService.synthesize(text = text, tag = tag)
                    .onSuccess { file -> networkAudioPlayer.playLocalFile(file) }
            }
        }.onFailure { it.makeLog("BlessingSoundService") }
    }

    private companion object {
        const val PREFS_NAME = "blessing_wall_settings"
        const val KEY_SOUND_ENABLED = "sound_enabled"
        const val TAG_WEALTH_GOD = "blessing-wealth-god"
        const val TAG_GUANYIN = "blessing-guanyin"
        const val TAG_INCENSE = "blessing-incense"
    }
}
