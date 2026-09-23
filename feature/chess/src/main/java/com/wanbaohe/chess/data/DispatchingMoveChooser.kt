package com.wanbaohe.chess.data

import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.MoveChooser
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 按 [ChessAiSource] 分流走棋实现。
 *
 * - [ChessAiSource.WorkingModel] → 聊天 LLM(全局快速工作模型)
 * - [ChessAiSource.RemoteEngine] → 服务端国际象棋引擎(当前内置 Stockfish)
 *
 * LLM / 远程引擎失败分别由 [LlmMoveChooser] / [EngineMoveChooser] 内部经 [ChessMoveFallback] 本地兜底。
 */
@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val chessAiStore: ChessAiStore,
    private val llmMoveChooser: LlmMoveChooser,
    private val engineMoveChooser: EngineMoveChooser,
) : MoveChooser {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<ChessMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        return when (val source = chessAiStore.get().sourceFor(slot)) {
            ChessAiSource.WorkingModel -> llmMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
            )
            is ChessAiSource.RemoteEngine -> engineMoveChooser.choose(
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
