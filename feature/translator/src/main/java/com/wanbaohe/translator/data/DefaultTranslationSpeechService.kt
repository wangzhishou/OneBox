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

package com.wanbaohe.translator.data

import com.shifenmiao.base.audio.NetworkAudioPlayer
import com.shifenmiao.base.utils.StringUtils
import com.shifenmiao.common.utils.BaseUtils
import com.shifenmiao.interfaces.singleton.AppContext
import com.shifenmiao.storage.TokenStorage
import com.shifenmiao.tts.service.TTSService
import com.wanbaohe.translator.R
import com.wanbaohe.translator.domain.SpeechOutcome
import com.wanbaohe.translator.domain.TranslationSpeechService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultTranslationSpeechService @Inject constructor(
    private val ttsService: TTSService,
    private val audioPlayer: NetworkAudioPlayer,
) : TranslationSpeechService {
    override suspend fun speak(text: String): SpeechOutcome {
        val cached = ttsService.getAudioByTextAndTag(text, AUDIO_TAG)
        if (cached != null) {
            audioPlayer.playLocalFile(java.io.File(cached.filePath))
            return SpeechOutcome.Success
        }
        val isProxyRoute = ttsService.isProxyRoute()
        if (isProxyRoute && !TokenStorage.isLogin()) return SpeechOutcome.LoginRequired
        if (isProxyRoute && !BaseUtils.canConsumePoints(text)) return SpeechOutcome.NoPoints
        return ttsService.synthesize(text = text, tag = AUDIO_TAG).fold(
            onSuccess = { file ->
                audioPlayer.playLocalFile(file)
                if (isProxyRoute) {
                    val points = BaseUtils.tokenToPoints(StringUtils.calculateTokens(text))
                    BaseUtils.consumePoints(
                        degree = points,
                        desc = AppContext.getString(R.string.translation_tts_activity),
                        source = "translator",
                    )
                }
                SpeechOutcome.Success
            },
            onFailure = { SpeechOutcome.Failed(it.message.orEmpty()) },
        )
    }

    override fun stop() = audioPlayer.stopEffect()

    private companion object {
        const val AUDIO_TAG = "translation-read-aloud"
    }
}
