package com.wanbaohe.chess.data

import com.shifenmiao.core.constants.UrlConstants
import com.shifenmiao.model.ai.BoardGameEngineRequest
import com.shifenmiao.network.AiRequestUrlResolver
import com.shifenmiao.network.NetworkBuilder
import com.shifenmiao.network.api.BoardGameEngineService
import com.t8rin.imagetoolbox.core.domain.coroutines.DispatchersHolder
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 服务端国际象棋引擎走棋(当前内置 Stockfish,接口可按 [engineId] 扩展多引擎)。
 * 经 Go 网关 `POST /chess/engine/bestmove`,把 `bestmove`(UCI,如 "e2e4"/升变 "e7e8q")映射回 [legalMoves]。
 *
 * 失败时回退到 [ChessMoveFallback]（端侧浅层搜索）并在 reason 标明原因(不静默伪装引擎着法)。
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
        legalMoves: List<ChessMove>,
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
                    path = UrlConstants.CHESS_ENGINE_PROXY_PATH,
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
            return ChessMoveFallback.decision(boardState, legalMoves)
                ?.copy(reason = reason, fallbackUsed = true)
        }

        val selected = matchMove(body.bestmove, legalMoves)
        if (selected == null) {
            return ChessMoveFallback.decision(boardState, legalMoves)
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

    /** bestmove 是 UCI 坐标记谱(升变带尾字母),与 [ChessMove.notationUcci] 同格式 */
    private fun matchMove(bestmove: String, legalMoves: List<ChessMove>): ChessMove? {
        val key = bestmove.trim().lowercase()
        if (key.isEmpty()) return null
        return legalMoves.firstOrNull { it.notationUcci.trim().lowercase() == key }
    }

    companion object {
        private const val DEFAULT_MOVE_TIME_MS = 300
        private const val DEFAULT_SKILL = 12
    }
}
