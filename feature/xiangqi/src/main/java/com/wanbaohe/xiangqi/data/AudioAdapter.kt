package com.wanbaohe.xiangqi.data

import android.media.AudioManager
import android.media.ToneGenerator
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.wanbaohe.xiangqi.application.port.outbound.CachedAudio
import com.wanbaohe.xiangqi.application.port.outbound.SoundPlayer
import com.wanbaohe.xiangqi.application.port.outbound.TtsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioAdapter @Inject constructor(
    private val networkAudioPlayer: NetworkAudioPlayer,
) : SoundPlayer {

    override suspend fun playLocalFile(file: File) {
        networkAudioPlayer.playLocalFile(file)
    }

    override suspend fun playEffect(url: String) {
        networkAudioPlayer.playEffect(url)
    }

    override suspend fun playBackground(url: String) {
        networkAudioPlayer.playBackground(url)
    }

    override fun stopBackground() {
        networkAudioPlayer.stopBackground()
    }

    override suspend fun playBeep() {
        withContext(Dispatchers.Main) {
            runCatching {
                val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP, 90)
                Thread {
                    Thread.sleep(160)
                    runCatching { tone.release() }
                }.start()
            }
        }
    }
}

@Singleton
class TtsAdapter @Inject constructor(
    private val ttsService: com.shifenmiao.tts.service.TTSService,
) : TtsEngine {

    override suspend fun synthesize(text: String, tag: String): Result<File> {
        return ttsService.synthesize(text, tag)
    }

    override suspend fun regenerate(text: String, tag: String): Result<File> {
        return ttsService.regenerate(text, tag)
    }

    override suspend fun getAudioByTextAndTag(text: String, tag: String): CachedAudio? {
        val audio = ttsService.getAudioByTextAndTag(text, tag) ?: return null
        return CachedAudio(audio.filePath)
    }
}
