package com.shifenmiao.model.xiangqi

/**
 * 象棋模块对外暴露的只读/写入 DTO。
 *
 * 枚举字段统一用 String 表示（如 mode = "HUMAN_VS_LLM"），
 * 避免 core/model 直接依赖 feature 层的枚举类。
 */

data class XiangqiGameSummaryDto(
    val id: String,
    val title: String,
    /** GameMode.name: LOCAL_PVP / HUMAN_VS_LLM / LLM_VS_LLM / ONLINE_PVP */
    val mode: String,
    /** GameStatus.name: NOT_STARTED / IN_PROGRESS / CHECK / RED_WINS / BLACK_WINS / DRAW / RESIGNED */
    val status: String,
    val resultText: String,
    val updatedAt: Long,
)

data class XiangqiGameDetailDto(
    val id: String,
    val title: String,
    val mode: String,
    val status: String,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val moves: List<XiangqiMoveDto>,
)

data class XiangqiMoveDto(
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
)
