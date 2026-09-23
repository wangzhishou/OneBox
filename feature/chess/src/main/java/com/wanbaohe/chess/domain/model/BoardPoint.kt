package com.wanbaohe.chess.domain.model

/** 8×8 棋盘坐标;file 0-7 对应列 a-h,rank 0-7 对应行 1-8(rank 0 是白方底线) */
data class BoardPoint(
    val file: Int,
    val rank: Int,
) {
    val index: Int get() = rank * FILE_COUNT + file

    fun isInside(): Boolean = file in 0 until FILE_COUNT && rank in 0 until RANK_COUNT

    fun offset(fileDelta: Int, rankDelta: Int): BoardPoint =
        BoardPoint(file + fileDelta, rank + rankDelta)

    /** UCI 坐标记谱,如 "e4" */
    fun toCoordinate(): String = "${('a' + file)}${rank + 1}"

    companion object {
        const val FILE_COUNT = 8
        const val RANK_COUNT = 8

        fun fromCoordinate(text: String): BoardPoint? {
            if (text.length != 2) return null
            val file = text[0].lowercaseChar() - 'a'
            val rank = text[1].digitToIntOrNull()?.minus(1) ?: return null
            return BoardPoint(file, rank).takeIf { it.isInside() }
        }
    }
}
