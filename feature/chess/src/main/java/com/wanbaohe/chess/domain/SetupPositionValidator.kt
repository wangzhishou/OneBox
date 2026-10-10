package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import kotlin.math.abs

sealed interface SetupPositionIssue {
    data class KingCount(val side: Side) : SetupPositionIssue
    data class ImpossibleMaterial(val side: Side) : SetupPositionIssue
    data object KingsAdjacent : SetupPositionIssue
    data object PawnOnPromotionRank : SetupPositionIssue
    data object InvalidCastling : SetupPositionIssue
    data object InvalidEnPassant : SetupPositionIssue
    data object WrongTurn : SetupPositionIssue
    data object NoLegalMoves : SetupPositionIssue
}

object SetupPositionValidator {
    fun validate(board: BoardState): SetupPositionIssue? {
        for (side in Side.entries) {
            val pieces = board.board.filterNotNull().filter { it.side == side }
            if (pieces.count { it.type == PieceType.KING } != 1) return SetupPositionIssue.KingCount(side)
            val pawns = pieces.count { it.type == PieceType.PAWN }
            // Extra major/minor pieces are legal promotions, but each needs a missing pawn.
            val promotions = listOf(
                PieceType.QUEEN to 1, PieceType.ROOK to 2,
                PieceType.BISHOP to 2, PieceType.KNIGHT to 2,
            ).sumOf { (type, initial) -> (pieces.count { it.type == type } - initial).coerceAtLeast(0) }
            if (pawns > 8 || pieces.size > 16 || promotions > 8 - pawns) {
                return SetupPositionIssue.ImpossibleMaterial(side)
            }
        }
        val white = requireNotNull(GameArbiter.findKing(board, Side.WHITE))
        val black = requireNotNull(GameArbiter.findKing(board, Side.BLACK))
        if (abs(white.file - black.file) <= 1 && abs(white.rank - black.rank) <= 1) {
            return SetupPositionIssue.KingsAdjacent
        }
        if ((0..7).any { file ->
                board.pieceAt(BoardPoint(file, 0))?.type == PieceType.PAWN ||
                    board.pieceAt(BoardPoint(file, 7))?.type == PieceType.PAWN
            }
        ) return SetupPositionIssue.PawnOnPromotionRank
        if (!hasValidCastling(board)) return SetupPositionIssue.InvalidCastling
        if (!hasValidEnPassant(board)) return SetupPositionIssue.InvalidEnPassant
        if (GameArbiter.isInCheck(board, board.sideToMove.opposite())) return SetupPositionIssue.WrongTurn
        val status = GameArbiter.evaluateStatus(board)
        if (status != GameStatus.PLAYING && status != GameStatus.CHECK) return SetupPositionIssue.NoLegalMoves
        return null
    }

    fun hasValidCastling(board: BoardState): Boolean {
        val rights = board.castlingRights
        if (rights == "-" || rights.isEmpty()) return true
        if (rights.any { it !in "KQkq" } || rights.toSet().size != rights.length) return false
        return rights.all { flag ->
            val side = if (flag.isUpperCase()) Side.WHITE else Side.BLACK
            val rank = if (side == Side.WHITE) 0 else 7
            val file = if (flag.lowercaseChar() == 'k') 7 else 0
            board.pieceAt(BoardPoint(4, rank)) == Piece(side, PieceType.KING) &&
                board.pieceAt(BoardPoint(file, rank)) == Piece(side, PieceType.ROOK)
        }
    }

    fun hasValidEnPassant(board: BoardState): Boolean {
        val ep = board.enPassant ?: return true
        val expectedRank = if (board.sideToMove == Side.WHITE) 5 else 2
        if (ep.rank != expectedRank || board.pieceAt(ep) != null) return false
        val direction = if (board.sideToMove == Side.WHITE) -1 else 1
        return board.pieceAt(ep.offset(0, direction)) == Piece(board.sideToMove.opposite(), PieceType.PAWN) &&
            board.pieceAt(ep.offset(0, -direction)) == null && board.halfMoveClock == 0
    }
}
