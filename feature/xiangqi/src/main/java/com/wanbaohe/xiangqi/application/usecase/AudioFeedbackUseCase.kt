package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.port.outbound.AudioSettings
import com.wanbaohe.xiangqi.application.port.outbound.SoundPlayer
import com.wanbaohe.xiangqi.application.port.outbound.TtsEngine
import com.wanbaohe.xiangqi.domain.model.GameStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R

@Singleton
class AudioFeedbackUseCase @Inject constructor(
    private val soundPlayer: SoundPlayer,
    private val ttsEngine: TtsEngine,
) {

    data class SoundProfile(
        val status: GameStatus,
        val isCapture: Boolean,
    )

    suspend fun play(profile: SoundProfile, settings: AudioSettings) {
        if (tryTts(profile, settings)) return
        if (tryUrl(profile, settings)) return
        playBeep()
    }

    suspend fun playForMove(
        beforeFen: String,
        afterFen: String,
        settings: AudioSettings,
    ) {
        val status = deriveStatus(afterFen)
        val isCapture = isCapture(beforeFen, afterFen)
        play(SoundProfile(status, isCapture), settings)
    }

    private suspend fun tryTts(profile: SoundProfile, settings: AudioSettings): Boolean {
        if (!settings.ttsEnabled) return false
        val template = pickTemplate(profile)
        val text = settings.ttsTemplateTexts[template.tag]
            ?.takeIf { it.isNotBlank() }
            ?: template.defaultText
        val audio = ttsEngine.getAudioByTextAndTag(text, template.tag) ?: return false
        val file = File(audio.filePath)
        if (!file.exists() || file.length() <= 0L) return false
        soundPlayer.playLocalFile(file)
        return true
    }

    private suspend fun tryUrl(profile: SoundProfile, settings: AudioSettings): Boolean {
        val isCheck = profile.status == GameStatus.CHECK
        val url = if (isCheck) {
            settings.checkSoundUrl.ifBlank { settings.moveSoundUrl }
        } else {
            settings.moveSoundUrl
        }
        if (url.isBlank()) return false
        soundPlayer.playEffect(url)
        return true
    }

    private suspend fun playBeep() {
        withContext(Dispatchers.Main) {
            soundPlayer.playBeep()
        }
    }

    private fun deriveStatus(afterFen: String): GameStatus {
        return com.wanbaohe.xiangqi.domain.GameArbiter.evaluateStatus(
            com.wanbaohe.xiangqi.domain.FenCodec.parse(afterFen)
        )
    }

    private fun isCapture(beforeFen: String, afterFen: String): Boolean {
        val before = com.wanbaohe.xiangqi.domain.FenCodec.parse(beforeFen)
        val after = com.wanbaohe.xiangqi.domain.FenCodec.parse(afterFen)
        return before.board.count { it != null } > after.board.count { it != null }
    }

    data class TtsTemplate(
        val tag: String,
        val defaultText: String,
    ) {
        companion object {
            val MOVE = TtsTemplate("xiangqi-move", AppContext.getString(R.string.xiangqi_tts_default_move))
            val CAPTURE = TtsTemplate("xiangqi-capture", AppContext.getString(R.string.xiangqi_tts_default_capture))
            val CHECK = TtsTemplate("xiangqi-check", AppContext.getString(R.string.xiangqi_tts_default_check))
            val CHECKMATE = TtsTemplate("xiangqi-checkmate", AppContext.getString(R.string.xiangqi_tts_default_checkmate))
            val DRAW = TtsTemplate("xiangqi-draw", AppContext.getString(R.string.xiangqi_tts_default_draw))
            val ALL = listOf(MOVE, CAPTURE, CHECK, CHECKMATE, DRAW)
        }
    }

    private fun pickTemplate(profile: SoundProfile): TtsTemplate = when (profile.status) {
        GameStatus.RED_WINS, GameStatus.BLACK_WINS -> TtsTemplate.CHECKMATE
        GameStatus.DRAW -> TtsTemplate.DRAW
        GameStatus.CHECK -> TtsTemplate.CHECK
        else -> if (profile.isCapture) TtsTemplate.CAPTURE else TtsTemplate.MOVE
    }
}
