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
    val soundEnabled: Boolean = true,
)

data class XiangqiTTSTemplate(
    val tag: String,
    val labelResId: Int,
    val defaultTextRes: Int,
) {
    fun defaultText(): String = com.shifenmiao.interfaces.singleton.AppContext.getString(defaultTextRes)
}

object XiangqiTTSTemplates {
    val CAPTURE = XiangqiTTSTemplate(
        "xiangqi-capture",
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_capture,
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_default_capture,
    )
    val CHECK = XiangqiTTSTemplate(
        "xiangqi-check",
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_check,
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_default_check,
    )
    val CHECKMATE = XiangqiTTSTemplate(
        "xiangqi-checkmate",
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_checkmate,
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_default_checkmate,
    )
    val DRAW = XiangqiTTSTemplate(
        "xiangqi-draw",
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_draw,
        com.wanbaohe.xiangqi.R.string.xiangqi_tts_default_draw,
    )
    val ALL = listOf(CAPTURE, CHECK, CHECKMATE, DRAW)
}
