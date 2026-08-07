package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * item ↔ data 资源 1:1 关联。
 */
@Entity(
    tableName = "item_data_link",
    primaryKeys = ["item_id"],
    foreignKeys = [
        ForeignKey(entity = ItemEntity::class, parentColumns = ["id"], childColumns = ["item_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ItemDataEntity::class, parentColumns = ["id"], childColumns = ["data_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["data_id"], unique = true),
    ]
)
data class ItemDataLink(
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "data_id") val dataId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
