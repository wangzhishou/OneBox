package com.wanbaohe.chess.data

import com.shifenmiao.common.manager.AIEngineCatalogManager
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.port.outbound.AiOpponentUnavailableException
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.MoveChooser
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.ChessAiStore
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * 按 [ChessAiSource] 分流走棋实现。
 *
 * - [ChessAiSource.WorkingModel] → 本局选定的聊天服务与模型
 * - [ChessAiSource.RemoteEngine] → 服务端国际象棋引擎(当前内置 Stockfish)
 *
 * LLM / 远程引擎失败分别由 [LlmMoveChooser] / [EngineMoveChooser] 内部经 [ChessMoveFallback] 本地兜底;
 * 兜底主路径是端侧浅层搜索([com.wanbaohe.chess.data.search.ChessSearch]),离线时仍可正常对弈。
 */
@Singleton
class DispatchingMoveChooser @Inject constructor(
    private val chessAiStore: ChessAiStore,
    private val llmMoveChooser: LlmMoveChooser,
    private val engineMoveChooser: EngineMoveChooser,
    private val aiEngineCatalogManager: AIEngineCatalogManager,
) : MoveChooser {

    override suspend fun choose(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<ChessMove>,
        slot: EngineSlot,
    ): MoveDecision? = chooseForGame(boardState, fen, history, legalMoves, slot, null)

    override suspend fun chooseForGame(
        boardState: BoardState,
        fen: String,
        history: List<String>,
        legalMoves: List<ChessMove>,
        slot: EngineSlot,
        playerConfig: GameAiPlayerConfig?,
    ): MoveDecision? {
        if (playerConfig != null && !playerConfig.isSupported) throw AiOpponentUnavailableException()
        return when (val source = playerConfig?.source ?: chessAiStore.get().sourceFor(slot)) {
            ChessAiSource.WorkingModel -> {
                if (playerConfig == null) {
                    llmMoveChooser.choose(boardState, fen, history, legalMoves, slot)
                } else {
                    val model = playerConfig.model?.takeIf { it.name.isNotBlank() }
                        ?: throw AiOpponentUnavailableException()
                    if (playerConfig.engineName.isBlank() || playerConfig.engineProtocol.isBlank()) {
                        throw AiOpponentUnavailableException()
                    }
                    val engine = aiEngineCatalogManager.getEngineByNameAndProtocol(
                        playerConfig.engineName, playerConfig.engineProtocol,
                    ) ?: throw AiOpponentUnavailableException()
                    if (engine.requestProtocol.name != playerConfig.engineProtocol ||
                        engine.requestProtocol == AiRequestProtocol.LOCAL_ON_DEVICE || engine.requestProtocol.isNonChat
                    ) {
                        throw AiOpponentUnavailableException()
                    }
                    val models = aiEngineCatalogManager.observeModelsByProvider().first()[engine.name.lowercase()].orEmpty()
                    if (models.none { it.name == model.name }) throw AiOpponentUnavailableException()
                    llmMoveChooser.chooseWithEngine(boardState, fen, history, legalMoves, slot, engine.copy(model = model))
                }
            }
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
