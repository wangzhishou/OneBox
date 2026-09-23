package com.wanbaohe.chess.domain.model

/**
 * 国际象棋着法。
 * [promotion] 为升变目标子(仅兵到末行时非 null);字段名 notationUcci 与象棋/五子棋对齐,
 * 持久化列复用 move_ucci(UCI 坐标,如 "e2e4"/"e7e8q")。
 */
data class ChessMove(
    val from: BoardPoint,
    val to: BoardPoint,
    val piece: Piece,
    val captured: Piece? = null,
    val promotion: PieceType? = null,
    val notationUcci: String = "",
    val notationCn: String = "",
)
