package com.wanbaohe.chess.application

import com.shifenmiao.model.ModelProvider.AppJson
import com.wanbaohe.chess.application.dto.ExportLabels
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.usecase.ExportGameUseCase
import com.wanbaohe.chess.application.usecase.GameMutationLock
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.ImportedHistoryCursor
import com.wanbaohe.chess.application.usecase.ManageGameUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameOrigin
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.yield
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class GameHistoryBackupTest {
    @Test
    fun jsonIncludesRedoFutureAndCursorWithoutMutatingTheExportedGame() = runBlocking {
        val fixture = Fixture()
        fixture.recordOpening()
        fixture.manage.undo("game", 2)
        fixture.manage.pause("game")
        val before = fixture.games.game
        val pliesBefore = fixture.moves.plies.toList()

        val backup = AppJson.parseToJsonElement(fixture.export.asJson("game")).jsonObject

        assertEquals(2, backup.getValue("currentPly").jsonPrimitive.int)
        assertEquals(4, backup.getValue("moves").jsonArray.size)
        assertEquals(before.title, backup.getValue("title").jsonPrimitive.content)
        assertEquals(before.origin, AppJson.decodeFromString<GameOrigin>(backup.getValue("origin").toString()))
        assertEquals(before, fixture.games.game)
        assertEquals(pliesBefore, fixture.moves.plies)
    }

    @Test
    fun restoringABackupCursorLeavesItsFutureAvailableForPairedRedo() = runBlocking {
        val original = Fixture()
        original.recordOpening()
        val endFen = original.games.game.currentFen
        original.manage.undo("game", 2)
        original.manage.pause("game")
        val expected = original.games.game
        val backup = AppJson.parseToJsonElement(original.export.asJson("game")).jsonObject
        val imported = Fixture()
        val moves = backup.getValue("moves").jsonArray
        imported.replay(moves)
        val cursor = ImportedHistoryCursor.read(backup.getValue("currentPly").jsonPrimitive.int, moves.size)
        imported.manage.undo("game", moves.size - cursor)
        imported.manage.pause("game")

        assertEquals(expected.currentFen, imported.games.game.currentFen)
        assertEquals(expected.currentPly, imported.games.game.currentPly)
        assertEquals(GameStatus.PAUSED, imported.games.game.status)
        assertEquals(4, imported.moves.plies.size)
        assertEquals(GameStatus.PAUSED, imported.manage.redo("game", 2)?.status)
        assertEquals(endFen, imported.games.game.currentFen)
        assertEquals(4, imported.moves.plies.size)
        assertEquals(expected, original.games.game)
    }

    @Test
    fun aPlyZeroBackupPreservesTheExactCustomFenAndAllRedo() = runBlocking {
        val initialFen = "r3k2r/8/8/3pP3/8/8/8/R3K2R w KQkq d6 0 9"
        val original = Fixture(initialFen)
        original.push("e5d6")
        original.manage.undo("game")
        original.manage.pause("game")
        val backup = AppJson.parseToJsonElement(original.export.asJson("game")).jsonObject
        val imported = Fixture(backup.getValue("initialFen").jsonPrimitive.content)
        imported.replay(backup.getValue("moves").jsonArray)
        imported.manage.undo("game", 1)
        imported.manage.pause("game")

        assertEquals(0, backup.getValue("currentPly").jsonPrimitive.int)
        assertEquals(initialFen, imported.games.game.currentFen)
        assertEquals(1, imported.moves.plies.size)
        imported.manage.redo("game")
        assertEquals(original.moves.plies.single().afterFen, imported.games.game.currentFen)
    }

    @Test
    fun explicitImportedResultsBelongToTheRestoredCursorNotTheEndOfRedo() = runBlocking {
        val original = Fixture()
        original.recordOpening()
        original.manage.undo("game", 2)
        original.manage.resign("game", Side.WHITE)
        val backup = AppJson.parseToJsonElement(original.export.asJson("game")).jsonObject
        val imported = Fixture()
        val moves = backup.getValue("moves").jsonArray
        imported.replay(moves)
        imported.manage.undo("game", moves.size - backup.getValue("currentPly").jsonPrimitive.int)
        imported.manage.applyImportedResult(
            "game", GameStatus.RESIGNED, backup.getValue("winnerSide").jsonPrimitive.content,
        )
        imported.manage.pause("game")

        assertEquals("RESIGNED", backup.getValue("result").jsonPrimitive.content)
        assertEquals(original.games.game.currentFen, imported.games.game.currentFen)
        assertEquals(GameStatus.RESIGNED, imported.games.game.status)
        assertEquals(Side.BLACK.name, imported.games.game.winnerSide)
        assertEquals(4, imported.moves.plies.size)
    }

    @Test
    fun textNotationIncludesOnlyTheActiveLine() = runBlocking {
        val fixture = Fixture()
        fixture.recordOpening()
        fixture.manage.undo("game", 2)
        val text = fixture.export.asText("game", ExportLabels("Chess", "Title", "Initial", "Result"), "")
        assertTrue(text.contains("e2e4"))
        assertTrue(text.contains("e7e5"))
        assertFalse(text.contains("g1f3"))
        assertFalse(text.contains("b8c6"))
        assertEquals(4, fixture.moves.plies.size)
    }

    @Test
    fun backupWaitsForAcceptedPersistenceWithoutPausingOrChangingTheSource() = runBlocking {
        val fixture = Fixture()
        fixture.push("e2e4")
        fixture.push("e7e5")
        val entered = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        fixture.moves.beforeInsert = { entered.complete(Unit); release.await() }
        val commit = async { fixture.push("g1f3") }
        entered.await()
        val exported = async { fixture.export.asJson("game") }
        yield()
        assertFalse(exported.isCompleted)
        release.complete(Unit)
        commit.await()
        val before = fixture.games.game
        val backup = AppJson.parseToJsonElement(exported.await()).jsonObject

        assertEquals(3, backup.getValue("currentPly").jsonPrimitive.int)
        assertEquals(3, backup.getValue("moves").jsonArray.size)
        assertEquals(GameStatus.PLAYING, fixture.games.game.status)
        assertEquals(before, fixture.games.game)
    }

    private class Fixture(initialFen: String = FenCodec.INITIAL_FEN) {
        val games = MemoryGames(GameEntity(
            id = "game", title = "Game \"A\"\nPractice", mode = GameMode.LOCAL_PVP,
            whitePlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.HUMAN,
            redPlayerConfigJson = "{}", blackPlayerConfigJson = "{}",
            initialFen = initialFen, currentFen = initialFen, currentPly = 0,
            status = GameStatus.PLAYING, resultText = "", winnerSide = "",
            startedAt = 0, lastMoveAt = 0, lastPlayedAt = 0, updatedAt = 1,
            origin = GameOrigin("source", 0, initialFen, "Original"),
        ))
        val moves = MemoryMoves()
        private val query = GameQueryUseCase(games, moves)
        private val mutationLock = GameMutationLock()
        val manage = ManageGameUseCase(games, moves, query, mutationLock)
        private val play = PlayMoveUseCase(games, moves, query, mutationLock)
        val export = ExportGameUseCase(query, mutationLock)

        suspend fun recordOpening() {
            listOf("e2e4", "e7e5", "g1f3", "b8c6").forEach { push(it) }
        }

        suspend fun replay(moves: JsonArray) {
            moves.forEach { push(it.jsonObject.getValue("move").jsonPrimitive.content) }
        }

        suspend fun push(uci: String) {
            val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first { it.notationUcci == uci }
            assertIs<PlayMoveUseCase.Result.Success>(play.commit("game", move, importing = true))
        }
    }
}
