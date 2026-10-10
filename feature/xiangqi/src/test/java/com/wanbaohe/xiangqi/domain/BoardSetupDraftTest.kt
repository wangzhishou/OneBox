package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.Piece
import com.wanbaohe.xiangqi.domain.model.PieceType
import com.wanbaohe.xiangqi.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardSetupDraftTest {
    private val initial = FenCodec.parse(FenCodec.INITIAL_FEN)

    @Test
    fun selectionDoesNotChangePosition() {
        val selected = BoardSetupDraft(initial).tap(BoardPoint(0, 9))
        assertEquals(BoardPoint(0, 9), selected.selectedPoint)
        assertFalse(selected.hasChanges)
        assertEquals(initial, selected.boardState)
    }

    @Test
    fun movesEditOnlyTheCopyAndDoNotAdvanceTheTurn() {
        val from = BoardPoint(0, 9)
        val to = BoardPoint(0, 8)
        val moved = BoardSetupDraft(initial).tap(from).tap(to)
        assertNull(moved.boardState.pieceAt(from))
        assertEquals(initial.pieceAt(from), moved.boardState.pieceAt(to))
        assertEquals(initial.pieceAt(from), moved.original.pieceAt(from))
        assertEquals(Side.RED, moved.boardState.sideToMove)
        assertTrue(moved.hasChanges)
    }

    @Test
    fun placingAndRemovingPiecesCanBeUndoneWithoutAffectingTheOriginal() {
        val point = BoardPoint(4, 5)
        val piece = Piece(Side.RED, PieceType.ROOK)
        val placed = BoardSetupDraft(initial).selectPiece(piece).tap(point)
        val removed = placed.selectTool(SetupTool.ERASE).tap(point)
        assertNull(removed.boardState.pieceAt(point))
        assertEquals(piece, removed.undo().boardState.pieceAt(point))
        assertNull(removed.undo().redo().boardState.pieceAt(point))
        assertNull(initial.pieceAt(point))
    }

    @Test
    fun sideToMoveParticipatesInUndoAndChangeDetection() {
        val changed = BoardSetupDraft(initial).setSideToMove(Side.BLACK)
        assertTrue(changed.hasChanges)
        assertFalse(changed.undo().hasChanges)
        assertEquals(Side.BLACK, changed.undo().redo().boardState.sideToMove)
    }

    @Test
    fun aNewEditDiscardsOnlyTheEditorRedoStack() {
        val changed = BoardSetupDraft(initial).move(BoardPoint(0, 9), BoardPoint(0, 8)).undo()
        assertTrue(changed.redoBoards.isNotEmpty())
        assertTrue(changed.setSideToMove(Side.BLACK).redoBoards.isEmpty())
    }

    @Test
    fun newGameResetsCountersButAnUntouchedDraftDoesNotBecomeAChange() {
        val source = initial.copy(halfMoveClock = 119, fullMoveNumber = 40)
        val draft = BoardSetupDraft(source)
        assertFalse(draft.hasChanges)
        assertEquals(0, draft.startPosition().halfMoveClock)
        assertEquals(1, draft.startPosition().fullMoveNumber)
        assertEquals(119, draft.original.halfMoveClock)
    }

    @Test
    fun dragCanReplaceAPieceAndUndoRestoresBothSquares() {
        val from = BoardPoint(0, 9)
        val to = BoardPoint(1, 9)
        val draft = BoardSetupDraft(initial).move(from, to)
        assertEquals(initial.pieceAt(from), draft.boardState.pieceAt(to))
        assertEquals(initial, draft.undo().boardState)
    }

    @Test
    fun clearAndResetRemainDraftOperations() {
        val draft = BoardSetupDraft(initial).clear()
        assertTrue(draft.boardState.board.all { it == null })
        assertEquals(initial, draft.original)
        assertFalse(draft.reset().hasChanges)
    }
}
