package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Side

/**
 * 棋局结果的**唯一词表**。
 *
 * [GameResultCode] 是持久化到 `GameEntity.resultText` 与导出 JSON `result` 字段的唯一合法取值，
 * 所有写入点（落子、认输、悔棋/重做、重开）必须经 [GameResultResolver] 映射，
 * 禁止直接写 `status.name` 或硬编码字面量——否则同一字段会出现多套方言，
 * 消费方（历史列表、导出、回放）无法可靠解读。
 */
object GameResultCode {
    /** 非终局。落库为空串（而非 "NONE"）以兼容历史数据。 */
    const val NONE = ""
    const val RED = "RED"
    const val BLACK = "BLACK"
    const val DRAW = "DRAW"
    const val RESIGNED = "RESIGNED"

    /** 唯一合法取值集合，供校验与测试引用。 */
    val ALL = setOf(NONE, RED, BLACK, DRAW, RESIGNED)
}

/**
 * 统一的棋局结果解析器。
 *
 * 将 [GameStatus] 映射为持久化所需的 resultText / winnerSide 字符串，
 * 避免在多个 UseCase 中重复相同的 when 逻辑。
 *
 * 与 [GameStatus] 的分工：`GameStatus` 是**状态机**（含 PLAYING/CHECK/PAUSED/NOT_STARTED），
 * `resultText` 只表达**终局结果**，非终局一律为 [GameResultCode.NONE]。
 */
object GameResultResolver {

    /**
     * 根据棋局状态返回结果标识文本（用于**持久化**）。
     *
     * - RED_WINS -> "RED"
     * - BLACK_WINS -> "BLACK"
     * - DRAW -> "DRAW"
     * - RESIGNED -> "RESIGNED"
     * - 其他（进行中/暂停/未开始）-> ""
     */
    fun resultText(status: GameStatus): String = when (status) {
        GameStatus.RED_WINS -> GameResultCode.RED
        GameStatus.BLACK_WINS -> GameResultCode.BLACK
        GameStatus.DRAW -> GameResultCode.DRAW
        GameStatus.RESIGNED -> GameResultCode.RESIGNED
        else -> GameResultCode.NONE
    }

    /**
     * 根据棋局状态返回获胜方名称（用于**持久化**）。
     *
     * - RED_WINS -> Side.RED.name
     * - BLACK_WINS -> Side.BLACK.name
     * - 其他 -> ""
     *
     * 认输是非盘面终局，`status` 单独推不出胜方，须配合 [resignWinnerCode]；故本函数对 RESIGNED 返回空串。
     */
    fun winnerSide(status: GameStatus): String = when (status) {
        GameStatus.RED_WINS -> Side.RED.name
        GameStatus.BLACK_WINS -> Side.BLACK.name
        else -> ""
    }

    /** 认输方 -> 获胜方代码。认输是唯一需要显式记录胜方的非盘面终局。 */
    fun resignWinnerCode(resigningSide: Side): String = resigningSide.opposite().name

    /** 是否为需要显式记录胜方的非盘面终局。悔棋/重做据此保留结果，不得被盘面状态覆盖。 */
    fun isExplicitTerminal(status: GameStatus): Boolean = status == GameStatus.RESIGNED
}
