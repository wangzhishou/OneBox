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

package com.wanbaohe.gomoku.application.port.outbound

import com.wanbaohe.gomoku.domain.model.GameMode
import com.wanbaohe.gomoku.domain.model.GameOrigin
import com.wanbaohe.gomoku.domain.model.GameStatus
import com.wanbaohe.gomoku.domain.model.PlayerType
import kotlinx.coroutines.flow.Flow

interface GameStore {
    fun observeAll(): Flow<List<GameSummaryEntity>>
    fun observeById(gameId: String): Flow<GameEntity?>
    suspend fun getById(gameId: String): GameEntity?
    suspend fun insert(entity: GameEntity): String
    suspend fun update(entity: GameEntity)
    suspend fun archive(gameId: String)
}

data class GameEntity(
    val id: String,
    val title: String,
    val mode: GameMode,
    val whitePlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val redPlayerConfigJson: String,
    val blackPlayerConfigJson: String,
    val initialFen: String,
    val currentFen: String,
    val currentPly: Int,
    val status: GameStatus,
    val resultText: String,
    val winnerSide: String,
    val startedAt: Long,
    val lastMoveAt: Long,
    val lastPlayedAt: Long,
    val updatedAt: Long,
    val origin: GameOrigin? = null,
)

data class GameSummaryEntity(
    val id: String,
    val title: String,
    val mode: GameMode,
    val whitePlayerType: PlayerType,
    val blackPlayerType: PlayerType,
    val status: GameStatus,
    val resultText: String,
    val updatedAt: Long,
    /** 已走着法数（`gomoku_game.current_ply`），历史卡片用于展示手数。 */
    val plyCount: Int,
    val lastPlayedAt: Long = 0L,
    val redPlayerConfigJson: String = "{}",
    val blackPlayerConfigJson: String = "{}",
    val origin: GameOrigin? = null,
)
