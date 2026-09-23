package com.wanbaohe.chess.data

/**
 * UI 层历史上使用的一套 DTO 别名。
 *
 * 这些类型曾是与 `application.dto` 逐字段重复的独立 data class（Component 层靠一层 `toLegacy()`
 * 纯搬运转换），改一个字段要同步两处、极易漏。现收敛为类型别名：**唯一事实源在 `application.dto`**。
 */
typealias ChessGameSummary = com.wanbaohe.chess.application.dto.GameSummary

typealias ChessPlyRecord = com.wanbaohe.chess.application.dto.PlyRecord

typealias TextExportLabels = com.wanbaohe.chess.application.dto.ExportLabels

data class ChessSettings(
    val moveSoundUrl: String = "",
    val backgroundMusicUrl: String = "",
    val checkSoundUrl: String = "",
    val ttsEnabled: Boolean = true,
    val ttsTemplateTexts: Map<String, String> = emptyMap(),
    val soundEnabled: Boolean = true,
)

data class ChessTTSTemplate(
    val tag: String,
    val labelResId: Int,
    val defaultText: String,
)

object ChessTTSTemplates {
    val CAPTURE = ChessTTSTemplate("chess-capture", com.wanbaohe.chess.R.string.chess_tts_capture, "吃！")
    val CHECK = ChessTTSTemplate("chess-check", com.wanbaohe.chess.R.string.chess_tts_check, "将军！")
    val CHECKMATE = ChessTTSTemplate("chess-checkmate", com.wanbaohe.chess.R.string.chess_tts_checkmate, "将死！")
    val DRAW = ChessTTSTemplate("chess-draw", com.wanbaohe.chess.R.string.chess_tts_draw, "和棋！")
    val ALL = listOf(CAPTURE, CHECK, CHECKMATE, DRAW)
}
