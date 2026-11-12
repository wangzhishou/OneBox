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

package com.wanbaohe.xiangqi.domain.model

data class BoardState(
    val board: List<Piece?>,
    val sideToMove: Side,
    val halfMoveClock: Int = 0,
    val fullMoveNumber: Int = 1,
) {
    init {
        require(board.size == BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) {
            "Board must contain exactly ${BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT} cells"
        }
    }

    fun pieceAt(point: BoardPoint): Piece? =
        if (point.isInside()) board[point.index] else null

    fun withPieceMoved(move: XiangqiMove): BoardState {
        val nextBoard = board.toMutableList()
        nextBoard[move.from.index] = null
        nextBoard[move.to.index] = move.piece
        return copy(
            board = nextBoard,
            sideToMove = sideToMove.opposite(),
            halfMoveClock = nextHalfMoveClock(move),
            fullMoveNumber = if (sideToMove == Side.BLACK) fullMoveNumber + 1 else fullMoveNumber,
        )
    }

    private fun nextHalfMoveClock(move: XiangqiMove): Int =
        if (move.captured != null || move.piece.type == PieceType.PAWN) 0 else halfMoveClock + 1
}
