package com.shifenmiao.database.gomoku.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.gomoku.entity.GomokuAiTaskEntity
import com.shifenmiao.database.gomoku.entity.GomokuGameEntity
import com.shifenmiao.database.gomoku.entity.GomokuPlyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GomokuGameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(game: GomokuGameEntity)

    @Query("SELECT * FROM gomoku_game WHERE archived = 0 ORDER BY last_played_at DESC")
    fun observeGames(): Flow<List<GomokuGameEntity>>

    @Query("SELECT * FROM gomoku_game WHERE id = :gameId LIMIT 1")
    fun observeGame(gameId: String): Flow<GomokuGameEntity?>

    @Query("SELECT * FROM gomoku_game WHERE id = :gameId LIMIT 1")
    suspend fun getGame(gameId: String): GomokuGameEntity?

    @Query("UPDATE gomoku_game SET archived = 1 WHERE id = :gameId")
    suspend fun archive(gameId: String)
}

@Dao
interface GomokuPlyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ply: GomokuPlyEntity)

    @Query("SELECT * FROM gomoku_ply WHERE game_id = :gameId ORDER BY ply ASC")
    fun observePlies(gameId: String): Flow<List<GomokuPlyEntity>>

    @Query("SELECT * FROM gomoku_ply WHERE game_id = :gameId ORDER BY ply ASC")
    suspend fun getPlies(gameId: String): List<GomokuPlyEntity>

    @Query("SELECT * FROM gomoku_ply WHERE game_id = :gameId AND ply = :ply LIMIT 1")
    suspend fun getPly(gameId: String, ply: Int): GomokuPlyEntity?

    @Query("DELETE FROM gomoku_ply WHERE game_id = :gameId AND ply > :ply")
    suspend fun deleteAfterPly(gameId: String, ply: Int)

    @Query("DELETE FROM gomoku_ply WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}

@Dao
interface GomokuAiTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: GomokuAiTaskEntity)

    @Query("SELECT * FROM gomoku_ai_task WHERE game_id = :gameId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestTask(gameId: String): GomokuAiTaskEntity?

    @Query("DELETE FROM gomoku_ai_task WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
