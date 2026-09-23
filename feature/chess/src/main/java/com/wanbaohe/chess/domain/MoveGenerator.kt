package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.ChessMove

private val ORTHOGONAL_DELTAS = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
private val DIAGONAL_DELTAS = listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)
private val KING_DELTAS = ORTHOGONAL_DELTAS + DIAGONAL_DELTAS
private val KNIGHT_DELTAS = listOf(
    -2 to -1, -2 to 1, -1 to -2, -1 to 2,
    1 to -2, 1 to 2, 2 to -1, 2 to 1,
)
private val PROMOTION_TYPES = listOf(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)

/**
 * 伪合法着法生成(不含自陷将军过滤,过滤在 [GameArbiter])。
 * 覆盖:滑动(车/象/后)、马跳、王一步、兵(直进/起始两格/斜吃/吃过路兵/升变四候选)、王车易位。
 */
object MoveGenerator {

    fun pseudoLegalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<ChessMove> {
        val moves = boardState.occupiedSquares()
            .filter { (_, piece) -> piece.side == side }
            .flatMap { (point, piece) -> movesForPiece(boardState, point, piece) }
        return moves + castlingMoves(boardState, side)
    }

    private fun movesForPiece(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<ChessMove> = when (piece.type) {
        PieceType.KING -> stepMoves(boardState, from, piece, KING_DELTAS)
        PieceType.QUEEN -> slideMoves(boardState, from, piece, ORTHOGONAL_DELTAS + DIAGONAL_DELTAS)
        PieceType.ROOK -> slideMoves(boardState, from, piece, ORTHOGONAL_DELTAS)
        PieceType.BISHOP -> slideMoves(boardState, from, piece, DIAGONAL_DELTAS)
        PieceType.KNIGHT -> stepMoves(boardState, from, piece, KNIGHT_DELTAS)
        PieceType.PAWN -> pawnMoves(boardState, from, piece)
    }

    private fun slideMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
        deltas: List<Pair<Int, Int>>,
    ): List<ChessMove> = deltas.flatMap { (df, dr) ->
        buildList {
            var pos = from.offset(df, dr)
            while (pos.isInside()) {
                val target = boardState.pieceAt(pos)
                if (target == null) {
                    add(ChessMove(from, pos, piece))
                } else {
                    if (target.side != piece.side) add(ChessMove(from, pos, piece, target))
                    break
                }
                pos = pos.offset(df, dr)
            }
        }
    }

    private fun stepMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
        deltas: List<Pair<Int, Int>>,
    ): List<ChessMove> = deltas.mapNotNull { (df, dr) ->
        val to = from.offset(df, dr)
        if (!to.isInside()) return@mapNotNull null
        val target = boardState.pieceAt(to)
        if (target?.side == piece.side) return@mapNotNull null
        ChessMove(from, to, piece, target)
    }

    private fun pawnMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<ChessMove> = buildList {
        val forward = if (piece.side == Side.WHITE) 1 else -1
        val startRank = if (piece.side == Side.WHITE) 1 else 6
        val lastRank = if (piece.side == Side.WHITE) 7 else 0

        fun addPawnMove(to: BoardPoint, captured: Piece? = null) {
            if (to.rank == lastRank) {
                // 升变:后/车/象/马 四个候选
                PROMOTION_TYPES.forEach { add(ChessMove(from, to, piece, captured, promotion = it)) }
            } else {
                add(ChessMove(from, to, piece, captured))
            }
        }

        // 直进
        val one = from.offset(0, forward)
        if (one.isInside() && boardState.pieceAt(one) == null) {
            addPawnMove(one)
            val two = from.offset(0, forward * 2)
            if (from.rank == startRank && boardState.pieceAt(two) == null) {
                add(ChessMove(from, two, piece))
            }
        }
        // 斜吃(含吃过路兵)
        listOf(-1, 1).forEach { df ->
            val to = from.offset(df, forward)
            if (!to.isInside()) return@forEach
            val target = boardState.pieceAt(to)
            if (target != null && target.side != piece.side) {
                addPawnMove(to, target)
            } else if (boardState.enPassant == to) {
                val captured = boardState.pieceAt(BoardPoint(to.file, from.rank))
                if (captured?.type == PieceType.PAWN && captured.side != piece.side) {
                    add(ChessMove(from, to, piece, captured))
                }
            }
        }
    }

    /**
     * 王车易位:王车未动(易位权在)、路径空、王不处于将军、途经格不被攻击。
     * 攻击检测借道 [GameArbiter.isSquareAttacked](与象棋 MoveGenerator 借 GameArbiter 同一先例)。
     */
    private fun castlingMoves(boardState: BoardState, side: Side): List<ChessMove> {
        val rights = boardState.castlingRights
        if (rights.isEmpty() || rights == "-") return emptyList()
        val rank = if (side == Side.WHITE) 0 else 7
        val kingFrom = BoardPoint(4, rank)
        val king = boardState.pieceAt(kingFrom)
        if (king?.type != PieceType.KING || king.side != side) return emptyList()
        // 被将军时不可易位
        if (GameArbiter.isSquareAttacked(boardState, kingFrom, side.opposite())) return emptyList()

        return buildList {
            // 短易位(K/k):王 e→g,车 h→f
            val kingSide = if (side == Side.WHITE) 'K' else 'k'
            if (kingSide in rights) {
                val rook = boardState.pieceAt(BoardPoint(7, rank))
                val pathClear = boardState.pieceAt(BoardPoint(5, rank)) == null &&
                    boardState.pieceAt(BoardPoint(6, rank)) == null
                val safe = !GameArbiter.isSquareAttacked(boardState, BoardPoint(5, rank), side.opposite()) &&
                    !GameArbiter.isSquareAttacked(boardState, BoardPoint(6, rank), side.opposite())
                if (rook?.type == PieceType.ROOK && rook.side == side && pathClear && safe) {
                    add(ChessMove(kingFrom, BoardPoint(6, rank), king))
                }
            }
            // 长易位(Q/q):王 e→c,车 a→d;b 格只要求空(不要求不被攻击)
            val queenSide = if (side == Side.WHITE) 'Q' else 'q'
            if (queenSide in rights) {
                val rook = boardState.pieceAt(BoardPoint(0, rank))
                val pathClear = boardState.pieceAt(BoardPoint(1, rank)) == null &&
                    boardState.pieceAt(BoardPoint(2, rank)) == null &&
                    boardState.pieceAt(BoardPoint(3, rank)) == null
                val safe = !GameArbiter.isSquareAttacked(boardState, BoardPoint(2, rank), side.opposite()) &&
                    !GameArbiter.isSquareAttacked(boardState, BoardPoint(3, rank), side.opposite())
                if (rook?.type == PieceType.ROOK && rook.side == side && pathClear && safe) {
                    add(ChessMove(kingFrom, BoardPoint(2, rank), king))
                }
            }
        }
    }

    private fun BoardState.occupiedSquares(): List<Pair<BoardPoint, Piece>> = buildList {
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            for (file in 0 until BoardPoint.FILE_COUNT) {
                val point = BoardPoint(file, rank)
                pieceAt(point)?.let { add(point to it) }
            }
        }
    }
}
