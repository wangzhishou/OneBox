package com.wanbaohe.chess.domain.model

data class Piece(
    val side: Side,
    val type: PieceType,
) {
    /** 子力价值(分),AI 兜底吃子贪心用 */
    val value: Int
        get() = when (type) {
            PieceType.PAWN -> 1
            PieceType.KNIGHT -> 3
            PieceType.BISHOP -> 3
            PieceType.ROOK -> 5
            PieceType.QUEEN -> 9
            PieceType.KING -> 100
        }
}
