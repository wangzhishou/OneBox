package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.shifenmiao.database.chat_prompt.entity.PromptEntity

/**
 * item ↔ prompt 资源 1:1 关联。
 */
@Entity(
    tableName = "item_prompt_link",
    primaryKeys = ["item_id"],
    foreignKeys = [
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["item_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PromptEntity::class, parentColumns = ["id"], childColumns = ["prompt_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["prompt_id"], unique = true),
    ]
)
data class ItemPromptLink(
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "prompt_id") val promptId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
