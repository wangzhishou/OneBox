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
        if (!TokenStorage.isLogin()) return SpeechOutcome.LoginRequired
        if (!BaseUtils.canConsumePoints(text)) return SpeechOutcome.NoPoints
        return ttsService.synthesize(text = text, tag = AUDIO_TAG).fold(
            onSuccess = { file ->
                audioPlayer.playLocalFile(file)
                val points = BaseUtils.tokenToPoints(StringUtils.calculateTokens(text))
                BaseUtils.consumePoints(
                    degree = points,
                    desc = AppContext.getString(R.string.translation_tts_activity),
                    source = "translator",
                )
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
