package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side

/**
 * Pure position codec(FEN 风格)。局面字符串是模块级事实源:
 * 持久化、回放、AI prompt、导入导出全部走它。
 *
 * 格式:`<board> <side> <moveNumber>`,board 为 15 行 `/` 分隔,
 * 每行 15 格,`b`=黑 `w`=白,连续空位用数字压缩(如 `3b11`)。
 */
object FenCodec {
    const val INITIAL_FEN: String = "15/15/15/15/15/15/15/15/15/15/15/15/15/15/15 b 1"

    fun parse(fen: String): BoardState {
        val parts = fen.trim().split(Regex("\\s+"))
        require(parts.size >= 2) { "Invalid FEN: side to move is missing" }

        return BoardState(
            board = parseBoard(parts.first()),
            sideToMove = parseSide(parts[1]),
            moveNumber = parts.getOrNull(2)?.toIntOrNull() ?: 1,
        )
    }

    fun encode(boardState: BoardState): String {
        val boardFen = encodeBoard(boardState.board)
        val side = if (boardState.sideToMove == Side.BLACK) 'b' else 'w'
        return "$boardFen $side ${boardState.moveNumber}"
    }

    private fun parseBoard(boardFen: String): List<Side?> {
        val rows = boardFen.split('/')
        require(rows.size == BoardPoint.RANK_COUNT) { "Invalid FEN: expected ${BoardPoint.RANK_COUNT} ranks" }
        return rows.flatMap(::parseRank).also { cells ->
            require(cells.size == BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) {
                "Invalid FEN: expected ${BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT} cells"
            }
        }
    }

    private fun parseRank(rankFen: String): List<Side?> {
        val cells = buildList {
            var emptyRun = 0
            fun flushEmptyRun() {
                if (emptyRun > 0) {
                    repeat(emptyRun) { add(null) }
                    emptyRun = 0
                }
            }
            rankFen.forEach { char ->
                when {
                    char.isDigit() -> emptyRun = emptyRun * 10 + char.digitToInt()
                    else -> {
                        flushEmptyRun()
                        when (char) {
                            'b' -> add(Side.BLACK)
                            'w' -> add(Side.WHITE)
                            else -> error("Unsupported FEN cell: $char")
                        }
                    }
                }
            }
            flushEmptyRun()
        }
        require(cells.size == BoardPoint.FILE_COUNT) {
            "Invalid FEN: expected ${BoardPoint.FILE_COUNT} files in rank '$rankFen'"
        }
        return cells
    }

    private fun encodeBoard(board: List<Side?>): String = buildString {
        for (rank in 0 until BoardPoint.RANK_COUNT) {
            appendRank(board, rank)
            if (rank != BoardPoint.RANK_COUNT - 1) append('/')
        }
    }

    private fun StringBuilder.appendRank(board: List<Side?>, rank: Int) {
        var emptyCount = 0
        for (file in 0 until BoardPoint.FILE_COUNT) {
            val stone = board[BoardPoint(file, rank).index]
            if (stone == null) {
                emptyCount++
                continue
            }
            if (emptyCount > 0) {
                append(emptyCount)
                emptyCount = 0
            }
            append(if (stone == Side.BLACK) 'b' else 'w')
        }
        if (emptyCount > 0) append(emptyCount)
    }

    private fun parseSide(value: String): Side =
        if (value.lowercase() == "w") Side.WHITE else Side.BLACK
}
