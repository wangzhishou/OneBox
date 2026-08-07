package com.shifenmiao.database.decision_wheel.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 转盘配置实体
 */
@Entity(tableName = "decision_wheels")
data class WheelEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val lastUsedAt: Long = 0L,
    val useCount: Int = 0
)

/**
 * 转盘选项实体
 */
@Entity(tableName = "wheel_options")
data class WheelOptionEntity(
    @PrimaryKey
    val id: String,
    val wheelId: String,
    val name: String,
    val colorHex: String, // 颜色的十六进制字符串
    val position: Int // 选项在转盘中的位置
)

/**
 * 转盘使用历史记录
 */
@Entity(tableName = "wheel_history")
data class WheelHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wheelId: String,
    val selectedOptionId: String,
    val selectedOptionName: String,
    val timestamp: Long
)

