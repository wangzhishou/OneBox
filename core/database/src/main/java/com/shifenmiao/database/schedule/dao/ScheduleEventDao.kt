package com.shifenmiao.database.schedule.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.schedule.entity.ScheduleEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleEventDao {

    @Query("SELECT * FROM schedule_event ORDER BY start_utc_millis ASC, updated_at DESC")
    fun observeAll(): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_event WHERE linked_task_id = :taskId ORDER BY start_utc_millis ASC, updated_at DESC")
    fun observeByLinkedTaskId(taskId: String): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_event WHERE linked_task_id = :taskId ORDER BY updated_at DESC")
    suspend fun getByLinkedTaskId(taskId: String): List<ScheduleEventEntity>

    @Query("SELECT * FROM schedule_event WHERE id = :eventId LIMIT 1")
    suspend fun getById(eventId: String): ScheduleEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(event: ScheduleEventEntity)

    @Query("DELETE FROM schedule_event WHERE id = :eventId")
    suspend fun deleteById(eventId: String)
}

