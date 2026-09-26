package com.wanbaohe.xiangqi.data.search

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side

/**
 * 端侧兜底搜索用的局面评估:子力价值 + 子力位置表(PST),红方为正。
 *
 * PST 一律按「红方视角」书写:下标 = rank * 9 + file,rank 0 是黑方底线(棋盘上方)、
 * rank 9 是红方底线。黑方按 rank 镜像查表(9 - rank),因此下表只需维护一份。
 *
 * 表值取自象棋引擎常见的经验形状,量级是「分」,与子力价值同单位:
 * 兵过河逐步升值、车马炮偏好中路与开阔位置、士象归位微调、将帅离底线小幅扣分。
 * 这里刻意不做机动性/兵形等更贵的项——兜底搜索的深度(4~5 层)才是棋力主要来源,
 * 评估函数每节点都会被调用,贵项会直接吃掉一层深度。
 */
internal object XiangqiEvaluator {

    /** 将帅不计入子力:将帅失踪由搜索层按「已被将死」直接处理,不走评估 */
    private fun material(type: PieceType): Int = when (type) {
        PieceType.ROOK -> 900
        PieceType.CANNON -> 450
        PieceType.KNIGHT -> 400
        PieceType.BISHOP -> 200
        PieceType.ADVISOR -> 200
        PieceType.PAWN -> 100
        PieceType.KING -> 0
    }

    /** 红方视角的位置分,rank 0 = 黑方底线 */
    private val RED_PST: Map<PieceType, IntArray> = mapOf(
        PieceType.PAWN to intArrayOf(
            0, 3, 6, 9, 12, 9, 6, 3, 0,
            18, 36, 56, 80, 120, 80, 56, 36, 18,
            14, 26, 42, 60, 80, 60, 42, 26, 14,
            10, 20, 30, 34, 40, 34, 30, 20, 10,
            6, 12, 18, 18, 20, 18, 18, 12, 6,
            2, 0, 8, 0, 8, 0, 8, 0, 2,
            0, 0, -2, 0, 4, 0, -2, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
        ),
        PieceType.ROOK to intArrayOf(
            14, 14, 12, 18, 16, 18, 12, 14, 14,
            16, 20, 18, 24, 26, 24, 18, 20, 16,
            12, 12, 12, 18, 18, 18, 12, 12, 12,
            12, 18, 16, 22, 22, 22, 16, 18, 12,
            12, 14, 12, 18, 18, 18, 12, 14, 12,
            12, 16, 14, 20, 20, 20, 14, 16, 12,
            6, 10, 8, 14, 14, 14, 8, 10, 6,
            4, 8, 6, 14, 12, 14, 6, 8, 4,
            8, 4, 8, 16, 8, 16, 8, 4, 8,
            -2, 10, 6, 14, 12, 14, 6, 10, -2,
        ),
        PieceType.KNIGHT to intArrayOf(
            4, 8, 16, 12, 4, 12, 16, 8, 4,
            4, 10, 28, 16, 8, 16, 28, 10, 4,
            12, 14, 16, 20, 18, 20, 16, 14, 12,
            8, 24, 18, 24, 20, 24, 18, 24, 8,
            6, 16, 14, 18, 16, 18, 14, 16, 6,
            4, 12, 16, 14, 12, 14, 16, 12, 4,
            2, 6, 8, 6, 10, 6, 8, 6, 2,
            4, 2, 8, 8, 4, 8, 8, 2, 4,
            0, 2, 4, 4, -2, 4, 4, 2, 0,
            0, -4, 0, 0, 0, 0, 0, -4, 0,
        ),
        PieceType.CANNON to intArrayOf(
            6, 4, 0, -10, -12, -10, 0, 4, 6,
            2, 2, 0, -4, -14, -4, 0, 2, 2,
            2, 2, 0, -10, -8, -10, 0, 2, 2,
            0, 0, -2, 4, 10, 4, -2, 0, 0,
            0, 0, 0, 2, 8, 2, 0, 0, 0,
            -2, 0, 4, 2, 6, 2, 4, 0, -2,
            0, 0, 0, 2, 4, 2, 0, 0, 0,
            4, 0, 8, 6, 10, 6, 8, 0, 4,
            0, 2, 4, 6, 6, 6, 4, 2, 0,
            0, 0, 2, 6, 6, 6, 2, 0, 0,
        ),
        PieceType.ADVISOR to intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 3, 0, 0, 0, 0,
            0, 0, 0, 2, 0, 2, 0, 0, 0,
        ),
        PieceType.BISHOP to intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 1, 0, 0, 0, 1, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            1, 0, 0, 0, 3, 0, 0, 0, 1,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
        ),
        PieceType.KING to intArrayOf(
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, -2, -3, -2, 0, 0, 0,
            0, 0, 0, 0, 1, 0, 0, 0, 0,
            0, 0, 0, 1, 3, 1, 0, 0, 0,
        ),
    )

    /** 黑方视角直接查表用的镜像表:下标仍是 rank * 9 + file,但 rank 已翻转 */
    private val BLACK_PST: Map<PieceType, IntArray> = RED_PST.mapValues { (_, table) ->
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
            val table = if (piece.side == Side.RED) RED_PST else BLACK_PST
            val positional = table[piece.type]?.get(index) ?: 0
            val value = MATERIAL[piece.type.ordinal] + positional
            score += if (piece.side == Side.RED) value else -value
        }
        return if (sideToMove == Side.RED) score else -score
    }
}
