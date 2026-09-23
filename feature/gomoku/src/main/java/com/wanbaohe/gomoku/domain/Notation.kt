package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GomokuMove

/** 坐标记谱(如 "H8"),五子棋无中西两套记谱,notationUcci/notationCn 同值 */
object Notation {

    fun format(move: GomokuMove): String = move.to.toCoordinate()

    fun parse(text: String): BoardPoint? = BoardPoint.fromCoordinate(text.trim())
}
