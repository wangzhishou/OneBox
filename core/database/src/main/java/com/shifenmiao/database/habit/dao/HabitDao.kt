package com.shifenmiao.database.habit.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.habit.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HabitEntity)

    @Query("SELECT * FROM habit WHERE is_archived = 0 ORDER BY sort_order ASC, created_at ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit WHERE id = :habitId")
    fun observeById(habitId: String): Flow<HabitEntity?>

    @Query("SELECT * FROM habit WHERE id = :habitId")
    suspend fun getById(habitId: String): HabitEntity?

    @Query("DELETE FROM habit WHERE id = :habitId")
    suspend fun deleteById(habitId: String)
}
