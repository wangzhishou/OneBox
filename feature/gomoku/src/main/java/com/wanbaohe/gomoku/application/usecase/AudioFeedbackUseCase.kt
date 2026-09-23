package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.audio.GomokuAudioDefaults
import com.wanbaohe.gomoku.application.port.outbound.AudioSettings
import com.wanbaohe.gomoku.application.port.outbound.SoundPlayer
import com.wanbaohe.gomoku.application.port.outbound.TtsEngine
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.gomoku.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFeedbackUseCase @Inject constructor(
    private val soundPlayer: SoundPlayer,
    private val ttsEngine: TtsEngine,
) {

    data class SoundProfile(
        val status: GameStatus,
    ) {
        /** 普通落子(非终局)——不触发 TTS 播报 */
        val isPlainMove: Boolean
            get() = status != GameStatus.BLACK_WINS &&
                status != GameStatus.WHITE_WINS &&
                status != GameStatus.DRAW &&
                status != GameStatus.RESIGNED
    }

    suspend fun play(profile: SoundProfile, settings: AudioSettings) {
        if (!settings.soundEnabled) return
        // 每步落子都播报太吵, 普通落子只放音效; 终局(胜负/和棋)才走 TTS 播报。
        if (!profile.isPlainMove && tryTts(profile, settings)) return
        if (tryUrl(profile, settings)) return
        playBeep()
    }

    suspend fun playForMove(afterFen: String, settings: AudioSettings) {
        val board = FenCodec.parse(afterFen)
        val winner = GameArbiter.detectWinner(board)
        val status = when (winner) {
            Side.BLACK -> GameStatus.BLACK_WINS
            Side.WHITE -> GameStatus.WHITE_WINS
            null -> if (board.isFull()) GameStatus.DRAW else GameStatus.PLAYING
        }
        play(SoundProfile(status), settings)
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
        val isTerminal = !profile.isPlainMove
        val url = if (isTerminal) {
            settings.checkSoundUrl.ifBlank { settings.moveSoundUrl }
        } else {
            settings.moveSoundUrl
        }.ifBlank { if (isTerminal) GomokuAudioDefaults.WIN else GomokuAudioDefaults.MOVE }
        soundPlayer.playEffect(url)
        return true
    }

    private suspend fun playBeep() {
        withContext(Dispatchers.Main) {
            soundPlayer.playBeep()
        }
    }

    data class TtsTemplate(
        val tag: String,
        val defaultText: String,
    ) {
        companion object {
            val WIN = TtsTemplate("gomoku-win", AppContext.getString(R.string.gomoku_tts_default_win))
            val DRAW = TtsTemplate("gomoku-draw", AppContext.getString(R.string.gomoku_tts_default_draw))
            val ALL = listOf(WIN, DRAW)
        }
    }

    private fun pickTemplate(profile: SoundProfile): TtsTemplate = when (profile.status) {
        GameStatus.BLACK_WINS, GameStatus.WHITE_WINS, GameStatus.RESIGNED -> TtsTemplate.WIN
        else -> TtsTemplate.DRAW
    }
}
