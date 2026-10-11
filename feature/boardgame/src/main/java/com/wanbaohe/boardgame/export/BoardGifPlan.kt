package com.wanbaohe.boardgame.export

import com.t8rin.imagetoolbox.core.domain.model.IntegerSize
import kotlin.math.roundToInt

data class BoardReplayMove<Position>(
    val ply: Int,
    val beforePosition: Position,
    val afterPosition: Position,
    val notation: String,
    val moveCode: String,
    val moverLabel: String,
)

class BoardReplaySnapshot<Position>(
    val title: String,
    val initialPosition: Position,
    moves: List<BoardReplayMove<Position>>,
    val currentPly: Int,
    val resultText: String,
) {
    val moves: List<BoardReplayMove<Position>> = moves.toList()

    init {
        require(currentPly >= 0)
        require(this.moves.all { it.ply > 0 })
        require(this.moves.zipWithNext().all { (first, second) -> second.ply == first.ply + 1 }) {
            "The replay contains missing or duplicate moves"
        }
    }

    val wholeRange: BoardGifRange
        get() = BoardGifRange(0, (moves.size - 1).coerceAtLeast(0))

    fun rangeNearCurrent(): BoardGifRange {
        if (moves.isEmpty()) return wholeRange
        val count = minOf(NEARBY_MOVE_COUNT, moves.size)
        val focus = moves.indexOfLast { it.ply <= currentPly }.coerceAtLeast(0)
        val first = (focus - count / 2).coerceIn(0, moves.size - count)
        return BoardGifRange(first, first + count - 1)
    }

    companion object {
        const val NEARBY_MOVE_COUNT = 20
        const val LONG_GAME_MOVE_COUNT = 40
    }
}

data class BoardGifRange(val firstMoveIndex: Int, val lastMoveIndex: Int) {
    init {
        require(firstMoveIndex >= 0 && lastMoveIndex >= firstMoveIndex)
    }

    val moveCount: Int
        get() = lastMoveIndex - firstMoveIndex + 1
}

enum class BoardGifSpeed(val durationMillis: Int) {
    Fast(400),
    Standard(800),
    Slow(1200),
}

enum class BoardGifResolution(val widthPixels: Int) {
    Compact(480),
    Standard(640),
}

data class BoardGifFrame<Position>(
    val position: Position,
    val ply: Int,
    val move: BoardReplayMove<Position>?,
    val durationMillis: Int,
    val isFirst: Boolean,
    val isLast: Boolean,
)

class BoardGifPlan<Position>(
    val snapshot: BoardReplaySnapshot<Position>,
    val range: BoardGifRange,
    val speed: BoardGifSpeed,
    val resolution: BoardGifResolution,
    val boardAspectRatio: Float,
) {
    init {
        require(boardAspectRatio.isFinite() && boardAspectRatio > 0f)
        require(snapshot.moves.isEmpty() || range.lastMoveIndex < snapshot.moves.size)
    }

    val isStatic: Boolean
        get() = snapshot.moves.isEmpty()

    val isWholeGame: Boolean
        get() = range == snapshot.wholeRange

    val frameCount: Int
        get() = if (isStatic) 1 else range.moveCount + 1

    val durationMillis: Long
        get() = if (isStatic) 0 else {
            FIRST_FRAME_MILLIS.toLong() + (range.moveCount - 1L) * speed.durationMillis + LAST_FRAME_MILLIS
        }

    val logicalHeight: Float
        get() = HEADER_HEIGHT + (LOGICAL_WIDTH - BOARD_PADDING * 2) / boardAspectRatio + FOOTER_HEIGHT

    val size: IntegerSize
        get() = IntegerSize(
            width = resolution.widthPixels,
            height = (logicalHeight * resolution.widthPixels / LOGICAL_WIDTH).roundToInt(),
        )

    fun frameAt(index: Int): BoardGifFrame<Position> {
        require(index in 0 until frameCount)
        if (isStatic) {
            return BoardGifFrame(
                position = snapshot.initialPosition,
                ply = snapshot.currentPly,
                move = null,
                durationMillis = LAST_FRAME_MILLIS,
                isFirst = true,
                isLast = true,
            )
        }
        val firstMove = snapshot.moves[range.firstMoveIndex]
        if (index == 0) {
            return BoardGifFrame(
                position = firstMove.beforePosition,
                ply = firstMove.ply - 1,
                move = null,
                durationMillis = FIRST_FRAME_MILLIS,
                isFirst = true,
                isLast = false,
            )
        }
        val move = snapshot.moves[range.firstMoveIndex + index - 1]
        val isLast = index == frameCount - 1
        return BoardGifFrame(
            position = move.afterPosition,
            ply = move.ply,
            move = move,
            durationMillis = if (isLast) LAST_FRAME_MILLIS else speed.durationMillis,
            isFirst = false,
            isLast = isLast,
        )
    }

    companion object {
        const val LOGICAL_WIDTH = 320f
        const val HEADER_HEIGHT = 44f
        const val FOOTER_HEIGHT = 60f
        const val BOARD_PADDING = 12f
        const val FIRST_FRAME_MILLIS = 1200
        const val LAST_FRAME_MILLIS = 2000
    }
}
