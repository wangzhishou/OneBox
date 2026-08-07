package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

private val ORTHOGONAL_DELTAS = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
private val DIAGONAL_DELTAS = listOf(-1 to -1, 1 to -1, -1 to 1, 1 to 1)

object MoveGenerator {

    fun pseudoLegalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<XiangqiMove> {
        return boardState.occupiedSquares()
            .filter { (_, piece) -> piece.side == side }
            .flatMap { (point, piece) -> movesForPiece(boardState, point, piece) }
    }

    private fun movesForPiece(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> = when (piece.type) {
        PieceType.KING -> kingMoves(boardState, from, piece)
        PieceType.ADVISOR -> advisorMoves(boardState, from, piece)
        PieceType.BISHOP -> bishopMoves(boardState, from, piece)
        PieceType.KNIGHT -> knightMoves(boardState, from, piece)
        PieceType.ROOK -> rookMoves(boardState, from, piece)
        PieceType.CANNON -> cannonMoves(boardState, from, piece)
        PieceType.PAWN -> pawnMoves(boardState, from, piece)
    }

    private fun rookMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> = ORTHOGONAL_DELTAS.flatMap { (df, dr) ->
        scanSliding(boardState, from, piece, df, dr, cannon = false)
    }

    private fun cannonMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> = ORTHOGONAL_DELTAS.flatMap { (df, dr) ->
        scanSliding(boardState, from, piece, df, dr, cannon = true)
    }

    private fun scanSliding(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
        df: Int,
        dr: Int,
        cannon: Boolean,
    ): List<XiangqiMove> = buildList {
        var pos = from.offset(df, dr)
        var screenSeen = false
        while (pos.isInside()) {
            val target = boardState.pieceAt(pos)
            when {
                !cannon -> {
                    if (target == null || target.side != piece.side) {
                        add(XiangqiMove(from, pos, piece, target))
                    }
                    if (target != null) break
                }
                !screenSeen && target == null -> add(XiangqiMove(from, pos, piece))
                !screenSeen -> screenSeen = true
                target != null -> {
                    if (target.side != piece.side) add(XiangqiMove(from, pos, piece, target))
                    break
                }
            }
            pos = pos.offset(df, dr)
        }
    }

    private fun knightMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> = KnightPattern.ALL.flatMap { pattern ->
        val leg = from.offset(pattern.legFileDelta, pattern.legRankDelta)
        if (!leg.isInside() || boardState.pieceAt(leg) != null) return@flatMap emptyList()
        pattern.targets.mapNotNull { (tf, tr) ->
            buildMoveIfReachable(boardState, from, from.offset(tf, tr), piece)
        }
    }

    private fun bishopMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> = listOf(-2 to -2, 2 to -2, -2 to 2, 2 to 2).mapNotNull { (df, dr) ->
        val to = from.offset(df, dr)
        val eye = from.offset(df / 2, dr / 2)
        if (to.isInside() && to.isOwnRiverSide(piece.side) && boardState.pieceAt(eye) == null) {
            buildMoveIfReachable(boardState, from, to, piece)
        } else null
    }

    private fun advisorMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> = DIAGONAL_DELTAS.mapNotNull { (df, dr) ->
        val to = from.offset(df, dr)
        if (to.isInsidePalace(piece.side)) buildMoveIfReachable(boardState, from, to, piece) else null
    }

    private fun kingMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> {
        val palace = ORTHOGONAL_DELTAS.mapNotNull { (df, dr) ->
            val to = from.offset(df, dr)
            if (to.isInsidePalace(piece.side)) buildMoveIfReachable(boardState, from, to, piece) else null
        }
        return palace + flyingKingMove(boardState, from, piece)
    }

    private fun flyingKingMove(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> {
        val enemyKing = GameArbiter.findKing(boardState, piece.side.opposite()) ?: return emptyList()
        if (enemyKing.file != from.file || !GameArbiter.clearBetween(boardState, from, enemyKing)) {
            return emptyList()
        }
        val captured = boardState.pieceAt(enemyKing)?.takeIf { it.side != piece.side } ?: return emptyList()
        return listOf(XiangqiMove(from, enemyKing, piece, captured))
    }

    private fun pawnMoves(
        boardState: BoardState,
        from: BoardPoint,
        piece: Piece,
    ): List<XiangqiMove> {
        val forward = if (piece.side == Side.RED) -1 else 1
        val targets = buildList {
            add(from.offset(0, forward))
            if (from.hasCrossedRiver(piece.side)) {
                add(from.offset(-1, 0))
                add(from.offset(1, 0))
            }
        }
        return targets.mapNotNull { buildMoveIfReachable(boardState, from, it, piece) }
    }

    private fun buildMoveIfReachable(
        boardState: BoardState,
        from: BoardPoint,
        to: BoardPoint,
        piece: Piece,
    ): XiangqiMove? {
        if (!to.isInside()) return null
        val target = boardState.pieceAt(to)
        if (target?.side == piece.side) return null
        return XiangqiMove(from, to, piece, target)
    }

    private fun BoardState.occupiedSquares(): List<Pair<BoardPoint, Piece>> = buildList {
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            for (file in 0 until BoardPoint.FILE_COUNT) {
                val point = BoardPoint(file, rank)
                pieceAt(point)?.let { add(point to it) }
            }
        }
    }

    private fun BoardPoint.isInsidePalace(side: Side): Boolean {
        val inFile = file in 3..5
        val inRank = if (side == Side.RED) rank in 7..9 else rank in 0..2
        return isInside() && inFile && inRank
    }

    private fun BoardPoint.isOwnRiverSide(side: Side): Boolean =
        if (side == Side.RED) rank >= 5 else rank <= 4

    private fun BoardPoint.hasCrossedRiver(side: Side): Boolean =
        if (side == Side.RED) rank <= 4 else rank >= 5
}

private data class KnightPattern(
    val legFileDelta: Int,
    val legRankDelta: Int,
    val targets: List<Pair<Int, Int>>,
) {
    companion object {
        val ALL = listOf(
            KnightPattern(0, -1, listOf(-1 to -2, 1 to -2)),
            KnightPattern(0, 1, listOf(-1 to 2, 1 to 2)),
            KnightPattern(-1, 0, listOf(-2 to -1, -2 to 1)),
            KnightPattern(1, 0, listOf(2 to -1, 2 to 1)),
        )
    }
}
