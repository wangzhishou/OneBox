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

package com.shifenmiao.database.chess.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.chess.entity.ChessAiTaskEntity
import com.shifenmiao.database.chess.entity.ChessGameEntity
import com.shifenmiao.database.chess.entity.ChessPlyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChessGameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(game: ChessGameEntity)

    @Query("SELECT * FROM chess_game WHERE archived = 0 ORDER BY last_played_at DESC")
    fun observeGames(): Flow<List<ChessGameEntity>>

    @Query("SELECT * FROM chess_game WHERE id = :gameId AND archived = 0 LIMIT 1")
    fun observeGame(gameId: String): Flow<ChessGameEntity?>

    @Query("SELECT * FROM chess_game WHERE id = :gameId AND archived = 0 LIMIT 1")
    suspend fun getGame(gameId: String): ChessGameEntity?

    @Query("UPDATE chess_game SET archived = 1 WHERE id = :gameId")
    suspend fun archive(gameId: String)
}

@Dao
interface ChessPlyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ply: ChessPlyEntity)

    @Query("SELECT * FROM chess_ply WHERE game_id = :gameId ORDER BY ply ASC")
    fun observePlies(gameId: String): Flow<List<ChessPlyEntity>>

    @Query("SELECT * FROM chess_ply WHERE game_id = :gameId ORDER BY ply ASC")
    suspend fun getPlies(gameId: String): List<ChessPlyEntity>

    @Query("SELECT * FROM chess_ply WHERE game_id = :gameId AND ply = :ply LIMIT 1")
    suspend fun getPly(gameId: String, ply: Int): ChessPlyEntity?

    @Query("DELETE FROM chess_ply WHERE game_id = :gameId AND ply > :ply")
    suspend fun deleteAfterPly(gameId: String, ply: Int)

    @Query("DELETE FROM chess_ply WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}

@Dao
interface ChessAiTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: ChessAiTaskEntity)

    @Query("SELECT * FROM chess_ai_task WHERE game_id = :gameId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestTask(gameId: String): ChessAiTaskEntity?

    @Query("DELETE FROM chess_ai_task WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
