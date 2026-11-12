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
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove

data class InteractionState(
    val selectedPoint: BoardPoint? = null,
    val candidateTargets: Set<BoardPoint> = emptySet(),
    val pendingMove: ChessMove? = null,
    /** 升变待选:同一起止点的 4 个升变候选(后/车/象/马),UI 弹选择器 */
    val pendingPromotionMoves: List<ChessMove> = emptyList(),
)

sealed interface GameAction {
    data class TapCell(val point: BoardPoint) : GameAction
    data object ClearSelection : GameAction
}

/**
 * Pure UI interaction reducer。两步交互:点己方棋子选中(亮候选格),再点候选格生成着法。
 * 升变不直接 pendingMove,先交出 4 个候选由 UI 弹选择器,选完后调用方再提交。
 */
object GameReducer {

    fun reduce(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
        previous: InteractionState,
        action: GameAction,
    ): InteractionState = when (action) {
        GameAction.ClearSelection -> InteractionState()
        is GameAction.TapCell -> onTap(boardState, legalMoves, previous, action.point)
    }

    private fun onTap(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
        previous: InteractionState,
        point: BoardPoint,
    ): InteractionState {
        val selected = previous.selectedPoint
        if (selected != null) {
            val moves = legalMoves.filter { it.from == selected && it.to == point }
            if (moves.isNotEmpty()) {
                // 升变:同一起止点多候选,交给 UI 选择
                if (moves.any { it.promotion != null }) {
                    return InteractionState(
                        selectedPoint = selected,
                        pendingPromotionMoves = moves,
                    )
                }
                return InteractionState(pendingMove = moves.first())
            }
        }
        return selectPoint(boardState, legalMoves, point)
    }

    private fun selectPoint(
        boardState: BoardState,
        legalMoves: List<ChessMove>,
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
