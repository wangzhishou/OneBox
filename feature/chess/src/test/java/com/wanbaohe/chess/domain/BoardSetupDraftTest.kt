/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.chess.domain

import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.model.Piece
import com.wanbaohe.chess.domain.model.PieceType
import com.wanbaohe.chess.domain.model.Side
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BoardSetupDraftTest {
    private val opening = FenCodec.parse(FenCodec.INITIAL_FEN)

    @Test
    fun movingAPieceClearsHistoryDependentRightsWithoutChangingTheSource() {
        val draft = BoardSetupDraft(opening).move(BoardPoint(4, 1), BoardPoint(4, 3))
        assertTrue(draft.hasChanges)
        assertEquals("-", draft.boardState.castlingRights)
        assertNull(draft.boardState.enPassant)
        assertEquals("KQkq", opening.castlingRights)
        assertEquals(Piece(Side.WHITE, PieceType.PAWN), opening.pieceAt(BoardPoint(4, 1)))
    }

    @Test
    fun movingAKingOrRookBackNeverInventsCastlingRights() {
        val king = BoardSetupDraft(opening)
            .move(BoardPoint(4, 0), BoardPoint(4, 2)).move(BoardPoint(4, 2), BoardPoint(4, 0))
        assertEquals(opening.board, king.boardState.board)
        assertEquals("-", king.boardState.castlingRights)
        assertTrue(king.hasChanges)
        val rook = BoardSetupDraft(opening)
            .move(BoardPoint(0, 0), BoardPoint(0, 2)).move(BoardPoint(0, 2), BoardPoint(0, 0))
        assertEquals("-", rook.boardState.castlingRights)
    }

    @Test
    fun paletteReplacementAndErasingClearEnPassantAndCastling() {
        val board = FenCodec.parse("r3k2r/8/8/3pP3/8/8/8/R3K2R w KQkq d6 0 9")
        val placed = BoardSetupDraft(board).selectPiece(Piece(Side.WHITE, PieceType.QUEEN)).tap(BoardPoint(2, 2))
        assertNull(placed.boardState.enPassant)
        assertEquals("-", placed.boardState.castlingRights)
        val erased = BoardSetupDraft(board).selectTool(SetupTool.ERASE).tap(BoardPoint(0, 0))
        assertNull(erased.boardState.enPassant)
        assertEquals("-", erased.boardState.castlingRights)
    }

    @Test
    fun changingOnlyTheTurnClearsEnPassantButKeepsValidCastling() {
        val board = FenCodec.parse("r3k2r/8/8/3pP3/8/8/8/R3K2R w KQkq d6 0 9")
        val draft = BoardSetupDraft(board).setSideToMove(Side.BLACK)
        assertNull(draft.boardState.enPassant)
        assertEquals(board.castlingRights, draft.boardState.castlingRights)
        assertEquals(board, draft.undo().boardState)
    }

    @Test
    fun selectionAndClockNormalizationAreNotEdits() {
        val board = opening.copy(halfMoveClock = 7, fullMoveNumber = 28)
        val draft = BoardSetupDraft(board).tap(BoardPoint(4, 1)).selectTool(SetupTool.MOVE)
        assertFalse(draft.hasChanges)
        assertTrue(draft.undoBoards.isEmpty())
        assertEquals(0, draft.startPosition().halfMoveClock)
        assertEquals(1, draft.startPosition().fullMoveNumber)
        assertFalse(draft.copy(boardState = draft.startPosition()).hasChanges)
        assertFalse(draft.setSideToMove(Side.WHITE).hasChanges)
    }

    @Test
    fun noOpPlacementOrEraseDoesNotClearRights() {
        val placed = BoardSetupDraft(opening).selectPiece(Piece(Side.WHITE, PieceType.PAWN)).tap(BoardPoint(4, 1))
        assertFalse(placed.hasChanges)
        assertFalse(BoardSetupDraft(opening).selectTool(SetupTool.ERASE).tap(BoardPoint(3, 3)).hasChanges)
    }

    @Test
    fun draftUndoRedoRestoresExactMetadataAndIsIndependentOfGameHistory() {
        val edited = BoardSetupDraft(opening).clear()
        val undone = edited.undo()
        assertEquals(opening, undone.boardState)
        assertFalse(undone.hasChanges)
        assertEquals(edited.boardState, undone.redo().boardState)
        assertEquals(opening, edited.original)
        assertTrue(undone.setSideToMove(Side.BLACK).redoBoards.isEmpty())
    }

    @Test
    fun resetRestoresStandardOpeningAndItsRights() {
        val draft = BoardSetupDraft(opening).clear().reset()
        assertEquals(opening, draft.startPosition())
        assertFalse(draft.hasChanges)
        assertEquals("-", draft.undo().boardState.castlingRights)
    }

    @Test
    fun tappingAnOccupiedTargetReplacesItWithTheSelectedPiece() {
        val draft = BoardSetupDraft(opening).tap(BoardPoint(4, 0)).tap(BoardPoint(3, 0))
        assertNull(draft.boardState.pieceAt(BoardPoint(4, 0)))
        assertEquals(Piece(Side.WHITE, PieceType.KING), draft.boardState.pieceAt(BoardPoint(3, 0)))
    }
}
