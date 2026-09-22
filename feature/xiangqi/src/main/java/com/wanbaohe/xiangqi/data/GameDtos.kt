package com.wanbaohe.xiangqi.data

/**
 * UI 层历史上使用的一套 DTO 别名。
 *
 * 这些类型曾是与 `application.dto` 逐字段重复的独立 data class（Component 层靠一层 `toLegacy()`
 * 纯搬运转换），改一个字段要同步两处、极易漏。现收敛为类型别名：**唯一事实源在 `application.dto`**。
 */
typealias XiangqiGameSummary = com.wanbaohe.xiangqi.application.dto.GameSummary

typealias XiangqiPlyRecord = com.wanbaohe.xiangqi.application.dto.PlyRecord

typealias TextExportLabels = com.wanbaohe.xiangqi.application.dto.ExportLabels

data class XiangqiSettings(
    val moveSoundUrl: String = "",
    val backgroundMusicUrl: String = "",
    val checkSoundUrl: String = "",
    val ttsEnabled: Boolean = true,
    val ttsTemplateTexts: Map<String, String> = emptyMap(),
)

data class XiangqiTTSTemplate(
    val tag: String,
    val labelResId: Int,
    val defaultText: String,
)

object XiangqiTTSTemplates {
    val MOVE = XiangqiTTSTemplate("xiangqi-move", com.wanbaohe.xiangqi.R.string.xiangqi_tts_move, "落子")
    val CAPTURE = XiangqiTTSTemplate("xiangqi-capture", com.wanbaohe.xiangqi.R.string.xiangqi_tts_capture, "吃！")
    val CHECK = XiangqiTTSTemplate("xiangqi-check", com.wanbaohe.xiangqi.R.string.xiangqi_tts_check, "将军！")
    val CHECKMATE = XiangqiTTSTemplate("xiangqi-checkmate", com.wanbaohe.xiangqi.R.string.xiangqi_tts_checkmate, "将死！")
    val DRAW = XiangqiTTSTemplate("xiangqi-draw", com.wanbaohe.xiangqi.R.string.xiangqi_tts_draw, "和棋！")
    val ALL = listOf(MOVE, CAPTURE, CHECK, CHECKMATE, DRAW)
}
