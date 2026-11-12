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

package com.wanbaohe.xiangqi.data

import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.port.outbound.AiOpponentUnavailableException
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiStore
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 按象棋专用 [XiangqiAiSource] 分流走棋实现。
 *
 * - [XiangqiAiSource.WorkingModel] → 聊天 LLM（全局快速工作模型）
 * - [XiangqiAiSource.Jev] → 判断模型
 * - [XiangqiAiSource.RemoteEngine] → 服务端 UCI 引擎（当前内置 Pikafish）
 * - [XiangqiAiSource.LocalEngine] → 端侧 Fairy-Stockfish（需已下载权重）
 *
 * 新增开源象棋驱动时，若仍是 UCI 协议可复用 [PikafishMoveChooser]（传不同 engineId）；
 * 若协议不同再加一路 MoveChooser 并在此 when 分支。
 */
@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val xiangqiAiStore: XiangqiAiStore,
    private val llmMoveChooser: LlmMoveChooser,
    private val jevMoveChooser: JevMoveChooser,
    private val pikafishMoveChooser: PikafishMoveChooser,
    private val localEngineMoveChooser: LocalEngineMoveChooser,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
) : MoveChooser {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision? = chooseForGame(boardState, fen, history, legalMoves, slot, null)

    override suspend fun chooseForGame(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
        playerConfig: GameAiPlayerConfig?,
    ): MoveDecision? {
        val source = playerConfig?.source ?: xiangqiAiStore.get().sourceFor(slot)
        return when (source) {
            XiangqiAiSource.WorkingModel -> {
                if (playerConfig == null) {
                    llmMoveChooser.choose(boardState, fen, history, legalMoves, slot)
                } else {
                    val model = playerConfig.model ?: throw AiOpponentUnavailableException()
                    val engine = aiEngineCatalogManager.getEngineByNameAndProtocol(
                        playerConfig.engineName,
                        playerConfig.engineProtocol,
                    ) ?: throw AiOpponentUnavailableException()
                    llmMoveChooser.chooseWithEngine(boardState, fen, history, legalMoves, slot, engine.copy(model = model))
                }
            }
            XiangqiAiSource.Jev -> jevMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
            )
            XiangqiAiSource.LocalEngine -> localEngineMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                legalMoves = legalMoves,
            )
            is XiangqiAiSource.RemoteEngine -> pikafishMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
                engineId = source.engineId,
            )
        }
    }
}
