package com.wanbaohe.xiangqi.application

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.wanbaohe.xiangqi.application.dto.GameAiPlayerConfig
import com.wanbaohe.xiangqi.application.dto.GameDetail
import com.wanbaohe.xiangqi.application.dto.GamePreparation
import com.wanbaohe.xiangqi.application.dto.GameSummary
import com.wanbaohe.xiangqi.application.dto.PlyRecord
import com.wanbaohe.xiangqi.application.dto.prepareFrom
import com.wanbaohe.xiangqi.application.port.outbound.XiangqiAiSource
import com.wanbaohe.xiangqi.application.usecase.GameQueryUseCase
import com.wanbaohe.xiangqi.domain.FenCodec
import com.wanbaohe.xiangqi.domain.GameArbiter
import com.wanbaohe.xiangqi.domain.model.GameMode
import com.wanbaohe.xiangqi.domain.model.GameOrigin
import com.wanbaohe.xiangqi.domain.model.GameStatus
import com.wanbaohe.xiangqi.domain.model.PlayerType
import com.wanbaohe.xiangqi.domain.model.Side
import kotlinx.serialization.encodeToString
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class GamePreparationTest {
    @Test
    fun firstEntryDefaultsToHumanRedAndAiBlack() {
        val draft = GamePreparation()
        assertEquals(GameMode.HUMAN_VS_LLM, draft.setup.mode)
        assertEquals(PlayerType.HUMAN, draft.setup.playerTypeFor(Side.RED))
        assertEquals(PlayerType.LLM, draft.setup.playerTypeFor(Side.BLACK))
        assertEquals(FenCodec.INITIAL_FEN, draft.initialFen)
        assertNull(draft.origin)
    }

    @Test
    fun derivedGamePreservesItsSourceSnapshotAndAiWithoutCopyingMoves() {
        val detail = sourceGame()
        val edited = FenCodec.parse(detail.currentFen).copy(sideToMove = Side.RED)
        val draft = detail.prepareFrom(edited)
        assertEquals(detail.blackAiConfig, draft.blackAiConfig)
        assertEquals(detail.id, draft.origin?.gameId)
        assertEquals(detail.currentPly, draft.origin?.ply)
        assertEquals(detail.currentFen, draft.origin?.fen)
        assertEquals(detail.title, draft.origin?.title)
        assertEquals(0, FenCodec.parse(draft.initialFen).halfMoveClock)
        assertEquals(1, FenCodec.parse(draft.initialFen).fullMoveNumber)
        assertEquals(1, detail.plies.size)
    }

    @Test
    fun practiceAtTheInitialPositionUsesTheActualCustomInitialFen() {
        val detail = sourceGame()
        val draft = detail.prepareFrom(FenCodec.parse(detail.initialFen), sourcePly = 0)
        assertEquals(detail.initialFen, draft.origin?.fen)
        assertEquals(0, draft.origin?.ply)
    }

    @Test
    fun opponentSnapshotNeverContainsConnectionCredentials() {
        val engine = AiEngine(
            name = "test-provider",
            title = "Test provider",
            model = AiModel(name = "test-model", title = "Test model", updateTime = 1),
            authorizationCode = "unit-test-only-not-to-be-persisted",
        )
        val config = GameAiPlayerConfig.capture(XiangqiAiSource.WorkingModel, engine)
        val json = config.encode()
        assertFalse(json.contains("authorizationCode"))
        assertFalse(json.contains("unit-test-only-not-to-be-persisted"))
        assertEquals(config, GameAiPlayerConfig.decode(json))
        assertEquals("test-model", config.model?.name)
    }

    @Test
    fun legacyEmptyPlayerConfigIsRecognizedAndRemoteConfigRoundTrips() {
        assertNull(GameAiPlayerConfig.decode("{}"))
        val config = GameAiPlayerConfig(sourceKey = "pikafish")
        assertEquals(config, GameAiPlayerConfig.decode(config.encode()))
    }

    @Test
    fun sourceSnapshotSurvivesSerializationAndDoesNotDependOnALiveSourceGame() {
        val origin = GameOrigin("source", 15, FenCodec.INITIAL_FEN, "Game \"A\"\nPractice")
        assertEquals(origin, AppJson.decodeFromString<GameOrigin>(AppJson.encodeToString(origin)))
    }

    @Test
    fun noHistoryOrOnlyFinishedHistoryDoesNotReplaceTheDefaultPreparationBoard() {
        assertNull(GameQueryUseCase.mostRecentUnfinishedHumanAiGame(emptyList()))
        assertNull(GameQueryUseCase.mostRecentUnfinishedHumanAiGame(listOf(summary("finished", GameStatus.RED_WINS, 10))))
    }

    @Test
    fun reopeningUsesLastPlayedTimeInsteadOfARenameOrOtherMetadataUpdate() {
        val renamed = summary("renamed", GameStatus.PAUSED, 5).copy(updatedAt = 100)
        val played = summary("played", GameStatus.CHECK, 10)
        assertEquals(played, GameQueryUseCase.mostRecentUnfinishedHumanAiGame(listOf(renamed, played)))
    }

    @Test
    fun localAndOnlineGamesDoNotReplaceTheDefaultHumanAiEntry() {
        val ai = summary("ai", GameStatus.PAUSED, 5)
        val local = summary("local", GameStatus.PLAYING, 10).copy(mode = GameMode.LOCAL_PVP)
        val online = summary("online", GameStatus.PLAYING, 15).copy(mode = GameMode.ONLINE_PVP)
        assertEquals(ai, GameQueryUseCase.mostRecentUnfinishedHumanAiGame(listOf(local, online, ai)))
    }

    private fun summary(id: String, status: GameStatus, playedAt: Long) = GameSummary(
        id, id, GameMode.HUMAN_VS_LLM, PlayerType.HUMAN, PlayerType.LLM, status, "", playedAt, 0, playedAt,
    )

    private fun sourceGame(): GameDetail {
        val initial = FenCodec.parse(FenCodec.INITIAL_FEN).copy(sideToMove = Side.BLACK, fullMoveNumber = 12)
        val move = GameArbiter.legalMoves(initial).first()
        val initialFen = FenCodec.encode(initial)
        val currentFen = FenCodec.encode(initial.withPieceMoved(move))
        return GameDetail(
            id = "source",
            title = "Source",
            mode = GameMode.HUMAN_VS_LLM,
            redPlayerType = PlayerType.HUMAN,
            blackPlayerType = PlayerType.LLM,
            initialFen = initialFen,
            currentFen = currentFen,
            currentPly = 1,
            status = GameStatus.PAUSED,
            resultText = "",
            winnerSide = "",
            startedAt = 1,
            lastMoveAt = 2,
            blackAiConfig = GameAiPlayerConfig(sourceKey = "pikafish"),
            plies = listOf(PlyRecord(1, move.notationUcci, move.notationCn, Side.BLACK, initialFen, currentFen, "", "", 10)),
        )
    }
}
