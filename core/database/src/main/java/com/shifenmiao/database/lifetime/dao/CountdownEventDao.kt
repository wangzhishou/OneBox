package com.shifenmiao.database.lifetime.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shifenmiao.database.lifetime.entity.CountdownEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 倒数日 DAO
 */
@Dao
interface CountdownEventDao {

    @Query("SELECT * FROM countdown_events ORDER BY sortOrder ASC, createdAt ASC")
    fun getAllCountdowns(): Flow<List<CountdownEventEntity>>

    @Query("SELECT * FROM countdown_events WHERE id = :id")
    suspend fun getCountdownById(id: Long): CountdownEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCountdown(countdown: CountdownEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCountdowns(countdowns: List<CountdownEventEntity>)

    @Update
    suspend fun updateCountdown(countdown: CountdownEventEntity)

    @Delete
    suspend fun deleteCountdown(countdown: CountdownEventEntity)

    @Query("DELETE FROM countdown_events WHERE id = :id")
    suspend fun deleteCountdownById(id: Long)

    @Query("SELECT COUNT(*) FROM countdown_events WHERE isPreset = 1")
    suspend fun getPresetCountdownsCount(): Int
}
