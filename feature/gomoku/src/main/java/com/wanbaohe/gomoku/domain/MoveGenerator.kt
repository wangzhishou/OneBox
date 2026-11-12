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
import com.wanbaohe.gomoku.domain.model.GomokuMove

/**
 * 五子棋走法生成:所有空位均可落子,无吃子/阻挡概念。
 */
object MoveGenerator {

    fun pseudoLegalMoves(boardState: BoardState, side: Side = boardState.sideToMove): List<GomokuMove> {
        return buildList {
            for (rank in 0 until BoardPoint.RANK_COUNT) {
                for (file in 0 until BoardPoint.FILE_COUNT) {
                    val point = BoardPoint(file, rank)
                    if (boardState.isEmpty(point)) {
                        add(GomokuMove(to = point, side = side))
                    }
                }
            }
        }
    }
}
