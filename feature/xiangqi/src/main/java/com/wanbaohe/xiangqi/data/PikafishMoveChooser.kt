package com.wanbaohe.xiangqi.data

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiProvider
import com.shifenmiao.model.ai.XiangqiEngineRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.XiangqiEngineService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务端 UCI/UCCI 象棋引擎走棋（当前内置 Pikafish，接口可按 [engineId] 扩展多引擎）。
 * 经 Go 网关 `POST /xiangqi/engine/bestmove`，把 `bestmove` 映射回 [legalMoves]。
 *
 * 失败时回退到 [HeuristicMoveFallback] 并在 reason 标明原因（不静默伪装引擎着法）。
 */
@Singleton
class PikafishMoveChooser @Inject constructor(
    private val xiangqiEngineService: XiangqiEngineService,
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
            return HeuristicMoveFallback.decision(legalMoves)
                ?.copy(reason = reason, fallbackUsed = true)
        }

        val selected = matchMove(body.bestmove, legalMoves)
        if (selected == null) {
            return HeuristicMoveFallback.decision(legalMoves)
                ?.copy(reason = "$engineId: unmapped bestmove=${body.bestmove}", fallbackUsed = true)
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
