package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.Side

enum class SetupTool { MOVE, PLACE, ERASE }

data class BoardSetupDraft(
    val original: BoardState,
    val boardState: BoardState = original,
    val selectedPoint: BoardPoint? = null,
    val tool: SetupTool = SetupTool.MOVE,
    val placementPiece: Piece? = null,
    val undoBoards: List<BoardState> = emptyList(),
    val redoBoards: List<BoardState> = emptyList(),
) {
    val hasChanges: Boolean
        get() = original.board != boardState.board || original.sideToMove != boardState.sideToMove

    fun startPosition(): BoardState = boardState.copy(halfMoveClock = 0, fullMoveNumber = 1)

    fun selectTool(tool: SetupTool): BoardSetupDraft =
        copy(tool = tool, selectedPoint = null, placementPiece = null)

    fun selectPiece(piece: Piece): BoardSetupDraft =
        copy(tool = SetupTool.PLACE, placementPiece = piece, selectedPoint = null)

    fun tap(point: BoardPoint): BoardSetupDraft {
        require(point.isInside())
        return when (tool) {
            SetupTool.ERASE -> place(point, null)
            SetupTool.PLACE -> place(point, requireNotNull(placementPiece))
            SetupTool.MOVE -> when {
                selectedPoint == point -> copy(selectedPoint = null)
                boardState.pieceAt(point) != null -> copy(selectedPoint = point)
                selectedPoint != null -> move(selectedPoint, point)
                else -> this
            }
        }
    }

    fun move(from: BoardPoint, to: BoardPoint): BoardSetupDraft {
        require(from.isInside() && to.isInside())
        val piece = requireNotNull(boardState.pieceAt(from))
        if (from == to) return copy(selectedPoint = null)
        val cells = boardState.board.toMutableList()
        cells[from.index] = null
        cells[to.index] = piece
        return record(boardState.copy(board = cells))
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

    private fun place(point: BoardPoint, piece: Piece?): BoardSetupDraft {
        val cells = boardState.board.toMutableList()
        cells[point.index] = piece
        return record(boardState.copy(board = cells))
    }

    private fun record(next: BoardState): BoardSetupDraft =
        if (next == boardState) this else copy(
            boardState = next,
            selectedPoint = null,
            undoBoards = undoBoards + boardState,
            redoBoards = emptyList(),
        )
}
