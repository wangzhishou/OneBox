package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * item ↔ category 多对多关联。
 *
 * 行为规约：
 * - item 删除：CASCADE（条目没了分类关联没意义）
 * - category 删除：RESTRICT（删除分类是显式运营动作，必须先解绑）
 * - 唯一索引 (item_id, category_id) 由复合 PK 覆盖
 * - category_id 单独索引用于"该分类下的所有 item"反查
 * - item_id 不再单独索引（PK 最左前缀已覆盖）
 */
@Entity(
    tableName = "item_category",
    primaryKeys = ["item_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["category_id"])
    ]
)
data class ItemCategoryCrossRef(
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "category_id") val categoryId: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
