package com.shifenmiao.database.item.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.shifenmiao.model.Source

/**
 * 分类（全局共享，按使用反查 listType）。
 * - 唯一索引 (name, source)
 * - 删除分类用 RESTRICT（业务上分类有"被引用"语义，不能随手 CASCADE）
 * - 不再保留 listType 字段；"该 listType 用哪些分类" 在 query 阶段 JOIN item_category 推算
 */
@Entity(
    tableName = "category",
    indices = [
        Index(value = ["name", "source"], unique = true)
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "can_edit", defaultValue = "0") val canEdit: Boolean = false,
    @ColumnInfo(name = "source", defaultValue = "0") val source: Source = Source.REMOTE,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

fun Category.toModel(): com.shifenmiao.model.Category {
    return com.shifenmiao.model.Category(
        id = this.id,
        name = this.name,
        canEdit = this.canEdit,
        source = this.source,
        updatedAt = this.updatedAt,
    )
}
