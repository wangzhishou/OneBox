package com.wanbaohe.gomoku.application

import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.dto.prepareFrom
import com.wanbaohe.gomoku.application.port.outbound.AiOpponentUnavailableException
import com.wanbaohe.gomoku.application.port.outbound.AiTaskEntity
import com.wanbaohe.gomoku.application.port.outbound.AiTaskStatus
import com.wanbaohe.gomoku.application.port.outbound.AiTaskStore
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.MoveChooser
import com.wanbaohe.gomoku.application.port.outbound.MoveDecision
import com.wanbaohe.gomoku.application.usecase.AiOrchestrationUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.PlayMoveUseCase
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.BoardState
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.GomokuMove
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
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
    private val games = MemoryGames(testGame(fen = FenCodec.INITIAL_FEN.replace(" b ", " w "), status = GameStatus.PLAYING))
    private val moves = MemoryMoves()
    private var duringRequest: suspend () -> Unit = {}
    private var receivedConfig: GameAiPlayerConfig? = null
    private var receivedHistory: List<String> = emptyList()
    private var chooserCalls = 0
    private val chooser = object : MoveChooser {
        override suspend fun choose(
            boardState: BoardState, fen: String, history: List<String>, legalMoves: List<GomokuMove>, slot: EngineSlot,
        ): MoveDecision? = error("A saved per-game opponent is required")

        override suspend fun chooseForGame(
            boardState: BoardState, fen: String, history: List<String>, legalMoves: List<GomokuMove>, slot: EngineSlot,
            playerConfig: GameAiPlayerConfig?,
        ): MoveDecision {
            chooserCalls++
            receivedConfig = playerConfig
            receivedHistory = history
            duringRequest()
            return MoveDecision(legalMoves.first(), "", "", false)
        }
    }
    private var lastTask: AiTaskEntity? = null
    private val tasks = object : AiTaskStore {
        override suspend fun getLatestByGame(gameId: String): AiTaskEntity? = lastTask
        override suspend fun upsert(entity: AiTaskEntity) { lastTask = entity }
        override suspend fun deleteByGame(gameId: String) { lastTask = null }
    }
    private val query = GameQueryUseCase(games, moves)
    private val useCase = AiOrchestrationUseCase(chooser, PlayMoveUseCase(games, moves, query), query, tasks)

    @Test
    fun requestUsesWhiteConfigFromTheLegacyRedColumn() = runBlocking {
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(GameAiPlayerConfig(sourceKey = "rapfi"), receivedConfig)
        assertEquals(Side.WHITE, moves.plies.single().moverSide)
        assertEquals(AiTaskStatus.DONE, lastTask?.status)
    }

    @Test
    fun anAiBlackGameUsesTheBlackColumnNotTheWhiteOpponent() = runBlocking {
        val black = GameAiPlayerConfig(sourceKey = "rapfi", engineTitle = "Black snapshot")
        games.game = games.game.copy(
            currentFen = FenCodec.INITIAL_FEN, initialFen = FenCodec.INITIAL_FEN,
            blackPlayerType = PlayerType.LLM, whitePlayerType = PlayerType.HUMAN,
            blackPlayerConfigJson = black.encode(), redPlayerConfigJson = "{}",
        )
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(black, receivedConfig)
        assertEquals(Side.BLACK, moves.plies.single().moverSide)
    }

    @Test
    fun anEmptyOrMalformedSavedOpponentNeverRequestsAGlobalDefault() = runBlocking {
        for (config in listOf("{}", "not json")) {
            games.game = games.game.copy(redPlayerConfigJson = config)
            val result = assertIs<AiOrchestrationUseCase.Outcome.Failed>(useCase.requestMove("game", EngineSlot.FAST))
            assertEquals("AI_OPPONENT_UNAVAILABLE", result.reason)
        }
        assertEquals(0, chooserCalls)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun corruptedSavedOpponentsRemainUnavailableWhenPreparingPractice(): Unit = runBlocking {
        for (json in listOf("not-json", """{"model":{}}""", GameAiPlayerConfig(sourceKey = "unknown-engine").encode())) {
            games.game = games.game.copy(redPlayerConfigJson = json)
            val detail = assertNotNull(query.getById("game"))
            val config = assertNotNull(detail.whiteAiConfig)
            assertFalse(config.isSupported)
            assertEquals(config, detail.prepareFrom(FenCodec.parse(detail.currentFen)).whiteAiConfig)
        }
        games.game = games.game.copy(redPlayerConfigJson = "{}")
        assertNull(query.getById("game")?.whiteAiConfig)
    }

    @Test
    fun pausedAndHumanTurnsNeverRequestAi() = runBlocking {
        games.game = games.game.copy(status = GameStatus.PAUSED)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        games.game = games.game.copy(status = GameStatus.PLAYING, currentFen = FenCodec.INITIAL_FEN)
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(0, chooserCalls)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun theAiOnlySeesActiveHistoryNotUndoneFutureMoves() = runBlocking {
        val first = moves.addMove("game", 1, FenCodec.INITIAL_FEN, BoardPoint(7, 7))
        moves.addMove("game", 2, first, BoardPoint(7, 8))
        games.game = games.game.copy(initialFen = FenCodec.INITIAL_FEN, currentFen = first, currentPly = 1)
        assertIs<AiOrchestrationUseCase.Outcome.Committed>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals(listOf("H8"), receivedHistory)
        assertEquals(2, moves.plies.size)
    }

    @Test
    fun pausingDuringARequestPreventsItsResponseFromCommitting() = runBlocking {
        duringRequest = { games.game = games.game.copy(status = GameStatus.PAUSED) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun changedOpponentConfigurationMakesTheResponseStale() = runBlocking {
        duringRequest = { games.game = games.game.copy(redPlayerConfigJson = GameAiPlayerConfig(sourceKey = "another-engine").encode()) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun bothPlayerConfigurationsAreCheckedEvenWhenTheOtherPlayerIsHuman() = runBlocking {
        duringRequest = { games.game = games.game.copy(blackPlayerConfigJson = """{"name":"changed"}""") }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun aChangedPositionOrPlyCannotAcceptTheOldResponse() = runBlocking {
        duringRequest = { games.game = games.game.copy(currentPly = 1) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
        games.game = games.game.copy(currentPly = 0)
        duringRequest = { games.game = games.game.copy(currentFen = FenCodec.INITIAL_FEN) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun changedSeatsCannotAcceptAnAiMove() = runBlocking {
        duringRequest = { games.game = games.game.copy(whitePlayerType = PlayerType.HUMAN) }
        assertIs<AiOrchestrationUseCase.Outcome.Stale>(useCase.requestMove("game", EngineSlot.FAST))
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun unavailableSavedOpponentIsAnErrorNotASilentDefaultSwitch() = runBlocking {
        duringRequest = { throw AiOpponentUnavailableException() }
        val result = assertIs<AiOrchestrationUseCase.Outcome.Failed>(useCase.requestMove("game", EngineSlot.FAST))
        assertEquals("AI_OPPONENT_UNAVAILABLE", result.reason)
        assertEquals(1, chooserCalls)
        assertEquals(AiTaskStatus.FAILED, lastTask?.status)
        assertTrue(moves.plies.isEmpty())
    }

    @Test
    fun cancellationEvenWithoutACooperativeChooserCannotCommitALateResponse() {
        duringRequest = { currentCoroutineContext().cancel() }
        assertFailsWith<CancellationException> { runBlocking { useCase.requestMove("game", EngineSlot.FAST) } }
        assertTrue(moves.plies.isEmpty())
    }
}
