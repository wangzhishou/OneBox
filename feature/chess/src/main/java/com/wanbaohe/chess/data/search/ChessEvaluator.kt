package com.wanbaohe.chess.data.search

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side

/**
 * 端侧兜底搜索用的局面评估:子力价值 + 子力位置表(PST),白方为正,单位是「厘兵」(centipawn)。
 *
 * PST 按白方视角书写:下标 = rank * 8 + file,rank 0 是白方底线。黑方按 rank 镜像查表
 * (7 - rank),左右不镜像(table 左右对称)。
 *
 * 表值取经典的简化评估表形状:兵越靠近升变行越值钱、马象争中心、车占开放线/次底线、
 * 后早期不宜乱动、王中局贴角、轻子与兵鼓励展开。这里不做机动性、兵形、王翼兵盾等更贵的项,
 * 评估每节点都要跑,深度比精细度更划算。
 */
internal object ChessEvaluator {

    /** 将帅不计入子力:将帅失踪由搜索层按「已被将死」处理,不走评估 */
    private fun material(type: PieceType): Int = when (type) {
        PieceType.PAWN -> 100
        PieceType.KNIGHT -> 320
        PieceType.BISHOP -> 330
        PieceType.ROOK -> 500
        PieceType.QUEEN -> 900
        PieceType.KING -> 0
    }

    /** 白方视角位置分,rank 0 = 白方底线 */
    private val WHITE_PST: Map<PieceType, IntArray> = mapOf(
        PieceType.PAWN to intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0,
            5, 10, 10, -20, -20, 10, 10, 5,
            5, -5, -10, 0, 0, -10, -5, 5,
            0, 0, 0, 20, 20, 0, 0, 0,
            5, 5, 10, 25, 25, 10, 5, 5,
            10, 10, 20, 30, 30, 20, 10, 10,
            50, 50, 50, 50, 50, 50, 50, 50,
            0, 0, 0, 0, 0, 0, 0, 0,
        ),
        PieceType.KNIGHT to intArrayOf(
            -50, -40, -30, -30, -30, -30, -40, -50,
            -40, -20, 0, 5, 5, 0, -20, -40,
            -30, 5, 10, 15, 15, 10, 5, -30,
            -30, 0, 15, 20, 20, 15, 0, -30,
            -30, 5, 15, 20, 20, 15, 5, -30,
            -30, 0, 10, 15, 15, 10, 0, -30,
            -40, -20, 0, 0, 0, 0, -20, -40,
            -50, -40, -30, -30, -30, -30, -40, -50,
        ),
        PieceType.BISHOP to intArrayOf(
            -20, -10, -10, -10, -10, -10, -10, -20,
            -10, 5, 0, 0, 0, 0, 5, -10,
            -10, 10, 10, 10, 10, 10, 10, -10,
            -10, 0, 10, 10, 10, 10, 0, -10,
            -10, 5, 5, 10, 10, 5, 5, -10,
            -10, 0, 5, 10, 10, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -20, -10, -10, -10, -10, -10, -10, -20,
        ),
        PieceType.ROOK to intArrayOf(
            0, 0, 0, 5, 5, 0, 0, 0,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            -5, 0, 0, 0, 0, 0, 0, -5,
            5, 10, 10, 10, 10, 10, 10, 5,
            0, 0, 0, 0, 0, 0, 0, 0,
        ),
        PieceType.QUEEN to intArrayOf(
            -20, -10, -10, -5, -5, -10, -10, -20,
            -10, 0, 5, 0, 0, 0, 0, -10,
            -10, 5, 5, 5, 5, 5, 0, -10,
            0, 0, 5, 5, 5, 5, 0, -5,
            -5, 0, 5, 5, 5, 5, 0, -5,
            -10, 0, 5, 5, 5, 5, 0, -10,
            -10, 0, 0, 0, 0, 0, 0, -10,
            -20, -10, -10, -5, -5, -10, -10, -20,
        ),
        PieceType.KING to intArrayOf(
            20, 30, 10, 0, 0, 10, 30, 20,
            20, 20, 0, 0, 0, 0, 20, 20,
            -10, -20, -20, -20, -20, -20, -20, -10,
            -20, -30, -30, -40, -40, -30, -30, -20,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
            -30, -40, -40, -50, -50, -40, -40, -30,
        ),
    )

    /** 黑方查表用的镜像表:下标仍是 rank * 8 + file,但 rank 已翻转 */
    private val BLACK_PST: Map<PieceType, IntArray> = WHITE_PST.mapValues { (_, table) ->
        IntArray(table.size) { index ->
            val file = index % BoardPoint.FILE_COUNT
            val rank = index / BoardPoint.FILE_COUNT
            table[(BoardPoint.RANK_COUNT - 1 - rank) * BoardPoint.FILE_COUNT + file]
        }
    }

    private val MATERIAL: IntArray = PieceType.entries
        .map { material(it) }
        .toIntArray()

    /**
     * 返回以 [sideToMove] 为视角的分数(negamax 约定:越大越有利于行棋方)。
     */
    fun evaluate(cells: List<Piece?>, sideToMove: Side): Int {
        var score = 0
        for (index in cells.indices) {
            val piece = cells[index] ?: continue
            val table = if (piece.side == Side.WHITE) WHITE_PST else BLACK_PST
            val positional = table[piece.type]?.get(index) ?: 0
            val value = MATERIAL[piece.type.ordinal] + positional
            score += if (piece.side == Side.WHITE) value else -value
        }
        return if (sideToMove == Side.WHITE) score else -score
    }
}
