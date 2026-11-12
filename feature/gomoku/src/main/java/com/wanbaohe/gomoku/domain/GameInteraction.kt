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
import com.wanbaohe.gomoku.domain.model.GomokuMove

data class InteractionState(
    val selectedPoint: BoardPoint? = null,
    val candidateTargets: Set<BoardPoint> = emptySet(),
    val pendingMove: GomokuMove? = null,
)

sealed interface GameAction {
    data class TapCell(val point: BoardPoint) : GameAction
    data object ClearSelection : GameAction
}

/**
 * Pure UI interaction reducer。五子棋无"选中棋子再走位"两步交互:
 * 点击合法空位即直接生成 pendingMove;点击其他位置清除选中态。
 */
object GameReducer {

    fun reduce(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
        previous: InteractionState,
        action: GameAction,
    ): InteractionState = when (action) {
        GameAction.ClearSelection -> InteractionState()
        is GameAction.TapCell -> onTap(boardState, legalMoves, previous, action.point)
    }

    private fun onTap(
        boardState: BoardState,
        legalMoves: List<GomokuMove>,
        previous: InteractionState,
        point: BoardPoint,
    ): InteractionState {
        if (!point.isInside() || !boardState.isEmpty(point)) return InteractionState()
        val move = legalMoves.find { it.to == point } ?: return InteractionState()
        return InteractionState(
            selectedPoint = point,
            pendingMove = move,
        )
    }
}
