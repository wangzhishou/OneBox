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
import com.wanbaohe.xiangqi.data.local.XiangqiEngineWeights
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务端 UCI/UCCI 象棋引擎走棋（当前内置 Pikafish，接口可按 [engineId] 扩展多引擎）。
 * 经 Go 网关 `POST /xiangqi/engine/bestmove`，把 `bestmove` 映射回 [legalMoves]。
 *
 * 失败时的回退顺序（每一步都在 reason 里标明来源，不静默伪装引擎着法）：
 * 1. [LocalXiangqiEngine] 端侧 Fairy-Stockfish（权重已安装时）—— 离线也有强棋力；
 * 2. [HeuristicMoveFallback] 端侧浅层搜索 —— 零依赖的最后一道防线。
 */
@Singleton
class PikafishMoveChooser @Inject constructor(
    private val xiangqiEngineService: XiangqiEngineService,
    private val localEngine: LocalXiangqiEngine,
    private val engineWeights: XiangqiEngineWeights,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    /** 权重是 10.74MB 的可选下载,成功一次即可长期离线用;失败不影响对局 */
    private val weightsInstallStarted = AtomicBoolean(false)
    private val backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
        engineId: String,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        ensureLocalEngineWeights()

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
                    fallbackUsed = true,
                )
            }
        }
        return HeuristicMoveFallback.decision(boardState, legalMoves)
            ?.withFailureReason("$failureReason | local-engine=${if (localEngine.isReady()) "failed" else "unavailable"}")
    }

    /**
     * 权重 10.74MB，属于可选下载：开局时后台装一次，装好之后离线也能用端侧引擎。
     * 刻意放在引擎走棋路径上（而不是等 UI）——当前还没有下载入口页，
     * 正式接入时应改为设置页的显式入口 + 进度展示，这里只保证「用着用着自己就有了」。
     */
    private fun ensureLocalEngineWeights() {
        if (!localEngine.isPackaged() || engineWeights.isInstalled()) return
        if (!weightsInstallStarted.compareAndSet(false, true)) return
        backgroundScope.launch {
            val file = engineWeights.ensureInstalled()
            if (file == null) {
                Log.w(TAG, "端侧象棋引擎权重未就绪，继续使用服务端引擎")
                // 允许下次对局再试
                weightsInstallStarted.set(false)
            } else {
                Log.i(TAG, "端侧象棋引擎权重已就绪，后续离线对局可用")
            }
        }
    }

    private fun matchMove(bestmove: String, legalMoves: List<XiangqiMove>): XiangqiMove? {
        val key = bestmove.trim().lowercase()
        if (key.isEmpty()) return null
        return legalMoves.firstOrNull { it.notationUcci.trim().lowercase() == key }
    }

    companion object {
        private const val TAG = "PikafishMoveChooser"
        private const val DEFAULT_MOVE_TIME_MS = 400
        private const val DEFAULT_SKILL = 12
    }
}
