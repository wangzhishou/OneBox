package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.prepareFrom
import com.wanbaohe.chess.application.dto.prepareRestart
import com.wanbaohe.chess.application.port.outbound.AiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.MoveChooser
import com.wanbaohe.chess.application.port.outbound.MoveDecision
import com.wanbaohe.chess.application.port.outbound.PlyEntity
import com.wanbaohe.chess.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.GameMutationLock
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.BoardState
import com.wanbaohe.chess.domain.model.ChessMove
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AiOrchestrationUseCaseTest {
    private val moves = MemoryMoves()
    private val games = MemoryGames(
        GameEntity(
            id = "game", title = "Game", mode = GameMode.HUMAN_VS_LLM,
            whitePlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.LLM,
            redPlayerConfigJson = "{}", blackPlayerConfigJson = GameAiPlayerConfig(sourceKey = "stockfish").encode(),
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
            legalMoves: List<ChessMove>, slot: EngineSlot,
        ): MoveDecision? = error("The saved game opponent must be passed explicitly")

        override suspend fun chooseForGame(
            boardState: BoardState, fen: String, history: List<String>,
            legalMoves: List<ChessMove>, slot: EngineSlot, playerConfig: GameAiPlayerConfig?,
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
    private val useCase = AiOrchestrationUseCase(
        chooser, PlayMoveUseCase(games, moves, query, GameMutationLock()), query, tasks,
    )

    @Test
    fun requestUsesSavedOpponentAndCanStartFromBlackToMove() = runBlocking {
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(GameAiPlayerConfig(sourceKey = "stockfish"), receivedConfig)
        assertEquals(Side.BLACK, moves.plies.single().moverSide)
    }

    @Test
    fun pausedAndHumanTurnsNeverStartAnAiRequest() = runBlocking {
        games.game = games.game.copy(status = GameStatus.PAUSED)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        games.game = games.game.copy(status = GameStatus.PLAYING, currentFen = FenCodec.INITIAL_FEN)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(0, chooserCalls)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun aiHistoryExcludesUndoneFutureMoves() = runBlocking {
        val before = FenCodec.parse(games.game.currentFen)
        val move = GameArbiter.legalMoves(before).first()
        moves.plies += PlyEntity(
            "future", "game", 1, move.notationUcci, move.notationCn, Side.BLACK,
            games.game.currentFen, FenCodec.encode(before.withPieceMoved(move)),
            false, false, false, "", "", 0,
        )
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(receivedHistory.isEmpty())
        assertEquals(1, moves.plies.size)
    }

    @Test
    fun pausingDuringRequestRejectsTheResponse() = runBlocking {
        duringRequest = { games.game = games.game.copy(status = GameStatus.PAUSED) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun changingEitherOpponentRejectsTheOldResponse() = runBlocking {
        duringRequest = {
            games.game = games.game.copy(redPlayerConfigJson = GameAiPlayerConfig(sourceKey = "working_model").encode())
        }
        // Even a non-active AI seat matters. Exercise this with an AI duel.
        games.game = games.game.copy(mode = GameMode.LLM_VS_LLM, whitePlayerType = PlayerType.LLM,
            redPlayerConfigJson = GameAiPlayerConfig(sourceKey = "stockfish").encode())
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.DUEL_B))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun changingRawPlayerConfigurationCannotAcceptAnOldResponse() = runBlocking {
        duringRequest = { games.game = games.game.copy(redPlayerConfigJson = """{"changed":true}""") }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun corruptedSavedOpponentsStayUnavailableInPracticeAndRestart(): Unit = runBlocking {
        for (json in listOf("not-json", """{"model":{}}""", GameAiPlayerConfig(sourceKey = "unknown-engine").encode())) {
            games.game = games.game.copy(blackPlayerConfigJson = json)
            val detail = assertNotNull(query.getById("game"))
            val config = assertNotNull(detail.blackAiConfig)
            assertFalse(config.isSupported)
            assertEquals(config, detail.prepareFrom(FenCodec.parse(detail.currentFen)).blackAiConfig)
            assertEquals(config, detail.prepareRestart().blackAiConfig)
        }
        games.game = games.game.copy(blackPlayerConfigJson = "{}")
        assertNull(query.getById("game")?.blackAiConfig)
    }

    @Test
    fun changingFenOrPlyRejectsTheOldResponse() = runBlocking {
        duringRequest = { games.game = games.game.copy(currentPly = 2) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun changingOnlyFenMetadataStillRejectsTheOldResponse() = runBlocking {
        duringRequest = { games.game = games.game.copy(currentFen = games.game.currentFen.replace(" 0 1", " 0 2")) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun finishedRecordsNeverRequestAnAiResponse() = runBlocking {
        games.game = games.game.copy(status = GameStatus.DRAW)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(0, chooserCalls)
    }

    @Test
    fun activeHistoryIsChronologicalAndBoundedToTheLastSixActivePlies() = runBlocking {
        val detail = requireNotNull(query.getById("game"))
        val record = com.wanbaohe.chess.application.dto.PlyRecord(
            1, "move-1", "", Side.BLACK, detail.initialFen, detail.currentFen, "", "", 0,
        )
        val plies = (1..10).map { record.copy(ply = it, moveUcci = "move-$it") }.reversed()
        assertEquals((3..8).map { "move-$it" }, AiOrchestrationUseCase.activeHistory(detail.copy(currentPly = 8, plies = plies)))
    }

    @Test
    fun cancellationStopsALateChooserResponseBeforeCommit() {
        duringRequest = { currentCoroutineContext().cancel() }
        assertFailsWith<CancellationException> { runBlocking { useCase.requestMove("game", EngineSlot.FAST) } }
        assertTrue(moves.plies.isEmpty())
    }
}
