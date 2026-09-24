package com.shifenmiao.model.gomoku

/**
 * 五子棋模块对外暴露的只读/写入 DTO。
 *
 * 枚举字段统一用 String 表示（如 mode = "HUMAN_VS_LLM"），
 * 避免 core/model 直接依赖 feature 层的枚举类。
 */

data class GomokuGameSummaryDto(
    val id: String,
    val title: String,
    /** GameMode.name: LOCAL_PVP / HUMAN_VS_LLM / LLM_VS_LLM / ONLINE_PVP */
    val mode: String,
    /** GameStatus.name: NOT_STARTED / PAUSED / PLAYING / BLACK_WINS / WHITE_WINS / DRAW / RESIGNED */
    val status: String,
    val resultText: String,
    val updatedAt: Long,
)

data class GomokuGameDetailDto(
    val id: String,
    val title: String,
    val mode: String,
    val status: String,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val moves: List<GomokuMoveDto>,
)

data class GomokuMoveDto(
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
)

/** import_json 的结构化结果:导入/跳过的手数透传给 Agent 工具返回。 */
data class GomokuImportResultDto(
    val gameId: String,
    val importedPlies: Int,
    val skippedPlies: Int,
)
