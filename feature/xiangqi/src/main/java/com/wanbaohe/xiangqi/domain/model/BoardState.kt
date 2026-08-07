package com.wanbaohe.xiangqi.domain.model

data class BoardState(
    val board: List<Piece?>,
    val sideToMove: Side,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
) {
    init {
        require(board.size == BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) {
            "Board must contain exactly ${BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT} cells"
        }
    }

    fun pieceAt(point: BoardPoint): Piece? =
        if (point.isInside()) board[point.index] else null

    fun withPieceMoved(move: XiangqiMove): BoardState {
        val nextBoard = board.toMutableList()
        nextBoard[move.from.index] = null
        nextBoard[move.to.index] = move.piece
        return copy(
            board = nextBoard,
            sideToMove = sideToMove.opposite(),
            halfMoveClock = nextHalfMoveClock(move),
            fullMoveNumber = if (sideToMove == Side.BLACK) fullMoveNumber + 1 else fullMoveNumber,
        )
    }

    private fun nextHalfMoveClock(move: XiangqiMove): Int =
        if (move.captured != null || move.piece.type == PieceType.PAWN) 0 else halfMoveClock + 1
}
