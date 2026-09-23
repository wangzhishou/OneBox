package com.wanbaohe.gomoku.data

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
 * - [GomokuAiSource.WorkingModel] → 聊天 LLM(全局快速工作模型)
 * - [GomokuAiSource.RemoteEngine] → 服务端五子棋引擎(当前内置 Rapfi)
 *
 * LLM / 远程引擎失败分别由 [LlmMoveChooser] / [EngineMoveChooser] 内部经 [GomokuMoveFallback] 本地兜底。
 */
@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val gomokuAiStore: GomokuAiStore,
    private val llmMoveChooser: LlmMoveChooser,
    private val engineMoveChooser: EngineMoveChooser,
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
}
