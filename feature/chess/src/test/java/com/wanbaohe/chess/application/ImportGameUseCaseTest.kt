package com.wanbaohe.chess.application

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.chess.application.dto.GameAiPlayerConfig
import com.wanbaohe.chess.application.port.outbound.AiTaskEntity
import com.wanbaohe.chess.application.port.outbound.AiTaskStore
import com.wanbaohe.chess.application.port.outbound.ChessAiSource
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.usecase.DeleteGameUseCase
import com.wanbaohe.chess.application.usecase.ExportGameUseCase
import com.wanbaohe.chess.application.usecase.GameMutationLock
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.ImportFailureCause
import com.wanbaohe.chess.application.usecase.ImportGameUseCase
import com.wanbaohe.chess.application.usecase.ImportResult
import com.wanbaohe.chess.application.usecase.ManageGameUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.application.usecase.SkippedPly
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ImportGameUseCaseTest {
    private val games = MemoryGames(game())
    private val moves = MemoryMoves()
    private val lock = GameMutationLock()
    private val query = GameQueryUseCase(games, moves)
    private val manage = ManageGameUseCase(games, moves, query, lock)
    private val play = PlayMoveUseCase(games, moves, query, lock)
    private val export = ExportGameUseCase(query, lock)
    private var createdGames = 0
    private val tasks = object : AiTaskStore {
        override suspend fun getLatestByGame(gameId: String): AiTaskEntity? = null
        override suspend fun upsert(entity: AiTaskEntity) = Unit
        override suspend fun deleteByGame(gameId: String) = Unit
    }
    private val importer = ImportGameUseCase(
        createImportedGame = { preparation ->
            val id = "imported-${++createdGames}"
            games.insert(game(id, preparation.initialFen).copy(
                title = preparation.title,
                mode = preparation.setup.mode,
                whitePlayerType = preparation.setup.playerTypeFor(Side.WHITE),
                blackPlayerType = preparation.setup.playerTypeFor(Side.BLACK),
                redPlayerConfigJson = preparation.whiteAiConfig?.encode() ?: "{}",
                blackPlayerConfigJson = preparation.blackAiConfig?.encode() ?: "{}",
                status = GameStatus.NOT_STARTED,
                origin = preparation.origin,
                startedAt = 0,
                lastMoveAt = 0,
                lastPlayedAt = 0,
            ))
        },
        playMove = play,
        manageGame = manage,
        deleteGame = DeleteGameUseCase(games, moves, tasks, lock),
    )

    @Test
    fun savedChatIdentityCursorFutureAndSourceRoundTripWithoutCredentialsOrSourceWrites() = runBlocking {
        val config = chatConfig()
        games.game = games.game.copy(
            mode = GameMode.HUMAN_VS_LLM, blackPlayerType = PlayerType.LLM,
            blackPlayerConfigJson = config.encode(),
            origin = GameOrigin("source", 0, FenCodec.INITIAL_FEN, "Original \"game\""),
        )
        recordOpening()
        val endFen = games.game.currentFen
        manage.undo("game", 2)
        manage.pause("game")
        val source = games.game
        val sourcePlies = moves.getByGame("game")
        val backup = export.asJson("game")

        listOf("authorizationCode", "not-a-real-credential-test-only", "requestUrl", "private.invalid").forEach {
            assertFalse(backup.contains(it))
        }
        val result = assertIs<ImportResult.Success>(importer.importJson("", backup, "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(GameMode.HUMAN_VS_LLM, imported.mode)
        assertEquals(PlayerType.HUMAN, imported.whitePlayerType)
        assertEquals(PlayerType.LLM, imported.blackPlayerType)
        assertNull(imported.whiteAiConfig)
        assertEquals(config, imported.blackAiConfig)
        assertEquals(source.currentFen, imported.currentFen)
        assertEquals(2, imported.currentPly)
        assertEquals(4, imported.plies.size)
        assertEquals(GameStatus.PAUSED, imported.status)
        assertEquals(source.origin, imported.origin)
        assertEquals(endFen, manage.redo(result.gameId, 2)?.currentFen)
        assertEquals(source, games.game)
        assertEquals(sourcePlies, moves.getByGame("game"))
    }

    @Test
    fun bothAiSeatsRetainIndependentSourceAndProviderSnapshots() = runBlocking {
        val white = chatConfig()
        val black = GameAiPlayerConfig(sourceKey = "stockfish")
        games.game = games.game.copy(
            mode = GameMode.LLM_VS_LLM, whitePlayerType = PlayerType.LLM, blackPlayerType = PlayerType.LLM,
            redPlayerConfigJson = white.encode(), blackPlayerConfigJson = black.encode(),
        )
        val result = assertIs<ImportResult.Success>(importer.importJson("", export.asJson("game"), "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(GameMode.LLM_VS_LLM, imported.mode)
        assertEquals(white, imported.whiteAiConfig)
        assertEquals(black, imported.blackAiConfig)
        assertEquals(white.encode(), games.records.getValue(result.gameId).redPlayerConfigJson)
    }

    @Test
    fun savedRemoteEngineNeverRequiresOrCapturesAGlobalChatModel() = runBlocking {
        val remote = GameAiPlayerConfig(sourceKey = "stockfish")
        val result = assertIs<ImportResult.Success>(importer.importJson("", humanAiPayload(
            AppJson.encodeToJsonElement(remote),
        ).toString(), "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(remote, imported.blackAiConfig)
        assertEquals(ChessAiSource.default, imported.blackAiConfig?.source)
        assertNull(imported.blackAiConfig?.model)
    }

    @Test
    fun legacyFilesDefaultToLocalTwoHumansAndTheEndOfAllAcceptedMoves() = runBlocking {
        val result = assertIs<ImportResult.Success>(importer.importJson(
            "", payload(listOf("e2e4", "e7e5")).toString(), "Legacy",
        ))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(GameMode.LOCAL_PVP, imported.mode)
        assertEquals(PlayerType.HUMAN, imported.whitePlayerType)
        assertEquals(PlayerType.HUMAN, imported.blackPlayerType)
        assertEquals(2, imported.currentPly)
        assertEquals(GameStatus.PAUSED, imported.status)
        assertEquals("Legacy", imported.title)
        assertNull(imported.whiteAiConfig)
        assertNull(imported.blackAiConfig)
    }

    @Test
    fun onlineBackupsBecomeLocalReplaysAndNeverRestoreRoomConnectionData() = runBlocking {
        val roomConfig = """{"roomId":"old-room","mySide":"WHITE","opponentName":"Guest"}"""
        games.game = games.game.copy(
            mode = GameMode.ONLINE_PVP, blackPlayerType = PlayerType.REMOTE,
            redPlayerConfigJson = roomConfig, blackPlayerConfigJson = roomConfig,
        )
        recordOpening()
        val original = games.game
        val backup = export.asJson("game")
        assertFalse(backup.contains("old-room"))
        assertFalse(backup.contains("roomId"))
        val result = assertIs<ImportResult.Success>(importer.importJson("", backup, "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(GameMode.LOCAL_PVP, imported.mode)
        assertEquals(PlayerType.HUMAN, imported.whitePlayerType)
        assertEquals(PlayerType.HUMAN, imported.blackPlayerType)
        assertEquals("", imported.onlineMetadata.roomId)
        assertEquals("{}", games.records.getValue(result.gameId).redPlayerConfigJson)
        assertEquals("{}", games.records.getValue(result.gameId).blackPlayerConfigJson)
        assertEquals(4, imported.currentPly)
        assertEquals(GameStatus.PAUSED, imported.status)
        assertEquals(original, games.game)

        val legacyOnline = JsonObject(payload(emptyList()) + mapOf(
            "mode" to JsonPrimitive(GameMode.ONLINE_PVP.name),
            "roomId" to JsonPrimitive("must-not-reconnect"),
        ))
        val legacyResult = assertIs<ImportResult.Success>(importer.importJson("", legacyOnline.toString(), "Imported"))
        assertEquals(GameMode.LOCAL_PVP, query.getById(legacyResult.gameId)?.mode)
        assertEquals("", query.getById(legacyResult.gameId)?.onlineMetadata?.roomId)
    }

    @Test
    fun declaredAiSeatsRejectMissingNullEmptyAndWrongShapeConfigsBeforeCreatingAGame() = runBlocking {
        val valid = humanAiPayload(AppJson.encodeToJsonElement(chatConfig()))
        val files = listOf(
            JsonObject(valid - "blackAiConfig"),
            humanAiPayload(JsonNull),
            humanAiPayload(buildJsonObject {}),
            humanAiPayload(JsonPrimitive("not-json")),
        )
        files.forEach { assertInvalidJson(it.toString()) }
        assertEquals(0, createdGames)
        assertEquals(1, games.records.size)
    }

    @Test
    fun unknownSourcesAndMissingSavedProviderOrModelIdentityNeverBecomeGlobalDefaults() = runBlocking {
        val valid = AppJson.encodeToJsonElement(chatConfig()).jsonObject
        val invalid = listOf(
            JsonObject(valid + ("sourceKey" to JsonPrimitive(""))),
            JsonObject(valid + ("sourceKey" to JsonPrimitive("unsupported-engine"))),
            JsonObject(valid + ("engineName" to JsonPrimitive(""))),
            JsonObject(valid - "engineProtocol"),
            JsonObject(valid - "model"),
            JsonObject(valid + ("model" to JsonNull)),
            JsonObject(valid + ("model" to buildJsonObject { put("title", "Missing name") })),
            JsonObject(valid + ("model" to buildJsonObject { put("name", "Missing title") })),
        )
        invalid.forEach { assertInvalidJson(humanAiPayload(it).toString()) }
        assertEquals(0, createdGames)
    }

    @Test
    fun malformedUnknownAndUnsupportedChatProtocolsAreRejectedWithoutFallback() = runBlocking {
        val valid = AppJson.encodeToJsonElement(chatConfig()).jsonObject
        listOf("", "unknown", "openai", "LOCAL_ON_DEVICE", "JEV", "PIKAFISH").forEach { protocol ->
            assertInvalidJson(humanAiPayload(JsonObject(
                valid + ("engineProtocol" to JsonPrimitive(protocol)),
            )).toString())
        }
        assertEquals(0, createdGames)
    }

    @Test
    fun incompatibleModesSeatsAndConfigsAreReportedAsInvalidJson() = runBlocking {
        val valid = humanAiPayload(AppJson.encodeToJsonElement(chatConfig()))
        val invalid = listOf(
            JsonObject(valid + ("mode" to JsonPrimitive(GameMode.LOCAL_PVP.name))),
            JsonObject(valid + ("blackPlayerType" to JsonPrimitive(PlayerType.HUMAN.name))),
            JsonObject(valid + ("mode" to JsonPrimitive(GameMode.ONLINE_PVP.name))),
            JsonObject(valid + ("mode" to JsonPrimitive("unknown"))),
            JsonObject(valid + ("blackPlayerType" to JsonPrimitive("unknown"))),
            JsonObject(valid + ("whitePlayerType" to JsonPrimitive(1))),
            JsonObject(valid + ("blackPlayerType" to JsonPrimitive(PlayerType.REMOTE.name))),
        )
        invalid.forEach { assertInvalidJson(it.toString()) }
        assertEquals(0, createdGames)
    }

    @Test
    fun configsCannotImportAWholeEngineCredentialsOrAChatModelIntoARemoteSource() = runBlocking {
        val valid = AppJson.encodeToJsonElement(chatConfig()).jsonObject
        val model = valid.getValue("model").jsonObject
        listOf(
            JsonObject(valid + ("authorizationCode" to JsonPrimitive("not-a-real-credential-test-only"))),
            JsonObject(valid + ("requestUrl" to JsonPrimitive("https://private.invalid"))),
            JsonObject(valid + ("sourceKey" to JsonPrimitive("stockfish"))),
            JsonObject(valid + ("model" to JsonPrimitive(false))),
            JsonObject(valid + ("model" to JsonObject(model + ("canNetwork" to JsonNull)))),
            JsonObject(valid + ("model" to JsonObject(model + ("temperature" to JsonPrimitive("not-a-number"))))),
            JsonObject(valid + ("model" to JsonObject(model + ("apiKey" to JsonPrimitive("not-a-real-credential"))))),
        ).forEach { assertInvalidJson(humanAiPayload(it).toString()) }
        assertEquals(0, createdGames)
    }

    @Test
    fun aPlyZeroImportKeepsExactCastlingEnPassantCountersAndTheRedoFuture() = runBlocking {
        val customFen = "r3k2r/8/8/3pP3/8/8/8/R3K2R w KQkq d6 0 9"
        val file = JsonObject(payload(listOf("e5d6"), JsonPrimitive(0)) + mapOf(
            "initialFen" to JsonPrimitive(customFen), "status" to JsonPrimitive(GameStatus.PAUSED.name),
            "origin" to AppJson.encodeToJsonElement(GameOrigin("source", 0, customFen, "Original")),
        ))
        val result = assertIs<ImportResult.Success>(importer.importJson("", file.toString(), "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(customFen, imported.initialFen)
        assertEquals(customFen, imported.currentFen)
        assertEquals(customFen, imported.origin?.fen)
        assertEquals(0, imported.currentPly)
        assertEquals(1, imported.plies.size)
        assertEquals(GameStatus.PAUSED, imported.status)
        assertEquals(imported.plies.single().afterFen, manage.redo(result.gameId)?.currentFen)
    }

    @Test
    fun resultsAndWinnersApplyAfterRestoringTheCursorNotToTheEndOfRedo() = runBlocking {
        recordOpening()
        manage.undo("game", 2)
        manage.resign("game", Side.WHITE)
        val original = games.game
        val result = assertIs<ImportResult.Success>(importer.importJson("", export.asJson("game"), "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(original.currentFen, imported.currentFen)
        assertEquals(2, imported.currentPly)
        assertEquals(4, imported.plies.size)
        assertEquals(GameStatus.RESIGNED, imported.status)
        assertEquals("RESIGNED", imported.resultText)
        assertEquals(Side.BLACK.name, imported.winnerSide)
        assertEquals(original, games.game)
    }

    @Test
    fun aCheckmatingRedoFutureDoesNotFinishTheRestoredEarlierCursor() = runBlocking {
        val file = JsonObject(payload(
            listOf("f2f3", "e7e5", "g2g4", "d8h4"), JsonPrimitive(3),
        ) + ("status" to JsonPrimitive(GameStatus.PAUSED.name)))
        val result = assertIs<ImportResult.Success>(importer.importJson("", file.toString(), "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(3, imported.currentPly)
        assertEquals(4, imported.plies.size)
        assertEquals(GameStatus.PAUSED, imported.status)
        assertEquals("", imported.resultText)
        assertEquals("", imported.winnerSide)
        assertEquals(GameStatus.BLACK_WINS, manage.redo(result.gameId)?.status)
    }

    @Test
    fun skippedMovesAdjustTheCursorWhilePreservingValidFutureMoves() = runBlocking {
        val file = payload(listOf("e2e4", "illegal", "e7e5", "g1f3"), JsonPrimitive(3))
        val result = assertIs<ImportResult.Success>(importer.importJson("", file.toString(), "Imported"))
        val imported = assertNotNull(query.getById(result.gameId))
        assertEquals(listOf(SkippedPly(2, "illegal")), result.skippedPlies)
        assertEquals(3, result.importedPlies)
        assertEquals(2, imported.currentPly)
        assertEquals(3, imported.plies.size)
        assertEquals(imported.plies.last().afterFen, manage.redo(result.gameId)?.currentFen)
    }

    @Test
    fun invalidCursorsAndWrongFieldShapesFailBeforePersistence() = runBlocking {
        listOf(JsonNull, JsonPrimitive("0"), JsonPrimitive(true), JsonPrimitive(-1),
            JsonPrimitive(3), JsonPrimitive(0.5)).forEach {
            assertInvalidJson(payload(listOf("e2e4"), it).toString())
        }
        listOf(
            JsonObject(payload(emptyList()) + ("moves" to JsonPrimitive("not-array"))),
            JsonObject(payload(emptyList()) + ("origin" to JsonPrimitive("not-object"))),
            JsonObject(payload(emptyList()) + ("title" to JsonPrimitive(42))),
            JsonObject(payload(emptyList()) + ("winnerSide" to JsonPrimitive("RED"))),
            JsonObject(payload(emptyList()) + mapOf(
                "result" to JsonPrimitive("WHITE"), "winnerSide" to JsonPrimitive("BLACK"),
            )),
        ).forEach { assertInvalidJson(it.toString()) }
        assertEquals(0, createdGames)
    }

    @Test
    fun invalidFenAndMalformedJsonAreReportedWithTheRightFailureCause() = runBlocking {
        assertInvalidJson("{broken")
        val invalidFen = JsonObject(payload(emptyList()) + ("initialFen" to JsonPrimitive("not-fen")))
        val failure = assertIs<ImportResult.Failure>(importer.importJson("", invalidFen.toString(), "Imported"))
        assertEquals(ImportFailureCause.INVALID_FEN, failure.cause)
        val invalidOriginFen = JsonObject(payload(emptyList()) + ("origin" to buildJsonObject {
            put("gameId", "source"); put("ply", 0); put("title", "Source"); put("fen", "not-fen")
        }))
        assertEquals(ImportFailureCause.INVALID_FEN, assertIs<ImportResult.Failure>(
            importer.importJson("", invalidOriginFen.toString(), "Imported"),
        ).cause)
        assertEquals(0, createdGames)
    }

    @Test
    fun importingAnEmptyStartedBackupPausesWithoutInventingActualPlayActivity() = runBlocking {
        val result = assertIs<ImportResult.Success>(importer.importJson("", export.asJson("game"), "Imported"))
        val imported = games.records.getValue(result.gameId)
        assertEquals(GameStatus.PAUSED, imported.status)
        assertEquals(0, imported.currentPly)
        assertEquals(0L, imported.lastPlayedAt)
        assertEquals(0L, imported.startedAt)
    }

    @Test
    fun cancellingDuringAcceptedPersistenceRemovesTheIncompleteImportedRecord() = runBlocking {
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        moves.beforeInsert = { entered.complete(Unit); release.await() }
        val import = async { importer.importJson("", payload(listOf("e2e4", "e7e5")).toString(), "Imported") }
        entered.await()
        import.cancel()
        release.complete(Unit)
        import.join()
        assertTrue(import.isCancelled)
        assertEquals(setOf("game"), games.records.keys)
        assertTrue(moves.plies.isEmpty())
    }

    private suspend fun assertInvalidJson(file: String) {
        val result = assertIs<ImportResult.Failure>(importer.importJson("", file, "Imported"))
        assertEquals(ImportFailureCause.INVALID_JSON, result.cause)
    }

    private suspend fun recordOpening() {
        listOf("e2e4", "e7e5", "g1f3", "b8c6").forEach { notation ->
            val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first { it.notationUcci == notation }
            assertIs<PlayMoveUseCase.Result.Success>(play.commit("game", move, importing = true))
        }
    }

    private fun chatConfig(): GameAiPlayerConfig = GameAiPlayerConfig.capture(
        ChessAiSource.WorkingModel,
        AiEngine(
            name = "saved-provider", title = "Saved service", requestProtocol = AiRequestProtocol.ANTHROPIC_COMPATIBLE,
            model = AiModel(name = "saved-model", title = "Saved model", updateTime = 1),
            authorizationCode = "not-a-real-credential-test-only", requestUrl = "https://private.invalid",
        ),
    )

    private fun humanAiPayload(config: JsonElement): JsonObject = JsonObject(payload(emptyList()) + mapOf(
        "mode" to JsonPrimitive(GameMode.HUMAN_VS_LLM.name),
        "whitePlayerType" to JsonPrimitive(PlayerType.HUMAN.name),
        "blackPlayerType" to JsonPrimitive(PlayerType.LLM.name),
        "blackAiConfig" to config,
    ))

    private fun payload(moves: List<String>, cursor: JsonElement? = null): JsonObject = buildJsonObject {
        put("initialFen", FenCodec.INITIAL_FEN)
        put("moves", buildJsonArray { moves.forEach { add(buildJsonObject { put("move", it) }) } })
        cursor?.let { put("currentPly", it) }
    }

    private fun game(id: String = "game", fen: String = FenCodec.INITIAL_FEN): GameEntity = GameEntity(
        id = id, title = "Original", mode = GameMode.LOCAL_PVP,
        whitePlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.HUMAN,
        redPlayerConfigJson = "{}", blackPlayerConfigJson = "{}",
        initialFen = fen, currentFen = fen, currentPly = 0, status = GameStatus.PLAYING,
        resultText = "", winnerSide = "", startedAt = 1, lastMoveAt = 1, lastPlayedAt = 10, updatedAt = 10,
    )
}
