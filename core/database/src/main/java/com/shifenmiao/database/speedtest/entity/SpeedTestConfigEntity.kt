package com.shifenmiao.database.speedtest.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** 测速配置数据库实体 */
@Entity(tableName = "speed_test_config")
data class SpeedTestConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** 配置名称，用于界面展示 */
    val name: String,
    /** 测速下载 URL */
    val testUrl: String,
    /** 预估消耗流量 (MB) */
    val estimatedDataMb: Int,
    /** 最大测速时长 (秒) */
    val durationSeconds: Int,
    /** 是否为当前激活配置（同时只有一个为 true） */
    val isActive: Boolean = false,
    /** 是否为内置预设（预设不允许删除） */
    val isPreset: Boolean = false
)

