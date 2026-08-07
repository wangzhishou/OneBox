package com.shifenmiao.database.schedule.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.schedule.entity.ScheduleSyncStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleSyncStateDao {

    @Query("SELECT * FROM schedule_sync_state WHERE provider_type = :providerType LIMIT 1")
    fun observeByProvider(providerType: String): Flow<ScheduleSyncStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: ScheduleSyncStateEntity)
}

