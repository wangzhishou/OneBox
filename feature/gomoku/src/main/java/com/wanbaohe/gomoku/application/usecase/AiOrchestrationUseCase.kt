package com.wanbaohe.gomoku.application.usecase

import com.wanbaohe.gomoku.application.dto.GameDetail
import com.wanbaohe.gomoku.application.port.outbound.AiTaskEntity
import com.wanbaohe.gomoku.application.port.outbound.AiTaskStatus
import com.wanbaohe.gomoku.application.port.outbound.AiTaskStore
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.MoveChooser
import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiOrchestrationUseCase @Inject constructor(
    private val moveChooser: MoveChooser,
    private val playMove: PlayMoveUseCase,
    private val query: GameQueryUseCase,
    private val aiTaskStore: AiTaskStore,
) {

    sealed interface Outcome {
        data class Committed(val detail: GameDetail) : Outcome
        data class Stale(val reason: StaleReason) : Outcome
        data class Failed(val reason: String) : Outcome
    }

    enum class StaleReason { POSITION_CHANGED, MOVE_REJECTED }

    suspend fun requestMove(gameId: String, slot: EngineSlot): Outcome {
        val detail = query.getById(gameId) ?: return Outcome.Failed("Game not found")
        val boardState = FenCodec.parse(detail.currentFen)
        val requestFen = detail.currentFen
        val targetPly = detail.currentPly + 1

        saveTaskRunning(gameId, targetPly, requestFen)

        val decision = callMoveChooser(boardState, requestFen, detail, slot)
            ?: return Outcome.Failed("No legal move")

        return commitIfConsistent(gameId, requestFen, targetPly, detail.currentPly, decision)
    }

    suspend fun retry(gameId: String, slot: EngineSlot): Outcome = requestMove(gameId, slot)

    suspend fun clearTasks(gameId: String) {
        aiTaskStore.deleteByGame(gameId)
    }

    private suspend fun saveTaskRunning(gameId: String, targetPly: Int, requestJson: String) {
        aiTaskStore.upsert(
            AiTaskEntity(
                id = "$gameId:$targetPly",
                gameId = gameId,
                targetPly = targetPly,
                requestJson = requestJson,
                status = AiTaskStatus.RUNNING,
            ),
        )
    }

    private suspend fun callMoveChooser(
        boardState: com.wanbaohe.gomoku.domain.model.BoardState,
        requestFen: String,
        detail: GameDetail,
        slot: EngineSlot,
    ): MoveDecision? = moveChooser.choose(
        boardState = boardState,
        fen = requestFen,
        // 历史走坐标记谱: 与 legalMoves 的候选键同一种记法, 模型最终回的就是坐标
        // 
        history = detail.plies.takeLast(6).map { it.moveUcci.ifBlank { it.moveCn } },
        legalMoves = GameArbiter.legalMoves(boardState),
        slot = slot,
    )

    private suspend fun commitIfConsistent(
        gameId: String,
        requestFen: String,
        targetPly: Int,
        expectedCurrentPly: Int,
        decision: MoveDecision,
    ): Outcome {
        val latest = query.getById(gameId)
        if (latest == null || latest.currentFen != requestFen || latest.currentPly != expectedCurrentPly) {
            markFailed(gameId, targetPly, requestFen, "Position changed during AI request")
            return Outcome.Stale(StaleReason.POSITION_CHANGED)
        }

        val result = playMove.commit(
            gameId = gameId,
            move = decision.move,
            // 落库时打上机器可读前缀：UI 据此提示"这手是本地兜底，不是引擎给的"。
            // 远程引擎不可用时兜底是静默的，用户只会觉得"AI 变笨了"，必须让它可见。
            aiReason = decision.storedReason(),
            aiRawResponse = decision.rawResponse,
        )

        if (result !is PlayMoveUseCase.Result.Success || result.detail.currentPly != targetPly) {
            markFailed(gameId, targetPly, requestFen, "Move rejected")
            return Outcome.Stale(StaleReason.MOVE_REJECTED)
        }

        markFinished(gameId, targetPly, decision.move.notationUcci, decision.rawResponse)
        return Outcome.Committed(result.detail)
    }

    private suspend fun markFailed(gameId: String, targetPly: Int, requestJson: String, error: String) {
        aiTaskStore.upsert(
            AiTaskEntity(
                id = "$gameId:$targetPly",
                gameId = gameId,
                targetPly = targetPly,
                requestJson = requestJson,
                status = AiTaskStatus.FAILED,
                errorMessage = error,
            ),
        )
    }

    private suspend fun markFinished(gameId: String, targetPly: Int, moveUcci: String, responseJson: String) {
        aiTaskStore.upsert(
            AiTaskEntity(
                id = "$gameId:$targetPly",
                gameId = gameId,
                targetPly = targetPly,
                requestJson = "",
                status = AiTaskStatus.DONE,
                validatedMove = moveUcci,
                responseJson = responseJson,
            ),
        )
    }
}
