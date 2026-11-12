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

package com.wanbaohe.chess.application.port.outbound

import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.Side
import com.wanbaohe.chess.domain.model.ChessMove

interface MoveChooser {
    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<ChessMove>,
        slot: EngineSlot,
    ): MoveDecision?

    suspend fun chooseForGame(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<ChessMove>,
        slot: EngineSlot,
        playerConfig: GameAiPlayerConfig?,
    ): MoveDecision? = choose(boardState, fen, history, legalMoves, slot)
}

class AiOpponentUnavailableException : IllegalStateException("Saved AI opponent is unavailable")

enum class EngineSlot { FAST, DUEL_A, DUEL_B }

data class MoveDecision(
    val move: ChessMove,
    val reason: String,
    val rawResponse: String,
    val fallbackUsed: Boolean,
) {

    /**
     * 落库用的 `aiReason`。
     *
     * 兜底着法带 [LOCAL_FALLBACK_MARKER] 前缀，UI 据此区分"引擎给的手"与"本地兜底的手"。
     * 远程引擎故障时兜底是静默的（表现只是 AI 突然变笨），没有这个标记用户无从察觉。
     */
    fun storedReason(): String =
        if (fallbackUsed) LOCAL_FALLBACK_MARKER + reason else reason

    companion object {
        /** 持久化在 `PlyEntity.aiReason` 里的机器可读前缀，勿随意改动。 */
        const val LOCAL_FALLBACK_MARKER = "local-fallback::"

        /** 判断某一步是否由本地兜底产生。 */
        fun isLocalFallback(storedReason: String): Boolean =
            storedReason.startsWith(LOCAL_FALLBACK_MARKER)
    }
}
