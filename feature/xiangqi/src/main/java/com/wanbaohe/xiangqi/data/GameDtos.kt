package com.wanbaohe.xiangqi.data

import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType

data class XiangqiGameSummary(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val status: GameStatus,
    val resultText: String,
    val updatedAt: Long,
)

data class XiangqiPlyRecord(
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
    val beforeFen: String,
    val afterFen: String,
    val aiReason: String,
    val aiRawResponse: String,
    val thinkDurationMs: Long,
)

data class XiangqiGameDetail(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val status: GameStatus,
    val startedAt: Long,
    val lastMoveAt: Long,
    val plies: List<XiangqiPlyRecord>,
)

data class TextExportLabels(
    val header: String,
    val titleLabel: String,
    val initialFenLabel: String,
    val resultLabel: String,
)

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
