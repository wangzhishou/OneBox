package com.wanbaohe.chess.application.usecase

import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.chess.R
import com.wanbaohe.chess.application.audio.ChessAudioDefaults
import com.wanbaohe.chess.application.port.outbound.AudioSettings
import com.wanbaohe.chess.application.port.outbound.SoundPlayer
import com.wanbaohe.chess.application.port.outbound.TtsEngine
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.model.GameStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioFeedbackUseCase @Inject constructor(
    private val soundPlayer: SoundPlayer,
    private val ttsEngine: TtsEngine,
) {
    private val prepareMutex = Mutex()
    private val preparingKeys = mutableSetOf<String>()

    data class SoundProfile(
        val status: GameStatus,
        val isCapture: Boolean,
    )

    fun stopEffects() {
        soundPlayer.stopEffect()
    }

    suspend fun play(profile: SoundProfile, settings: AudioSettings) {
        if (!settings.soundEnabled) return
        val cue = cueOf(profile)
        if (cue == Cue.SILENT) return
        if (cue != Cue.MOVE && tryTts(cue, settings)) return
        playCue(cue, settings)
    }

    suspend fun playForMove(
        beforeFen: String,
        afterFen: String,
        settings: AudioSettings,
    ) {
        val status = com.wanbaohe.chess.domain.GameArbiter.evaluateStatus(FenCodec.parse(afterFen))
        val before = FenCodec.parse(beforeFen)
        val after = FenCodec.parse(afterFen)
        val isCapture = before.board.count { it != null } > after.board.count { it != null }
        play(SoundProfile(status, isCapture), settings)
    }

    /** 登录且语音开关开着时，把还没有文件的默认句补上。失败则继续用音效。 */
    suspend fun prepareCustomVoice(settings: AudioSettings) {
        if (!settings.ttsEnabled || !ttsEngine.canPrepareVoice()) return
        for (template in TtsTemplate.ALL) {
            val text = lineText(settings, template)
            val key = "${template.tag}\u0000$text"
            if (hasCachedVoice(text, template.tag)) continue
            val claimed = prepareMutex.withLock { preparingKeys.add(key) }
            if (!claimed) continue
            try {
                ttsEngine.synthesize(text, template.tag)
            } finally {
                prepareMutex.withLock { preparingKeys.remove(key) }
            }
        }
    }

    private suspend fun tryTts(cue: Cue, settings: AudioSettings): Boolean {
        if (!settings.ttsEnabled || !ttsEngine.isLoggedIn()) return false
        val template = templateFor(cue)
        val text = lineText(settings, template)
        if (!hasCachedVoice(text, template.tag)) return false
        val audio = ttsEngine.getAudioByTextAndTag(text, template.tag) ?: return false
        soundPlayer.playLocalFile(File(audio.filePath))
        return true
    }

    private suspend fun hasCachedVoice(text: String, tag: String): Boolean {
        val audio = ttsEngine.getAudioByTextAndTag(text, tag) ?: return false
        val file = File(audio.filePath)
        return file.exists() && file.length() > 0L
    }

    private suspend fun playCue(cue: Cue, settings: AudioSettings) {
        val url = when (cue) {
            Cue.MOVE -> settings.moveSoundUrl.ifBlank { ChessAudioDefaults.MOVE }
            Cue.CAPTURE -> ChessAudioDefaults.CAPTURE
            Cue.CHECK -> settings.checkSoundUrl.ifBlank { ChessAudioDefaults.CHECK }
            Cue.MATE -> ChessAudioDefaults.CHECKMATE
            Cue.DRAW -> ChessAudioDefaults.DRAW
            Cue.SILENT -> return
        }
        if (url.isBlank()) playBeep() else soundPlayer.playEffect(url)
    }

    private suspend fun playBeep() {
        withContext(Dispatchers.Main) {
            soundPlayer.playBeep()
        }
    }

    private fun lineText(settings: AudioSettings, template: TtsTemplate): String =
        settings.ttsTemplateTexts[template.tag]?.takeIf { it.isNotBlank() } ?: template.defaultText()

    private enum class Cue { SILENT, MOVE, CAPTURE, CHECK, MATE, DRAW }

    private fun cueOf(profile: SoundProfile): Cue = when (profile.status) {
        GameStatus.WHITE_WINS, GameStatus.BLACK_WINS -> Cue.MATE
        GameStatus.DRAW -> Cue.DRAW
        GameStatus.RESIGNED -> Cue.SILENT
        GameStatus.CHECK -> Cue.CHECK
        else -> if (profile.isCapture) Cue.CAPTURE else Cue.MOVE
    }

    private fun templateFor(cue: Cue): TtsTemplate = when (cue) {
        Cue.MATE -> TtsTemplate.CHECKMATE
        Cue.DRAW -> TtsTemplate.DRAW
        Cue.CHECK -> TtsTemplate.CHECK
        else -> TtsTemplate.CAPTURE
    }

    data class TtsTemplate(
        val tag: String,
        val defaultTextRes: Int,
    ) {
        fun defaultText(): String = AppContext.getString(defaultTextRes)

        companion object {
            val CAPTURE = TtsTemplate("chess-capture", R.string.chess_tts_default_capture)
            val CHECK = TtsTemplate("chess-check", R.string.chess_tts_default_check)
            val CHECKMATE = TtsTemplate("chess-checkmate", R.string.chess_tts_default_checkmate)
            val DRAW = TtsTemplate("chess-draw", R.string.chess_tts_default_draw)
            val ALL = listOf(CAPTURE, CHECK, CHECKMATE, DRAW)
        }
    }
}
