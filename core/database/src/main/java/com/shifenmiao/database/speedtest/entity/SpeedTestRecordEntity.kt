package com.shifenmiao.database.speedtest.entity

import androidx.room.ColumnInfo
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
    /** 上传速度 (Mbps)，-1 表示未测量 */
    @ColumnInfo(defaultValue = "-1")
    val uploadMbps: Float = -1f,
    /** 网络延迟 (ms)，-1 表示未能测量 */
    val latencyMs: Int,
    @ColumnInfo(defaultValue = "''")
    val clientIp: String = "",
    @ColumnInfo(defaultValue = "''")
    val clientLocation: String = "",
    @ColumnInfo(defaultValue = "''")
    val carrier: String = "",
    @ColumnInfo(defaultValue = "''")
    val serverHost: String = "",
    @ColumnInfo(defaultValue = "''")
    val latencyNode: String = "",
    @ColumnInfo(defaultValue = "''")
    val downloadNode: String = "",
    @ColumnInfo(defaultValue = "''")
    val uploadNode: String = "",
    /** 记录时间戳 (ms) */
    val recordedAt: Long = System.currentTimeMillis()
)

