package com.shifenmiao.database.speedtest.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 测速记录数据库实体 */
@Entity(tableName = "speed_test_record")
data class SpeedTestRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 网络类型，如 "5G移动网络"、"WiFi" */
    val networkType: String,
    /** 下载速度 (Mbps) */
    val downloadMbps: Float,
    /** 网络延迟 (ms)，-1 表示未能测量 */
    val latencyMs: Int,
    /** 记录时间戳 (ms) */
    val recordedAt: Long = System.currentTimeMillis()
)

