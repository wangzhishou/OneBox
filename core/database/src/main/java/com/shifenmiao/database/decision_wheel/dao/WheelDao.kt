package com.shifenmiao.database.decision_wheel.dao

import androidx.room.*
import com.shifenmiao.database.decision_wheel.entity.WheelEntity
import com.shifenmiao.database.decision_wheel.entity.WheelHistoryEntity
import com.shifenmiao.database.decision_wheel.entity.WheelOptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WheelDao {

    // Wheel CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWheel(wheel: WheelEntity): Long

    @Update
    suspend fun updateWheel(wheel: WheelEntity)

    @Delete
    suspend fun deleteWheel(wheel: WheelEntity)

    @Query("SELECT * FROM decision_wheels ORDER BY lastUsedAt DESC")
    fun getAllWheels(): Flow<List<WheelEntity>>

    @Query("SELECT * FROM decision_wheels WHERE id = :wheelId")
    suspend fun getWheelById(wheelId: String): WheelEntity?

    @Query("SELECT * FROM decision_wheels ORDER BY lastUsedAt DESC LIMIT :limit")
    fun getRecentWheels(limit: Int = 5): Flow<List<WheelEntity>>

    @Query("UPDATE decision_wheels SET lastUsedAt = :timestamp, useCount = useCount + 1 WHERE id = :wheelId")
    suspend fun updateWheelUsage(wheelId: String, timestamp: Long)

    // Option CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOption(option: WheelOptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOptions(options: List<WheelOptionEntity>)

    @Update
    suspend fun updateOption(option: WheelOptionEntity)

    @Delete
    suspend fun deleteOption(option: WheelOptionEntity)

    @Query("SELECT * FROM wheel_options WHERE wheelId = :wheelId ORDER BY position ASC")
    suspend fun getOptionsByWheelId(wheelId: String): List<WheelOptionEntity>

    @Query("SELECT * FROM wheel_options WHERE wheelId = :wheelId ORDER BY position ASC")
    fun getOptionsByWheelIdFlow(wheelId: String): Flow<List<WheelOptionEntity>>

    @Query("DELETE FROM wheel_options WHERE wheelId = :wheelId")
    suspend fun deleteOptionsByWheelId(wheelId: String)

    // History operations
    @Insert
    suspend fun insertHistory(history: WheelHistoryEntity): Long

    @Query("SELECT * FROM wheel_history WHERE wheelId = :wheelId ORDER BY timestamp DESC LIMIT :limit")
    fun getWheelHistory(wheelId: String, limit: Int = 20): Flow<List<WheelHistoryEntity>>

    @Query("SELECT * FROM wheel_history ORDER BY timestamp DESC LIMIT :limit")
    fun getAllHistory(limit: Int = 50): Flow<List<WheelHistoryEntity>>

    @Query("DELETE FROM wheel_history WHERE wheelId = :wheelId")
    suspend fun deleteHistoryByWheelId(wheelId: String)

    @Query("DELETE FROM wheel_history")
    suspend fun clearAllHistory()

    // Composite operations
    @Transaction
    suspend fun insertWheelWithOptions(wheel: WheelEntity, options: List<WheelOptionEntity>) {
        insertWheel(wheel)
        insertOptions(options)
    }

    @Transaction
    suspend fun deleteWheelWithOptions(wheelId: String) {
        getWheelById(wheelId)?.let { wheel ->
            deleteWheel(wheel)
            deleteOptionsByWheelId(wheelId)
            deleteHistoryByWheelId(wheelId)
        }
    }
}

