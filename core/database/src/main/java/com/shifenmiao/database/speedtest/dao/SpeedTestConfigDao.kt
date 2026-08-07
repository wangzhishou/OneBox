package com.shifenmiao.database.speedtest.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.shifenmiao.database.speedtest.entity.SpeedTestConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SpeedTestConfigDao {

    /** 获取所有配置（Flow 实时更新，按 id 升序） */
    @Query("SELECT * FROM speed_test_config ORDER BY id ASC")
    abstract fun getAll(): Flow<List<SpeedTestConfigEntity>>

    /** 获取当前激活的配置 */
    @Query("SELECT * FROM speed_test_config WHERE isActive = 1 LIMIT 1")
    abstract suspend fun getActive(): SpeedTestConfigEntity?

    /** 插入一条配置 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(entity: SpeedTestConfigEntity): Long

    /** 更新一条配置 */
    @Update
    abstract suspend fun update(entity: SpeedTestConfigEntity)

    /** 按 id 删除配置（外层调用方应确保只删除非预设配置） */
    @Query("DELETE FROM speed_test_config WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    /** 清除所有激活标记 */
    @Query("UPDATE speed_test_config SET isActive = 0")
    abstract suspend fun clearAllActive()

    /** 设置指定 id 为激活 */
    @Query("UPDATE speed_test_config SET isActive = 1 WHERE id = :id")
    abstract suspend fun activateById(id: Long)

    /** 获取配置总数 */
    @Query("SELECT COUNT(*) FROM speed_test_config")
    abstract suspend fun getCount(): Int

    /** 原子切换激活配置（事务保证一致性） */
    @Transaction
    open suspend fun setActive(id: Long) {
        clearAllActive()
        activateById(id)
    }
}

