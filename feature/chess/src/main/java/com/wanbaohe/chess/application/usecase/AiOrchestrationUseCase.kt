package com.wanbaohe.chess.application.usecase

import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.dto.aiConfigFor
import com.wanbaohe.chess.application.port.outbound.AiOpponentUnavailableException
import com.wanbaohe.chess.application.port.outbound.AiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskStatus
import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.MoveChooser
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import com.shifenmiao.interfaces.singleton.AppContext
import com.wanbaohe.chess.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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

    companion object {
        fun isConsistent(expected: GameDetail, latest: GameDetail?): Boolean =
            latest != null && latest.currentFen == expected.currentFen &&
                latest.currentPly == expected.currentPly && latest.status == expected.status &&
                latest.status in setOf(GameStatus.PLAYING, GameStatus.CHECK) &&
                latest.mode == expected.mode && latest.whitePlayerType == expected.whitePlayerType &&
                latest.blackPlayerType == expected.blackPlayerType &&
                latest.whiteAiConfig == expected.whiteAiConfig && latest.blackAiConfig == expected.blackAiConfig &&
                latest.whitePlayerConfigJson == expected.whitePlayerConfigJson &&
                latest.blackPlayerConfigJson == expected.blackPlayerConfigJson

        fun activeHistory(detail: GameDetail): List<String> =
            detail.plies.filter { it.ply in 1..detail.currentPly }.sortedBy { it.ply }.takeLast(6)
                .map { it.moveUcci.ifBlank { it.moveCn } }
    }

    suspend fun requestMove(gameId: String, slot: EngineSlot): Outcome {
        val detail = query.getById(gameId) ?: return Outcome.Failed(AppContext.getString(R.string.chess_game_not_found))
        if (detail.status != GameStatus.PLAYING && detail.status != GameStatus.CHECK) {
            return Outcome.Stale(StaleReason.POSITION_CHANGED)
        }
        val boardState = FenCodec.parse(detail.currentFen)
        val playerType = if (boardState.sideToMove == Side.WHITE) detail.whitePlayerType else detail.blackPlayerType
        if (playerType != PlayerType.LLM) return Outcome.Stale(StaleReason.POSITION_CHANGED)
        if (detail.aiConfigFor(boardState.sideToMove)?.isSupported != true) {
            return Outcome.Failed(AppContext.getString(R.string.chess_ai_opponent_unavailable))
        }
        val requestFen = detail.currentFen
        val targetPly = detail.currentPly + 1

        saveTaskRunning(gameId, targetPly, requestFen)

        val decision = try {
            callMoveChooser(boardState, requestFen, detail, slot)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: AiOpponentUnavailableException) {
            val reason = AppContext.getString(R.string.chess_ai_opponent_unavailable)
            markFailed(gameId, targetPly, requestFen, reason)
            return Outcome.Failed(reason)
        } catch (_: Exception) {
            val reason = AppContext.getString(R.string.chess_ai_error)
            markFailed(gameId, targetPly, requestFen, reason)
            return Outcome.Failed(reason)
        } ?: run {
            val reason = AppContext.getString(R.string.chess_ai_error)
            markFailed(gameId, targetPly, requestFen, reason)
            return Outcome.Failed(reason)
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
        boardState: com.wanbaohe.chess.domain.model.BoardState,
        requestFen: String,
        detail: GameDetail,
        slot: EngineSlot,
    ): MoveDecision? = moveChooser.chooseForGame(
        boardState = boardState,
        fen = requestFen,
        history = activeHistory(detail),
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
        if (!isConsistent(expected, latest)) {
            markFailed(gameId, targetPly, requestFen, "Position changed during AI request")
            return Outcome.Stale(StaleReason.POSITION_CHANGED)
        }

        currentCoroutineContext().ensureActive()
        val result = playMove.commit(
            gameId = gameId,
            move = decision.move,
            // 落库时打上机器可读前缀：UI 据此提示"这手是本地兜底，不是引擎给的"。
            // 远程引擎不可用时兜底是静默的，用户只会觉得"AI 变笨了"，必须让它可见。
            aiReason = decision.storedReason(),
            aiRawResponse = decision.rawResponse,
            expected = expected,
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
