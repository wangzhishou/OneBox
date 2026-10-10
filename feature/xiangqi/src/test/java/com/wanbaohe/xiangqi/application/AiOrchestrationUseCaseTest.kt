package com.wanbaohe.xiangqi.application

import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskEntity
import com.wanbaohe.xiangqi.application.port.outbound.AiTaskStore
import com.wanbaohe.xiangqi.application.port.outbound.EngineSlot
import com.wanbaohe.xiangqi.application.port.outbound.GameEntity
import com.wanbaohe.xiangqi.application.port.outbound.GameStore
import com.wanbaohe.xiangqi.application.port.outbound.GameSummaryEntity
import com.wanbaohe.xiangqi.application.port.outbound.MoveChooser
import com.wanbaohe.xiangqi.application.port.outbound.MoveDecision
import com.wanbaohe.xiangqi.application.port.outbound.MoveStore
import com.wanbaohe.xiangqi.application.port.outbound.PlyEntity
import com.wanbaohe.xiangqi.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.application.usecase.PlayMoveUseCase
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.model.BoardState
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import com.wanbaohe.xiangqi.domain.model.XiangqiMove
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiOrchestrationUseCaseTest {
    private val moves = MemoryMoves()
    private val games = MemoryGame(
        GameEntity(
            id = "game", title = "Game", mode = GameMode.HUMAN_VS_LLM,
            redPlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.LLM,
            redPlayerConfigJson = "{}", blackPlayerConfigJson = GameAiPlayerConfig(sourceKey = "local_engine").encode(),
            initialFen = FenCodec.INITIAL_FEN.replace(" w ", " b "),
            currentFen = FenCodec.INITIAL_FEN.replace(" w ", " b "),
            currentPly = 0, status = GameStatus.PLAYING, resultText = "", winnerSide = "",
            startedAt = 1, lastMoveAt = 1, lastPlayedAt = 1, updatedAt = 1,
        ),
    )
    private var duringRequest: suspend () -> Unit = {}
    private var receivedConfig: GameAiPlayerConfig? = null
    private var receivedHistory: List<String> = emptyList()
    private var chooserCalls = 0
    private val chooser = object : MoveChooser {
        override suspend fun choose(
            boardState: BoardState, fen: String, history: List<String>,
            legalMoves: List<XiangqiMove>, slot: EngineSlot,
        ): MoveDecision? = error("The per-game configuration must be passed to the chooser")

        override suspend fun chooseForGame(
            boardState: BoardState, fen: String, history: List<String>,
            legalMoves: List<XiangqiMove>, slot: EngineSlot, playerConfig: GameAiPlayerConfig?,
        ): MoveDecision {
            chooserCalls++
            receivedConfig = playerConfig
            receivedHistory = history
            duringRequest()
            return MoveDecision(legalMoves.first(), "", "", false)
        }
    }
    private val tasks = object : AiTaskStore {
        private var task: AiTaskEntity? = null
        override suspend fun getLatestByGame(gameId: String): AiTaskEntity? = task
        override suspend fun upsert(entity: AiTaskEntity) { task = entity }
        override suspend fun deleteByGame(gameId: String) { task = null }
    }
    private val query = GameQueryUseCase(games, moves)
    private val useCase = AiOrchestrationUseCase(chooser, PlayMoveUseCase(games, moves, query), query, tasks)

    @Test
    fun requestUsesTheSavedOpponentAndRecordsABlackFirstMove() = runBlocking {
        val outcome = useCase.requestMove("game", EngineSlot.FAST)
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(outcome)
        assertEquals(GameAiPlayerConfig(sourceKey = "local_engine"), receivedConfig)
        assertEquals(1, moves.plies.size)
        assertEquals(Side.BLACK, moves.plies.single().moverSide)
    }

    @Test
    fun pausedGameNeverStartsAnAiRequest() = runBlocking {
        games.game = games.game.copy(status = GameStatus.PAUSED)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(0, chooserCalls)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun aHumanTurnNeverStartsAnAiRequest() = runBlocking {
        games.game = games.game.copy(currentFen = FenCodec.INITIAL_FEN)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(0, chooserCalls)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun theAiNeverSeesMovesThatWereUndone() = runBlocking {
        val board = FenCodec.parse(games.game.currentFen)
        val move = GameArbiter.legalMoves(board).first()
        moves.plies += PlyEntity(
            id = "future", gameId = "game", ply = 1,
            moveUcci = move.notationUcci, moveCn = move.notationCn, moverSide = Side.BLACK,
            beforeFen = games.game.currentFen, afterFen = FenCodec.encode(board.withPieceMoved(move)),
            isCapture = false, isCheck = false, isCheckmate = false,
            aiReason = "", aiRawResponse = "", thinkDurationMs = 0,
        )
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(receivedHistory.isEmpty())
        assertEquals(1, moves.plies.size)
    }

    @Test
    fun aResponseAfterPausingCannotAlterTheBoard() = runBlocking {
        duringRequest = { games.game = games.game.copy(status = GameStatus.PAUSED) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun aResponseFromThePreviousOpponentCannotCommitAfterSwitching() = runBlocking {
        duringRequest = {
            games.game = games.game.copy(blackPlayerConfigJson = GameAiPlayerConfig(sourceKey = "pikafish").encode())
        }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun aResponseForAPreviousPositionCannotCommit() = runBlocking {
        duringRequest = { games.game = games.game.copy(currentFen = FenCodec.INITIAL_FEN) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun cancellingAnAiRequestPreventsALateMove() {
        duringRequest = { currentCoroutineContext().cancel() }
        assertFailsWith<CancellationException> {
            runBlocking { useCase.requestMove("game", EngineSlot.FAST) }
        }
        assertTrue(moves.plies.isEmpty())
    }

    private class MemoryGame(var game: GameEntity) : GameStore {
        override fun observeAll(): Flow<List<GameSummaryEntity>> = flowOf(emptyList())
        override fun observeById(gameId: String): Flow<GameEntity?> = flowOf(game)
        override suspend fun getById(gameId: String): GameEntity? = game.takeIf { it.id == gameId }
        override suspend fun insert(entity: GameEntity): String { game = entity; return entity.id }
        override suspend fun update(entity: GameEntity) { game = entity }
        override suspend fun archive(gameId: String) = Unit
    }

    private class MemoryMoves : MoveStore {
        val plies = mutableListOf<PlyEntity>()
        override fun observeByGame(gameId: String): Flow<List<PlyEntity>> = flowOf(plies.toList())
        override suspend fun getByGame(gameId: String): List<PlyEntity> = plies.toList()
        override suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity? = plies.find { it.ply == plyNumber }
        override suspend fun insert(entity: PlyEntity) { plies += entity }
        override suspend fun deleteAfterPly(gameId: String, ply: Int) { plies.removeAll { it.ply > ply } }
        override suspend fun deleteByGame(gameId: String) { plies.clear() }
    }
}
