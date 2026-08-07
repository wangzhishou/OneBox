package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

private const val DRAW_HALF_MOVE_CLOCK = 120

/**
 * Pure function arbiter. Given a board state and a desired move, produces the
 * resulting board state and game status — or rejects the move.
 *
 * This is the single domain entry point for move validation and application.
 * No IO, no mutation of external state, no framework dependencies.
 */
object GameArbiter {

    fun legalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<XiangqiMove> {
        return MoveGenerator.pseudoLegalMoves(boardState, side)
            .filter { isLegalAfterMove(boardState, it, side) }
    }

    fun applyMove(boardState: BoardState, move: XiangqiMove): MoveOutcome {
        val piece = boardState.pieceAt(move.from)
            ?: return MoveOutcome.Rejected("No piece at source")

        if (piece.side != boardState.sideToMove) {
            return MoveOutcome.Rejected("Not your turn")
        }

        val legal = legalMoves(boardState, boardState.sideToMove)
        val matched = legal.firstOrNull { it.from == move.from && it.to == move.to }
            ?: return MoveOutcome.Rejected("Illegal move")

        val nextState = boardState.withPieceMoved(matched)
        val status = evaluateStatus(nextState)
        return MoveOutcome.Applied(nextState, matched, status)
    }

    fun evaluateStatus(boardState: BoardState): GameStatus {
        val moves = legalMoves(boardState)
        val inCheck = isInCheck(boardState, boardState.sideToMove)
        return when {
            moves.isEmpty() && boardState.sideToMove == Side.RED -> GameStatus.BLACK_WINS
            moves.isEmpty() && boardState.sideToMove == Side.BLACK -> GameStatus.RED_WINS
            boardState.halfMoveClock >= DRAW_HALF_MOVE_CLOCK -> GameStatus.DRAW
            inCheck -> GameStatus.CHECK
            else -> GameStatus.PLAYING
        }
    }

    fun isInCheck(boardState: BoardState, side: Side): Boolean {
        val king = findKing(boardState, side) ?: return true
        if (kingsFacing(boardState)) return true
        return MoveGenerator.pseudoLegalMoves(boardState, side.opposite())
            .any { it.to == king }
    }

    fun findKing(boardState: BoardState, side: Side): BoardPoint? {
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            for (file in 0 until BoardPoint.FILE_COUNT) {
                val point = BoardPoint(file, rank)
                val piece = boardState.pieceAt(point)
                if (piece?.side == side && piece.type == PieceType.KING) return point
            }
        }
        return null
    }

    fun clearBetween(boardState: BoardState, a: BoardPoint, b: BoardPoint): Boolean = when {
        a.file == b.file -> clearFile(boardState, a, b)
        a.rank == b.rank -> clearRank(boardState, a, b)
        else -> false
    }

    private fun isLegalAfterMove(boardState: BoardState, move: XiangqiMove, side: Side): Boolean {
        val piece = boardState.pieceAt(move.from) ?: return false
        if (piece.side != side) return false
        val normalized = move.copy(piece = piece, captured = boardState.pieceAt(move.to))
        val next = boardState.withPieceMoved(normalized)
        return !isInCheck(next, side)
    }

    private fun kingsFacing(boardState: BoardState): Boolean {
        val redKing = findKing(boardState, Side.RED) ?: return false
        val blackKing = findKing(boardState, Side.BLACK) ?: return false
        return redKing.file == blackKing.file && clearBetween(boardState, redKing, blackKing)
    }

    private fun clearFile(boardState: BoardState, a: BoardPoint, b: BoardPoint): Boolean {
        val ranks = if (a.rank < b.rank) (a.rank + 1) until b.rank else (b.rank + 1) until a.rank
        return ranks.none { rank -> boardState.pieceAt(BoardPoint(a.file, rank)) != null }
    }

    private fun clearRank(boardState: BoardState, a: BoardPoint, b: BoardPoint): Boolean {
        val files = if (a.file < b.file) (a.file + 1) until b.file else (b.file + 1) until a.file
        return files.none { file -> boardState.pieceAt(BoardPoint(file, a.rank)) != null }
    }
}

sealed interface MoveOutcome {
    data class Applied(
        val boardState: BoardState,
        val move: XiangqiMove,
        val status: GameStatus,
    ) : MoveOutcome

    data class Rejected(val reason: String) : MoveOutcome
}
