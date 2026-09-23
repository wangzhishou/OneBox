package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.ChessMove

/** UCI 坐标记谱(如 "e2e4",升变如 "e7e8q");模块内唯一记谱,中西不分套 */
object UciNotation {

    fun format(move: ChessMove): String = format(move.from, move.to, move.promotion?.let { promotionChar(it) })

    fun format(from: BoardPoint, to: BoardPoint, promotion: String? = null): String =
        from.toCoordinate() + to.toCoordinate() + (promotion ?: "")

    private fun promotionChar(type: com.wanbaohe.chess.domain.model.PieceType): String = when (type) {
        com.wanbaohe.chess.domain.model.PieceType.QUEEN -> "q"
        com.wanbaohe.chess.domain.model.PieceType.ROOK -> "r"
        com.wanbaohe.chess.domain.model.PieceType.BISHOP -> "b"
        com.wanbaohe.chess.domain.model.PieceType.KNIGHT -> "n"
        else -> ""
    }
}

/** 解析/格式化入口(与五子棋 Notation 同形态) */
object Notation {

    fun parse(text: String): BoardPoint? = BoardPoint.fromCoordinate(text.trim())

    /** 解析 UCI 着法(如 "e2e4"/"e7e8q")为起止坐标 */
    fun parseMove(uci: String): Pair<BoardPoint, BoardPoint>? {
        val t = uci.trim()
        if (t.length < 4) return null
        val from = BoardPoint.fromCoordinate(t.substring(0, 2)) ?: return null
        val to = BoardPoint.fromCoordinate(t.substring(2, 4)) ?: return null
        return from to to
    }
}
