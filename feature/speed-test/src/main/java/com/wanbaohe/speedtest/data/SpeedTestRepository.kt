package com.wanbaohe.speedtest.data

import com.wanbaohe.speedtest.domain.SpeedTestPhase
import com.wanbaohe.speedtest.domain.SpeedTestRecord
import kotlinx.coroutines.flow.Flow

interface SpeedTestRepository {
    /** 开始测速，返回进度 Flow（MeasuringLatency → Downloading → Done | Error） */
    fun startTest(config: SpeedTestConfig, networkType: String): Flow<SpeedTestPhase>

    /** 持久化一条测速结果 */
    suspend fun saveRecord(record: SpeedTestRecord)

    /** 获取所有历史记录（Flow 实时更新） */
    fun getHistory(): Flow<List<SpeedTestRecord>>

    /** 清除所有历史 */
    suspend fun clearHistory()
}

