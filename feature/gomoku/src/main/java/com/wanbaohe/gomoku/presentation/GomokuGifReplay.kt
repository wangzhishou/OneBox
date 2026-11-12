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

package com.wanbaohe.gomoku.presentation

import com.wanbaohe.boardgame.export.BoardReplayMove
import com.wanbaohe.boardgame.export.BoardReplaySnapshot
import com.wanbaohe.gomoku.component.GomokuAnalysisUiState
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.Notation
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.Side

data class GomokuGifPosition(
    val boardState: BoardState,
    val lastMove: BoardPoint? = null,
)

fun GomokuAnalysisUiState.toGifReplaySnapshot(
    title: String,
    resultText: String,
    blackLabel: String,
    whiteLabel: String,
): BoardReplaySnapshot<GomokuGifPosition> {
    require(isLoaded) { "The replay has not loaded" }
    return BoardReplaySnapshot(
        title = title,
        initialPosition = GomokuGifPosition(FenCodec.parse(initialFen)),
        currentPly = currentPly,
        resultText = resultText,
        moves = plies.sortedBy { it.ply }.map { record ->
            val lastMove = requireNotNull(Notation.parse(record.moveUcci)) {
                "Invalid move at ply ${record.ply}"
            }
            BoardReplayMove(
                ply = record.ply,
                beforePosition = GomokuGifPosition(FenCodec.parse(record.beforeFen)),
                afterPosition = GomokuGifPosition(FenCodec.parse(record.afterFen), lastMove),
                notation = record.moveCn.ifBlank { record.moveUcci },
                moveCode = record.moveUcci,
                moverLabel = if (record.moverSide == Side.BLACK) blackLabel else whiteLabel,
            )
        },
    )
}
