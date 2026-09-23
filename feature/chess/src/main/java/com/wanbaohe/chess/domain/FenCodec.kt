package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side

/**
 * 标准 FEN codec。FEN 是模块级事实源:持久化、回放、AI prompt、导入导出全部走它。
 *
 * 格式:`<board> <side> <castling> <enPassant> <halfmove> <fullmove>`,
 * board 从第 8 行(黑方底线)写到第 1 行,棋子 rnbqkp 小写黑大写白,数字压缩空格。
 */
object FenCodec {
    const val INITIAL_FEN: String = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    fun parse(fen: String): BoardState {
        val parts = fen.trim().split(Regex("\\s+"))
        require(parts.size >= 2) { "Invalid FEN: side to move is missing" }

        return BoardState(
            board = parseBoard(parts.first()),
            sideToMove = if (parts[1].lowercase() == "b") Side.BLACK else Side.WHITE,
            castlingRights = parts.getOrNull(2)?.takeIf { it.isNotBlank() } ?: "-",
            enPassant = parts.getOrNull(3)?.takeIf { it != "-" }?.let { BoardPoint.fromCoordinate(it) },
            halfMoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0,
            fullMoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1,
        )
    }

    fun encode(boardState: BoardState): String {
        val side = if (boardState.sideToMove == Side.WHITE) 'w' else 'b'
        val ep = boardState.enPassant?.toCoordinate() ?: "-"
        return "${encodeBoard(boardState.board)} $side ${boardState.castlingRights.ifEmpty { "-" }} $ep " +
            "${boardState.halfMoveClock} ${boardState.fullMoveNumber}"
    }

    private fun parseBoard(boardFen: String): List<Piece?> {
        val rows = boardFen.split('/')
        require(rows.size == BoardPoint.RANK_COUNT) { "Invalid FEN: expected ${BoardPoint.RANK_COUNT} ranks" }
        // FEN 第一行是 rank 8;内部 rank 0 = 第 1 行,翻转顺序
        return rows.reversed().flatMap(::parseRank).also { cells ->
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
        // 从 rank 8(内部 index 7)往 rank 1 写
        for (rank in (BoardPoint.RANK_COUNT - 1) downTo 0) {
            appendRank(board, rank)
            if (rank != 0) append('/')
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

    private fun Char.toPiece(): Piece {
        val side = if (isUpperCase()) Side.WHITE else Side.BLACK
        val type = when (lowercaseChar()) {
            'k' -> PieceType.KING
            'q' -> PieceType.QUEEN
            'r' -> PieceType.ROOK
            'b' -> PieceType.BISHOP
            'n' -> PieceType.KNIGHT
            'p' -> PieceType.PAWN
            else -> error("Unsupported FEN piece: $this")
        }
        return Piece(side, type)
    }

    private fun Piece.toFenChar(): Char {
        val c = when (type) {
            PieceType.KING -> 'k'
            PieceType.QUEEN -> 'q'
            PieceType.ROOK -> 'r'
            PieceType.BISHOP -> 'b'
            PieceType.KNIGHT -> 'n'
            PieceType.PAWN -> 'p'
        }
        return if (side == Side.WHITE) c.uppercaseChar() else c
    }
}
