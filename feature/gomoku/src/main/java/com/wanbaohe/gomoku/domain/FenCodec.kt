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
        require(parts.size in 2..3) { "Invalid FEN: expected board, side and optional move number" }
        val moveNumber = parts.getOrNull(2)?.let {
            requireNotNull(it.toIntOrNull()) { "Invalid FEN: invalid move number" }
        } ?: 1
        require(moveNumber >= 1) { "Invalid FEN: move number must be positive" }

        return BoardState(
            board = parseBoard(parts.first()),
            sideToMove = parseSide(parts[1]),
            moveNumber = moveNumber,
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
        val cells = buildList<Side?> {
            var index = 0
            while (index < rankFen.length) {
                val char = rankFen[index]
                if (char in '0'..'9') {
                    val start = index
                    while (index < rankFen.length && rankFen[index] in '0'..'9') index++
                    val run = requireNotNull(rankFen.substring(start, index).toIntOrNull()) {
                        "Invalid FEN: invalid empty run"
                    }
                    require(run in 1..BoardPoint.FILE_COUNT && size + run <= BoardPoint.FILE_COUNT) {
                        "Invalid FEN: empty run is too wide"
                    }
                    repeat(run) { add(null) }
                } else {
                    add(when (char) {
                        'b' -> Side.BLACK
                        'w' -> Side.WHITE
                        else -> throw IllegalArgumentException("Unsupported FEN cell: $char")
                    })
                    require(size <= BoardPoint.FILE_COUNT) { "Invalid FEN: rank is too wide" }
                    index++
                }
            }
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

    private fun parseSide(value: String): Side = when (value.lowercase()) {
        "b" -> Side.BLACK
        "w" -> Side.WHITE
        else -> throw IllegalArgumentException("Invalid FEN: unsupported side")
    }
}
