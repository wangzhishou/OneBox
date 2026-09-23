package com.wanbaohe.gomoku.domain.model

/**
 * 五子棋着法:在空位落一子,无吃子/无起点。
 * 字段名与象棋对齐(notationUcci/notationCn),持久化列复用 move_ucci/move_cn。
 */
data class GomokuMove(
    val to: BoardPoint,
    val side: Side,
    val notationUcci: String = "",
    val notationCn: String = "",
)
