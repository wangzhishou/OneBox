package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side

enum class SetupTool { MOVE, PLACE, ERASE }

data class BoardSetupDraft private constructor(
    val original: BoardState,
    val boardState: BoardState,
    val selectedPoint: BoardPoint? = null,
    val tool: SetupTool = SetupTool.MOVE,
    val placementSide: Side? = null,
    val undoBoards: List<BoardState> = emptyList(),
    val redoBoards: List<BoardState> = emptyList(),
) {
    constructor(board: BoardState) : this(
        original = board.copy(board = board.board.toList()),
        boardState = board.copy(board = board.board.toList()),
    )

    val hasChanges: Boolean
        get() = original.board != boardState.board || original.sideToMove != boardState.sideToMove

    fun startPosition(): BoardState = boardState.copy(board = boardState.board.toList(), moveNumber = 1)

    fun selectTool(tool: SetupTool): BoardSetupDraft =
        copy(tool = tool, selectedPoint = null, placementSide = null)

    fun selectStone(side: Side): BoardSetupDraft =
        copy(tool = SetupTool.PLACE, placementSide = side, selectedPoint = null)

    fun tap(point: BoardPoint): BoardSetupDraft {
        require(point.isInside())
        return when (tool) {
            SetupTool.ERASE -> place(point, null)
            SetupTool.PLACE -> place(point, requireNotNull(placementSide))
            SetupTool.MOVE -> when {
                selectedPoint == point -> copy(selectedPoint = null)
                boardState.stoneAt(point) != null -> copy(selectedPoint = point)
                selectedPoint != null -> move(selectedPoint, point)
                else -> this
            }
        }
    }

    fun move(from: BoardPoint, to: BoardPoint): BoardSetupDraft {
        require(from.isInside() && to.isInside())
        val stone = boardState.stoneAt(from) ?: return this
        if (from == to) return copy(selectedPoint = null)
        if (!boardState.isEmpty(to)) return copy(selectedPoint = to)
        val cells = boardState.board.toMutableList()
        cells[from.index] = null
        cells[to.index] = stone
        return record(boardState.copy(board = cells.toList()))
    }

    fun setSideToMove(side: Side): BoardSetupDraft = record(boardState.copy(sideToMove = side))
    fun reset(): BoardSetupDraft = record(FenCodec.parse(FenCodec.INITIAL_FEN))
    fun clear(): BoardSetupDraft = record(boardState.copy(board = List(boardState.board.size) { null }))

    fun undo(): BoardSetupDraft {
        val previous = undoBoards.lastOrNull() ?: return this
        return copy(
            boardState = previous,
            selectedPoint = null,
            undoBoards = undoBoards.dropLast(1),
            redoBoards = redoBoards + boardState,
        )
    }

    fun redo(): BoardSetupDraft {
        val next = redoBoards.lastOrNull() ?: return this
        return copy(
            boardState = next,
            selectedPoint = null,
            undoBoards = undoBoards + boardState,
            redoBoards = redoBoards.dropLast(1),
        )
    }

    private fun place(point: BoardPoint, side: Side?): BoardSetupDraft {
        val cells = boardState.board.toMutableList()
        cells[point.index] = side
        return record(boardState.copy(board = cells.toList()))
    }

    private fun record(next: BoardState): BoardSetupDraft =
        if (next == boardState) this else copy(
            boardState = next,
            selectedPoint = null,
            undoBoards = undoBoards + boardState,
            redoBoards = emptyList(),
        )
}
