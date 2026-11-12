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

package com.wanbaohe.gomoku.domain.model

/**
 * 五子棋局面:15×15 格子,每格为空或一方棋子。
 * FEN 风格字符串为事实源(见 [com.wanbaohe.gomoku.domain.FenCodec])。
 */
data class BoardState(
    val board: List<Side?>,
    val sideToMove: Side,
    val moveNumber: Int = 1,
) {
    init {
        require(board.size == BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) {
            "Board must contain exactly ${BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT} cells"
        }
    }

    fun stoneAt(point: BoardPoint): Side? =
        if (point.isInside()) board[point.index] else null

    fun isEmpty(point: BoardPoint): Boolean = stoneAt(point) == null

    fun withStonePlaced(move: GomokuMove): BoardState {
        val nextBoard = board.toMutableList()
        nextBoard[move.to.index] = move.side
        return copy(
            board = nextBoard,
            sideToMove = sideToMove.opposite(),
            moveNumber = moveNumber + 1,
        )
    }

    fun placedCount(): Int = board.count { it != null }

    fun isFull(): Boolean = board.all { it != null }

    companion object {
        fun empty(sideToMove: Side = Side.BLACK): BoardState =
            BoardState(
                board = List(BoardPoint.FILE_COUNT * BoardPoint.RANK_COUNT) { null },
                sideToMove = sideToMove,
            )
    }
}
