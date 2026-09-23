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

    @Query("SELECT * FROM chess_game WHERE id = :gameId LIMIT 1")
    fun observeGame(gameId: String): Flow<ChessGameEntity?>

    @Query("SELECT * FROM chess_game WHERE id = :gameId LIMIT 1")
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
