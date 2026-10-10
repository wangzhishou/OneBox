package com.wanbaohe.gomoku.domain

import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardSetupDraftTest {
    @Test
    fun editingAndCancellingNeverAlterTheSourceBoard() {
        val cells = MutableList<Side?>(225) { null }
        val source = BoardState(cells, Side.BLACK, 17)
        val draft = BoardSetupDraft(source).selectStone(Side.WHITE).tap(BoardPoint(7, 7))
        assertNull(source.stoneAt(BoardPoint(7, 7)))
        assertEquals(17, source.moveNumber)
        assertEquals(Side.WHITE, draft.boardState.stoneAt(BoardPoint(7, 7)))
        cells[0] = Side.BLACK
        assertNull(draft.original.stoneAt(BoardPoint(0, 0)))
        assertNull(draft.boardState.stoneAt(BoardPoint(0, 0)))
        assertEquals(1, draft.startPosition().moveNumber)
    }

    @Test
    fun placingMovingErasingAndChangingTurnHaveIndependentUndoRedo() {
        val empty = BoardSetupDraft(BoardState.empty())
        val placed = empty.selectStone(Side.BLACK).tap(BoardPoint(7, 7))
        val moved = placed.selectTool(SetupTool.MOVE).tap(BoardPoint(7, 7)).tap(BoardPoint(8, 7))
        assertNull(moved.boardState.stoneAt(BoardPoint(7, 7)))
        assertEquals(Side.BLACK, moved.boardState.stoneAt(BoardPoint(8, 7)))
        val erased = moved.selectTool(SetupTool.ERASE).tap(BoardPoint(8, 7))
        val whiteTurn = erased.setSideToMove(Side.WHITE)
        assertEquals(Side.BLACK, whiteTurn.undo().boardState.sideToMove)
        assertEquals(moved.boardState, erased.undo().boardState)
        assertEquals(whiteTurn.boardState, whiteTurn.undo().redo().boardState)
        assertFalse(placed.undo().hasChanges)
        assertTrue(whiteTurn.hasChanges)
        assertEquals(BoardState.empty().board, empty.boardState.board)
    }

    @Test
    fun anEditAfterUndoDiscardsOnlyTheEditorsFuture() {
        val placed = BoardSetupDraft(BoardState.empty()).selectStone(Side.BLACK).tap(BoardPoint(0, 0))
        val undone = placed.undo()
        val changed = undone.selectStone(Side.WHITE).tap(BoardPoint(1, 1))
        assertTrue(changed.redoBoards.isEmpty())
        assertEquals(1, placed.boardState.placedCount())
        assertEquals(Side.BLACK, placed.boardState.stoneAt(BoardPoint(0, 0)))
    }

    @Test
    fun clearKeepsTheChosenTurnAndResetRestoresBlackFirst() {
        val draft = BoardSetupDraft(BoardState.empty()).selectStone(Side.WHITE).tap(BoardPoint(1, 1))
            .setSideToMove(Side.WHITE)
        assertEquals(Side.WHITE, draft.clear().boardState.sideToMove)
        assertEquals(0, draft.clear().boardState.placedCount())
        assertEquals(Side.BLACK, draft.reset().boardState.sideToMove)
        assertEquals(draft.boardState, draft.reset().undo().boardState)
    }
}
