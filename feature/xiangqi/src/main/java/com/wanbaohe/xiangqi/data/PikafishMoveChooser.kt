package com.wanbaohe.xiangqi.data

import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.shifenmiao.model.ai.XiangqiEngineRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.api.XiangqiEngineService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务端 Pikafish 走棋。经 Go 网关 `POST /xiangqi/engine/bestmove`，
 * 把返回的 UCCI `bestmove` 映射回 [legalMoves]。
 *
 * 失败时不静默伪装成引擎着法：回退到 [HeuristicMoveFallback]，并在 reason 标明原因。
 */
@Singleton
class PikafishMoveChooser @Inject constructor(
    private val aiEngineManager: AIEngineManager,
    private val xiangqiEngineService: XiangqiEngineService,
    dispatchersHolder: DispatchersHolder,
) : MoveChooser, DispatchersHolder by dispatchersHolder {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val engine = slot.toEngine()
        if (engine.requestProtocol != AiRequestProtocol.PIKAFISH) {
            return HeuristicMoveFallback.decision(legalMoves)
                ?.copy(reason = "pikafish: protocol mismatch", fallbackUsed = true)
        }

        val request = XiangqiEngineRequest(
            fen = fen,
            moveTimeMs = DEFAULT_MOVE_TIME_MS,
            skill = resolveSkill(engine.model.name),
        )

        val outcome = withContext(ioDispatcher) {
            runCatching {
                val url = AiRequestUrlResolver.resolveRequestUrl(engine)
                val response = xiangqiEngineService.bestMove(url = url, body = request).execute()
                if (!response.isSuccessful) {
                    return@runCatching Result.failure(
                        IllegalStateException("http ${response.code()}"),
                    )
                }
                val body = response.body()
                    ?: return@runCatching Result.failure(IllegalStateException("empty body"))
                Result.success(body)
            }.getOrElse { Result.failure(it) }
        }

        val body = outcome.getOrNull()
        if (body == null) {
            val reason = "pikafish: ${outcome.exceptionOrNull()?.message ?: "failed"}"
            return HeuristicMoveFallback.decision(legalMoves)
                ?.copy(reason = reason, fallbackUsed = true)
        }

        val selected = matchMove(body.bestmove, legalMoves)
        if (selected == null) {
            return HeuristicMoveFallback.decision(legalMoves)
                ?.copy(reason = "pikafish: unmapped bestmove=${body.bestmove}", fallbackUsed = true)
        }

        val scoreText = when {
            body.scoreMate != null -> "mate=${body.scoreMate}"
            body.scoreCp != null -> "cp=${body.scoreCp}"
            else -> "score=?"
        }
        return MoveDecision(
            move = selected,
            reason = "pikafish $scoreText depth=${body.depth ?: 0} t=${body.timeMs ?: 0}ms",
            rawResponse = "bestmove=${body.bestmove} pv=${body.pv.orEmpty()}",
            fallbackUsed = false,
        )
    }

    private fun matchMove(bestmove: String, legalMoves: List<XiangqiMove>): XiangqiMove? {
        val key = bestmove.trim().lowercase()
        if (key.isEmpty()) return null
        return legalMoves.firstOrNull { it.notationUcci.trim().lowercase() == key }
    }

    /**
     * 模型名可带难度：`pikafish-skill-8` → 8；默认 12。skill 范围 0-20。
     */
    private fun resolveSkill(modelName: String): Int {
        val matched = SKILL_REGEX.find(modelName.lowercase())?.groupValues?.getOrNull(1)?.toIntOrNull()
        return matched?.coerceIn(0, 20) ?: DEFAULT_SKILL
    }

    private fun EngineSlot.toEngine(): AiEngine = when (this) {
        EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
        EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
        EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
    }

    companion object {
        private const val DEFAULT_MOVE_TIME_MS = 400
        private const val DEFAULT_SKILL = 12
        private val SKILL_REGEX = Regex("""skill-(\d{1,2})""")
    }
}
