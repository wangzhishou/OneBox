package com.wanbaohe.gomoku.domain.model

/** 五子棋只有黑白两方,黑先白后 */
enum class Side {
    BLACK,
    WHITE;

    fun opposite(): Side = if (this == BLACK) WHITE else BLACK
}
