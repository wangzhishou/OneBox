package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side

/**
 * Pure FEN codec. FEN is the module-wide source of truth for persistence,
 * replay, AI prompts, import and export.
 */
object FenCodec {
    const val INITIAL_FEN: String = "rnbakabnr/9/1c5c1/p1p1p1p1p/9/9/P1P1P1P1P/1C5C1/9/RNBAKABNR w 0 1"

    fun parse(fen: String): BoardState {
        val parts = fen.trim().split(Regex("\\s+"))
        require(parts.size >= 2) { "Invalid FEN: side to move is missing" }

        return BoardState(
            board = parseBoard(parts.first()),
            sideToMove = parseSide(parts[1]),
            halfMoveClock = parts.getOrNull(2)?.toIntOrNull() ?: 0,
            fullMoveNumber = parts.getOrNull(3)?.toIntOrNull() ?: 1,
        )
    }

    fun encode(boardState: BoardState): String {
        val boardFen = encodeBoard(boardState.board)
        val side = if (boardState.sideToMove == Side.BLACK) 'b' else 'w'
        return "$boardFen $side ${boardState.halfMoveClock} ${boardState.fullMoveNumber}"
    }

    private fun parseBoard(boardFen: String): List<Piece?> {
        val rows = boardFen.split('/')
        require(rows.size == BoardPoint.RANK_COUNT) { "Invalid FEN: expected ${BoardPoint.RANK_COUNT} ranks" }
        return rows.flatMap(::parseRank).also { cells ->
            require(cells.size == BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) {
                "Invalid FEN: expected ${BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT} cells"
            }
        }
    }

    private fun parseRank(rankFen: String): List<Piece?> {
        val cells = buildList {
            rankFen.forEach { char ->
                if (char.isDigit()) {
                    repeat(char.digitToInt()) { add(null) }
                } else {
                    add(char.toPiece())
                }
            }
        }
        require(cells.size == BoardPoint.FILE_COUNT) {
            "Invalid FEN: expected ${BoardPoint.FILE_COUNT} files in rank '$rankFen'"
        }
        return cells
    }

    private fun encodeBoard(board: List<Piece?>): String = buildString {
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            appendRank(board, rank)
            if (rank != BoardPoint.RANK_COUNT - 1) append('/')
        }
    }

    private fun StringBuilder.appendRank(board: List<Piece?>, rank: Int) {
        var emptyCount = 0
        for (file in 0 until BoardPoint.FILE_COUNT) {
            val piece = board[BoardPoint(file, rank).index]
            if (piece == null) {
                emptyCount++
                continue
            }
            if (emptyCount > 0) {
                append(emptyCount)
                emptyCount = 0
            }
            append(piece.toFenChar())
        }
        if (emptyCount > 0) append(emptyCount)
    }

    private fun parseSide(value: String): Side =
        if (value.lowercase() == "b") Side.BLACK else Side.RED

    private fun Char.toPiece(): Piece = when (this) {
        'K' -> Piece(Side.RED, PieceType.KING)
        'A' -> Piece(Side.RED, PieceType.ADVISOR)
        'B', 'E' -> Piece(Side.RED, PieceType.BISHOP)
        'N', 'H' -> Piece(Side.RED, PieceType.KNIGHT)
        'R' -> Piece(Side.RED, PieceType.ROOK)
        'C' -> Piece(Side.RED, PieceType.CANNON)
        'P' -> Piece(Side.RED, PieceType.PAWN)
        'k' -> Piece(Side.BLACK, PieceType.KING)
        'a' -> Piece(Side.BLACK, PieceType.ADVISOR)
        'b', 'e' -> Piece(Side.BLACK, PieceType.BISHOP)
        'n', 'h' -> Piece(Side.BLACK, PieceType.KNIGHT)
        'r' -> Piece(Side.BLACK, PieceType.ROOK)
        'c' -> Piece(Side.BLACK, PieceType.CANNON)
        'p' -> Piece(Side.BLACK, PieceType.PAWN)
        else -> error("Unsupported FEN piece: $this")
    }

    private fun Piece.toFenChar(): Char = when (type) {
        PieceType.KING -> if (side == Side.RED) 'K' else 'k'
        PieceType.ADVISOR -> if (side == Side.RED) 'A' else 'a'
        PieceType.BISHOP -> if (side == Side.RED) 'B' else 'b'
        PieceType.KNIGHT -> if (side == Side.RED) 'N' else 'n'
        PieceType.ROOK -> if (side == Side.RED) 'R' else 'r'
        PieceType.CANNON -> if (side == Side.RED) 'C' else 'c'
        PieceType.PAWN -> if (side == Side.RED) 'P' else 'p'
    }
}
