package com.wanbaohe.chess.application

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.dto.GameDetail
import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.dto.GameSummary
import com.wanbaohe.chess.application.dto.PlyRecord
import com.wanbaohe.chess.application.dto.engineSlotFor
import com.wanbaohe.chess.application.dto.prepareFrom
import com.wanbaohe.chess.application.dto.prepareRestart
import com.wanbaohe.chess.application.dto.prepareStandardOpening
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.EngineSlot
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.BoardSetupDraft
import com.wanbaohe.chess.domain.model.BoardPoint
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class GamePreparationTest {
    @Test
    fun firstEntryIsHumanWhiteAgainstAiBlackWithNoSourceRecord() {
        val draft = GamePreparation()
        assertEquals(GameMode.HUMAN_VS_LLM, draft.setup.mode)
        assertEquals(PlayerType.HUMAN, draft.setup.playerTypeFor(Side.WHITE))
        assertEquals(PlayerType.LLM, draft.setup.playerTypeFor(Side.BLACK))
        assertEquals(FenCodec.INITIAL_FEN, draft.initialFen)
        assertEquals(ChessAiSource.RemoteEngine("stockfish"), ChessAiSource.default)
        assertNull(draft.origin)
    }

    @Test
    fun aiDuelSlotsFollowChessWhiteFirstRatherThanBlackFirst() {
        assertEquals(EngineSlot.DUEL_A, GameMode.LLM_VS_LLM.engineSlotFor(Side.WHITE))
        assertEquals(EngineSlot.DUEL_B, GameMode.LLM_VS_LLM.engineSlotFor(Side.BLACK))
    }

    @Test
    fun practiceAtPlyZeroUsesTheActualCustomOpeningAndNormalizesOnlyPracticeClocks() {
        val source = sourceGame()
        val draft = source.prepareFrom(FenCodec.parse(source.initialFen), 0)
        assertEquals(source.initialFen, draft.origin?.fen)
        assertEquals(0, draft.origin?.ply)
        val practice = FenCodec.parse(draft.initialFen)
        val original = FenCodec.parse(source.initialFen)
        assertEquals(original.board, practice.board)
        assertEquals(original.castlingRights, practice.castlingRights)
        assertEquals(0, practice.halfMoveClock)
        assertEquals(1, practice.fullMoveNumber)
    }

    @Test
    fun sourceSnapshotDoesNotChangeWhenSourceIsUndoneOrRenamed() {
        val source = sourceGame()
        val draft = source.prepareFrom(FenCodec.parse(source.currentFen))
        val changed = source.copy(title = "Renamed", currentFen = source.initialFen, currentPly = 0, plies = emptyList())
        assertEquals(source.currentFen, draft.origin?.fen)
        assertEquals(source.title, draft.origin?.title)
        assertEquals(1, draft.origin?.ply)
        assertEquals(source.blackAiConfig, draft.blackAiConfig)
        assertEquals(0, changed.currentPly)
        assertEquals(1, source.plies.size)
    }

    @Test
    fun editingAPlayedPositionPreparesANewOpeningWithoutChangingSourceHistory() {
        val source = sourceGame()
        val before = FenCodec.parse(source.currentFen)
        val edited = BoardSetupDraft(before).move(BoardPoint(4, 1), BoardPoint(4, 3)).startPosition()
        val practice = source.prepareFrom(edited)
        assertEquals(source.currentFen, practice.origin?.fen)
        assertEquals(FenCodec.encode(edited), practice.initialFen)
        assertEquals(1, source.currentPly)
        assertEquals(1, source.plies.size)
        assertEquals("KQkq", FenCodec.parse(source.initialFen).castlingRights)
    }

    @Test
    fun restartingACustomOpeningAndChoosingAStandardOpeningAreDifferentPreparations() {
        val source = sourceGame().copy(origin = GameOrigin("older-source", 0, FenCodec.INITIAL_FEN, "Original"))
        val restart = source.prepareRestart()
        assertEquals(FenCodec.parse(source.initialFen).board, FenCodec.parse(restart.initialFen).board)
        assertEquals(Side.BLACK, FenCodec.parse(restart.initialFen).sideToMove)
        assertEquals(source.origin, restart.origin)
        val standard = source.prepareStandardOpening()
        assertEquals(FenCodec.INITIAL_FEN, standard.initialFen)
        assertNull(standard.origin)
        assertEquals(source.blackAiConfig, standard.blackAiConfig)
    }

    @Test
    fun opponentSnapshotContainsIdentityAndModelButNeverCredentials() {
        val engine = AiEngine(
            name = "test-provider", title = "Test provider",
            model = AiModel(name = "test-model", title = "Test model", updateTime = 1),
            authorizationCode = "not-a-real-credential-test-only",
        )
        val config = GameAiPlayerConfig.capture(ChessAiSource.WorkingModel, engine)
        val encoded = config.encode()
        assertFalse(encoded.contains("authorizationCode"))
        assertFalse(encoded.contains("not-a-real-credential-test-only"))
        assertFalse(encoded.contains("apiBaseUrl"))
        assertEquals(config, GameAiPlayerConfig.decode(encoded))
        assertEquals(engine.requestProtocol.name, config.engineProtocol)
    }

    @Test
    fun remoteSnapshotsAndSourceMetadataRoundTrip() {
        val remote = GameAiPlayerConfig(sourceKey = "stockfish")
        assertEquals(remote, GameAiPlayerConfig.decode(remote.encode()))
        assertNull(GameAiPlayerConfig.decode("{}"))
        assertNull(GameAiPlayerConfig.decode("not-json"))
        val origin = GameOrigin("source", 23, sourceGame().initialFen, "Game \"A\"\nPractice")
        assertEquals(origin, AppJson.decodeFromString<GameOrigin>(AppJson.encodeToString(origin)))
    }

    @Test
    fun onlyEmptyLegacyConfigsMayCaptureTheCurrentDefault() {
        assertTrue(GameAiPlayerConfig.isLegacyEmpty("{}"))
        assertTrue(GameAiPlayerConfig.isLegacyEmpty(" { } "))
        assertTrue(GameAiPlayerConfig.isLegacyEmpty(""))
        assertFalse(GameAiPlayerConfig.isLegacyEmpty("not-json"))
        assertFalse(GameAiPlayerConfig.isLegacyEmpty("""{"model":{}}"""))
        assertFalse(GameAiPlayerConfig.isLegacyEmpty(GameAiPlayerConfig(sourceKey = "stockfish").encode()))
    }

    @Test
    fun unsupportedSourcesAndIncompleteChatSnapshotsCannotDispatch() {
        assertTrue(GameAiPlayerConfig(sourceKey = "stockfish").isSupported)
        assertFalse(GameAiPlayerConfig().isSupported)
        assertFalse(GameAiPlayerConfig(sourceKey = "unknown-engine").isSupported)
        assertFalse(GameAiPlayerConfig(sourceKey = "working_model").isSupported)
        val config = GameAiPlayerConfig.capture(ChessAiSource.WorkingModel, AiEngine(
            name = "provider", model = AiModel(name = "model", title = "Model", updateTime = 1),
        ))
        assertTrue(config.isSupported)
        assertFalse(config.copy(engineProtocol = "unknown").isSupported)
        assertFalse(config.copy(engineProtocol = "LOCAL_ON_DEVICE").isSupported)
        assertFalse(config.copy(engineProtocol = "JEV").isSupported)
    }

    @Test
    fun malformedSourceSnapshotsAreNotSilentlyAccepted() {
        listOf(
            "{}", """{"gameId":"","ply":1,"fen":"${FenCodec.INITIAL_FEN}","title":"Source"}""",
            """{"gameId":"source","ply":-1,"fen":"${FenCodec.INITIAL_FEN}","title":"Source"}""",
        ).forEach { value ->
            assertFailsWith<IllegalArgumentException>(value) { AppJson.decodeFromString<GameOrigin>(value) }
        }
    }

    @Test
    fun restoredGameUsesActualPlayTimeNotRenameOrReviewTime() {
        val renamed = summary("renamed", GameStatus.PAUSED, 5).copy(updatedAt = 100)
        val played = summary("played", GameStatus.CHECK, 10)
        assertEquals(played, GameQueryUseCase.mostRecentUnfinishedHumanAiGame(listOf(renamed, played)))
    }

    @Test
    fun finishedLocalOnlineAndNeverPlayedRecordsCannotReplacePreparation() {
        val ai = summary("ai", GameStatus.PAUSED, 5)
        val ignored = listOf(
            summary("finished", GameStatus.WHITE_WINS, 20),
            summary("not-started", GameStatus.NOT_STARTED, 21),
            summary("never-played", GameStatus.PAUSED, 0),
            summary("local", GameStatus.PLAYING, 22).copy(mode = GameMode.LOCAL_PVP),
            summary("online", GameStatus.PLAYING, 23).copy(mode = GameMode.ONLINE_PVP),
        )
        assertEquals(ai, GameQueryUseCase.mostRecentUnfinishedHumanAiGame(ignored + ai))
        assertNull(GameQueryUseCase.mostRecentUnfinishedHumanAiGame(ignored))
        assertNull(GameQueryUseCase.mostRecentUnfinishedHumanAiGame(emptyList()))
    }

    private fun summary(id: String, status: GameStatus, playedAt: Long) = GameSummary(
        id = id, title = id, mode = GameMode.HUMAN_VS_LLM,
        blackPlayerType = PlayerType.LLM, whitePlayerType = PlayerType.HUMAN,
        status = status, resultText = "", updatedAt = playedAt, plyCount = 0, lastPlayedAt = playedAt,
    )

    private fun sourceGame(): GameDetail {
        val initial = FenCodec.parse("r3k2r/8/8/8/8/8/4P3/R3K2R b KQkq - 7 28")
        val move = GameArbiter.legalMoves(initial).first()
        val initialFen = FenCodec.encode(initial)
        val after = FenCodec.encode(initial.withPieceMoved(move))
        return GameDetail(
            id = "source", title = "Source", mode = GameMode.HUMAN_VS_LLM,
            blackPlayerType = PlayerType.LLM, whitePlayerType = PlayerType.HUMAN,
            initialFen = initialFen, currentFen = after, currentPly = 1,
            status = GameStatus.PAUSED, resultText = "", winnerSide = "", startedAt = 1, lastMoveAt = 2,
            blackAiConfig = GameAiPlayerConfig(sourceKey = "stockfish"),
            plies = listOf(PlyRecord(1, move.notationUcci, move.notationCn, Side.BLACK, initialFen, after, "", "", 10)),
        )
    }
}
