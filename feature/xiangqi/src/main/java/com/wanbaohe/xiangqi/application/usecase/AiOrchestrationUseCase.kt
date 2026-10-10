package com.wanbaohe.xiangqi.application.usecase

import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.dto.aiConfigFor
import com.wanbaohe.xiangqi.application.port.outbound.AiOpponentUnavailableException
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.xiangqi.R
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskEntity
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStatus
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStore
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

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
        if (detail.status != GameStatus.PLAYING && detail.status != GameStatus.CHECK) {
            return Outcome.Stale(StaleReason.POSITION_CHANGED)
        }
        val boardState = FenCodec.parse(detail.currentFen)
        val playerType = if (boardState.sideToMove == Side.RED) detail.redPlayerType else detail.blackPlayerType
        if (playerType != PlayerType.LLM) return Outcome.Stale(StaleReason.POSITION_CHANGED)
        val requestFen = detail.currentFen
        val targetPly = detail.currentPly + 1

        saveTaskRunning(gameId, targetPly, requestFen)

        val decision = try {
            callMoveChooser(boardState, requestFen, detail, slot)
        } catch (_: AiOpponentUnavailableException) {
            val message = AppContext.getString(R.string.xiangqi_ai_opponent_unavailable)
            markFailed(gameId, targetPly, requestFen, message)
            return Outcome.Failed(message)
        } ?: run {
            markFailed(gameId, targetPly, requestFen, "No legal move")
            return Outcome.Failed("No legal move")
        }

        currentCoroutineContext().ensureActive()
        return commitIfConsistent(gameId, requestFen, targetPly, detail, decision)
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
        boardState: com.wanbaohe.xiangqi.domain.model.BoardState,
        requestFen: String,
        detail: GameDetail,
        slot: EngineSlot,
    ): MoveDecision? = moveChooser.chooseForGame(
        boardState = boardState,
        fen = requestFen,
        // 历史走 UCCI: 与 legalMoves 的候选键同一种记法, Jev 的英文 prompt 也直接可读
        // (中文记谱在英文题型里是噪音, 且模型最终要回的就是 UCCI)。
        history = detail.plies.filter { it.ply <= detail.currentPly }
            .takeLast(6).map { it.moveUcci.ifBlank { it.moveCn } },
        legalMoves = GameArbiter.legalMoves(boardState),
        slot = slot,
        playerConfig = detail.aiConfigFor(boardState.sideToMove),
    )

    private suspend fun commitIfConsistent(
        gameId: String,
        requestFen: String,
        targetPly: Int,
        expected: GameDetail,
        decision: MoveDecision,
    ): Outcome {
        val latest = query.getById(gameId)
        if (latest == null || latest.currentFen != requestFen || latest.currentPly != expected.currentPly ||
            (latest.status != GameStatus.PLAYING && latest.status != GameStatus.CHECK) ||
            latest.redAiConfig != expected.redAiConfig || latest.blackAiConfig != expected.blackAiConfig
        ) {
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
