package com.wanbaohe.gomoku.data

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.BoardGameEngineRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.network.api.BoardGameEngineService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GomokuMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务端五子棋引擎走棋(当前内置 Rapfi,接口可按 [engineId] 扩展多引擎)。
 * 经 Go 网关 `POST /gomoku/engine/bestmove`,把 `bestmove` 坐标(如 "H8")映射回 [legalMoves]。
 *
 * 失败时回退到 [GomokuMoveFallback]（端侧浅层搜索）并在 reason 标明原因(不静默伪装引擎着法)。
 */
@Singleton
class EngineMoveChooser @Inject constructor(
    private val boardGameEngineService: BoardGameEngineService,
    dispatchersHolder: DispatchersHolder,
) : DispatchersHolder by dispatchersHolder {

    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<GomokuMove>,
        slot: EngineSlot,
        engineId: String,
    ): MoveDecision? {
        if (legalMoves.isEmpty()) return null

        val request = BoardGameEngineRequest(
            fen = fen,
            engine = engineId,
            moveTimeMs = DEFAULT_MOVE_TIME_MS,
            skill = DEFAULT_SKILL,
        )

        val outcome = withContext(ioDispatcher) {
            runCatching {
                val url = AiRequestUrlResolver.joinUrl(
                    baseUrl = NetworkBuilder.getBaseUrl(),
                    path = UrlConstants.GOMOKU_ENGINE_PROXY_PATH,
                )
                val response = boardGameEngineService.bestMove(url = url, body = request).execute()
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
            return GomokuMoveFallback.decision(boardState, legalMoves)
                ?.copy(reason = reason, fallbackUsed = true)
        }

        val selected = matchMove(body.bestmove, legalMoves)
        if (selected == null) {
            return GomokuMoveFallback.decision(boardState, legalMoves)
                ?.copy(reason = "$engineId: unmapped bestmove=${body.bestmove}", fallbackUsed = true)
        }

        val scoreText = body.scoreCp?.let { "cp=$it" } ?: "score=?"
        return MoveDecision(
            move = selected,
            reason = "$engineId $scoreText depth=${body.depth ?: 0} t=${body.timeMs ?: 0}ms",
            rawResponse = "bestmove=${body.bestmove}",
            fallbackUsed = false,
        )
    }

    /** bestmove 是连续 A-O 坐标(如 "H8"),与 [GomokuMove.notationUcci] 同格式,匹配不区分大小写 */
    private fun matchMove(bestmove: String, legalMoves: List<GomokuMove>): GomokuMove? {
        val key = bestmove.trim()
        if (key.isEmpty()) return null
        return legalMoves.firstOrNull { it.notationUcci.trim().equals(key, ignoreCase = true) }
    }

    companion object {
        private const val DEFAULT_MOVE_TIME_MS = 300
        private const val DEFAULT_SKILL = 12
    }
}
