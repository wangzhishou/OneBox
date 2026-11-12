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

package com.wanbaohe.xiangqi.domain

import com.wanbaohe.xiangqi.domain.model.BoardPoint
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

data class InteractionState(
    val selectedPoint: BoardPoint? = null,
    val candidateTargets: Set<BoardPoint> = emptySet(),
    val pendingMove: XiangqiMove? = null,
)

sealed interface GameAction {
    data class TapCell(val point: BoardPoint) : GameAction
    data object ClearSelection : GameAction
}

/**
 * Pure UI interaction reducer. Derives selection state and pending moves from
 * immutable board state plus legal moves. Never mutates game truth.
 */
object GameReducer {

    fun reduce(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        previous: InteractionState,
        action: GameAction,
    ): InteractionState = when (action) {
        GameAction.ClearSelection -> InteractionState()
        is GameAction.TapCell -> onTap(boardState, legalMoves, previous, action.point)
    }

    private fun onTap(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        previous: InteractionState,
        point: BoardPoint,
    ): InteractionState {
        val selected = previous.selectedPoint
        val move = selected?.let { from ->
            legalMoves.find { it.from == from && it.to == point }
        }
        if (move != null) return InteractionState(pendingMove = move)
        return selectPoint(boardState, legalMoves, point)
    }

    private fun selectPoint(
        boardState: BoardState,
        legalMoves: List<XiangqiMove>,
        point: BoardPoint,
    ): InteractionState {
        val piece = boardState.pieceAt(point)
        if (piece?.side != boardState.sideToMove) return InteractionState()
        return InteractionState(
            selectedPoint = point,
            candidateTargets = legalMoves.asSequence()
                .filter { it.from == point }
                .map { it.to }
                .toSet(),
        )
    }
}
