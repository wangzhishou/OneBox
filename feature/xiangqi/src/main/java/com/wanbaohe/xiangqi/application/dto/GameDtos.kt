package com.wanbaohe.xiangqi.application.dto

import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side

data class GameSummary(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val status: GameStatus,
    val resultText: String,
    val updatedAt: Long,
    /** 已走着法数，历史卡片展示用。 */
    val plyCount: Int,
)

data class GameDetail(
    val id: String,
    val title: String,
    val mode: GameMode,
    val redPlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val status: GameStatus,
    /** 落库的结果码（[GameResultCode]），供导出透传与 UI 展示，不要从 [status] 反推。 */
    val resultText: String,
    /**
     * 落库的胜方（`Side.name`）。
     *
     * 认输属于非盘面终局，`status` 单独推不出胜方，必须透传本字段，
     * 否则认输局导出 `winnerSide` 为空、再导入时胜方丢失。
     */
    val winnerSide: String,
    val startedAt: Long,
    val lastMoveAt: Long,
    val plies: List<PlyRecord>,
    val onlineMetadata: OnlineGameMetadata = OnlineGameMetadata(),
)

data class OnlineGameMetadata(
    val roomId: String = "",
    val mySide: Side = Side.RED,
    val opponentName: String = "",
    val opponentAvatarUrl: String = "",
    val initialFen: String = "",
)

data class PlyRecord(
    val ply: Int,
    val moveUcci: String,
    val moveCn: String,
    /** 行棋方。文本棋谱与回放着法表必须按它分组，不能按 ply 的奇偶/位置配对（黑先残局会整体错位）。 */
    val moverSide: Side,
    val beforeFen: String,
    val afterFen: String,
    val aiReason: String,
    val aiRawResponse: String,
    val thinkDurationMs: Long,
)

/**
 * 回放着法表的一行：一个回合的红方着手与黑方着手。
 *
 * 黑先局面下首行的 [red] 为 null，渲染时应显示省略占位（文本棋谱写作 `1. ... <黑手>`）。
 * 导出与 UI 共用同一份分组结果，避免两处各自 `chunked(2)` 造成同源错位。
 */
data class NotationRow(
    val turn: Int,
    val red: PlyRecord?,
    val black: PlyRecord?,
)

object NotationRows {

    /**
     * 按 [PlyRecord.moverSide] 把着法表折叠成回合行。
     *
     * 关键：**红方只能在"本行还没有黑手"时占用当前行**。黑先局面下第一个着手是黑方，
     * 它必须单独成为 `1. ... <黑手>`，随后红方才与下一个黑手配成 `2. <红手> <黑手>`。
     * 若红方到达时不检查本行是否已由黑方开局，红手会被塞到黑手前面凑成一行（R1 排在 B1 前），
     * 整谱错位——这正是本函数要修的场景。
     *
     * 同一方连续两手（导入残缺棋谱、悔棋后重下等）时开启新行而不是互相覆盖，保证不丢着法。
     */
    fun of(plies: List<PlyRecord>): List<NotationRow> {
        val rows = mutableListOf<NotationRow>()
        var red: PlyRecord? = null
        var black: PlyRecord? = null

        fun flush() {
            if (red == null && black == null) return
            rows += NotationRow(turn = rows.size + 1, red = red, black = black)
            red = null
            black = null
        }

        plies.forEach { ply ->
            when (ply.moverSide) {
                Side.RED -> {
                    // red 已占 或 本行是"黑先开局行"（只有 black）——两种情况都要先收行
                    if (red != null || black != null) flush()
                    red = ply
                }
                Side.BLACK -> {
                    if (black != null) flush()
                    black = ply
                }
            }
        }
        flush()
        return rows
    }
}

data class ExportLabels(
    val header: String,
    val titleLabel: String,
    val initialFenLabel: String,
    val resultLabel: String,
)
