package com.wanbaohe.gomoku.data

/**
 * UI 层历史上使用的一套 DTO 别名。
 *
 * 这些类型曾是与 `application.dto` 逐字段重复的独立 data class（Component 层靠一层 `toLegacy()`
 * 纯搬运转换），改一个字段要同步两处、极易漏。现收敛为类型别名：**唯一事实源在 `application.dto`**。
 */
typealias GomokuGameSummary = com.wanbaohe.gomoku.application.dto.GameSummary

typealias GomokuPlyRecord = com.wanbaohe.gomoku.application.dto.PlyRecord

typealias TextExportLabels = com.wanbaohe.gomoku.application.dto.ExportLabels

data class GomokuSettings(
    val moveSoundUrl: String = "",
    val backgroundMusicUrl: String = "",
    val checkSoundUrl: String = "",
    val ttsEnabled: Boolean = true,
    val ttsTemplateTexts: Map<String, String> = emptyMap(),
    val soundEnabled: Boolean = true,
)

data class GomokuTTSTemplate(
    val tag: String,
    val labelResId: Int,
    val defaultText: String,
)

object GomokuTTSTemplates {
    val WIN = GomokuTTSTemplate("gomoku-win", com.wanbaohe.gomoku.R.string.gomoku_tts_win, "五连，胜！")
    val DRAW = GomokuTTSTemplate("gomoku-draw", com.wanbaohe.gomoku.R.string.gomoku_tts_draw, "和棋！")
    val ALL = listOf(WIN, DRAW)
}
