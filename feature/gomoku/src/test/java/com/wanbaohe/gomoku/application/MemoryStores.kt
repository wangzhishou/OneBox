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

package com.wanbaohe.gomoku.application

import com.wanbaohe.gomoku.application.dto.GameAiPlayerConfig
import com.wanbaohe.gomoku.application.port.outbound.GameEntity
import com.wanbaohe.gomoku.application.port.outbound.GameStore
import com.wanbaohe.gomoku.application.port.outbound.GameSummaryEntity
import com.wanbaohe.gomoku.application.port.outbound.MoveStore
import com.wanbaohe.gomoku.application.port.outbound.PlyEntity
import com.wanbaohe.gomoku.domain.FenCodec
import com.wanbaohe.gomoku.domain.GameArbiter
import com.wanbaohe.gomoku.domain.model.BoardPoint
import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal fun testGame(
    id: String = "game",
    fen: String = FenCodec.INITIAL_FEN,
    status: GameStatus = GameStatus.PAUSED,
): GameEntity = GameEntity(
    id = id, title = "Source", mode = GameMode.HUMAN_VS_LLM,
    whitePlayerType = PlayerType.LLM, blackPlayerType = PlayerType.HUMAN,
    redPlayerConfigJson = GameAiPlayerConfig(sourceKey = "rapfi").encode(),
    blackPlayerConfigJson = "{}",
    initialFen = fen, currentFen = fen, currentPly = 0, status = status,
    resultText = "", winnerSide = "", startedAt = 1, lastMoveAt = 1, lastPlayedAt = 1, updatedAt = 1,
)

internal class MemoryGames(initial: GameEntity) : GameStore {
    val games = mutableMapOf(initial.id to initial)
    var game: GameEntity
        get() = requireNotNull(games["game"])
        set(value) { games[value.id] = value }

    override fun observeAll(): Flow<List<GameSummaryEntity>> = flowOf(emptyList())
    override fun observeById(gameId: String): Flow<GameEntity?> = flowOf(games[gameId])
    override suspend fun getById(gameId: String): GameEntity? = games[gameId]
    override suspend fun insert(entity: GameEntity): String { games[entity.id] = entity; return entity.id }
    override suspend fun update(entity: GameEntity) { games[entity.id] = entity }
    override suspend fun archive(gameId: String) { games.remove(gameId) }
}

internal class MemoryMoves : MoveStore {
    val plies = mutableListOf<PlyEntity>()
    override fun observeByGame(gameId: String): Flow<List<PlyEntity>> = flowOf(plies.filter { it.gameId == gameId })
    override suspend fun getByGame(gameId: String): List<PlyEntity> = plies.filter { it.gameId == gameId }
    override suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity? = plies.find { it.gameId == gameId && it.ply == plyNumber }
    override suspend fun insert(entity: PlyEntity) { plies += entity }
    override suspend fun deleteAfterPly(gameId: String, ply: Int) { plies.removeAll { it.gameId == gameId && it.ply > ply } }
    override suspend fun deleteByGame(gameId: String) { plies.removeAll { it.gameId == gameId } }

    fun addMove(gameId: String, ply: Int, beforeFen: String, point: BoardPoint): String {
        val board = FenCodec.parse(beforeFen)
        val move = GameArbiter.legalMoves(board).first { it.to == point }
        val after = FenCodec.encode(board.withStonePlaced(move))
        plies += PlyEntity(
            "$gameId:$ply", gameId, ply, move.notationUcci, move.notationCn, move.side, beforeFen, after,
            false, false, false, "", "", 0,
        )
        return after
    }
}
