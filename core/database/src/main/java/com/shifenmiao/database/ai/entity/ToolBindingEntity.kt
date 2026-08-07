package com.shifenmiao.database.ai.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * 默认工具绑定关系。
 *
 * 统一承接 Agent / Prompt 的默认工具作用域。
 */
@Entity(
    tableName = "tool_binding",
    primaryKeys = ["owner_type", "owner_id", "tool_name"],
    indices = [
        Index(value = ["owner_type", "owner_id"]),
        Index(value = ["tool_name"])
    ]
)
data class ToolBindingEntity(
    @ColumnInfo(name = "owner_type")
    val ownerType: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: Int,
    @ColumnInfo(name = "tool_name")
    val toolName: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int
) {
    object OwnerType {
        const val AGENT = "AGENT"
        const val PROMPT = "PROMPT"
        // CHAT / ASSISTANT 旧 owner_type 已废弃 (首轮默认统一走 bootstrapModes 兜底),
        // 常量一并删除. 旧行由 AppDatabase.MIGRATION_2_3 清理.
    }
}
