package com.wanbaohe.speedtest.data

import kotlinx.coroutines.flow.Flow

interface SpeedTestConfigRepository {
    fun getAll(): Flow<List<SpeedTestConfig>>
    suspend fun getActive(): SpeedTestConfig?
    suspend fun insert(config: SpeedTestConfig): Long
    suspend fun update(config: SpeedTestConfig)
    suspend fun deleteById(id: Long)
    suspend fun setActive(id: Long)
    /** 数据库为空时插入默认预设配置 */
    suspend fun initDefaultsIfEmpty()
}

