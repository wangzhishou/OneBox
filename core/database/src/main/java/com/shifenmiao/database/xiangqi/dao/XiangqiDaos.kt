package com.shifenmiao.database.xiangqi.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.xiangqi.entity.XiangqiAiTaskEntity
import com.shifenmiao.database.xiangqi.entity.XiangqiGameEntity
import com.shifenmiao.database.xiangqi.entity.XiangqiPlyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface XiangqiGameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(game: XiangqiGameEntity)

    @Query("SELECT * FROM xiangqi_game WHERE archived = 0 ORDER BY last_played_at DESC")
    fun observeGames(): Flow<List<XiangqiGameEntity>>

    @Query("SELECT * FROM xiangqi_game WHERE id = :gameId LIMIT 1")
    fun observeGame(gameId: String): Flow<XiangqiGameEntity?>

    @Query("SELECT * FROM xiangqi_game WHERE id = :gameId LIMIT 1")
    suspend fun getGame(gameId: String): XiangqiGameEntity?

    @Query("UPDATE xiangqi_game SET archived = 1 WHERE id = :gameId")
    suspend fun archive(gameId: String)
}

@Dao
interface XiangqiPlyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(ply: XiangqiPlyEntity)

    @Query("SELECT * FROM xiangqi_ply WHERE game_id = :gameId ORDER BY ply ASC")
    fun observePlies(gameId: String): Flow<List<XiangqiPlyEntity>>

    @Query("SELECT * FROM xiangqi_ply WHERE game_id = :gameId ORDER BY ply ASC")
    suspend fun getPlies(gameId: String): List<XiangqiPlyEntity>

    @Query("SELECT * FROM xiangqi_ply WHERE game_id = :gameId AND ply = :ply LIMIT 1")
    suspend fun getPly(gameId: String, ply: Int): XiangqiPlyEntity?

    @Query("DELETE FROM xiangqi_ply WHERE game_id = :gameId AND ply > :ply")
    suspend fun deleteAfterPly(gameId: String, ply: Int)

    @Query("DELETE FROM xiangqi_ply WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}

@Dao
interface XiangqiAiTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: XiangqiAiTaskEntity)

    @Query("SELECT * FROM xiangqi_ai_task WHERE game_id = :gameId ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestTask(gameId: String): XiangqiAiTaskEntity?

    @Query("DELETE FROM xiangqi_ai_task WHERE game_id = :gameId")
    suspend fun deleteByGameId(gameId: String)
}
