package com.shifenmiao.database.altitude.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.altitude.entity.AltitudeRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 海拔记录数据访问对象
 */
@Dao
interface AltitudeRecordDao {

    /** 插入一条海拔记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AltitudeRecordEntity): Long

    /** 按时间倒序获取所有记录（Flow，实时更新） */
    @Query("SELECT * FROM altitude_record ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<AltitudeRecordEntity>>

    /** 获取最近 N 条记录（用于趋势图） */
    @Query("SELECT * FROM altitude_record ORDER BY recordedAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<AltitudeRecordEntity>>

    /** 按 ID 删除单条记录 */
    @Query("DELETE FROM altitude_record WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** 清空所有记录 */
    @Query("DELETE FROM altitude_record")
    suspend fun clearAll()

    /** 获取记录总数（用于空态判断） */
    @Query("SELECT COUNT(*) FROM altitude_record")
    suspend fun getCount(): Int
}

