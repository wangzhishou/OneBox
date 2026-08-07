package com.wanbaohe.xiangqi.domain.model

data class BoardPoint(
    val file: Int,
    val rank: Int,
) {
    val index: Int get() = rank * FILE_COUNT + file

    fun isInside(): Boolean = file in 0 until FILE_COUNT && rank in 0 until RANK_COUNT

    fun offset(fileDelta: Int, rankDelta: Int): BoardPoint =
        BoardPoint(file + fileDelta, rank + rankDelta)

    companion object {
        const val FILE_COUNT = 9
        const val RANK_COUNT = 10
    }
}
