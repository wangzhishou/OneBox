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

package com.wanbaohe.gomoku.data

import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.AiOpponentUnavailableException
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.MoveChooser
import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiStore
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 按 [GomokuAiSource] 分流走棋实现。
 *
 * - [GomokuAiSource.WorkingModel] → 对局快照中的聊天服务/模型(连接设置按身份实时读取)
 * - [GomokuAiSource.RemoteEngine] → 服务端五子棋引擎(当前内置 Rapfi)
 *
 * LLM / 远程引擎失败分别由 [LlmMoveChooser] / [EngineMoveChooser] 内部经 [GomokuMoveFallback] 本地兜底;
 * 兜底主路径是端侧浅层搜索([com.wanbaohe.gomoku.data.search.GomokuSearch]),离线时仍可正常对弈。
 */
@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val gomokuAiStore: GomokuAiStore,
    private val llmMoveChooser: LlmMoveChooser,
    private val engineMoveChooser: EngineMoveChooser,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
) : MoveChooser {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<GomokuMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        return when (val source = gomokuAiStore.get().sourceFor(slot)) {
            GomokuAiSource.WorkingModel -> llmMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
            )
            is GomokuAiSource.RemoteEngine -> engineMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
                engineId = source.engineId,
            )
        }
    }

    override suspend fun chooseForGame(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<GomokuMove>,
        slot: EngineSlot,
        playerConfig: GameAiPlayerConfig?,
    ): MoveDecision? {
        val config = playerConfig?.takeIf { it.isSupported } ?: throw AiOpponentUnavailableException()
        return when (val source = config.source) {
            GomokuAiSource.WorkingModel -> {
                val engine = aiEngineCatalogManager.getEngineByNameAndProtocol(config.engineName, config.engineProtocol)
                    ?: throw AiOpponentUnavailableException()
                if (!engine.hasAvailableChatRoute()) throw AiOpponentUnavailableException()
                llmMoveChooser.chooseWithEngine(
                    boardState, fen, history, legalMoves, slot,
                    engine.copy(model = requireNotNull(config.model)),
                )
            }
            is GomokuAiSource.RemoteEngine -> engineMoveChooser.choose(
                boardState, fen, history, legalMoves, slot, source.engineId,
            )
        }
    }
}
