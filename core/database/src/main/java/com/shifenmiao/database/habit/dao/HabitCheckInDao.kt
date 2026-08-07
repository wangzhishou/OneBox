package com.shifenmiao.database.habit.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.habit.entity.HabitCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCheckInDao {

    /** 冲突(同 habit+epochDay 已打卡)时忽略,返回 -1。 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: HabitCheckInEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: HabitCheckInEntity)

    @Query("SELECT * FROM habit_check_in WHERE date_epoch_day = :epochDay ORDER BY checked_at ASC")
    fun observeByDate(epochDay: Long): Flow<List<HabitCheckInEntity>>

    /** 日期范围 [startDay, endDay] 内的全部打卡,统计用。 */
    @Query("SELECT * FROM habit_check_in WHERE date_epoch_day BETWEEN :startDay AND :endDay ORDER BY date_epoch_day ASC, checked_at ASC")
    suspend fun getBetween(startDay: Long, endDay: Long): List<HabitCheckInEntity>

    @Query("DELETE FROM habit_check_in WHERE habit_id = :habitId AND date_epoch_day = :epochDay")
    suspend fun deleteByHabitAndDate(habitId: String, epochDay: Long)

    @Query("DELETE FROM habit_check_in WHERE habit_id = :habitId")
    suspend fun deleteByHabitId(habitId: String)
}
