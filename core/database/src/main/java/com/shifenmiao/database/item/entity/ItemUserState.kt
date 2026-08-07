package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * item 的本地用户态（收藏 / 置顶 / 可编辑）。
 *
 * 拆出独立表的收益：
 * 1. 同步 item 服务端字段时不再需要保护 N 个本地列
 * 2. "显示已收藏" 等过滤查询走小表的索引
 * 3. 服务端下架 item 时的 CASCADE 不再误删用户标记外的字段
 *
 * 与 item 是 1:1 关系，由 [itemId] PK 强制。
 */
@Entity(
    tableName = "item_user_state",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["is_favorited"]),
        Index(value = ["is_pinned", "pinned_at"]),
    ]
)
data class ItemUserState(
    @PrimaryKey
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "is_favorited", defaultValue = "0") val isFavorited: Boolean = false,
    @ColumnInfo(name = "is_pinned", defaultValue = "0") val isPinned: Boolean = false,
    @ColumnInfo(name = "pinned_at") val pinnedAt: Long? = null,
    @ColumnInfo(name = "can_edit", defaultValue = "0") val canEdit: Boolean = false,
    @ColumnInfo(name = "requires_auth", defaultValue = "0") val requiresAuth: Boolean = false,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
