package com.wanbaohe.gomoku.application

import com.shifenmiao.model.ModelProvider.AppJson
import com.shifenmiao.model.ai.AiEngine
import com.shifenmiao.model.ai.AiModel
import com.shifenmiao.model.ai.AiRequestProtocol
import com.wanbaohe.gomoku.application.dto.ExportLabels
import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.GomokuAiSource
import com.wanbaohe.gomoku.application.usecase.ExportGameUseCase
import com.wanbaohe.gomoku.application.usecase.GameQueryUseCase
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameStatus
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExportGameUseCaseTest {
    private val games = MemoryGames(testGame())
    private val moves = MemoryMoves()
    private val export = ExportGameUseCase(GameQueryUseCase(games, moves))

    @Test
    fun exportKeepsAnImmutableSourceSnapshotEvenWhenTheSourceDoesNotExist() = runBlocking {
        val origin = GameOrigin("removed-source", 0, FenCodec.INITIAL_FEN, "Source \"title\"\nline")
        games.game = games.game.copy(origin = origin, title = "Practice \"title\"")
        val json = AppJson.parseToJsonElement(export.asJson("game")).jsonObject
        assertEquals("Practice \"title\"", json.getValue("title").jsonPrimitive.content)
        assertEquals(origin, GameOrigin.decode(json.getValue("origin").toString()))
        val text = export.asText("game", ExportLabels("Gomoku", "Title", "Initial", "Result", "Source"), "")
        assertTrue(text.contains(origin.gameId))
        assertTrue(text.contains(origin.fen))
        assertTrue(text.contains(origin.title))
    }

    @Test
    fun jsonKeepsRedoFutureAndCursorWhileTextOnlyExportsActiveHistory() = runBlocking {
        val first = moves.addMove("game", 1, games.game.initialFen, BoardPoint(7, 7))
        moves.addMove("game", 2, first, BoardPoint(7, 8))
        games.game = games.game.copy(currentFen = first, currentPly = 1)
        val json = AppJson.parseToJsonElement(export.asJson("game")).jsonObject
        assertEquals(3, json.getValue("version").jsonPrimitive.int)
        assertEquals(1, json.getValue("currentPly").jsonPrimitive.int)
        assertEquals(GameStatus.PAUSED.name, json.getValue("status").jsonPrimitive.content)
        assertEquals(listOf("H8", "H9"), json.getValue("moves").jsonArray.map {
            it.jsonObject.getValue("move").jsonPrimitive.content
        })
        val text = export.asText("game", ExportLabels("Gomoku", "Title", "Initial", "Result"), "")
        assertTrue(text.contains("1. H8"))
        assertFalse(text.contains("H9"))
        assertEquals(2, moves.plies.size)
        assertEquals(first, export.asFen("game"))
    }

    @Test
    fun jsonSortsAllStoredPliesIncludingTheFuture() = runBlocking {
        val first = moves.addMove("game", 1, games.game.initialFen, BoardPoint(7, 7))
        moves.addMove("game", 2, first, BoardPoint(7, 8))
        moves.plies.reverse()
        games.game = games.game.copy(currentFen = games.game.initialFen, currentPly = 0)
        val json = AppJson.parseToJsonElement(export.asJson("game")).jsonObject
        assertEquals(0, json.getValue("currentPly").jsonPrimitive.int)
        assertEquals(listOf("H8", "H9"), json.getValue("moves").jsonArray.map {
            it.jsonObject.getValue("move").jsonPrimitive.content
        })
    }

    @Test
    fun customEmptyHistoryPositionIsPreservedExactly() = runBlocking {
        val custom = FenCodec.INITIAL_FEN.replace(" b 1", " w 42")
        games.game = games.game.copy(initialFen = custom, currentFen = custom)
        val json = AppJson.parseToJsonElement(export.asJson("game")).jsonObject
        assertEquals(custom, json.getValue("initialFen").jsonPrimitive.content)
        assertTrue(json.getValue("moves").jsonArray.isEmpty())
    }

    @Test
    fun aWhiteFirstPracticeIsNotExportedAsBlackFirst() = runBlocking {
        val custom = FenCodec.INITIAL_FEN.replace(" b ", " w ")
        val first = moves.addMove("game", 1, custom, BoardPoint(7, 7))
        val second = moves.addMove("game", 2, first, BoardPoint(7, 8))
        games.game = games.game.copy(initialFen = custom, currentFen = second, currentPly = 2)
        val text = export.asText("game", ExportLabels("Gomoku", "Title", "Initial", "Result"), "")
        assertTrue(text.contains("1. ... H8"))
        assertTrue(text.contains("2. H9"))
    }

    @Test
    fun exportStoresOnlyTheSavedProviderAndModelNeverApiCredentialsOrGlobalDefaults() = runBlocking {
        val config = GameAiPlayerConfig.capture(
            GomokuAiSource.WorkingModel,
            AiEngine(
                name = "saved-provider", title = "Saved provider",
                model = AiModel(name = "saved-model", title = "Saved model", updateTime = 1),
                requestProtocol = AiRequestProtocol.OPENAI_COMPATIBLE,
                authorizationCode = "unit-test-private-value", requestUrl = "https://unit.invalid",
            ),
        )
        games.game = games.game.copy(redPlayerConfigJson = config.encode())
        val encoded = export.asJson("game")
        assertFalse(encoded.contains("authorizationCode"))
        assertFalse(encoded.contains("unit-test-private-value"))
        assertFalse(encoded.contains("unit.invalid"))
        val snapshot = assertNotNull(GameAiPlayerConfig.decode(
            AppJson.parseToJsonElement(encoded).jsonObject.getValue("whiteAiConfig").toString(),
        ))
        assertEquals(config, snapshot)
    }
}
