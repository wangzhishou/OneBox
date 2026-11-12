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

package com.wanbaohe.chess.data

import android.media.AudioManager
import android.media.ToneGenerator
import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.wanbaohe.chess.application.port.outbound.CachedAudio
import com.wanbaohe.chess.application.port.outbound.SoundPlayer
import com.wanbaohe.chess.application.port.outbound.TtsEngine
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

    override fun stopEffect() {
        networkAudioPlayer.stopEffect()
    }

    override suspend fun playBackground(url: String) {
        networkAudioPlayer.playBackground(url)
    }

    override fun pauseBackground() {
        networkAudioPlayer.pauseBackground()
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
        return ttsService.synthesize(text = text, tag = tag)
    }

    override suspend fun regenerate(text: String, tag: String): Result<File> {
        return ttsService.regenerate(text = text, tag = tag)
    }

    override suspend fun getAudioByTextAndTag(text: String, tag: String): CachedAudio? {
        val audio = ttsService.getAudioByTextAndTag(text, tag) ?: return null
        return CachedAudio(audio.filePath)
    }

    override fun canPrepareVoice(): Boolean {
        val config = ttsService.getConfig()
        return config.isValid() && (
            !ttsService.isProxyRoute(config) ||
                com.shifenmiao.storage.TokenStorage.isLogin()
            )
    }
}
