package com.wanbaohe.xiangqi.data

import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiStore
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 按象棋专用 [XiangqiAiSource] 分流走棋实现。
 *
 * - [XiangqiAiSource.WorkingModel] → 聊天 LLM（全局快速工作模型）
 * - [XiangqiAiSource.Jev] → 判断模型
 * - [XiangqiAiSource.RemoteEngine] → 服务端 UCI 引擎（当前内置 Pikafish）
 *
 * 新增开源象棋驱动时，若仍是 UCI 协议可复用 [PikafishMoveChooser]（传不同 engineId）；
 * 若协议不同再加一路 MoveChooser 并在此 when 分支。
 */
@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val xiangqiAiStore: XiangqiAiStore,
    private val llmMoveChooser: LlmMoveChooser,
    private val jevMoveChooser: JevMoveChooser,
    private val pikafishMoveChooser: PikafishMoveChooser,
) : MoveChooser {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<XiangqiMove>,
        slot: EngineSlot,
    ): MoveDecision? {
        val source = xiangqiAiStore.get().sourceFor(slot)
        return when (source) {
            XiangqiAiSource.WorkingModel -> llmMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
            )
            XiangqiAiSource.Jev -> jevMoveChooser.choose(
                boardState = boardState,
                fen = fen,
                history = history,
                legalMoves = legalMoves,
                slot = slot,
            )
            is XiangqiAiSource.RemoteEngine -> pikafishMoveChooser.choose(
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
