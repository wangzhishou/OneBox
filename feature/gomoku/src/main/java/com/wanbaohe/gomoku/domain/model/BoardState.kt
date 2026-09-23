package com.wanbaohe.gomoku.domain.model

/**
 * 五子棋局面:15×15 格子,每格为空或一方棋子。
 * FEN 风格字符串为事实源(见 [com.wanbaohe.gomoku.domain.FenCodec])。
 */
data class BoardState(
    val board: List<Side?>,
    val sideToMove: Side,
    val moveNumber: Int = 1,
) {
    init {
        require(board.size == BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) {
            "Board must contain exactly ${BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT} cells"
        }
    }

    fun stoneAt(point: BoardPoint): Side? =
        if (point.isInside()) board[point.index] else null

    fun isEmpty(point: BoardPoint): Boolean = stoneAt(point) == null

    fun withStonePlaced(move: GomokuMove): BoardState {
        val nextBoard = board.toMutableList()
        nextBoard[move.to.index] = move.side
        return copy(
            board = nextBoard,
            sideToMove = sideToMove.opposite(),
            moveNumber = moveNumber + 1,
        )
    }

    fun placedCount(): Int = board.count { it != null }

    fun isFull(): Boolean = board.all { it != null }

    companion object {
        fun empty(sideToMove: Side = Side.BLACK): BoardState =
            BoardState(
                board = List(BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) { null },
                sideToMove = sideToMove,
            )
    }
}
