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

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.XiangqiEngineRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.XiangqiEngineService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.data.local.LocalXiangqiEngine
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务端 UCI/UCCI 象棋引擎走棋（当前内置 Pikafish，接口可按 [engineId] 扩展多引擎）。
 * 经 Go 网关 `POST /xiangqi/engine/bestmove`，把 `bestmove` 映射回 [legalMoves]。
 *
 * 失败时的回退顺序（每一步都在 reason 里标明来源，不静默伪装引擎着法）：
 * 1. [LocalXiangqiEngine] 端侧 Fairy-Stockfish（权重已安装时）—— 离线也有强棋力；
 * 2. [HeuristicMoveFallback] 端侧浅层搜索 —— 零依赖的最后一道防线。
 *
 * 权重下载**不在这里触发**:10.74MB 必须由用户在象棋设置页显式发起,不能借走棋偷跑流量。
 */
@Singleton
class PikafishMoveChooser @Inject constructor(
    private val xiangqiEngineService: XiangqiEngineService,
    private val localEngine: LocalXiangqiEngine,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
        engineId: String,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val engine = AiEngine.builtInEngine(AiProvider.Pikafish)
        val request = XiangqiEngineRequest(
            fen = fen,
            engine = engineId,
            moveTimeMs = DEFAULT_MOVE_TIME_MS,
            skill = DEFAULT_SKILL,
        )

        val outcome = withContext(ioDispatcher) {
            runCatching {
                val url = AiRequestUrlResolver.resolveRequestUrl(engine)
                val response = xiangqiEngineService.bestMove(url = url, body = request).execute()
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(IllegalStateException("http ${response.code()}"))
                }
                val body = response.body()
                    ?: return@runCatching Result.failure(IllegalStateException("empty body"))
                Result.success(body)
            }.getOrElse { Result.failure(it) }
        }

        val body = outcome.getOrNull()
        if (body == null) {
            val reason = "$engineId: ${outcome.exceptionOrNull()?.message ?: "failed"}"
            return fallbackMove(boardState, fen, legalMoves, reason)
        }

        val selected = matchMove(body.bestmove, legalMoves)
        if (selected == null) {
            return fallbackMove(boardState, fen, legalMoves, "$engineId: unmapped bestmove=${body.bestmove}")
        }

        val scoreText = when {
            body.scoreMate != null -> "mate=${body.scoreMate}"
            body.scoreCp != null -> "cp=${body.scoreCp}"
            else -> "score=?"
        }
        return MoveDecision(
            move = selected,
            reason = "$engineId $scoreText depth=${body.depth ?: 0} t=${body.timeMs ?: 0}ms",
            rawResponse = "bestmove=${body.bestmove} pv=${body.pv.orEmpty()}",
            fallbackUsed = false,
        )
    }

    /**
     * 远程引擎失败后的回退：先端侧 Fairy-Stockfish(象棋 NNUE)，再自研浅层搜索。
     * 未打包/未装权重/引擎异常都直接落到浅层搜索，绝不阻塞对局。
     *
     * 本地引擎接手标记为 [MoveDecision.localEngineSwap]（棋力正常，软提示）；
     * 只有浅层启发式才是 [MoveDecision.fallbackUsed]（需警示 + 引导下载）。
     */
    private suspend fun fallbackMove(
        boardState: BoardState,
        fen: String,
        legalMoves: List<XiangqiMove>,
        failureReason: String,
    ): MoveDecision? {
        val local = localEngine.bestMove(fen, legalMoves.map { it.notationUcci })
        if (local != null) {
            val matched = matchMove(local.ucci, legalMoves)
            if (matched != null) {
                val score = local.scoreCp?.let { "cp=$it" } ?: "score=?"
                return MoveDecision(
                    move = matched,
                    reason = "local-engine $score depth=${local.depth ?: 0} | $failureReason",
                    rawResponse = "local-engine bestmove=${local.ucci}",
                    fallbackUsed = false,
                    localEngineSwap = true,
                )
            }
        }
        return HeuristicMoveFallback.decision(boardState, legalMoves)
            ?.withFailureReason("$failureReason | local-engine=${if (localEngine.isReady()) "failed" else "unavailable"}")
    }

    private fun matchMove(bestmove: String, legalMoves: List<XiangqiMove>): XiangqiMove? {
        val key = bestmove.trim().lowercase()
        if (key.isEmpty()) return null
        return legalMoves.firstOrNull { it.notationUcci.trim().lowercase() == key }
    }

    companion object {
        private const val DEFAULT_MOVE_TIME_MS = 400
        private const val DEFAULT_SKILL = 12
    }
}
