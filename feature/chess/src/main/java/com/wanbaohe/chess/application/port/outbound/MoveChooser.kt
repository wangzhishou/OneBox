package com.wanbaohe.chess.application.port.outbound

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
}

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
