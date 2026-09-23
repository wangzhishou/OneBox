package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side

/**
 * 棋局结果的**唯一词表**。
 *
 * [GameResultCode] 是持久化到 `GameEntity.resultText` 与导出 JSON `result` 字段的唯一合法取值，
 * 所有写入点(落子、认输、悔棋/重做、重开)必须经 [GameResultResolver] 映射,
 * 禁止直接写 `status.name` 或硬编码字面量。
 */
object GameResultCode {
    /** 非终局。落库为空串(而非 "NONE")以兼容历史数据。 */
    const val NONE = ""
    const val BLACK = "BLACK"
    const val WHITE = "WHITE"
    const val DRAW = "DRAW"
    const val RESIGNED = "RESIGNED"

    /** 唯一合法取值集合,供校验与测试引用。 */
    val ALL = setOf(NONE, BLACK, WHITE, DRAW, RESIGNED)
}

/**
 * 统一的棋局结果解析器:将 [GameStatus] 映射为持久化所需的 resultText / winnerSide 字符串。
 */
object GameResultResolver {

    /**
     * 根据棋局状态返回结果标识文本(用于**持久化**)。
     * BLACK_WINS -> "BLACK";WHITE_WINS -> "WHITE";DRAW -> "DRAW";RESIGNED -> "RESIGNED";其他 -> ""
     */
    fun resultText(status: GameStatus): String = when (status) {
        GameStatus.BLACK_WINS -> GameResultCode.BLACK
        GameStatus.WHITE_WINS -> GameResultCode.WHITE
        GameStatus.DRAW -> GameResultCode.DRAW
        GameStatus.RESIGNED -> GameResultCode.RESIGNED
        else -> GameResultCode.NONE
    }

    /**
     * 根据棋局状态返回获胜方名称(用于**持久化**)。
     * 认输是非盘面终局,`status` 单独推不出胜方,须配合 [resignWinnerCode];故本函数对 RESIGNED 返回空串。
     */
    fun winnerSide(status: GameStatus): String = when (status) {
        GameStatus.BLACK_WINS -> Side.BLACK.name
        GameStatus.WHITE_WINS -> Side.WHITE.name
        else -> ""
    }

    /** 认输方 -> 获胜方代码。认输是唯一需要显式记录胜方的非盘面终局。 */
    fun resignWinnerCode(resigningSide: Side): String = resigningSide.opposite().name

    /** 是否为需要显式记录胜方的非盘面终局。悔棋/重做据此保留结果,不得被盘面状态覆盖。 */
    fun isExplicitTerminal(status: GameStatus): Boolean = status == GameStatus.RESIGNED
}
