package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.shifenmiao.database.agent.entity.ItemAgentEntity

/**
 * item ↔ agent 资源 1:1 关联。
 * - item_id 是 PK，强制 1:1
 * - agent_id UNIQUE，agent 资源不跨 item 共享
 * - FK CASCADE：删 item / 删 agent 时自动清理 link
 */
@Entity(
    tableName = "item_agent_link",
    primaryKeys = ["item_id"],
    foreignKeys = [
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["item_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ItemAgentEntity::class, parentColumns = ["id"], childColumns = ["agent_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["agent_id"], unique = true),
    ]
)
data class ItemAgentLink(
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "agent_id") val agentId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
