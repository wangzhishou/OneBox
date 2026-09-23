package com.wanbaohe.chess.domain.model

/**
 * 国际象棋局面。标准 FEN 为事实源(见 [com.wanbaohe.chess.domain.FenCodec])。
 *
 * @param castlingRights 易位权,FEN 记号子串("KQkq" 的任意子集,"-" 或空串表示无)
 * @param enPassant 吃过路兵目标格(上一手兵进两格时其身后的格),无则 null
 */
data class BoardState(
    val board: List<Piece?>,
    val sideToMove: Side,
    val castlingRights: String = "KQkq",
    val enPassant: BoardPoint? = null,
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

    fun withPieceMoved(move: ChessMove): BoardState {
        val nextBoard = board.toMutableList()
        nextBoard[move.from.index] = null

        // 吃过路兵:被吃的兵不在目标格上
        if (move.piece.type == PieceType.PAWN && enPassant == move.to && move.captured == null &&
            move.from.file != move.to.file
        ) {
            nextBoard[BoardPoint(move.to.file, move.from.rank).index] = null
        }
        // 王车易位:车跟王一起动
        if (move.piece.type == PieceType.KING && kotlin.math.abs(move.to.file - move.from.file) == 2) {
            val rank = move.from.rank
            if (move.to.file > move.from.file) {
                nextBoard[BoardPoint(5, rank).index] = nextBoard[BoardPoint(7, rank).index]
                nextBoard[BoardPoint(7, rank).index] = null
            } else {
                nextBoard[BoardPoint(3, rank).index] = nextBoard[BoardPoint(0, rank).index]
                nextBoard[BoardPoint(0, rank).index] = null
            }
        }

        nextBoard[move.to.index] = move.promotion?.let { Piece(move.piece.side, it) } ?: move.piece

        return copy(
            board = nextBoard,
            sideToMove = sideToMove.opposite(),
            castlingRights = nextCastlingRights(move),
            enPassant = nextEnPassant(move),
            halfMoveClock = if (move.captured != null || move.piece.type == PieceType.PAWN) 0 else halfMoveClock + 1,
            fullMoveNumber = if (sideToMove == Side.BLACK) fullMoveNumber + 1 else fullMoveNumber,
        )
    }

    /** 王/车动了或被吃,对应易位权失效 */
    private fun nextCastlingRights(move: ChessMove): String {
        var rights = castlingRights
        fun drop(vararg flags: Char) {
            flags.forEach { rights = rights.replace(it.toString(), "") }
        }
        when (move.piece.side) {
            Side.WHITE -> {
                if (move.piece.type == PieceType.KING) drop('K', 'Q')
                if (move.from == BoardPoint(0, 0) || move.to == BoardPoint(0, 0)) drop('Q')
                if (move.from == BoardPoint(7, 0) || move.to == BoardPoint(7, 0)) drop('K')
            }
            Side.BLACK -> {
                if (move.piece.type == PieceType.KING) drop('k', 'q')
                if (move.from == BoardPoint(0, 7) || move.to == BoardPoint(0, 7)) drop('q')
                if (move.from == BoardPoint(7, 7) || move.to == BoardPoint(7, 7)) drop('k')
            }
        }
        return rights.ifEmpty { "-" }
    }

    private fun nextEnPassant(move: ChessMove): BoardPoint? {
        if (move.piece.type != PieceType.PAWN) return null
        if (kotlin.math.abs(move.to.rank - move.from.rank) != 2) return null
        return BoardPoint(move.from.file, (move.from.rank + move.to.rank) / 2)
    }
}
