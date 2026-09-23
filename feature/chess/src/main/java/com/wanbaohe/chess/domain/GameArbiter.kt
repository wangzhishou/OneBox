package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.ChessMove

/** 50 回合规则:100 个半回合无吃子无兵动判和 */
private const val DRAW_HALF_MOVE_CLOCK = 100

private val ATTACK_ORTHOGONAL = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
private val ATTACK_DIAGONAL = listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)
private val ATTACK_KNIGHT = listOf(
    -2 to -1, -2 to 1, -1 to -2, -1 to 2,
    1 to -2, 1 to 2, 2 to -1, 2 to 1,
)
private val ATTACK_KING = ATTACK_ORTHOGONAL + ATTACK_DIAGONAL

/**
 * Pure function arbiter。legalMoves = 伪合法着法过滤自陷将军后补全记谱;
 * 终局判定:将死(无合法着+被将)/逼和(无合法着+未被将)/50 回合和。
 * 不做三次重复局面判和(二期)。
 */
object GameArbiter {

    fun legalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<ChessMove> {
        return MoveGenerator.pseudoLegalMoves(boardState, side)
            .filter { isLegalAfterMove(boardState, it, side) }
            .map { move ->
                // 记谱只有一种(UCI 坐标),notationCn 复用同值,落库列与象棋/五子棋对齐
                move.copy(
                    notationUcci = UciNotation.format(move),
                    notationCn = UciNotation.format(move),
                )
            }
    }

    fun applyMove(boardState: BoardState, move: ChessMove): MoveOutcome {
        val piece = boardState.pieceAt(move.from)
            ?: return MoveOutcome.Rejected("No piece at source")

        if (piece.side != boardState.sideToMove) {
            return MoveOutcome.Rejected("Not your turn")
        }

        val matched = legalMoves(boardState, boardState.sideToMove)
            .firstOrNull { it.from == move.from && it.to == move.to && it.promotion == move.promotion }
            ?: return MoveOutcome.Rejected("Illegal move")

        val nextState = boardState.withPieceMoved(matched)
        val status = evaluateStatus(nextState)
        return MoveOutcome.Applied(nextState, matched, status)
    }

    fun evaluateStatus(boardState: BoardState): GameStatus {
        val moves = legalMoves(boardState)
        val inCheck = isInCheck(boardState, boardState.sideToMove)
        return when {
            // 无合法着 + 被将 = 将死,行棋方负
            moves.isEmpty() && inCheck && boardState.sideToMove == Side.WHITE -> GameStatus.BLACK_WINS
            moves.isEmpty() && inCheck -> GameStatus.WHITE_WINS
            // 无合法着 + 未被将 = 逼和
            moves.isEmpty() -> GameStatus.DRAW
            boardState.halfMoveClock >= DRAW_HALF_MOVE_CLOCK -> GameStatus.DRAW
            inCheck -> GameStatus.CHECK
            else -> GameStatus.PLAYING
        }
    }

    fun isInCheck(boardState: BoardState, side: Side): Boolean {
        val king = findKing(boardState, side) ?: return true
        return isSquareAttacked(boardState, king, side.opposite())
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

    /**
     * [point] 是否被 [bySide] 攻击。
     * 直接按攻击几何检测(不走 pseudoLegalMoves)——王车易位生成要调本函数,
     * 走生成器会循环依赖。
     */
    fun isSquareAttacked(boardState: BoardState, point: BoardPoint, bySide: Side): Boolean {
        // 兵:白兵向上吃,即白兵位于 point 的左下/右下
        val pawnDir = if (bySide == Side.WHITE) -1 else 1
        for (df in listOf(-1, 1)) {
            val from = point.offset(df, pawnDir)
            val piece = if (from.isInside()) boardState.pieceAt(from) else null
            if (piece?.side == bySide && piece.type == PieceType.PAWN) return true
        }
        // 马
        for ((df, dr) in ATTACK_KNIGHT) {
            val piece = boardState.pieceAt(point.offset(df, dr)) ?: continue
            if (piece.side == bySide && piece.type == PieceType.KNIGHT) return true
        }
        // 王(相邻)
        for ((df, dr) in ATTACK_KING) {
            val piece = boardState.pieceAt(point.offset(df, dr)) ?: continue
            if (piece.side == bySide && piece.type == PieceType.KING) return true
        }
        // 正交滑动:车/后
        if (slidingAttacked(boardState, point, bySide, ATTACK_ORTHOGONAL, setOf(PieceType.ROOK, PieceType.QUEEN))) return true
        // 斜向滑动:象/后
        if (slidingAttacked(boardState, point, bySide, ATTACK_DIAGONAL, setOf(PieceType.BISHOP, PieceType.QUEEN))) return true
        return false
    }

    private fun slidingAttacked(
        boardState: BoardState,
        point: BoardPoint,
        bySide: Side,
        deltas: List<Pair<Int, Int>>,
        attackers: Set<PieceType>,
    ): Boolean {
        for ((df, dr) in deltas) {
            var pos = point.offset(df, dr)
            while (pos.isInside()) {
                val piece = boardState.pieceAt(pos)
                if (piece != null) {
                    if (piece.side == bySide && piece.type in attackers) return true
                    break
                }
                pos = pos.offset(df, dr)
            }
        }
        return false
    }

    private fun isLegalAfterMove(boardState: BoardState, move: ChessMove, side: Side): Boolean {
        val piece = boardState.pieceAt(move.from) ?: return false
        if (piece.side != side) return false
        val normalized = move.copy(
            piece = piece,
            captured = boardState.pieceAt(move.to)
                ?: enPassantCapture(boardState, move, piece),
        )
        val next = boardState.withPieceMoved(normalized)
        return !isInCheck(next, side)
    }

    /** 吃过路兵时目标格为空,被吃的兵在目标格正后方 */
    private fun enPassantCapture(boardState: BoardState, move: ChessMove, piece: Piece): Piece? {
        if (piece.type != PieceType.PAWN) return null
        if (boardState.enPassant != move.to) return null
        if (move.from.file == move.to.file) return null
        return boardState.pieceAt(BoardPoint(move.to.file, move.from.rank))
    }
}

sealed interface MoveOutcome {
    data class Applied(
        val boardState: BoardState,
        val move: ChessMove,
        val status: GameStatus,
    ) : MoveOutcome

    data class Rejected(val reason: String) : MoveOutcome
}
