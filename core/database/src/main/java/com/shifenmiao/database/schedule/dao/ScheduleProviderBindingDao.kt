package com.shifenmiao.database.schedule.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.schedule.entity.ScheduleProviderBindingEntity

@Dao
interface ScheduleProviderBindingDao {

    @Query("SELECT * FROM schedule_provider_binding WHERE local_event_id = :localEventId")
    suspend fun getByLocalEventId(localEventId: String): List<ScheduleProviderBindingEntity>

    @Query(
        "SELECT * FROM schedule_provider_binding WHERE local_event_id = :localEventId AND provider_type = :providerType LIMIT 1"
    )
    suspend fun getByLocalEventIdAndProviderType(
        localEventId: String,
        providerType: String,
    ): ScheduleProviderBindingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(binding: ScheduleProviderBindingEntity)

    @Query("DELETE FROM schedule_provider_binding WHERE id = :bindingId")
    suspend fun deleteById(bindingId: String)

    @Query("DELETE FROM schedule_provider_binding WHERE local_event_id = :localEventId")
    suspend fun deleteByLocalEventId(localEventId: String)
}

