package com.wanbaohe.gomoku.application

import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.AiTaskEntity
import com.wanbaohe.gomoku.application.port.outbound.AiTaskStore
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.usecase.DeleteGameUseCase
import com.wanbaohe.gomoku.application.usecase.ExportGameUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.application.usecase.ImportFailureCause
import com.wanbaohe.gomoku.application.usecase.ImportGameUseCase
import com.wanbaohe.gomoku.application.usecase.ImportResult
import com.wanbaohe.gomoku.application.usecase.ManageGameUseCase
import com.wanbaohe.gomoku.application.usecase.PlayMoveUseCase
import com.wanbaohe.gomoku.application.usecase.SkippedPly
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameResultCode
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.Side
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImportGameUseCaseTest {
    private val games = MemoryGames(testGame())
    private val moves = MemoryMoves()
    private val query = GameQueryUseCase(games, moves)
    private val manage = ManageGameUseCase(games, moves, query)
    private val export = ExportGameUseCase(query)
    private val tasks = object : AiTaskStore {
        override suspend fun getLatestByGame(gameId: String): AiTaskEntity? = null
        override suspend fun upsert(entity: AiTaskEntity) = Unit
        override suspend fun deleteByGame(gameId: String) = Unit
    }
    private val importer = ImportGameUseCase(
        createImportedGame = { preparation ->
            val gameId = UUID.randomUUID().toString()
            games.insert(testGame(gameId, preparation.initialFen, GameStatus.NOT_STARTED).copy(
                title = preparation.title,
                mode = preparation.setup.mode,
                blackPlayerType = preparation.setup.playerTypeFor(Side.BLACK),
                whitePlayerType = preparation.setup.playerTypeFor(Side.WHITE),
                blackPlayerConfigJson = preparation.blackAiConfig?.encode() ?: "{}",
                redPlayerConfigJson = preparation.whiteAiConfig?.encode() ?: "{}",
                origin = preparation.origin,
                startedAt = 0L,
                lastMoveAt = 0L,
                lastPlayedAt = 0L,
            ))
        },
        playMove = PlayMoveUseCase(games, moves, query),
        manageGame = manage,
        deleteGame = DeleteGameUseCase(games, moves, tasks),
    )

    @Test
    fun anUndoneJsonRoundTripKeepsThePositionCursorAndRedoHistory() {
        runBlocking {
            val first = moves.addMove("game", 1, games.game.initialFen, BoardPoint(7, 7))
            val second = moves.addMove("game", 2, first, BoardPoint(7, 8))
            games.game = games.game.copy(currentFen = second, currentPly = 2)
            manage.undo("game")
            val original = games.game

            val result = assertIs<ImportResult.Success>(importer.importJson("", export.asJson("game"), "Imported"))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(2, result.importedPlies)
            assertTrue(result.skippedPlies.isEmpty())
            assertEquals(first, restored.currentFen)
            assertEquals(1, restored.currentPly)
            assertEquals(GameStatus.PAUSED, restored.status)
            assertEquals(listOf(first, second), restored.plies.map { it.afterFen })
            assertEquals(original, games.game)

            val redone = assertNotNull(manage.redo(result.gameId))
            assertEquals(second, redone.currentFen)
            assertEquals(2, redone.currentPly)
            assertEquals(GameStatus.PAUSED, redone.status)
        }
    }

    @Test
    fun cursorZeroRestoresTheCustomInitialFenWithoutDiscardingTheFuture() {
        runBlocking {
            val initial = FenCodec.INITIAL_FEN.replace(" b 1", " w 42")
            val first = moves.addMove("game", 1, initial, BoardPoint(2, 2))
            games.game = games.game.copy(initialFen = initial, currentFen = first, currentPly = 1)
            manage.undo("game")
            val result = assertIs<ImportResult.Success>(importer.importJson("", export.asJson("game"), "Imported"))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(initial, restored.initialFen)
            assertEquals(initial, restored.currentFen)
            assertEquals(0, restored.currentPly)
            assertEquals(1, restored.plies.size)
            assertEquals(GameStatus.PAUSED, restored.status)
            assertEquals(first, manage.redo(result.gameId)?.currentFen)
        }
    }

    @Test
    fun aWinningRedoFutureDoesNotMakeTheRestoredEarlierCursorTerminal() {
        runBlocking {
            val file = payload(
                listOf("A1", "A15", "B1", "B15", "C1", "C15", "D1", "D15", "E1"),
                cursor = JsonPrimitive(8), status = GameStatus.PAUSED,
            )
            val result = assertIs<ImportResult.Success>(importer.importJson("", file.toString(), "Imported"))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(8, restored.currentPly)
            assertEquals(9, restored.plies.size)
            assertEquals(GameStatus.PAUSED, restored.status)
            assertEquals(GameResultCode.NONE, restored.resultText)
            assertNull(FenCodec.parse(restored.currentFen).stoneAt(BoardPoint(4, 0)))
            assertEquals(GameStatus.BLACK_WINS, manage.redo(result.gameId)?.status)
        }
    }

    @Test
    fun legacyFilesWithoutACursorRestoreTheEndOfAcceptedHistory() {
        runBlocking {
            val result = assertIs<ImportResult.Success>(importer.importJson(
                "", payload(listOf("H8", "H9")).toString(), "Legacy",
            ))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(2, restored.currentPly)
            assertEquals(2, restored.plies.size)
            assertEquals(restored.plies.last().afterFen, restored.currentFen)
            assertEquals(GameStatus.PAUSED, restored.status)
            assertEquals("Legacy", restored.title)
            assertEquals(FenCodec.INITIAL_FEN, restored.initialFen)
        }
    }

    @Test
    fun aSkippedPlyBeforeTheCursorIsReportedAndRemappedNotReplacedByAFuturePly() {
        runBlocking {
            val result = assertIs<ImportResult.Success>(importer.importJson(
                "", payload(listOf("H8", "H8", "H9"), JsonPrimitive(2), GameStatus.PAUSED).toString(), "Partial",
            ))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(listOf(SkippedPly(2, "H8")), result.skippedPlies)
            assertTrue(result.hasSkipped)
            assertEquals(2, result.importedPlies)
            assertEquals(1, restored.currentPly)
            assertEquals(2, restored.plies.size)
            assertEquals(restored.plies.first().afterFen, restored.currentFen)
            assertNull(FenCodec.parse(restored.currentFen).stoneAt(BoardPoint(7, 8)))
            assertEquals(restored.plies.last().afterFen, manage.redo(result.gameId)?.currentFen)
        }
    }

    @Test
    fun skippedFuturePliesDoNotMoveTheActiveCursorAndLegacySkipsUseHistoryEnd() {
        runBlocking {
            val future = assertIs<ImportResult.Success>(importer.importJson(
                "", payload(listOf("H8", "H9", "bad"), JsonPrimitive(1)).toString(), "Partial",
            ))
            val restored = assertNotNull(query.getById(future.gameId))
            assertEquals(listOf(SkippedPly(3, "bad")), future.skippedPlies)
            assertEquals(1, restored.currentPly)
            assertEquals(2, restored.plies.size)

            val legacy = assertIs<ImportResult.Success>(importer.importJson(
                "", payload(listOf("bad", "H8", "H9")).toString(), "Legacy partial",
            ))
            assertEquals(listOf(SkippedPly(1, "bad")), legacy.skippedPlies)
            assertEquals(2, query.getById(legacy.gameId)?.currentPly)
        }
    }

    @Test
    fun malformedCursorsFailBeforeAnyGameOrMoveIsPersisted() {
        runBlocking {
            val malformed = listOf(
                JsonPrimitive(-1), JsonPrimitive(3), JsonPrimitive(Long.MAX_VALUE),
                JsonPrimitive("1"), JsonPrimitive(1.5), JsonPrimitive(true), JsonNull,
                buildJsonObject { put("value", 1) },
            )
            for (cursor in malformed) {
                val before = games.games.toMap()
                val result = assertIs<ImportResult.Failure>(importer.importJson(
                    "", payload(listOf("H8", "H9"), cursor).toString(), "Invalid",
                ))
                assertEquals(ImportFailureCause.INVALID_JSON, result.cause)
                assertEquals(before, games.games)
                assertTrue(moves.plies.isEmpty())
            }
        }
    }

    @Test
    fun snapshotAndSavedOpponentSurviveRoundTripWithoutReadingGlobalDefaults() {
        runBlocking {
            val custom = FenCodec.INITIAL_FEN.replace(" b 1", " w 17")
            val config = GameAiPlayerConfig.capture(GomokuAiSource.WorkingModel, AiEngine(
                name = "saved-provider", title = "Saved provider",
                model = AiModel(name = "saved-model", title = "Saved model", updateTime = 1),
                requestProtocol = AiRequestProtocol.RESPONSES_COMPATIBLE,
                authorizationCode = "not-for-storage",
            ))
            val origin = GameOrigin("deleted-source", 0, custom, "Original \"title\"")
            games.game = games.game.copy(
                title = "Saved practice", initialFen = custom, currentFen = custom,
                redPlayerConfigJson = config.encode(), origin = origin,
            )
            val json = export.asJson("game")
            val result = assertIs<ImportResult.Success>(importer.importJson("", json, "Imported"))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(custom, restored.currentFen)
            assertEquals(0, restored.currentPly)
            assertEquals(GameStatus.PAUSED, restored.status)
            assertEquals(origin, restored.origin)
            assertEquals(config, restored.whiteAiConfig)
            assertEquals(config.encode(), restored.whitePlayerConfigJson)
            assertFalse(json.contains("not-for-storage"))
            games.archive("game")
            assertEquals(origin, query.getById(result.gameId)?.origin)
        }
    }

    @Test
    fun resignationAndWinnerApplyAtTheRestoredCursorAndSurviveRedo() {
        runBlocking {
            val first = moves.addMove("game", 1, games.game.initialFen, BoardPoint(7, 7))
            moves.addMove("game", 2, first, BoardPoint(7, 8))
            games.game = games.game.copy(
                currentFen = first, currentPly = 1,
                status = GameStatus.RESIGNED, resultText = GameResultCode.RESIGNED, winnerSide = Side.WHITE.name,
            )
            val result = assertIs<ImportResult.Success>(importer.importJson("", export.asJson("game"), "Imported"))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(first, restored.currentFen)
            assertEquals(1, restored.currentPly)
            assertEquals(2, restored.plies.size)
            assertEquals(GameStatus.RESIGNED, restored.status)
            assertEquals(GameResultCode.RESIGNED, restored.resultText)
            assertEquals(Side.WHITE.name, restored.winnerSide)
            val redone = assertNotNull(manage.redo(result.gameId))
            assertEquals(2, redone.currentPly)
            assertEquals(GameStatus.RESIGNED, redone.status)
            assertEquals(Side.WHITE.name, redone.winnerSide)
        }
    }

    @Test
    fun legacyTerminalResultAndWinnerAreRestoredWithoutACursor() {
        runBlocking {
            val file = buildJsonObject {
                payload(listOf("H8")).forEach { (key, value) -> put(key, value) }
                put("result", GameResultCode.RESIGNED)
                put("winnerSide", Side.BLACK.name)
            }
            val result = assertIs<ImportResult.Success>(importer.importJson("", file.toString(), "Legacy"))
            val restored = assertNotNull(query.getById(result.gameId))
            assertEquals(1, restored.currentPly)
            assertEquals(GameStatus.RESIGNED, restored.status)
            assertEquals(Side.BLACK.name, restored.winnerSide)
        }
    }

    @Test
    fun malformedSavedOpponentCannotSilentlyTurnIntoADefault() {
        runBlocking {
            val file = buildJsonObject {
                payload(emptyList(), JsonPrimitive(0)).forEach { (key, value) -> put(key, value) }
                put("whiteAiConfig", buildJsonObject {
                    put("sourceKey", "working_model")
                    put("engineName", "missing-model")
                })
            }
            val before = games.games.toMap()
            val result = assertIs<ImportResult.Failure>(importer.importJson("", file.toString(), "Invalid"))
            assertEquals(ImportFailureCause.INVALID_JSON, result.cause)
            assertEquals(before, games.games)
            assertTrue(moves.plies.isEmpty())
        }
    }

    private fun payload(
        history: List<String>,
        cursor: JsonElement? = null,
        status: GameStatus? = null,
    ): JsonObject = buildJsonObject {
        put("version", 1)
        put("initialFen", FenCodec.INITIAL_FEN)
        put("moves", buildJsonArray {
            history.forEach { move -> add(buildJsonObject { put("move", move) }) }
        })
        if (cursor != null) put("currentPly", cursor)
        if (status != null) put("status", status.name)
    }
}
