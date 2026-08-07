package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Side

/**
 * 统一的棋局结果解析器。
 *
 * 将 [GameStatus] 映射为持久化所需的 resultText / winnerSide 字符串，
 * 避免在多个 UseCase 中重复相同的 when 逻辑。
 */
object GameResultResolver {

    /**
     * 根据棋局状态返回结果标识文本（用于存储）。
     *
     * - RED_WINS -> "RED"
     * - BLACK_WINS -> "BLACK"
     * - DRAW -> "DRAW"
     * - 其他 -> ""
     */
    fun resultText(status: GameStatus): String = when (status) {
        GameStatus.RED_WINS -> "RED"
        GameStatus.BLACK_WINS -> "BLACK"
        GameStatus.DRAW -> "DRAW"
        else -> ""
    }

    /**
     * 根据棋局状态返回获胜方名称（用于存储）。
     *
     * - RED_WINS -> Side.RED.name
     * - BLACK_WINS -> Side.BLACK.name
     * - 其他 -> ""
     */
    fun winnerSide(status: GameStatus): String = when (status) {
        GameStatus.RED_WINS -> Side.RED.name
        GameStatus.BLACK_WINS -> Side.BLACK.name
        else -> ""
    }
}
