package com.shifenmiao.database.lifetime.dao

import androidx.room.*
import com.shifenmiao.database.lifetime.entity.FrequencyEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * 频率事件 DAO
 */
@Dao
interface FrequencyEventDao {

    /**
     * 获取所有启用的事件（Flow，自动更新）
     * 按排序顺序、预设优先、创建时间排序
     */
    @Query("SELECT * FROM frequency_events WHERE isEnabled = 1 ORDER BY sortOrder ASC, isPreset DESC, createdAt ASC")
    fun getAllEnabledEvents(): Flow<List<FrequencyEventEntity>>

    /**
     * 获取所有事件
     */
    @Query("SELECT * FROM frequency_events ORDER BY sortOrder ASC, isPreset DESC, createdAt ASC")
    fun getAllEvents(): Flow<List<FrequencyEventEntity>>

    /**
     * 根据 ID 获取事件
     */
    @Query("SELECT * FROM frequency_events WHERE id = :id")
    suspend fun getEventById(id: Long): FrequencyEventEntity?

    /**
     * 插入事件
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: FrequencyEventEntity): Long

    /**
     * 批量插入事件（用于预设事件）
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<FrequencyEventEntity>)

    /**
     * 更新事件
     */
    @Update
    suspend fun updateEvent(event: FrequencyEventEntity)

    /**
     * 删除事件
     */
    @Delete
    suspend fun deleteEvent(event: FrequencyEventEntity)

    /**
     * 切换启用状态
     */
    @Query("UPDATE frequency_events SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun toggleEnabled(id: Long, isEnabled: Boolean)

    /**
     * 检查是否已有预设事件
     */
    @Query("SELECT COUNT(*) FROM frequency_events WHERE isPreset = 1")
    suspend fun getPresetEventsCount(): Int
}

