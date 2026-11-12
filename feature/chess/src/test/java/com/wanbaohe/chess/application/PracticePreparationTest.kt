/*
 * OneBox is a free AI agent and toolbox for Android
 * Copyright (c) 2026 zhishouwang(网址兽)
 *
 * Origin: OneBox original work, not derived from ImageToolbox
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.wanbaohe.chess.application

import com.wanbaohe.chess.application.dto.GamePreparation
import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.usecase.GameMutationLock
import com.wanbaohe.chess.application.usecase.GameQueryUseCase
import com.wanbaohe.chess.application.usecase.ManageGameUseCase
import com.wanbaohe.chess.application.usecase.PlayMoveUseCase
import com.wanbaohe.chess.domain.FenCodec
import com.wanbaohe.chess.domain.GameArbiter
import com.wanbaohe.chess.domain.model.GameMode
import com.wanbaohe.chess.domain.model.GameStatus
import com.wanbaohe.chess.domain.model.PlayerType
import com.wanbaohe.chess.domain.model.Side
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PracticePreparationTest {
    private val games = MemoryGames(GameEntity(
        id = "game", title = "Source", mode = GameMode.HUMAN_VS_LLM,
        whitePlayerType = PlayerType.HUMAN, blackPlayerType = PlayerType.LLM,
        redPlayerConfigJson = "{}", blackPlayerConfigJson = "{}",
        initialFen = FenCodec.INITIAL_FEN, currentFen = FenCodec.INITIAL_FEN, currentPly = 0,
        status = GameStatus.PLAYING, resultText = "", winnerSide = "",
        startedAt = 1, lastMoveAt = 1, lastPlayedAt = 100, updatedAt = 100,
    ))
    private val moves = MemoryMoves()
    private val query = GameQueryUseCase(games, moves)
    private val lock = GameMutationLock()
    private val manage = ManageGameUseCase(games, moves, query, lock)
    private val play = PlayMoveUseCase(games, moves, query, lock)

    @Test
    fun directReplayPracticeAwaitsPauseWithoutChangingSourceFenOrHistory() = runBlocking {
        val move = GameArbiter.legalMoves(FenCodec.parse(games.game.currentFen)).first()
        assertIs<PlayMoveUseCase.Result.Success>(play.commit("game", move))
        val source = requireNotNull(query.getById("game"))
        val before = games.game
        val pliesBefore = moves.plies.toList()
        val pausing = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        games.beforeUpdate = { pausing.complete(Unit); release.await() }
        var openedPreparation: GamePreparation? = null

        val preparation = async {
            manage.preparePractice(source, FenCodec.parse(source.initialFen), 0)
                .also { openedPreparation = it }
        }
        pausing.await()
        assertNull(openedPreparation)
        assertEquals(GameStatus.PLAYING, games.game.status)
        release.complete(Unit)
        val prepared = preparation.await()

        assertEquals(GameStatus.PAUSED, games.game.status)
        assertEquals(before.currentFen, games.game.currentFen)
        assertEquals(before.currentPly, games.game.currentPly)
        assertEquals(before.lastPlayedAt, games.game.lastPlayedAt)
        assertEquals(pliesBefore, moves.plies)
        assertEquals(source.initialFen, prepared.origin?.fen)
        assertEquals(0, prepared.origin?.ply)
        assertEquals(FenCodec.INITIAL_FEN, prepared.initialFen)
    }

    @Test
    fun directReplayPracticePreservesTerminalStateAndExplicitWinner() = runBlocking {
        manage.resign("game", Side.WHITE)
        val before = games.game
        val source = requireNotNull(query.getById("game"))
        val prepared = manage.preparePractice(source, FenCodec.parse(source.initialFen), 0)

        assertEquals(before, games.game)
        assertEquals(GameStatus.RESIGNED, games.game.status)
        assertEquals(Side.BLACK.name, games.game.winnerSide)
        assertEquals(source.id, prepared.origin?.gameId)
        assertTrue(moves.plies.isEmpty())
    }
}
