package com.wanbaohe.xiangqi.domain.model

data class XiangqiMove(
    val from: BoardPoint,
    val to: BoardPoint,
    val piece: Piece,
    val captured: Piece? = null,
    val notationUcci: String = "",
    val notationCn: String = "",
)
