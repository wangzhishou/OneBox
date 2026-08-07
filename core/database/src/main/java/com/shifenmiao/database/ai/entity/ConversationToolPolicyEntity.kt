package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 会话级工具策略：
 * 记录当前聊天会话显式启用集合。
 */
@Entity(tableName = "conversation_tool_policy")
data class ConversationToolPolicyEntity(
    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    val conversationId: String,
    @ColumnInfo(name = "enabled_tool_names_json")
    val enabledToolNamesJson: String = "[]",
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
