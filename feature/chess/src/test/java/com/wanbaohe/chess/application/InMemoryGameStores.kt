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

import com.wanbaohe.chess.application.port.outbound.GameEntity
import com.wanbaohe.chess.application.port.outbound.GameStore
import com.wanbaohe.chess.application.port.outbound.GameSummaryEntity
import com.wanbaohe.chess.application.port.outbound.MoveStore
import com.wanbaohe.chess.application.port.outbound.PlyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class MemoryGames(initial: GameEntity) : GameStore {
    val records = mutableMapOf(initial.id to initial)
    var beforeUpdate: suspend () -> Unit = {}
    var game: GameEntity
        get() = requireNotNull(records["game"])
        set(value) { records[value.id] = value }

    override fun observeAll(): Flow<List<GameSummaryEntity>> = flowOf(emptyList())
    override fun observeById(gameId: String): Flow<GameEntity?> = flowOf(records[gameId])
    override suspend fun getById(gameId: String): GameEntity? = records[gameId]
    override suspend fun insert(entity: GameEntity): String { records[entity.id] = entity; return entity.id }
    override suspend fun update(entity: GameEntity) { beforeUpdate(); records[entity.id] = entity }
    override suspend fun archive(gameId: String) { records.remove(gameId) }
}

internal class MemoryMoves : MoveStore {
    val plies = mutableListOf<PlyEntity>()
    var beforeInsert: suspend () -> Unit = {}
    override fun observeByGame(gameId: String): Flow<List<PlyEntity>> = flowOf(plies.filter { it.gameId == gameId })
    override suspend fun getByGame(gameId: String): List<PlyEntity> = plies.filter { it.gameId == gameId }
    override suspend fun getPly(gameId: String, plyNumber: Int): PlyEntity? =
        plies.find { it.gameId == gameId && it.ply == plyNumber }
    override suspend fun insert(entity: PlyEntity) { beforeInsert(); plies += entity }
    override suspend fun deleteAfterPly(gameId: String, ply: Int) { plies.removeAll { it.gameId == gameId && it.ply > ply } }
    override suspend fun deleteByGame(gameId: String) { plies.removeAll { it.gameId == gameId } }
}
