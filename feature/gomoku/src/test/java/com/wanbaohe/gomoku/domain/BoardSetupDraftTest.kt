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
