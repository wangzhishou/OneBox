package com.shifenmiao.database.transfer

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 保存 channelId -> deviceName（浏览器客户端上报的人类可读设备名）。
 */
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val channelId: String,
    val deviceName: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

