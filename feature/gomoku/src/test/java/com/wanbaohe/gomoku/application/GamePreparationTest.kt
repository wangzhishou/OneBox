package com.wanbaohe.gomoku.application

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.dto.GamePreparation
import com.wanbaohe.gomoku.application.dto.GameSummary
import com.wanbaohe.gomoku.application.dto.engineSlotFor
import com.wanbaohe.gomoku.application.dto.prepareFrom
import com.wanbaohe.gomoku.application.port.outbound.EngineSlot
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GamePreparationTest {
    @Test
    fun firstEntryIsHumanBlackFirstAgainstAiWhiteWithoutAGame() {
        val draft = GamePreparation()
        assertEquals(GameMode.HUMAN_VS_LLM, draft.setup.mode)
        assertEquals(PlayerType.HUMAN, draft.setup.playerTypeFor(Side.BLACK))
        assertEquals(PlayerType.LLM, draft.setup.playerTypeFor(Side.WHITE))
        assertEquals(FenCodec.INITIAL_FEN, draft.initialFen)
        assertNull(draft.origin)
        assertEquals(GomokuAiSource.RemoteEngine("rapfi"), GomokuAiSource.default)
    }

    @Test
    fun blackIsDuelAAndWhiteIsDuelBNeverTheLegacyRedColumn() {
        assertEquals(EngineSlot.DUEL_A, GameMode.LLM_VS_LLM.engineSlotFor(Side.BLACK))
        assertEquals(EngineSlot.DUEL_B, GameMode.LLM_VS_LLM.engineSlotFor(Side.WHITE))
        assertEquals(EngineSlot.FAST, GameMode.HUMAN_VS_LLM.engineSlotFor(Side.WHITE))
    }

    @Test
    fun opponentSnapshotContainsIdentityProtocolAndModelButNoConnectionCredentials() {
        val engine = AiEngine(
            name = "provider", title = "Provider",
            requestProtocol = AiRequestProtocol.RESPONSES_COMPATIBLE,
            model = AiModel(name = "model", title = "Model", updateTime = 1),
            authorizationCode = "unit-test-value-not-for-storage",
            requestUrl = "https://unit.invalid", requestPath = "/api", proxyUrl = "https://proxy.invalid",
        )
        val config = GameAiPlayerConfig.capture(GomokuAiSource.WorkingModel, engine)
        val json = config.encode()
        assertEquals("provider", config.engineName)
        assertEquals("RESPONSES_COMPATIBLE", config.engineProtocol)
        assertEquals("model", config.model?.name)
        assertFalse(json.contains("authorizationCode"))
        assertFalse(json.contains("unit-test-value-not-for-storage"))
        assertFalse(json.contains("unit.invalid"))
        assertFalse(json.contains("proxy.invalid"))
        assertEquals(config, GameAiPlayerConfig.decode(json))
        assertTrue(config.isSupported)
    }

    @Test
    fun dedicatedSourceAndEmptyLegacyConfigRoundTrip() {
        assertNull(GameAiPlayerConfig.decode("{}"))
        assertNull(GameAiPlayerConfig.decode("not json"))
        val config = GameAiPlayerConfig(sourceKey = "rapfi")
        assertEquals(config, GameAiPlayerConfig.decode(config.encode()))
        assertTrue(config.isSupported)
        assertFalse(GameAiPlayerConfig(sourceKey = "working_model").isSupported)
        assertFalse(GameAiPlayerConfig(sourceKey = "unknown-engine").isSupported)
        assertTrue(GameAiPlayerConfig.isLegacyEmpty(" \n"))
        assertTrue(GameAiPlayerConfig.isLegacyEmpty("{ }"))
        assertFalse(GameAiPlayerConfig.isLegacyEmpty("not json"))
        assertFalse(GameAiPlayerConfig.isLegacyEmpty(config.encode()))
        val chat = GameAiPlayerConfig(
            sourceKey = "working_model", engineName = "provider", engineProtocol = "unknown-protocol",
            model = AiModel(name = "model", title = "Model", updateTime = 1),
        )
        assertFalse(chat.isSupported)
        assertFalse(chat.copy(engineProtocol = AiRequestProtocol.LOCAL_ON_DEVICE.name).isSupported)
    }

    @Test
    fun practiceSnapshotsTheExactReplayPlyAndNormalizesOnlyTheNewPosition() = runBlocking {
        val games = MemoryGames(testGame(fen = FenCodec.INITIAL_FEN.replace(" b 1", " w 12")))
        val moves = MemoryMoves()
        val afterFirst = moves.addMove("game", 1, games.game.initialFen, BoardPoint(7, 7))
        val afterSecond = moves.addMove("game", 2, afterFirst, BoardPoint(7, 8))
        games.game = games.game.copy(currentFen = afterSecond, currentPly = 2)
        val detail = requireNotNull(GameQueryUseCase(games, moves).getById("game"))
        val preparation = detail.prepareFrom(FenCodec.parse(afterFirst), 1)
        assertEquals(GameOrigin("game", 1, afterFirst, "Source"), preparation.origin)
        assertEquals(detail.whiteAiConfig, preparation.whiteAiConfig)
        assertEquals(1, FenCodec.parse(preparation.initialFen).moveNumber)
        assertEquals(Side.BLACK, FenCodec.parse(preparation.initialFen).sideToMove)
        assertNotEquals(afterFirst, preparation.initialFen)
        assertEquals(2, moves.plies.size)
        games.game = games.game.copy(title = "Renamed", currentPly = 0, currentFen = games.game.initialFen)
        games.archive("game")
        assertEquals(GameOrigin("game", 1, afterFirst, "Source"), GameOrigin.decode(requireNotNull(preparation.origin).encode()))
    }

    @Test
    fun emptyHistoryRetainsItsCustomInitialFenInPractice() = runBlocking {
        val custom = FenCodec.INITIAL_FEN.replace(" b 1", " w 42")
        val detail = requireNotNull(GameQueryUseCase(MemoryGames(testGame(fen = custom)), MemoryMoves()).getById("game"))
        val preparation = detail.prepareFrom(FenCodec.parse(custom), 0)
        assertEquals(custom, preparation.origin?.fen)
        assertEquals(0, preparation.origin?.ply)
        assertEquals(Side.WHITE, FenCodec.parse(preparation.initialFen).sideToMove)
        assertEquals(1, FenCodec.parse(preparation.initialFen).moveNumber)
    }

    @Test
    fun onlyActuallyPlayedUnfinishedHumanAiGamesCanRestore() {
        assertNull(GameQueryUseCase.mostRecentUnfinishedHumanAiGame(emptyList()))
        val candidates = listOf(
            summary("finished", GameStatus.BLACK_WINS, 100),
            summary("draft", GameStatus.NOT_STARTED, 110),
            summary("never-played", GameStatus.PAUSED, 0),
            summary("local", GameStatus.PLAYING, 120).copy(mode = GameMode.LOCAL_PVP),
            summary("online", GameStatus.PLAYING, 130).copy(mode = GameMode.ONLINE_PVP),
            summary("duel", GameStatus.PLAYING, 140).copy(mode = GameMode.LLM_VS_LLM),
        )
        assertNull(GameQueryUseCase.mostRecentUnfinishedHumanAiGame(candidates))
        val renamed = summary("renamed", GameStatus.PAUSED, 5).copy(updatedAt = 1000)
        val recent = summary("recent", GameStatus.PLAYING, 10)
        assertEquals(recent, GameQueryUseCase.mostRecentUnfinishedHumanAiGame(candidates + renamed + recent))
    }

    private fun summary(id: String, status: GameStatus, playedAt: Long) = GameSummary(
        id, id, GameMode.HUMAN_VS_LLM, PlayerType.HUMAN, PlayerType.LLM, status, "", 1, 0, playedAt,
    )
}
