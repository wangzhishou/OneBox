package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * item 点击统计（1:1 汇总行）。
 * 行数 = item 数，与点击次数无关；不会随用户行为线性增长。
 *
 * 索引：
 * - item_id 是 PK，不需要单独索引
 * - click_time 单索引保留，给"最近访问"用 ORDER BY DESC LIMIT 20
 * - 不再给 click_count 建索引（永远不会按它排序）
 */
@Entity(
    tableName = "item_click_stat",
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["click_time"])
    ]
)
data class ItemClickStatEntity(
    @PrimaryKey
    @ColumnInfo(name = "item_id") val itemId: Int,
    @ColumnInfo(name = "click_count", defaultValue = "0") val clickCount: Int = 0,
    /** null = 从未点击；非 null = 上次点击时间（epoch millis） */
    @ColumnInfo(name = "click_time") val clickTime: Long? = null,
) {
    /** DAO 还在用 java.util.Date 入参；提供一个兼容取景。 */
    val clickTimeAsDate: Date? get() = clickTime?.let(::Date)
}
