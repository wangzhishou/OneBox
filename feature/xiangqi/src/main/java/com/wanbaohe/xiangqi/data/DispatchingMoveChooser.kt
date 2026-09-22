package com.wanbaohe.xiangqi.data

import com.shifenmiao.common.manager.AIEngineManager
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val aiEngineManager: AIEngineManager,
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
        val chooser = when (slot.toEngine().requestProtocol) {
            AiRequestProtocol.JEV -> jevMoveChooser
            AiRequestProtocol.PIKAFISH -> pikafishMoveChooser
            else -> llmMoveChooser
        }
        return chooser.choose(
            boardState = boardState,
            fen = fen,
            history = history,
            legalMoves = legalMoves,
            slot = slot,
        )
    }

    private fun EngineSlot.toEngine(): AiEngine = when (this) {
        EngineSlot.FAST -> aiEngineManager.getFastAiEngine()
        EngineSlot.DUEL_A -> aiEngineManager.getDuelEngineA()
        EngineSlot.DUEL_B -> aiEngineManager.getDuelEngineB()
    }
}
