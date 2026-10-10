package com.wanbaohe.xiangqi.application.port.outbound

import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove

interface MoveChooser {
    suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision?

    suspend fun chooseForGame(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
        playerConfig: GameAiPlayerConfig?,
    ): MoveDecision? = choose(boardState, fen, history, legalMoves, slot)
}

enum class EngineSlot { FAST, DUEL_A, DUEL_B }

class AiOpponentUnavailableException : IllegalStateException()

data class MoveDecision(
    val move: XiangqiMove,
    val reason: String,
    val rawResponse: String,
    val fallbackUsed: Boolean,
    /** 云端失败后改由端侧强引擎出招——棋力正常，只需每局一次软提示，不走「引擎不可用」红条。 */
    val localEngineSwap: Boolean = false,
) {

    /**
     * 落库用的 `aiReason`。
     *
     * - [LOCAL_FALLBACK_MARKER]：浅层启发式最后一道兜底（棋力弱，需警示 + 引导下载本地引擎）
     * - [LOCAL_ENGINE_SWAPPED_MARKER]：云端失败后本地引擎接手（棋力正常，软提示即可）
     */
    fun storedReason(): String = when {
        fallbackUsed -> LOCAL_FALLBACK_MARKER + reason
        localEngineSwap -> LOCAL_ENGINE_SWAPPED_MARKER + reason
        else -> reason
    }

    companion object {
        /** 持久化在 `PlyEntity.aiReason` 里的机器可读前缀，勿随意改动。 */
        const val LOCAL_FALLBACK_MARKER = "local-fallback::"

        /** 云端失败后由端侧引擎顶上；UI 与浅层兜底区分开。 */
        const val LOCAL_ENGINE_SWAPPED_MARKER = "local-engine-swapped::"

        /** 判断某一步是否由浅层启发式兜底产生。 */
        fun isLocalFallback(storedReason: String): Boolean =
            storedReason.startsWith(LOCAL_FALLBACK_MARKER)

        /** 判断某一步是否为云端失败后的本地引擎接手。 */
        fun isLocalEngineSwap(storedReason: String): Boolean =
            storedReason.startsWith(LOCAL_ENGINE_SWAPPED_MARKER)

        /** 去掉机器前缀后的原因串（排障/展示用）。 */
        fun stripMarkers(storedReason: String): String = storedReason
            .removePrefix(LOCAL_FALLBACK_MARKER)
            .removePrefix(LOCAL_ENGINE_SWAPPED_MARKER)
    }
}
