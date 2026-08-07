package com.shifenmiao.database.speedtest.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.shifenmiao.database.speedtest.entity.SpeedTestRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeedTestRecordDao {

    /** 插入一条测速记录 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SpeedTestRecordEntity): Long

    /** 按时间倒序获取所有记录（Flow，实时更新） */
    @Query("SELECT * FROM speed_test_record ORDER BY recordedAt DESC")
    fun getAll(): Flow<List<SpeedTestRecordEntity>>

    /** 清空所有记录 */
    @Query("DELETE FROM speed_test_record")
    suspend fun clearAll()

    /** 获取记录总数 */
    @Query("SELECT COUNT(*) FROM speed_test_record")
    suspend fun getCount(): Int
}

