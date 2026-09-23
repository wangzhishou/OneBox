package com.wanbaohe.chess.domain.model

/** 国际象棋白先黑后 */
enum class Side {
    WHITE,
    BLACK;

    fun opposite(): Side = if (this == WHITE) BLACK else WHITE
}
